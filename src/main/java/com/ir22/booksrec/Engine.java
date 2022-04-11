package com.ir22.booksrec;

import java.util.*;
import java.io.*;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

/**
 *  This is the main class for the search engine.
 */
public class Engine {

    /** The inverted index. */
	Index index = new HashedIndex();

    /** The indexer creating the search index. */
    Indexer indexer;

    /** K-gram index */
    KGramIndex kgIndex = new KGramIndex(2);

    /** The searcher used to search the index. */
    Searcher searcher;

    /** Spell checker */
    SpellChecker speller;

    /** The engine GUI. */
    SearchGUI gui;

    /** Lock to prevent simultaneous access to the index. */
    Object indexLock = new Object();

    /** The patterns matching non-standard words (e-mail addresses, etc.) */
    String patterns_file = null;

    /** The file containing the logo. */
    String pic_file = "";

    /** The file containing the pageranks. */
    String rank_file = "";

    /** For persistent indexes, we might not need to do any indexing. */
    boolean is_indexing = true;

	public HashMap<Integer, String> idToDocName = new HashMap<Integer, String>();

    /* ----------------------------------------------- */


	private static class Book{
		int rating;
		String title;
		String authors;
		String summary;
		String genre;

		public int getRating() {
			return rating;
		}

		public void setRating(int rating) {
			this.rating = rating;
		}

		public String getGenre() {
			return genre;
		}

		public void setGenre(String genre) {
			this.genre = genre;
		}
		
		public String getAuthors() {
			return authors;
		}

		public void setAuthors(String authors) {
			this.authors = authors;
		}
		
		public String getSummary() {
			return summary;
		}

		public void setSummary(String summary) {
			this.summary = summary;
		}
		
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

	}
	
    /**  
     *   Constructor. 
     *   Indexes all chosen directories and files
     */
    public Engine( String[] args ) {


        decodeArgs( args );
		speller = new SpellChecker(index, kgIndex);
        indexer = new Indexer( index, kgIndex, patterns_file );
        searcher = new Searcher( index, kgIndex );
        gui = new SearchGUI( this );
        gui.init();
        /* 
         *   Calls the indexer to index the chosen directory structure.
         *   Access to the index is synchronized since we don't want to 
         *   search at the same time we're indexing new files (this might 
         *   corrupt the index).
         */

		RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		ElasticsearchClient client = new ElasticsearchClient(transport);
        if (is_indexing) {
			gui.displayInfoText( String.format( "Select books that you have read and liked."));
            synchronized ( indexLock ) {
				
			
				int id_search = 29496449;
				for(int i = 0; i < 151; i++){
					try {
						final String tmp = String.valueOf(id_search);
						Book book = client.get(b -> b
											   .index("books")
											   .id(tmp),
											   Book.class).source();
						idToDocName.put(id_search, book.getTitle());
						
						indexer.processFiles( book.getSummary(), book.getTitle(), id_search, is_indexing );

					} catch (Exception e) { continue; }
					id_search++;

				}

			}
			index.cleanup();
        } else {
            gui.displayInfoText( "Index already exists" );
        }

		gui.displayOptions(idToDocName.size());
    }


    /* ----------------------------------------------- */

    /**
     *   Decodes the command line arguments.
     */
    private void decodeArgs( String[] args ) {
        int i=0, j=0;
        while ( i < args.length ) {
            if ( "-p".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    patterns_file = args[i++];
                }
            } else if ( "-l".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    pic_file = args[i++];
                }
            } else if ( "-r".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    rank_file = args[i++];
                }
            } else if ( "-ni".equals( args[i] )) {
                i++;
                is_indexing = false;
            } else {
                System.err.println( "Unknown option: " + args[i] );
                break;
            }
        }                   
    }


    /* ----------------------------------------------- */
    public static void main( String[] args ) {
        Engine e = new Engine( args );
    }

}

