FROM centos:7

RUN yum install -y python3
RUN pip3 install flask
RUN pip3 install nltk
RUN pip3 install bs4
RUN pip3 install langdetect
RUN pip3 install elasticsearch
RUN pip3 install flask_cors

RUN mkdir /tmpp
COPY . /tmpp

CMD python3 /tmpp/data/backend/server.py