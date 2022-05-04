# DD2477-project

Project 6: Book recommendation engine

## Environment

docker (with docker-compose)
python3 with these libraries installed:
flask nltk elasticsearch beautifulsoup4 langdetect numpy

## How to run

```bash
# run elasticsearch and kibana as docker containers, the kibana will be available at localhost:5601
docker compose -f service.yml up
# use docker compose -f service.yml down to shutdown the service

# if you run it for the first time
cd src/backend/helper
# uncomment the line starts with 'meta_data_loader' and run this script to create the indexes
python3 loader.py
# uncomment the line starts with 'book_loader' and run this script to scrape some data (100 books by default)
python3 loader.py

# start an HTTP server for the frontend and the recommendation
cd src/backend
python3 server.py  # the UI could be visited at localhost:8092  
```