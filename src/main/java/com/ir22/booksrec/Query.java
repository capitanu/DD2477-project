package com.ir22.booksrec;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.*;
import java.nio.charset.*;
import java.io.*;


/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    static class QueryTerm {
        String term;
        double weight;
        QueryTerm( String t, double w ) {
            term = t;
            weight = w;
        }
    }

    /** 
     *  Representation of the query as a list of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryterm = new ArrayList<QueryTerm>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     */
    double alpha = 0.2;

    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 1 - alpha;
    
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
    
    
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
        StringTokenizer tok = new StringTokenizer( queryString );
        while ( tok.hasMoreTokens() ) {
            queryterm.add( new QueryTerm(tok.nextToken(), 1.0) );
        }    
    }
    
    
    /**
     *  Returns the number of terms
     */
    public int size() {
        return queryterm.size();
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
        double len = 0;
        for ( QueryTerm t : queryterm ) {
            len += t.weight; 
        }
        return len;
    }
    
    
    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        for ( QueryTerm t : queryterm ) {
            queryCopy.queryterm.add( new QueryTerm(t.term, t.weight) );
        }
        return queryCopy;
    }
    
    
    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results of the previous query.
     *  @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     *  @param engine The search engine object
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Engine engine ) {
		for(int i = 0 ; i < queryterm.size(); i++){
			QueryTerm qt = new QueryTerm(queryterm.get(i).term, queryterm.get(i).weight * alpha);
			queryterm.set(i, qt);
		}
		for(int i = 0; i < docIsRelevant.length; i++){
			if(docIsRelevant[i]){
				try {
					engine.indexer.index.wordsInDocs.clear();
					Reader reader = new InputStreamReader( new FileInputStream(engine.indexer.index.docNames.get(results.get(i).docID)), StandardCharsets.UTF_8 );
					Tokenizer tok = new Tokenizer( reader, true, false, true, engine.indexer.patterns_file );
					
					while ( tok.hasMoreTokens() ) {
						engine.indexer.index.wordsInDocs.add(tok.nextToken());
					}
					reader.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
				
				for(String word : engine.indexer.index.wordsInDocs){

					if(!queryterm.contains(word)){
						queryterm.add(new QueryTerm(word, beta * Collections.frequency(engine.indexer.index.wordsInDocs, word) / engine.indexer.index.wordsInDocs.size()));
					} else {
						queryterm.set(queryterm.indexOf(word), new QueryTerm(word, queryterm.get(queryterm.indexOf(word)).weight + beta * Collections.frequency(engine.indexer.index.wordsInDocs, word) / engine.indexer.index.wordsInDocs.size()));
					}					
				}
			}
		}
    }
}


