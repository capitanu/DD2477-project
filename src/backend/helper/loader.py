from urllib import request

from bs4 import BeautifulSoup
from langdetect import detect

from esQueries import ESClientManager
from esQueries.indices import Book, PostingList, PostingList_genre, IDF, IDF_genre, TfIDF
from helper.inv_index import InvIndex, InvIndexGenre
from helper.tfidf import TfIdfHelper, TfIdfHelperGenre

from helper.config import MAX_NUM

def book_loader(es, book, start_index):
    es.create_index(book)
    count = 0
    i = 0
    while count < MAX_NUM:
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
    pos_index = PostingList()
    pos_index_genre = PostingList_genre()
    tfidf_index = TfIDF()
    idf_index = IDF()
    idf_index_genres = IDF_genre()
    
    es.create_index(book)
    es.create_index(pos_index)
    es.create_index(pos_index_genre)
    es.create_index(tfidf_index)
    es.create_index(idf_index)
    es.create_index(idf_index_genres)
    
    inv_index = InvIndex(es, book, 'summary', "(\s|(?<!\d)[,!.](?!\d))+")
    inv_index_genre = InvIndexGenre(es, book, 'genre', ", ")
    
    for k, v in inv_index.get_inv_index().items():
        es.add_entries(pos_index, _id=None, doc={
            "word": k,
            "posting_list": v
        })
    for k, v in inv_index_genre.get_inv_index().items():
        es.add_entries(pos_index_genre, _id=None, doc={
            "word": k,
            "posting_list": v
        })


    # Creating tfidf vector
    tfidf_helper = TfIdfHelper(es, pos_index, inv_index.TF)
    tfidf_genre_helper = TfIdfHelperGenre(es, pos_index_genre, inv_index_genre.TF)

    for k, v in tfidf_helper.IDF.items():
        es.add_entries(idf_index, _id=None, doc={
            "word": k,
            "idf_score": v
        })

    for k, v in tfidf_genre_helper.IDF.items():
        es.add_entries(idf_index_genres, _id=None, doc={
            "word": k,
            "idf_score": v
        })


    for k, tf_vector in tfidf_helper.TF_IDF.items():
        title = es.get_by_id(book, k)['_source']['title']
        try:
            es.add_entries(tfidf_index, _id=None, doc={
                "docId": k,
                "title": title,
                "tfidf_vector": [{"word": word, "tfIdf": val} for word, val in tf_vector.items()],
                "tfidf_vector_genre": [{"word": word, "tfIdf": val} for word, val in tfidf_genre_helper.TF_IDF[k].items()]
            })
        except:
            es.add_entries(tfidf_index, _id=None, doc={
                "docId": k,
                "title": title,
                "tfidf_vector": [{"word": word, "tfIdf": val} for word, val in tf_vector.items()],
            })


if __name__ == '__main__':
    es_helper = ESClientManager()
    book_index = Book()
    # TODO: Some bug running both lines together
#    book_loader(es_helper, book_index, 29496449)
#    es_helper.client.transport.close()
#    es_helper = ESClientManager()
    meta_data_loader(es_helper, book_index)
