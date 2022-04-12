#!/usr/bin/env python

from elasticsearch import Elasticsearch

es = Elasticsearch()


resp = es.search(index="index", body={"query": {"bool": {"must": [{"bool": {"should": [{"match_phrase": {"word": "the"}}]}}]}}})

print(resp['hits']['hits'][0])

