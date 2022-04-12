from elasticsearch import Elasticsearch

es = Elasticsearch()

entry = {
    "word" : "the",
    "postings": [
        {
            "docID" : "3",
            "offsets" : ["3", "5", "14", "15"]
        },
        {
            "docID" : "14",
            "offsets" : ["2", "8", "27", "108"]
        }
    ],
}
es.index(index="index", body=entry)
