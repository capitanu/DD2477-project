package com.ir22.booksrec;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score = 0;
	public ArrayList<Integer> offsetlist;

    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
       return Double.compare( other.score, score );
    }

	public int compareToID( PostingsEntry other ) {
       return Integer.compare( other.docID, docID );
    }


	public void setScore(double score){
		this.score = score;
	}

	public PostingsEntry(int docID){
		this.offsetlist = new ArrayList<Integer>();
		this.docID = docID;
	}

	public PostingsEntry(int docID, double score){
		this.offsetlist = new ArrayList<Integer>();
		this.docID = docID;
		this.score = score;
	}

}

