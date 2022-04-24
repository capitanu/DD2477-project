#!/usr/bin/env python

from elasticsearch import Elasticsearch

es = Elasticsearch()

entry = {
    "word" : "the",
    "list": [
        {
            "docID" : "3",
            "offsetlist" : ["3", "5", "14", "15"]
        },
        {
            "docID" : "14",
            "offsetlist" : ["2", "8", "27", "108"]
        }
    ],
}

es.delete(index="summary-index", id="8babHoABsMW6PCgq_YhM")
es.delete(index="summary-index", id="8LaaHoABsMW6PCgqW4iw")
es.index(index="summary-index", body=entry)
