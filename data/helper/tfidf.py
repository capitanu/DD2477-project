import copy

import numpy


class TfIdfHelper:
    IDF = dict()

    def __init__(self, es, index, tf_dict):
        self.records = es.get_all_doc(index)
        self.tf_dict = tf_dict
        self.TF_IDF = copy.deepcopy(self.tf_dict)
        self._run()

    def _add_entry(self, word, val):
        if word not in self.IDF:
            tmp = numpy.log(100 / (1 + val))
            self.IDF[word] = tmp

    def _run(self):
        for rec in self.records:
            _id = rec['_id']
            word = rec['_source']['word']
            self._add_entry(word, len(rec['_source']['posting_list']))
        for doc_id in self.tf_dict.keys():
            for w in self.TF_IDF[doc_id].keys():
                self.TF_IDF[doc_id][w] *= self.IDF[w]