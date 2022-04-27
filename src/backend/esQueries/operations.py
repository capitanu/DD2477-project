from elasticsearch.helpers import scan

from esQueries import ESClientManager
from esQueries.indices import Book, TfIDF


def compute_mean_vector(args):
    """
    Computes the centroid vector
    :param args: list of dictionary
    :type args: list

    :return: Dictionary representing the centroid
    :rtype: dict
    """
    tot_doc = len(args)
    mean_dct = dict()
    for arg in args:
        for dict_val in arg:
            if dict_val['word'] not in mean_dct:
                mean_dct[dict_val['word']] = dict_val['tfIdf'] / tot_doc
            else:
                mean_dct[dict_val['word']] += dict_val['tfIdf'] / tot_doc

    return mean_dct


class BookCRUD(ESClientManager):
    def __init__(self):
        super().__init__()
        self.index = Book()
        self.tfidf = TfIDF()

    def fetch_all(self, title):
        return self.client.search(
            index=self.index.name,
            body={
                "query": {
                    "match": {
                        "title": {
                            "query": title,
                            "fuzziness": 1
                        }
                    }
                }
            }
        )

    def recommendation(self, *args):
        es = ESClientManager()

        doc_id = [
            es.client.search(index=self.index.name, query={
                "bool": {
                    "must": {
                        "match": {"title": title}
                    }
                }
            })['hits']['hits'][0]['_id']
            for title in args
        ]

        mean_dict = compute_mean_vector([_['_source']['tfidf_vector'] for i in doc_id for _ in scan(es.client, index=self.tfidf.name, query={
            'query': {"match": {"docId": f"{i}"}}})])
        
        mean_dict_genre = compute_mean_vector([_['_source']['tfidf_vector_genre'] for i in doc_id for _ in scan(es.client, index=self.tfidf.name, query={
            'query': {"match": {"docId": f"{i}"}}})])

        rec_tmp = list()
        for rec in es.get_all_doc(self.tfidf):
            # TODO: Check only within the genre or assign a composite score
            if rec['_source']['docId'] not in doc_id:
                dict_tmp = {
                    tmp['word']: tmp['tfIdf']
                    for tmp in rec['_source']['tfidf_vector']
                }
                intersection_key = dict_tmp.keys() & mean_dict.keys()

                try:
                    dict_tmp_genre = {
                        tmp['word']: tmp['tfIdf']
                        for tmp in rec['_source']['tfidf_vector_genre']
                    }
                    intersection_key_genre = dict_tmp_genre.keys() & mean_dict_genre.keys()
                    rec_tmp.append((rec['_source']['docId'], sum(dict_tmp_genre[k] * mean_dict_genre[k] for k in intersection_key_genre) + sum(dict_tmp[k] * mean_dict[k] for k in intersection_key)))
                except:
                    rec_tmp.append((rec['_source']['docId'], sum(dict_tmp[k] * mean_dict[k] for k in intersection_key)))

                # TODO: Divide by the euclidean distance value

        # Sorting the recommendation basis the score
        rec_tmp = sorted(rec_tmp, key=lambda tup: tup[1], reverse=True)
        resp = {
            "no_recommendations": len(rec_tmp),
            "hits": list()
        }
        i = 0
        for doc_id, score in rec_tmp:
            if i == 10 or score < 150:
                resp['no_recommendations'] = i+1
                return resp

            tmp = es.client.search(index=self.index.name, query={
                "bool": {
                    "must": {
                        "match": {"_id": doc_id}
                    }
                }
            })['hits']['hits'][0]['_source']
            resp['hits'].append({
                "docId": doc_id,
                "score": score,
                "title": tmp['title'],
                "summary": tmp['summary'],
                "authors": tmp['authors'],
                "rating": tmp['rating'],
                "genre": tmp['genre']
            })
            i += 1

    def suggest(self, title):
        return self.client.search(
            index=self.index.name,
            body={
                "suggest": {
                    "my_s5": {  # any string
                        "text": title,
                        "completion": {
                            "field": "title_suggestion"
                        }
                    }
                }
            }
        )
