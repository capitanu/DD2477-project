package com.ir22.booksrec;

import java.util.*;
import java.io.*;


public class HITSRanker {

    /**
     *   Max number of iterations for HITS
     */
    final static int MAX_NUMBER_OF_STEPS = 1000;

    /**
     *   Convergence criterion: hub and authority scores do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.001;

    /**
     *   The inverted index
     */
    Index index;

	/**
     *   Mapping from the titles to internal document ids used in the links file
     */
    HashMap<String,Integer> titleToId = new HashMap<String,Integer>();

    /**
     *   Sparse vector containing hub scores
     */
    HashMap<Integer,Double> hubs = new HashMap<Integer, Double>();

    /**
     *   Sparse vector containing authority scores
     */
    HashMap<Integer,Double> authorities = new HashMap<Integer, Double>();

	HashMap<Integer, HashMap<Integer, Boolean>> linksTo = new HashMap<Integer, HashMap<Integer,Boolean>>();
	HashMap<Integer, HashMap<Integer, Boolean>> linksBy = new HashMap<Integer, HashMap<Integer,Boolean>>();

    
    /* --------------------------------------------- */

    /**
     * Constructs the HITSRanker object
     * 
     * A set of linked documents can be presented as a graph.
     * Each page is a node in graph with a distinct nodeID associated with it.
     * There is an edge between two nodes if there is a link between two pages.
     * 
     * Each line in the links file has the following format:
     *  nodeID;outNodeID1,outNodeID2,...,outNodeIDK
     * This means that there are edges between nodeID and outNodeIDi, where i is between 1 and K.
     * 
     * Each line in the titles file has the following format:
     *  nodeID;pageTitle
     *  
     * NOTE: nodeIDs are consistent between these two files, but they are NOT the same
     *       as docIDs used by search engine's Indexer
     *
     * @param      linksFilename   File containing the links of the graph
     * @param      titlesFilename  File containing the mapping between nodeIDs and pages titles
     * @param      index           The inverted index
     */
    public HITSRanker( String linksFilename, String titlesFilename, Index index ) {
        this.index = index;
        readDocs( linksFilename , titlesFilename );
    }


    /* --------------------------------------------- */

    /**
     * A utility function that gets a file name given its path.
     * For example, given the path "davisWiki/hello.f",
     * the function will return "hello.f".
     *
     * @param      path  The file path
     *
     * @return     The file name.
     */
    private String getFileName( String path ) {
        String result = "";
        StringTokenizer tok = new StringTokenizer( path, "\\/" );
        while ( tok.hasMoreTokens() ) {
            result = tok.nextToken();
        }
        return result;
    }


    /**
     * Reads the files describing the graph of the given set of pages.
     *
     * @param      linksFilename   File containing the links of the graph
     */
    void readDocs( String linksFilename, String titlesFilename ) {
    	try {
    		BufferedReader linksFile = new BufferedReader(new FileReader(linksFilename));
            BufferedReader titlesFile = new BufferedReader(new FileReader(titlesFilename));
            
            String line;
            while ((line = titlesFile.readLine()) != null) {
            	String[] rtn = line.split(";");
                titleToId.put(rtn[1], Integer.parseInt(rtn[0]));
            }
            
            while ((line = linksFile.readLine()) != null) {
            	
            	int index = line.indexOf(";");
				int title = Integer.parseInt(line.substring(0, index));

				StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
				HashMap<Integer, Boolean> list = new HashMap<Integer, Boolean>();
				while( tok.hasMoreTokens()){
					String otherTitle = tok.nextToken();
					list.put(Integer.parseInt(otherTitle), true);
            	}
				
				linksTo.put(title, list);
				
				HashMap<Integer, Boolean> listBy;
				for( Map.Entry<Integer,Boolean> entry : list.entrySet() ) {
					listBy = linksBy.get(entry.getKey());
					if(listBy == null){
						listBy = new HashMap<Integer, Boolean>();
					}
					listBy.put(title, true);
					linksBy.put(entry.getKey(), listBy);
				}
			}
				
		} catch (IOException io) {
			io.printStackTrace();
		}
    	
    }
    
