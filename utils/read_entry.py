#!/usr/bin/env python

from elasticsearch import Elasticsearch

es = Elasticsearch("http://localhost:9200")

resp = es.search(index="books", body={
    'size': 10,
    'query': {
        'match_all': {}
    }
})

print(resp['hits']['hits'][1])
