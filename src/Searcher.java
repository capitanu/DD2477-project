/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;
    
    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
    }

	// How much TF_IDF should take part
	final static double COMBINATION_RATIO = 0.2;

	public void getCombination(ArrayList<ArrayList<String>> list, int N, Stack<String> stack, ArrayList<String> ret) {
        for (int i = 0; i < list.get(N).size(); i++) {
            stack.push(list.get(N).get(i));
            if (N < list.size() - 1) {
                getCombination(list, N + 1, stack, ret);
            } else {
                ArrayList<String> result = new ArrayList<String>(stack);
				StringBuilder sb = new StringBuilder();
				for(String el : result){
					sb.append(el + " ");
				}
                ret.add(sb.toString());
            }
            stack.pop();
        }
    }

	void generatePermutations(ArrayList<ArrayList<String>> lists, List<String> result, int depth, String current) {
		if (depth == lists.size()) {
			result.add(current);
			return;
		}

		for (int i = 0; i < lists.get(depth).size(); i++) {
			generatePermutations(lists, result, depth + 1, current + " " + lists.get(depth).get(i));
		}
	}

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType, NormalizationType normType ) {
		
		if(queryType == QueryType.INTERSECTION_QUERY){
			
			ArrayList<PostingsList> query_list = new ArrayList<PostingsList>();

			for (Query.QueryTerm qt: query.queryterm) {
				
				if (!qt.term.contains("*")) {
					query_list.add(index.getPostings(qt.term));
					continue;
				}

				List<KGramPostingsEntry> entries = new ArrayList<KGramPostingsEntry>();
				
				String token = "^" + qt.term + "$";

				for(String tmp : token.split("\\*")){

					if(tmp.length() >= kgIndex.getK()){
						for(int i = 0; i < tmp.length() - kgIndex.getK() + 1; i++){
							String kg = tmp.substring(i, i + kgIndex.getK());
							if(entries.isEmpty()){
								entries = kgIndex.getPostings(kg);
							} else {
								entries = kgIndex.intersect(entries, kgIndex.getPostings(kg));
							}
						}
					}
				}
				
				Pattern p = Pattern.compile(token.replace("*", ".*"));

				PostingsList postings = null;
				for(KGramPostingsEntry el : entries){
					String tmp = kgIndex.id2term.get(el.tokenID);
					tmp = tmp.substring(1, tmp.length() - 1);
					if(p.matcher(tmp).matches()) {
						postings = PostingsList.union(postings, index.getPostings(tmp));
					}
				}
				
				query_list.add(postings);
			}


			PostingsList result = query_list.get(0);
			for(int i = 1; i < query_list.size(); i++){
				result = PostingsList.intersect(result, query_list.get(i));
			}
			return result;
		}
		
		if(queryType == QueryType.PHRASE_QUERY){
			
			ArrayList<PostingsList> query_list = new ArrayList<PostingsList>();

			for (Query.QueryTerm qt: query.queryterm) {
				
				if (!qt.term.contains("*")) {
					query_list.add(index.getPostings(qt.term));
					continue;
				}

				List<KGramPostingsEntry> entries = new ArrayList<KGramPostingsEntry>();
				
				String token = "^" + qt.term + "$";

				for(String tmp : token.split("\\*")){

					if(tmp.length() >= kgIndex.getK()){
						for(int i = 0; i < tmp.length() - kgIndex.getK() + 1; i++){
							String kg = tmp.substring(i, i + kgIndex.getK());
							if(entries.isEmpty()){
								entries = kgIndex.getPostings(kg);
							} else {
								entries = kgIndex.intersect(entries, kgIndex.getPostings(kg));
							}
						}
					}
				}
				
				Pattern p = Pattern.compile(token.replace("*", ".*"));

				PostingsList postings = null;
				for(KGramPostingsEntry el : entries){
					String tmp = kgIndex.id2term.get(el.tokenID);
					tmp = tmp.substring(1, tmp.length() - 1);
					if(p.matcher(tmp).matches()) {
						postings = PostingsList.union(postings, index.getPostings(tmp));
					}
				}
				
				query_list.add(postings);
			}


			PostingsList result = query_list.get(0);
			for(int i = 1; i < query_list.size(); i++){
				result = PostingsList.intersectPhrase(result, query_list.get(i));
			}
			return result;
		}
		if(queryType == QueryType.RANKED_QUERY){
			PostingsList pl = null, pl2;
			HashMap<Integer, Double> lengths = new HashMap<Integer, Double>();

			ArrayList<PostingsList> query_list = new ArrayList<PostingsList>();

			for (Query.QueryTerm qt: query.queryterm) {
				
				if (!qt.term.contains("*")) {
					query_list.add(index.getPostings(qt.term));
					continue;
				}

				List<KGramPostingsEntry> entries = new ArrayList<KGramPostingsEntry>();
				
				String token = "^" + qt.term + "$";

				for(String tmp : token.split("\\*")){

					if(tmp.length() >= kgIndex.getK()){
						for(int i = 0; i < tmp.length() - kgIndex.getK() + 1; i++){
							String kg = tmp.substring(i, i + kgIndex.getK());
							if(entries.isEmpty()){
								entries = kgIndex.getPostings(kg);
							} else {
								entries = kgIndex.intersect(entries, kgIndex.getPostings(kg));
							}
						}
					}
				}
				
				Pattern p = Pattern.compile(token.replace("*", ".*"));

				for(KGramPostingsEntry el : entries){
					String tmp = kgIndex.id2term.get(el.tokenID);
					tmp = tmp.substring(1, tmp.length() - 1);
					if(p.matcher(tmp).matches()) {
						query_list.add(index.getPostings(tmp));
					}
				}
				
			}

			int i = 0;
			
			while(i < query_list.size()){
				pl2 = query_list.get(i);
				if(pl2 == null)
					continue;
				for(int j = 0; j < pl2.size(); j++){

					// TF IDF of document
					double idf_t = (double) Math.log( (double) index.docLengths.size() / (double) pl2.size() );
					double tf_idf_dt = ((double) pl2.get(j).offsetlist.size() * idf_t);
					pl2.get(j).score = tf_idf_dt;

				}

				pl = PostingsList.unionScore(pl, pl2);
				i++;
			}

			
			if(normType == NormalizationType.NUMBER_OF_WORDS) {
				for(int j = 0 ; j < pl.size(); j++){
					pl.get(j).score /= (double) index.docLengths.get(pl.get(j).docID);
				}
			}
			
			if(normType == NormalizationType.EUCLIDEAN) {									
				try {
					File euclideanReader = new File("./euclidean.txt");
					Scanner reader = new Scanner(euclideanReader);
					
					while(reader.hasNextLine()){
						String line = reader.nextLine();
						String[] parsed = line.split(":");
						lengths.put(Integer.parseInt(parsed[0]), Double.parseDouble(parsed[1]));
					}				
					
				} catch (Exception e) {
					e.printStackTrace();
				}

				for(int j = 0 ; j < pl.size(); j++){
					try {
						pl.get(j).score /= (double) lengths.get(pl.get(j).docID);
					} catch (Exception e) {
						pl.get(j).score /= (double) index.docLengths.get(pl.get(j).docID);
					}
				}
			}
			
			HashMap<Integer, Double> ranked = new HashMap<Integer, Double>();

			try {
				File pagerank = new File("./pagerank/pagerank.txt");
				Scanner reader = new Scanner(pagerank);

				while(reader.hasNextLine()){
					String line = reader.nextLine();
					String[] parsed = line.split(":");
					ranked.put(Integer.parseInt(parsed[0]), Double.parseDouble(parsed[1]));
				}				
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(rankingType == RankingType.TF_IDF){
				pl.sort();
				return pl;
			}

			if(rankingType == RankingType.PAGERANK) {
				for(int j = 0; j < pl.size(); j++){
					if(ranked.get(pl.get(j).docID) != null)
						pl.get(j).score = ranked.get(pl.get(j).docID);
					else {
						pl.get(j).score = 0;
					}
				}
				pl.sort();
				return pl;
			}

			if(rankingType == RankingType.COMBINATION) {
				for(int j = 0; j < pl.size(); j++){
					Double pagerank = ranked.get(pl.get(j).docID);
					if ( pagerank == null )
						pagerank = 0.0;
					pl.get(j).score = COMBINATION_RATIO * pl.get(j).score + (1 - COMBINATION_RATIO) * pagerank;
				}
				pl.sort();
				return pl;

			}

			if(rankingType == RankingType.HITS) {
				HITSRanker hits = new HITSRanker("/home/calin/kth/TCSCM1/DD2477_Search_Engines_and_Information_Retrieval_Systems/assignments/assignment2/pagerank/linksDavis.txt",
												 "/home/calin/kth/TCSCM1/DD2477_Search_Engines_and_Information_Retrieval_Systems/assignments/assignment2/pagerank/davisTitles.txt",
												 index);
				pl = hits.rank(pl);
				pl.sort();
				return pl;
			}
			
		}
		return null;
    }
}
