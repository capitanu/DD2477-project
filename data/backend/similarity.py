""""
TODO: Generate a graph of similar books at genre level
"""
from elasticsearch.helpers import scan

from esQueries import ESClientManager
from esQueries.indices import TfIDF, Book


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


def recommendation(*args):
    es = ESClientManager()
    book_index = Book()
    tfidf_index = TfIDF()

    doc_id = [
        es.client.search(index=book_index.name, query={
            "bool": {
                "must": {
                    "match": {"title": title}
                }
            }
        })['hits']['hits'][0]['_id']
        for title in args
    ]

    mean_dict = compute_mean_vector([_['_source']['tfidf_vector'] for i in doc_id for _ in scan(es.client, index=tfidf_index.name, query={
        'query': {"match": {"docId": f"{i}"}}})])

    rec_tmp = list()
    for rec in es.get_all_doc(tfidf_index):
        # TODO: Check only within the genre or assign a composite score
        if rec['_source']['docId'] not in doc_id:
            dict_tmp = {
                tmp['word']: tmp['tfIdf']
                for tmp in rec['_source']['tfidf_vector']
            }
            intersection_key = dict_tmp.keys() & mean_dict.keys()
            # TODO: Dive by the euclidean distance value
            rec_tmp.append((rec['_source']['docId'], sum(dict_tmp[k] * mean_dict[k] for k in intersection_key)))

    # Sorting the recommendation basis the score
    rec_tmp = sorted(rec_tmp, key=lambda tup: tup[1], reverse=True)
    resp = {
        "no_recommendations": len(rec_tmp)
        "hits": list()
    }
    for doc_id, score in rec_tmp:
        tmp = es.client.search(index=book_index.name, query={
            "bool": {
                "must": {
                    "match": {"_id": doc_id}
                }
            }
        })['hits']['hits'][0]['_source']
        resp['hits'].append({
            "docId": doc_id,
            "score": score,
            "title":tmp['title'],
            "summary": tmp['summary'],
            "authors": tmp['authors'],
            "rating": tmp['rating'],
            "genre": tmp['genre']
        })

    return resp
