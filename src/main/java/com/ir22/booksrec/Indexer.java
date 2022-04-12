package com.ir22.booksrec;

import java.io.*;
import java.util.*;
import java.nio.charset.*;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

/**
 *   Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer {

    /** The index to be built up by this Indexer. */
    Index index;

    /** K-gram index to be built up by this Indexer */
    KGramIndex kgIndex;

    /** The next docID to be generated. */
    private int lastDocID = 0;

    /** The patterns matching non-standard words (e-mail addresses, etc.) */
    String patterns_file;

	ElasticsearchClient client;

    /* ----------------------------------------------- */


    /** Constructor */
    public Indexer( Index index, KGramIndex kgIndex, String patterns_file ) {
        this.index = index;
        this.kgIndex = kgIndex;
        this.patterns_file = patterns_file;

		RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		this.client = new ElasticsearchClient(transport);

    }


    /** Generates a new document identifier as an integer. */
    private int generateDocID() {
        return lastDocID++;
    }

	public int getLastDocID(){
		return lastDocID;
	}
	
    /**
     *  Tokenizes and indexes the file @code{f}. If <code>f</code> is a directory,
     *  all its files and subdirectories are recursively processed.
     */
    public void processText( ElasticsearchClient client, String index, String text, int docID, boolean is_indexing ) {
        // do not try to index fs that cannot be read
        if (is_indexing) {
			try {
				Reader reader = new StringReader(text);
				Tokenizer tok = new Tokenizer( reader, true, false, true, patterns_file );

				int offset = 0;
				while ( tok.hasMoreTokens() ) {
					String token = tok.nextToken();
					ESQuerier.addPostingsEntry(client, index, token, docID, offset++);
					//insertIntoIndex( docID, token, offset++);
				}
				//index.docNames.put( docID, title );
				//index.docLengths.put( docID, offset );
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


    /* ----------------------------------------------- */


    /**
     *  Indexes one token.
     */
    public void insertIntoIndex( int docID, String token, int offset ) {		
        index.insert( token, docID, offset );
        if (kgIndex != null)
            kgIndex.insert(token);
    }
}

