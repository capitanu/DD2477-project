from urllib import request

from bs4 import BeautifulSoup
from langdetect import detect

from esQueries import ESClientManager
from esQueries.indices import Book, PostingList, IDF, TfIDF
from helper.inv_index import InvIndex
from helper.tfidf import TfIdfHelper

from helper.config import MAX_NUM


def book_loader(es, book, start_index):
    es.create_index(book)
    count = 0
    i = 0
    while count <= MAX_NUM:
        with request.urlopen("https://www.goodreads.com/book/show/{}".format(start_index + i)) as fp:
            data = fp.read().decode("utf8")

        soup = BeautifulSoup(data, "html.parser")
        book_summary = soup.find(id="descriptionContainer").text.strip("\n").strip(" ")
        try:
            if detect(book_summary) == 'en':
                book_title = soup.find(id="bookTitle").text.strip("\n").strip(" ")
                book_authors = soup.find(id="bookAuthors").text.strip("\n").strip().strip("by").strip()
                try:
                    book_rating = float(soup.find(id="bookMeta").text.strip("\n").strip(" ").partition('\n')[0])
                except ValueError:
                    book_rating = 0.0
                book_genre = set()
                for el in soup.find_all("a", class_="bookPageGenreLink"):
                    book_genre.add(str(el.text))
                entry = {
                    "title": str(book_title),
                    "title_suggestion": str(book_title),
                    "authors": str(book_authors),
                    "summary": str(book_summary),
                    "rating": book_rating,
                    "genre": list(book_genre)
                }
                es.add_entries(index=book, _id=start_index + i, doc=entry)
                count += 1
                print(f"Added entry {start_index + i}")
            i += 1
        except Exception as e:
            print(e)
            i += 1


def meta_data_loader(es, book):
    es.create_index(book)
    pos_index = PostingList()
    tfidf_index = TfIDF()
    idf_index = IDF()
    es.create_index(pos_index)
    es.create_index(tfidf_index)
    es.create_index(idf_index)
    inv_index = InvIndex(es, book)

    for k, v in inv_index.get_inv_index().items():
        es.add_entries(pos_index, _id=None, doc={
            "word": k,
            "posting_list": v
        })

    # Creating tfidf vector
    tfidf_helper = TfIdfHelper(es, pos_index, inv_index.TF)

    for k, v in tfidf_helper.IDF.items():
        es.add_entries(idf_index, _id=None, doc={
            "word": k,
            "idf_score": v
        })

    for k, tf_vector in tfidf_helper.TF_IDF.items():
        title = es.get_by_id(book, k)['_source']['title']
        es.add_entries(tfidf_index, _id=None, doc={
            "docId": k,
            "title": title,
            "tfidf_vector": [{"word": word, "tfIdf": val} for word, val in tf_vector.items()]
        })


if __name__ == '__main__':
    es_helper = ESClientManager()
    book_index = Book()
    # TODO: Some bug running both lines together
    book_loader(es_helper, book_index, 29496494)
    meta_data_loader(es_helper, book_index)
