package com.ir22.booksrec;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import java.util.ArrayList;
import java.util.List;

public class ESQuerier {

	static class PostingsList2 {

		static class PostingsEntry {
			public int docID;
			public ArrayList<Integer> offsetlist;

			public int getDocID(){
				return this.docID;
			}

			public void setDocID(int docID){
				this.docID = docID;
			}

			public ArrayList<Integer> getOffsetlist() {
				return this.offsetlist;
			}

			public void setOffsetlist(ArrayList<Integer> offsetlist) {
				this.offsetlist = offsetlist;
			}

		}
    
		ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();
		String word;

		public void setWord(String word){
			this.word = word;
		}

		public String getWord() {
			return this.word;
		}

		public void setList(ArrayList<PostingsEntry> list){
			this.list = list;
		}

		public ArrayList<PostingsEntry> getList(){
			return this.list;
		}
	}

	public static PostingsList getPostingsList(ElasticsearchClient client, String index, String token) {

		try {
			PostingsList2 search = client.search(s -> s
										.index(index)
										.query(q -> q
											   .term(t -> t
													 .field("word")
													 .value(v -> v.stringValue(token))
													 )),
										PostingsList2.class).hits().hits().get(0).source();


			if(search == null)
				return null;
			
			PostingsList pl = new PostingsList();
			pl.word = search.word;
			for(PostingsList2.PostingsEntry pe : search.list) {
				PostingsEntry pe_new = new PostingsEntry(pe.docID);
				pe_new.offsetlist = pe.offsetlist;
				pl.put(pe_new);
			}
			
			return pl;
		} catch (Exception e) {}
		return null;
	}

	public static void addPostingsEntry(ElasticsearchClient client, String index, String token, PostingsEntry pe) {

		try {
			List<Hit<PostingsList2>> search = client.search(s -> s
										.index(index)
										.query(q -> q
											   .term(t -> t
													 .field("word")
													 .value(v -> v.stringValue(token))
													 )),
													   PostingsList2.class).hits().hits();


			if(search.size() == 0) {
				PostingsList2 pl2 = new PostingsList2();
				PostingsList2.PostingsEntry pe2 = new PostingsList2.PostingsEntry();
				pe2.setDocID(pe.docID);
				pe2.setOffsetlist(pe.offsetlist);
				pl2.list.add(pe2);
				pl2.setWord(token);
				client.index(b -> b
							 .index(index)
							 .document(pl2)
							 .refresh(Refresh.True)
							 );
			} else {
				PostingsList2 pl2 = search.get(0).source();
				PostingsList2.PostingsEntry pe2 = new PostingsList2.PostingsEntry();				
				pe2.setDocID(pe.docID);
				pe2.setOffsetlist(pe.offsetlist);
				pl2.list.add(pe2);

				client.index(b -> b
							 .index(index)
							 .document(pl2)
							 .id(search.get(0).id())
							 .refresh(Refresh.True)
							 );
			}
			
		} catch (Exception e) {}
	}

	public static void main(String[] args) {
		RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		ElasticsearchClient client = new ElasticsearchClient(transport);
		
		String index = "summary-index";
		
		System.out.println(getPostingsList(client, index, "the"));


		PostingsEntry pe = new PostingsEntry(14);
		pe.offsetlist.add(19);
		pe.offsetlist.add(127);
		pe.offsetlist.add(503);
		
		addPostingsEntry(client, index, "them", pe);
		
	}
}
