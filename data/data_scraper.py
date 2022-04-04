#!/usr/bin/python

from bs4 import BeautifulSoup
import urllib.request
import re
from elasticsearch import Elasticsearch

es = Elasticsearch()
if es.ping():
    print('Successfully connected to elasticsearch')
else:
    print('Could not connect to elasticsearch')
    exit(1)


mapping = {
    "mappings": {
        "properties": {
            "title": {
                "type": "text"
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
}

    
if not es.indices.exists(index="books"):
    es.indices.create(index="books", body = mapping)

start_index = 29496443
number_of_books = 1
for i in range(start_index, start_index + number_of_books):
    fp = urllib.request.urlopen("https://www.goodreads.com/book/show/{}".format(i))
    mybytes = fp.read()
    fp.close()

    html_doc = mybytes.decode("utf8")

    soup = BeautifulSoup(html_doc, "html.parser")
    book_title = soup.find(id="bookTitle").text.strip("\n").strip(" ")
    book_authors = soup.find(id="bookAuthors").text.strip("\n").strip().strip("by").strip()
    book_summary = soup.find(id="descriptionContainer").text.strip("\n").strip(" ")
    try:
        book_rating = float(soup.find(id="bookMeta").text.strip("\n").strip(" ").partition('\n')[0])
    except:
        book_rating = 0
    book_genre = ""
    for el in soup.find_all("a", class_="bookPageGenreLink"):
        book_genre = book_genre + " " + str(el.text)    

    entry = {
        "title" : str(book_title),
        "authors": str(book_authors),
        "summary": str(book_summary),
        "rating": book_rating,
        "genre": str(book_genre)
    }
    print(entry)
    es.index(index="books", id=i, body=entry)



