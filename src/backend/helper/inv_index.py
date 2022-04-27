import re

from nltk import WordNetLemmatizer

class InvIndex:
    INV_INDEX = dict()
    TF = dict()
    lem = WordNetLemmatizer()

    def __init__(self, es, index, part, separator):
        self.part = part
        self.separator = separator
        self.records = es.get_all_doc(index)
        self.run()

    def _add_entry(self, str_val, off_set, doc_id):
        """
        Generates the dictionary that can passsed to elastic search handler
        :param str_val:
        :type str_val:
        :param off_set:
        :type off_set:
        :param doc_id:
        :type doc_id:
        :return:
        :rtype:
        """
        # Checks if the word is already present
        if str_val in self.INV_INDEX:
            doc_id_flag = False
            # Goes through list of dictionaries to find if doc_id  is already present
            for ix, dict_val in enumerate(self.INV_INDEX[str_val]):
                if doc_id == dict_val['docId']:
                    doc_id_flag = True
                    # Appends the off_set value to the list
                    if off_set not in self.INV_INDEX[str_val][ix]['offSetArray']:
                        self.INV_INDEX[str_val][ix]['offSetArray'].append(off_set)
                    break
            # If doc id is not present, create a new entry
            if not doc_id_flag:
                self.INV_INDEX[str_val].append({
                    "docId": doc_id,
                    "offSetArray": [off_set]
                })
        #  If the word has not already been encountered adds the following
        else:
            self.INV_INDEX[str_val] = [{"docId": doc_id,
                                        "offSetArray": [off_set]}]

    def _add_tf(self, doc_id, word):
        """
        Populates the term frequency of each word in the document
        :param doc_id:
        :type doc_id: int

        :param word:
        :type word: str

        :return: Nothing
        :rtype: None
        """
        if doc_id in self.TF:
            # If doc id is already present, checks if the word is already present
            if word in self.TF[doc_id]:
                self.TF[doc_id][word] += 1
            else:
                self.TF[doc_id][word] = 1
        else:
            self.TF[doc_id] = {
                word: 1
            }

    def run(self):
        # Iterating though documents
        for tmp_ix, rec in enumerate(self.records):
            doc_id = rec['_id']
            # TODO: Change the regex rule !
            # Splits the summary to token
            if self.part == 'genre':
                list_tmp = rec['_source'][self.part]
            else:
                list_tmp = [_ for _ in re.split(self.separator, rec['_source'][self.part]) if _ != '']

            for off_set, s in enumerate(list_tmp):
                # Cleaning the string to contain the stemmed version of every word
                cleaned_str = self.lem.lemmatize(s).lower()
                # Adds the string to the INV_IND
                self._add_entry(cleaned_str, off_set, doc_id)
                # Counts the number of terms in each document
                self._add_tf(doc_id, cleaned_str)

    def get_inv_index(self):
        return self.INV_INDEX

    def get_tf_vector(self):
        return self.TF


class InvIndexGenre:
    INV_INDEX = dict()
    TF = dict()
    lem = WordNetLemmatizer()

    def __init__(self, es, index, part, separator):
        self.part = part
        self.separator = separator
        self.records = es.get_all_doc(index)
        self.run()

    def _add_entry(self, str_val, off_set, doc_id):
        """
        Generates the dictionary that can passsed to elastic search handler
        :param str_val:
        :type str_val:
        :param off_set:
        :type off_set:
        :param doc_id:
        :type doc_id:
        :return:
        :rtype:
        """
        # Checks if the word is already present
        if str_val in self.INV_INDEX:
            doc_id_flag = False
            # Goes through list of dictionaries to find if doc_id  is already present
            for ix, dict_val in enumerate(self.INV_INDEX[str_val]):
                if doc_id == dict_val['docId']:
                    doc_id_flag = True
                    # Appends the off_set value to the list
                    if off_set not in self.INV_INDEX[str_val][ix]['offSetArray']:
                        self.INV_INDEX[str_val][ix]['offSetArray'].append(off_set)
                    break
            # If doc id is not present, create a new entry
            if not doc_id_flag:
                self.INV_INDEX[str_val].append({
                    "docId": doc_id,
                    "offSetArray": [off_set]
                })
        #  If the word has not already been encountered adds the following
        else:
            self.INV_INDEX[str_val] = [{"docId": doc_id,
                                        "offSetArray": [off_set]}]

    def _add_tf(self, doc_id, word):
        """
        Populates the term frequency of each word in the document
        :param doc_id:
        :type doc_id: int

        :param word:
        :type word: str

        :return: Nothing
        :rtype: None
        """
        if doc_id in self.TF:
            # If doc id is already present, checks if the word is already present
            if word in self.TF[doc_id]:
                self.TF[doc_id][word] += 1
            else:
                self.TF[doc_id][word] = 1
        else:
            self.TF[doc_id] = {
                word: 1
            }

    def run(self):
        # Iterating though documents
        for tmp_ix, rec in enumerate(self.records):
            doc_id = rec['_id']
            # TODO: Change the regex rule !
            # Splits the summary to token
            if self.part == 'genre':
                list_tmp = rec['_source'][self.part]
            else:
                list_tmp = [_ for _ in re.split(self.separator, rec['_source'][self.part]) if _ != '']

            for off_set, s in enumerate(list_tmp):
                # Cleaning the string to contain the stemmed version of every word
                cleaned_str = self.lem.lemmatize(s).lower()
                # Adds the string to the INV_IND
                self._add_entry(cleaned_str, off_set, doc_id)
                # Counts the number of terms in each document
                self._add_tf(doc_id, cleaned_str)

    def get_inv_index(self):
        return self.INV_INDEX

    def get_tf_vector(self):
        return self.TF
    
