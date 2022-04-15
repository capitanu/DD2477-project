from elasticsearch import Elasticsearch
from elasticsearch.helpers import scan


class ESClientManager:
    def __init__(self):
        self.client = Elasticsearch("http://localhost:9200")
        if self.client.ping():
            print('Successfully connected to elasticsearch')
        else:
            print('Could not connect to elasticsearch')

    def create_index(self, index):
        """

        :param index:
        :type index: Union[esQueries.indices.Index, esQueries.indices.Book]
        :return:
        :rtype:
        """
        if self.client.indices.exists(index=index.name):
            print(f"Index {index.name} already exists! Ignoring create prompt!")
        else:
            self.client.indices.create(index=index.name, mappings=index.mapping)

    def add_entries(self, index, _id, doc):
        self.client.index(index=index.name, id=_id, document=doc)

    def get_all_id(self, index):
        return [
            _['_id'] for _ in self.get_all_doc(index)
        ]

    def get_by_id(self, index, _id):
        return self.client.get(index = index.name, id=_id)

    def get_all_doc(self, index):
        return [_ for _ in scan(self.client, index=index.name, query={"query": {"match_all": {}}})]
