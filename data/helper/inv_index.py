import re

from nltk import WordNetLemmatizer


class InvIndex:
    INV_INDEX = dict()
    TF = dict()
    lem = WordNetLemmatizer()

    def __init__(self, es, index):
        self.records = es.get_all_doc(index)
        self.run()

    def _add_entry(self, str_val, off_set, doc_id):
        if str_val in self.INV_INDEX:
            doc_id_flag = False
            for ix, dict_val in enumerate(self.INV_INDEX[str_val]):
                if doc_id == dict_val['docId']:
                    doc_id_flag = True
                    if off_set not in self.INV_INDEX[str_val][ix]['offSetArray']:
                        self.INV_INDEX[str_val][ix]['offSetArray'].append(off_set)
                    break
            if not doc_id_flag:
                self.INV_INDEX[str_val].append({
                    "docId": doc_id,
                    "offSetArray": [off_set]
                })
        else:
            self.INV_INDEX[str_val] = [{"docId": doc_id,
                                        "offSetArray": [off_set]}]

    def _add_tf(self, doc_id, word):
        if doc_id in self.TF:
            if word in self.TF[doc_id]:
                self.TF[doc_id][word] += 1
            else:
                self.TF[doc_id][word] = 1
        else:
            self.TF[doc_id] = {
                word: 1
            }

    def run(self):
        for tmp_ix, rec in enumerate(self.records):
            doc_id = rec['_id']
            list_tmp = [_ for _ in re.split("\s|(?<!\d)[,!.](?!\d)", rec['_source']['summary']) if _ != '']
            for off_set, s in enumerate(list_tmp):
                cleaned_str = self.lem.lemmatize(s).lower()
                self._add_entry(cleaned_str, off_set, doc_id)
                self._add_tf(doc_id, cleaned_str)

    def get_inv_index(self):
        return self.INV_INDEX

    def get_tf_vector(self):
        return self.TF
