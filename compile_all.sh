#!/bin/sh
if ! [ -d build ];
then
   mkdir build
fi
javac -cp . -d build src/Engine.java src/HashedIndex.java src/HITSRanker.java src/Index.java src/Indexer.java src/KGramIndex.java src/KGramPostingsEntry.java src/NormalizationType.java src/PostingsEntry.java src/PostingsList.java src/Query.java src/QueryType.java src/RankingType.java src/Searcher.java src/SearchGUI.java src/SpellChecker.java src/SpellingOptionsDialog.java src/Tokenizer.java src/TokenTest.java  -Xlint:unchecked
