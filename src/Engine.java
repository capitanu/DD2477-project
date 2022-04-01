/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */

package ir;

import java.util.*;
import java.io.*;

/**
 *  This is the main class for the search engine.
 */
public class Engine {

    /** The inverted index. */
    //Index index = new PersistentScalableHashedIndex();
    //Index index = new PersistentHashedIndex();
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

    /** Directories that should be indexed. */
    ArrayList<String> dirNames = new ArrayList<String>();

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

    /* ----------------------------------------------- */


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
		File euclidean = new File("euclidean.txt");
        if (is_indexing) {
            synchronized ( indexLock ) {
                gui.displayInfoText( "Indexing, please wait..." );
                long startTime = System.currentTimeMillis();
                for ( int i=0; i<dirNames.size(); i++ ) {
                    File dokDir = new File( dirNames.get( i ));
                    indexer.processFiles( dokDir, is_indexing );
                }
                long elapsedTime = System.currentTimeMillis() - startTime;
				if(!euclidean.exists()){
					try {
						FileWriter euclideanWriter  = new FileWriter("euclidean.txt");
						for(Map.Entry<Integer, HashMap<String,Integer>> entry : indexer.docIndexEuclidean.entrySet()) {
							Integer docID = entry.getKey();
							HashMap<String, Integer> docWords = entry.getValue();
							double length = 0.0;
							for(Map.Entry<String, Integer> entry2 : docWords.entrySet()) {
								PostingsList pl2 = index.getPostings(entry2.getKey());
								Integer count = entry2.getValue();
								length += count*count* (double) Math.log( (double) index.docLengths.size() / (double) pl2.size())*(double) Math.log( (double) index.docLengths.size() / (double) pl2.size());
							}
							length = Math.sqrt(length);
							euclideanWriter.write(docID + ":" + length + "\n");
						}
						euclideanWriter.close();
					} catch (IOException io) {
						io.printStackTrace();
					}
					System.out.println("Computed euclidean distance.");
				}
                gui.displayInfoText( String.format( "Indexing done in %.1f seconds.", elapsedTime/1000.0 ));
                index.cleanup();
            }
        } else {
            gui.displayInfoText( "Index is loaded from disk" );
        }
    }


    /* ----------------------------------------------- */

    /**
     *   Decodes the command line arguments.
     */
    private void decodeArgs( String[] args ) {
        int i=0, j=0;
        while ( i < args.length ) {
            if ( "-d".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    dirNames.add( args[i++] );
                }
            } else if ( "-p".equals( args[i] )) {
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

