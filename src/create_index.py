#!/usr/bin/python

from bs4 import BeautifulSoup
import urllib.request
import re
from elasticsearch import Elasticsearch

es = Elasticsearch("http://localhost:9200")
if es.ping():
    print('Successfully connected to elasticsearch')
else:
    print('Could not connect to elasticsearch')
    exit(1)


mapping = {
    "mappings": {
        "properties": {
            "word": {"type": "text"},
            "postings": 
                {
                    "properties": {
                        "docID": {"type": "integer"},
                        "offsets": {"type": "integer"}
                    }
                }
        }
    }
}

    
if not es.indices.exists(index="authors-index"):
    es.indices.create(index="authors-index", body = mapping)



