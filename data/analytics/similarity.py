from elasticsearch.helpers import scan

from esQueries import ESClientManager
from esQueries.indices import TfIDF, Book


def compute_mean_vector(args):
    tot_doc = len(args)
    mean_dict = dict()
    for arg in args:
        for dict_val in arg:
            if dict_val['word'] not in mean_dict:
                mean_dict[dict_val['word']] = dict_val['tfIdf'] / tot_doc
            else:
                mean_dict[dict_val['word']] += dict_val['tfIdf'] / tot_doc

    return mean_dict


if __name__ == '__main__':
    es = ESClientManager()
    book_index = Book()
    tfidf_index = TfIDF()
    es.create_index(book_index)
    es.create_index(tfidf_index)
    doc_ids = [29496499, 29496500, 29496501]
    mean_dict = compute_mean_vector([_['_source']['tfidf_vector'] for i in doc_ids for _ in scan(es.client, index=tfidf_index.name, query={
        'query': {"match": {"docId": f"{i}"}}})])

    rec_tmp = list()
    for rec in es.get_all_doc(tfidf_index):
        if rec['_source']['docId'] not in doc_ids:
            dict_tmp = {
                tmp['word']: tmp['tfIdf']
                for tmp in rec['_source']['tfidf_vector']
            }
            intersection_key = dict_tmp.keys() & mean_dict.keys()
            rec_tmp.append((rec['_source']['docId'], sum(dict_tmp[k] * mean_dict[k] for k in intersection_key)))

    rec_tmp = sorted(rec_tmp, key=lambda tup:tup[1], reverse=True)

    print(rec_tmp)
