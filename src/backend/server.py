import json

from flask import Flask, request
from flask_cors import CORS

from backend.esQueries.operations import BookCRUD

book_crud = BookCRUD()

app = Flask(__name__, static_url_path='/', static_folder="./static")
CORS(app)


# es = Elasticsearch(
#     "http://localhost:9200"
# )
#
# if es.ping():
#     print('Successfully connected to elasticsearch')
# else:
#     print('Could not connect to elasticsearch')
#     exit(1)


@app.route("/")
def index():
    return app.send_static_file('index.html')


@app.route("/query", methods=['GET', 'POST'])
def query():
    req = json.loads(request.data)
    # response = es.search(
    #     index="books",
    #     body={
    #         "query": {
    #             "match": {
    #                 "title": {
    #                     "query": req.get("title", ""),
    #                     "fuzziness": 1
    #                 }
    #             }
    #         }
    #     }
    # )
    # return json.dumps(response["hits"])
    return json.dumps(book_crud.fetch_all(req.get("title", ""))["hits"])


@app.route("/recommend", methods=['GET', 'POST'])
def recommend():
    req = json.loads(request.data)
    return json.dumps(book_crud.recommendation(*req.get("titles", [])))


@app.route("/suggest", methods=['GET', 'POST'])
def suggest():
    req = json.loads(request.data)
    # response = es.search(
    #     index="books",
    #     body={
    #         "suggest": {
    #             "my_s5": {  # any string
    #                 "text": req.get("title", ""),
    #                 "completion": {
    #                     "field": "title_suggestion"
    #                 }
    #             }
    #         }
    #     }
    # )
    # return json.dumps(response["suggest"]["my_s5"])
    return json.dumps(book_crud.suggest(req.get("title", ""))["suggest"]["my_s5"])


app.run(port=8092)