    public void iterate(String[] titles) {
    	
    	for (int i = 0; i < titles.length; i++) {
			hubs.put(titleToId.get(titles[i]), 1.0);
			authorities.put(titleToId.get(titles[i]), 1.0);
			try{
				for(Map.Entry<Integer,Boolean> entry : linksTo.get(titleToId.get(titles[i])).entrySet()){
					hubs.put(entry.getKey(), 1.0);
					authorities.put(entry.getKey(), 1.0);
				}
				for(Map.Entry<Integer,Boolean> entry : linksBy.get(titleToId.get(titles[i])).entrySet()){
					hubs.put(entry.getKey(), 1.0);
					authorities.put(entry.getKey(), 1.0);
				}

			} catch (NullPointerException npe) {}
    	}
    	
		boolean converged = false;
    	while(!converged) {

			converged = true;
			
			double auth_sum = 0, hubs_sum = 0;
        	HashMap<Integer, Double> hubs_tmp = new HashMap<Integer, Double>();
    	    HashMap<Integer, Double> auth_tmp = new HashMap<Integer, Double>();
			
			double auth_err = 0, hubs_err = 0;
			for(Map.Entry<Integer, Double> hub : hubs.entrySet()){

				double sum = 0.0;
				try{
					for(Map.Entry<Integer, Boolean> to : linksTo.get(hub.getKey()).entrySet()){
						sum += authorities.getOrDefault(to.getKey(), 0.0);
					}
				} catch (NullPointerException npe) {}
				hubs_sum += sum*sum;
				hubs_tmp.put(hub.getKey(), sum);
			}

			for(Map.Entry<Integer, Double> auth : authorities.entrySet()){
				double sum = 0.0;
				try {
					for(Map.Entry<Integer, Boolean> by : linksBy.get(auth.getKey()).entrySet()){
						sum += hubs.getOrDefault(by.getKey(), 0.0);
					}
				} catch (NullPointerException npe) {}
				auth_sum += sum*sum;
				auth_tmp.put(auth.getKey(), sum);
			}			

			double hubs_sum_final = Math.sqrt(hubs_sum);
			double auth_sum_final = Math.sqrt(auth_sum);

			for(Map.Entry<Integer, Double> auth : auth_tmp.entrySet()){
				auth_tmp.replace(auth.getKey(), auth.getValue()/auth_sum_final);
				auth_err += (authorities.get(auth.getKey()) - auth_tmp.get(auth.getKey())) * (authorities.get(auth.getKey()) - auth_tmp.get(auth.getKey()));
			}			
			for(Map.Entry<Integer, Double> hub : hubs_tmp.entrySet()){
				hubs_tmp.replace(hub.getKey(), hub.getValue()/hubs_sum_final);
				hubs_err += (hubs.get(hub.getKey()) - hubs_tmp.get(hub.getKey())) * (hubs.get(hub.getKey()) - hubs_tmp.get(hub.getKey()));
			}
			
	        hubs = hubs_tmp;
	        authorities = auth_tmp;
	        
	        if(Math.sqrt(hubs_err) > EPSILON || Math.sqrt(auth_err) > EPSILON) 
				converged = false;
    	}
    }

    /**
     * Rank the documents in the subgraph induced by the documents present
     * in the postings list `post`.
     *
     * @param      post  The list of postings fulfilling a certain information need
     *
     * @return     A list of postings ranked according to the hub and authority scores.
     */
    PostingsList rank(PostingsList post) {

		String[] titles = new String[post.size()];
		for(int i = 0; i < post.size(); i++){
			titles[i] = getFileName(index.docNames.get(post.get(i).docID));
		}
		iterate(titles);
		for(int i = 0; i < post.size(); i++){
			String title = getFileName(index.docNames.get(post.get(i).docID));
			post.get(i).score = (hubs.get(titleToId.get(title)) + authorities.get(titleToId.get(title)))/2;
		}
		
        return post;
    }


    /**
     * Sort a hash map by values in the descending order
     *
     * @param      map    A hash map to sorted
     *
     * @return     A hash map sorted by values
     */
    private HashMap<Integer,Double> sortHashMapByValue(HashMap<Integer,Double> map) {
        if (map == null) {
            return null;
        } else {
            List<Map.Entry<Integer,Double> > list = new ArrayList<Map.Entry<Integer,Double> >(map.entrySet());
      
            Collections.sort(list, new Comparator<Map.Entry<Integer,Double>>() {
					public int compare(Map.Entry<Integer,Double> o1, Map.Entry<Integer,Double> o2) { 
						return (o2.getValue()).compareTo(o1.getValue()); 
					} 
				}); 
              
            HashMap<Integer,Double> res = new LinkedHashMap<Integer,Double>(); 
            for (Map.Entry<Integer,Double> el : list) { 
                res.put(el.getKey(), el.getValue()); 
            }
            return res;
        }
    } 


    /**
     * Write the first `k` entries of a hash map `map` to the file `fname`.
     *
     * @param      map        A hash map
     * @param      fname      The filename
     * @param      k          A number of entries to write
     */
    void writeToFile(HashMap<Integer,Double> map, String fname, int k) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fname));
            
            if (map != null) {
                int i = 0;
                for (Map.Entry<Integer,Double> e : map.entrySet()) {
                    i++;
                    writer.write(e.getKey() + ": " + String.format("%.5g%n", e.getValue()));
                    if (i >= k) break;
                }
            }
            writer.close();
        } catch (IOException e) {}
    }


    /**
     * Rank all the documents in the links file. Produces two files:
     *  hubs_top_30.txt with documents containing top 30 hub scores
     *  authorities_top_30.txt with documents containing top 30 authority scores
     */
    void rank() {
		iterate(titleToId.keySet().toArray(new String[0]));
        HashMap<Integer,Double> sortedHubs = sortHashMapByValue(hubs);
        HashMap<Integer,Double> sortedAuthorities = sortHashMapByValue(authorities);
        writeToFile(sortedHubs, "hubs_top_30.txt", 30);
        writeToFile(sortedAuthorities, "authorities_top_30.txt", 30);
    }


    /* --------------------------------------------- */


    public static void main( String[] args ) {
        if ( args.length != 2 ) {
            System.err.println( "Please give the names of the link and title files" );
        }
        else {
            HITSRanker hr = new HITSRanker( args[0], args[1], null );
            hr.rank();
        }
    }
} 
