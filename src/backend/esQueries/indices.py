class Index:
    name = None

    def __init__(self, name):
        self.name = name

    def by_id(self, _id):
        return {
            "query": {
                "bool": {
                    "must": [
                        {"match": {"id": f"{_id}"}},
                    ]
                }
            }
        }


class Book(Index):
    mapping = {
        "properties": {
            "title": {
                "type": "text"
            },
            "title_suggestion": {
                "type": "completion",
            },
            "authors": {
                "type": "text"
            },
            "summary": {
                "type": "text"
            },
            "rating": {
                "type": "double"
            },
            "genre": {
                "type": "text"
            }
        }
    }

    def __init__(self):
        super().__init__("books")


class PostingList(Index):
    mapping = {
        "properties": {
            "word": {
                "type": "text"
            },
            "posting_list": {
                "properties": {
                    "docId": {"type": "integer"},
                    "offSetArray": {"type": "integer"}
                }
            }
        }
    }

    def __init__(self):
        super().__init__("inverted_index")

class PostingList_genre(Index):
    mapping = {
        "properties": {
            "word": {
                "type": "text"
            },
            "posting_list": {
                "properties": {
                    "docId": {"type": "integer"},
                    "offSetArray": {"type": "integer"}
                }
            }
        }
    }

    def __init__(self):
        super().__init__("inverted_index_genre")
        

class IDF(Index):
    mapping = {
        "properties": {
            "word": {
                "type": "text"
            },
            "idf_score": {
                "type": "double"
            }
        }
    }

    def __init__(self):
        super().__init__("idf_index")

class IDF_genre(Index):
    mapping = {
        "properties": {
            "word": {
                "type": "text"
            },
            "idf_score": {
                "type": "double"
            }
        }
    }

    def __init__(self):
        super().__init__("idf_index_genre")


class TfIDF(Index):
    mapping = {
        "properties": {
            "docId": {
                "type": "integer"
            },
            "title": {
                "type": "text"
            },
            "tfidf_vector": {
                "properties": {
                    "word": {"type": "text"},
                    "tfIdf": {"type": "double"},
                }
            },
            "tfidf_vector_genre": {
                "properties": {
                    "word": {"type": "text"},
                    "tfIdf": {"type": "double"},
                }
            }

        }
    }

    def __init__(self):
        super().__init__("tfidf_index")
