/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.*;




/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    private static final boolean COMPUTE_ALL_IDFS = false;
	private HashMap<String, Double> idfs = new HashMap<String, Double>();

    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
		PostingsList pl;
		PostingsEntry pe;
		
		if(index.containsKey(token)){
			pl = index.get(token);
			pe = new PostingsEntry(docID);
			pl.put(pe, offset);
			return;
		}
		pl = new PostingsList();
		pe = new PostingsEntry(docID);
		pl.put(pe, offset);
		index.put(token, pl);
    }

    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
		return index.get(token);
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
		// We can compute the idf here for all the
		if(COMPUTE_ALL_IDFS){
			try {
				String patterns_file = "patterns.txt";
				Reader reader = new InputStreamReader( new FileInputStream(new File("/home/calin/kth/TCSCM1/DD2477_Search_Engines_and_Information_Retrieval_Systems/assignments/davisWiki/Davis_Food_Coop.f")), StandardCharsets.UTF_8 );
				Tokenizer tok = new Tokenizer( reader, true, false, true, patterns_file );
				while ( tok.hasMoreTokens() ) {
					String token = tok.nextToken();
					if(!idfs.containsKey(token)){
						PostingsList pl2 = this.getPostings(token);
						double idf_t = (double) Math.log( this.docLengths.size() / pl2.size() );
						idfs.put(token, idf_t);
					}
				}
				reader = new InputStreamReader( new FileInputStream(new File("/home/calin/kth/TCSCM1/DD2477_Search_Engines_and_Information_Retrieval_Systems/assignments/davisWiki/Resource_Recovery_Drive.f")), StandardCharsets.UTF_8 );
				tok = new Tokenizer( reader, true, false, true, patterns_file );
				while ( tok.hasMoreTokens() ) {
					String token = tok.nextToken();
					if(!idfs.containsKey(token)){
						PostingsList pl2 = this.getPostings(token);
						double idf_t = (double) Math.log( this.docLengths.size() / pl2.size() );
						idfs.put(token, idf_t);
					}
				}

				for(String key : idfs.keySet()){
					System.out.println(key + " -- idf: " + (Math.round(idfs.get(key) * 10000.0)/10000.0));
				}

			} catch(Exception e) {
				e.printStackTrace();
			}

		}
    }
}
