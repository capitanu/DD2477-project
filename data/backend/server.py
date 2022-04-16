import os

from elasticsearch import Elasticsearch
from flask import Flask, request, render_template, send_from_directory
from flask_cors import CORS
import json

from backend.similarity import recommendation

app = Flask(__name__, static_url_path='/', static_folder="./static")
CORS(app)

es = Elasticsearch(
    "http://localhost:9200"
)

if es.ping():
    print('Successfully connected to elasticsearch')
else:
    print('Could not connect to elasticsearch')
    exit(1)


@app.route("/")
def index():
    return app.send_static_file('index.html')


@app.route("/query", methods=['GET', 'POST'])
def query():
    a = request
    req = json.loads(request.data)
    response = es.search(
        index="books",
        body={
            "query": {
                "match": {
                    "title": {
                        "query": req.get("title", ""),
                        "fuzziness": 1
                    }
                }
            }
        }
    )
    return json.dumps(response["hits"])


@app.route("/recommend", methods=['GET', 'POST'])
def recommend():
    req = json.loads(request.data)
    return json.dumps(recommendation(*req.get("titles", [])))


@app.route("/suggest", methods=['GET', 'POST'])
def suggest():
    req = json.loads(request.data)
    response = es.search(
        index="books",
        body={
            "suggest": {
                "my_s5": {  # any string
                    "text": req.get("title", ""),
                    "completion": {
                        "field": "title_suggestion"
                    }
                }
            }
        }
    )
    return json.dumps(response["suggest"]["my_s5"])


app.run(port=8092)
