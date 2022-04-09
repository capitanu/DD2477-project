package com.ir22.booksrec;

import java.util.*;


public class SpellChecker {
    /** The regular inverted index to be used by the spell checker */
    Index index;

    /** K-gram index to be used by the spell checker */
    KGramIndex kgIndex;

    /** The auxiliary class for containing the value of your ranking function for a token */
    class KGramStat implements Comparable {
        double score;
        String token;

        KGramStat(String token, double score) {
            this.token = token;
            this.score = score;
        }

        public String getToken() {
            return token;
        }

        public int compareTo(Object other) {
            if (this.score == ((KGramStat)other).score) return 0;
            return this.score < ((KGramStat)other).score ? -1 : 1;
        }

        public String toString() {
            return token + ";" + score;
        }
    }

    /**
     * The threshold for Jaccard coefficient; a candidate spelling
     * correction should pass the threshold in order to be accepted
     */
    private static final double JACCARD_THRESHOLD = 0.4;


    /**
	 * The threshold for edit distance for a candidate spelling
	 * correction to be accepted.
	 */
    private static final int MAX_EDIT_DISTANCE = 2;


    public SpellChecker(Index index, KGramIndex kgIndex) {
        this.index = index;
        this.kgIndex = kgIndex;
    }

    /**
     *  Computes the Jaccard coefficient for two sets A and B, where the size of set A is 
     *  <code>szA</code>, the size of set B is <code>szB</code> and the intersection 
     *  of the two sets contains <code>intersection</code> elements.
     */
    private double jaccard(int szA, int szB, int intersection) {
		return (double) intersection / (szA + szB - intersection);
    }

    /**
     * Computing Levenshtein edit distance using dynamic programming.
     * Allowed operations are:
     *      => insert (cost 1)
     *      => delete (cost 1)
     *      => substitute (cost 2)
     */
    private int editDistance(String s1, String s2) {
        int levenshteinMatrix[][] = new int[s1.length()+1][s2.length()+1];

		for(int i = 0; i < levenshteinMatrix.length; i++) {
			levenshteinMatrix[i][0] = i;
		}
		for(int i = 0; i < levenshteinMatrix[0].length; i++) {
			levenshteinMatrix[0][i] = i;
		}

		for(int i = 0; i < levenshteinMatrix.length - 1; i++) {
			for(int j = 0; j < levenshteinMatrix[i].length - 1; j++) {
				int insert = levenshteinMatrix[i][j+1] + 1;
				int delete = levenshteinMatrix[i+1][j] + 1;
				int replace = 0;
				if(s1.charAt(i) == s2.charAt(j))
					replace = levenshteinMatrix[i][j];
				else
					replace = levenshteinMatrix[i][j] + 2;
				levenshteinMatrix[i+1][j+1] = Math.min(Math.min(replace, delete), insert);
			}
		}

		int distance = levenshteinMatrix[s1.length()][s2.length()];
        return distance;
    }

    /**
     *  Checks spelling of all terms in <code>query</code> and returns up to
     *  <code>limit</code> ranked suggestions for spelling correction.
     */
    public String[] check(Query query, int limit) {

        List<List<KGramStat>> corrections = new ArrayList<List<KGramStat>>();

        for (Query.QueryTerm qt: query.queryterm){
			
            ArrayList<KGramStat> terms = new ArrayList<KGramStat>();

			if (index.getPostings(qt.term) != null){
                terms.add(new KGramStat(qt.term, 2.0));
                corrections.add(terms);
                continue;
            }

            HashMap<Integer, Integer> matches = new HashMap<Integer, Integer>();

            String token = '^' + qt.term + '$';

            for(int i = 0; i < token.length() - kgIndex.getK() + 1; i++)
			 	for (KGramPostingsEntry el : kgIndex.getPostings(token.substring(i, i + kgIndex.getK())))
					matches.put(el.tokenID, matches.getOrDefault(el.tokenID, 0) + 1);			
			
            for (Integer tokenID : matches.keySet()) {
				String termGood = kgIndex.getTermByID(tokenID);

                int kgBad = qt.term.length() + 3 - kgIndex.getK();
                int kgGood = termGood.length() + 3 - kgIndex.getK();

                double jac = jaccard(kgBad, kgGood, matches.get(tokenID));
				double dist = editDistance(qt.term, termGood);
                if (jac >= JACCARD_THRESHOLD &&  dist <= MAX_EDIT_DISTANCE){
                    terms.add(new KGramStat(termGood, jac + (MAX_EDIT_DISTANCE - dist)/(double)MAX_EDIT_DISTANCE + index.getPostings(termGood).size() / index.docNames.size()));
                }
            }

            corrections.add(terms);
        }

        if (corrections.size() == 0)
			return null;

        List<KGramStat> multiword = mergeCorrections(corrections, limit);
		
        Collections.sort(multiword, Collections.reverseOrder());

		String[] result = new String[Math.min(multiword.size(),limit)];
        for (int i=0; i < multiword.size() && i < limit; ++i){
            result[i] = multiword.get(i).getToken();
        }

        return result;
    }


	/**
     *  Merging ranked candidate spelling corrections for all query terms available in
     *  <code>qCorrections</code> into one final merging of query phrases. Returns up
     *  to <code>limit</code> corrected phrases.
     */
	@SuppressWarnings("unchecked")
    private List<KGramStat> mergeCorrections(List<List<KGramStat>> qCorrections, int limit) {
		
        HashMap<String, PostingsList> postings = new HashMap<String, PostingsList>();
        Collections.sort(qCorrections.get(0));
		
        List<KGramStat> result = qCorrections.get(0);

        for (KGramStat el : result){
            postings.put(el.token, index.getPostings(el.token));
        }

        for (int t = 1; t < qCorrections.size(); t++){
            ArrayList<KGramStat> queries = new ArrayList<KGramStat>();
            List<KGramStat> queriesMerge = qCorrections.get(t);

            for (KGramStat s1: result){
                PostingsList p1 = postings.get(s1.token);
                for (KGramStat s2: queriesMerge){
					if(queries.size() > limit)
						break;
                    PostingsList p2 = index.getPostings(s2.token);
                    p1 = PostingsList.intersect(p1, p2);
                    if (p1.size() >= 0){
                        double score = s1.score + s2.score + p1.size() / index.docNames.size();
                        postings.put(s1.token + " " + s2.token, p1);
                        queries.add(new KGramStat(s1.token + " " + s2.token, score));
                    }
                }
            }

            Collections.sort(queries);
			
            if (queries.size() <= limit){
                result = queries;
            } else {
                result = queries.subList(0, limit);
            }

        }

        return result;
    }
}
