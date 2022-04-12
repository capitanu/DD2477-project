#!/usr/bin/env python
import json

from elasticsearch import Elasticsearch

es = Elasticsearch()


hit = es.search(index="index", body={"query": {"bool": {"must": [{"bool": {"should": [{"match_phrase": {"word": "the"}}]}}]}}})['hits']['hits'][0]


print(hit['_id'])

postings = hit['_source']['postings']
postings.append({'docID' : '25', 'offsets' : ['4', '6', '99']})
hit['_source']['postings'] = postings
print(hit)

resp = es.update(index="index", id=hit['_id'], body={"doc" : hit['_source']})
print(resp['result'])
