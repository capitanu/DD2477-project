/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PostingsList {
    
    /** The postings list */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();


    /** Number of postings in this list. */
    public int size() {
		return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
		return list.get( i );
    }

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(PostingsEntry pe : list){
			sb.append("{");
			sb.append("di : ");
			sb.append(pe.docID);
			sb.append(", ");
			sb.append("of : ");
			sb.append("");
			sb.append(pe.offsetlist.toString());
			sb.append("}, ");
		}

		sb.append("\n");
		return sb.toString();
	}

	/** Places a PostingsEntry in the list */
	public void put( PostingsEntry pe) {
		list.add(pe);
	}

	public void sort() {
		Collections.sort(this.list);
	}

	public void sortID() {
		Collections.sort(this.list, new Comparator<PostingsEntry>() {  
				@Override  
				public int compare(PostingsEntry pe1, PostingsEntry pe2) {
					return Integer.compare( pe1.docID, pe2.docID );
				}
			});
	}



	/** Places a PostingsEntry in the list */
	public void put( PostingsEntry pe, int offset ) {
		for(PostingsEntry el : list){
			if(el.docID == pe.docID){
				el.offsetlist.add(offset);
				return;
			}
		}
		pe.offsetlist.add(offset);
		list.add(pe);
		return;
	}

	/** Intersects two PostingsLists and returns only one */
	public static PostingsList intersect(PostingsList p1, PostingsList p2){
		if(p1 == null || p2 == null){
			return null;
		}

		p1.sortID();
		p2.sortID();
		
		PostingsList pl = new PostingsList();
		int p1_index = 0, p2_index = 0;
		while(p1_index < p1.size() && p2_index < p2.size()){
			if(p1.get(p1_index).docID == p2.get(p2_index).docID){
				pl.put(p1.get(p1_index));
				p1_index++;
				p2_index++;
			}
			else if (p1.get(p1_index).docID < p2.get(p2_index).docID)
				p1_index++;
			else
				p2_index++;
		}

		return pl;
	}

	/** Intersects two PostingsLists as a phrase and returns a merged list */
	public static PostingsList intersectPhrase(PostingsList p1, PostingsList p2) {
		if(p1 == null || p2 == null)
			return null;
		PostingsList pl = new PostingsList();
		p1.sortID();
		p2.sortID();
		int p1_index = 0, p2_index = 0;
		while(p1_index < p1.size() && p2_index < p2.size()){
			if(p1.get(p1_index).docID == p2.get(p2_index).docID){
				ArrayList<Integer> offsetlist1 = p1.get(p1_index).offsetlist;
				ArrayList<Integer> offsetlist2 = p2.get(p2_index).offsetlist;
				for(int offset : offsetlist1){
					if(offsetlist2.contains(offset + 1)){
						pl.put(new PostingsEntry(p1.get(p1_index).docID), offset + 1);
					}
				}
				p1_index++;
				p2_index++;
			}
			else if (p1.get(p1_index).docID < p2.get(p2_index).docID)
				p1_index++;
			else
				p2_index++;
		}

		return pl;
	}

	public static PostingsList union(PostingsList p1, PostingsList p2) {
		PostingsList pl = new PostingsList();
		if(p2 == null){
			return p1;
		}
		if(p1 == null){
			return p2;
		}

		for(int i = 0; i < p1.size(); i++){
			pl.put(p1.get(i));
		}
		
		for(int i = 0; i < p2.size(); i++){
			boolean contains = false;
			for(int j = 0; j < pl.size(); j++){
				if(p2.get(i).docID == pl.get(j).docID){
					pl.get(j).offsetlist.addAll(p2.get(i).offsetlist);
					Collections.sort(pl.get(j).offsetlist);
					contains = true;
					break;
				}
			}
			if(contains == false)
				pl.put(p2.get(i));
		}
		
		return pl;
	}

	/* Returns the union */
	public static PostingsList unionScore(PostingsList p1, PostingsList p2){
		PostingsList pl = new PostingsList();
		int p1_index = 0, p2_index = 0;

		if(p1 == null && p2 == null){
			return null;
		}
		if(p1 == null)
			return p2;

		if(p2 == null)
			return p1;

		p1.sortID();
		p2.sortID();

		while(p1_index < p1.size() && p2_index < p2.size()){
			if(p1.get(p1_index).docID == p2.get(p2_index).docID){
				double score = p1.get(p1_index).score + p2.get(p2_index).score;
				pl.put(new PostingsEntry(p1.get(p1_index).docID,  score));
				p1_index++;
				p2_index++;
			}
			else if (p1.get(p1_index).docID < p2.get(p2_index).docID) {
				pl.put(new PostingsEntry(p1.get(p1_index).docID, p1.get(p1_index).score));
				p1_index++;
			}
			else {
				pl.put(new PostingsEntry(p2.get(p2_index).docID, p2.get(p2_index).score));
				p2_index++;
			}
		}

		while(p1_index < p1.size()){
			pl.put(new PostingsEntry(p1.get(p1_index).docID, p1.get(p1_index).score));
			p1_index++;
		}

		while(p2_index < p2.size()){
			pl.put(new PostingsEntry(p2.get(p2_index).docID, p2.get(p2_index).score));
			p2_index++;
		}

		return pl;
		
	}

}

