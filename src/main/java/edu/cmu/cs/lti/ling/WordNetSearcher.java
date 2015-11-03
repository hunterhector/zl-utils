package edu.cmu.cs.lti.ling;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;
import java8.util.function.Function;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import org.javatuples.Pair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 1/29/15
 * Time: 11:42 PM
 */
public class WordNetSearcher {

    IDictionary dict;
    WordnetStemmer stemmer;


    public WordNetSearcher(String wnDictPath) throws IOException {
        URL url = null;
        try {
            url = new URL("file", null, wnDictPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) return;

        dict = new Dictionary(url);
        dict.open();

        stemmer = new WordnetStemmer(dict);
    }

    public List<String> stem(String word, POS pos) {
        return stemmer.findStems(word, pos);
    }


    public Set<Pair<String, String>> getDerivations(String word, String pos) {
        return getDerivations(word, pennTreeTag2POS(pos));
    }


    public Set<Pair<String, String>> getDerivations(String lemma, POS pos) {
        Set<Pair<String, String>> derivationWords = new HashSet<Pair<String, String>>();

        for (ISynset synset : getAllSynsets(lemma, pos)) {
            for (IWord iWord : synset.getWords()) {
                // Restrict the synset expansion here because we don't won't derivation of other words that have similar
                // meaning.
                if (iWord.getLemma().equals(lemma)) {
                    List<IWordID> derivationRelated = iWord.getRelatedWords(Pointer.DERIVATIONALLY_RELATED);
                    for (IWordID iWordID : derivationRelated) {
                        IWord derativeWord = dict.getWord(iWordID);
                        derivationWords.add(Pair.with(derativeWord.getLemma(), derativeWord.getPOS().toString()));
                    }
                }
            }
        }
        return derivationWords;
    }

    public List<String> getAllSynonyms(String wordType, String posTag) {
        return getAllSynonyms(wordType, pennTreeTag2POS(posTag));
    }

    public List<String> getAllSynonyms(String wordType, POS pos) {
        IIndexWord idxWord = dict.getIndexWord(wordType, pos);

        Set<String> synonyms = new HashSet<String>();

        if (idxWord != null) {
            for (IWordID wordId : idxWord.getWordIDs()) {
                IWord word = dict.getWord(wordId);
//                synonyms.addAll(word.getSynset().getWords().stream().map(IWord::getLemma).collect(Collectors.toList
// ()));
                synonyms.addAll(
                        StreamSupport.stream(word.getSynset().getWords()).map(new Function<IWord,
                                String>() {
                            @Override
                            public String apply(IWord iWord) {
                                return iWord.getLemma();
                            }
                        }).collect(Collectors.<String>toList())
                );
            }
        }
        return new ArrayList<String>(synonyms);
    }

    public List<ISynsetID> getAllHypernyms(ISynset synset) {
        return getAllHypernyms(synset, new HashSet<ISynset>());
    }

    public List<ISynsetID> getAllHypernyms(ISynset synset, Set<ISynset> alreadyVisited) {
        List<ISynsetID> allHyperNyms = new ArrayList<ISynsetID>();

        List<ISynsetID> thisHypers = synset.getRelatedSynsets(Pointer.HYPERNYM);
        alreadyVisited.add(synset);

        if (!thisHypers.isEmpty()) {
            for (ISynsetID thisHyper : thisHypers) {
                ISynset thisHyperSynset = dict.getSynset(thisHyper);
                if (!alreadyVisited.contains(thisHyperSynset)) {
                    allHyperNyms.add(thisHyper);
                    allHyperNyms.addAll(getAllHypernyms(thisHyperSynset, alreadyVisited));
                }
            }
        }

        return allHyperNyms;
    }

    public Set<String> getAllNounHypernymsForAllSense(String wordType) {
        return getAllHypernymsForAllSense(wordType, POS.NOUN);
    }

    public Set<String> getAllHypernymsForAllSense(String wordType, String posTag) {
        return getAllHypernymsForAllSense(wordType, pennTreeTag2POS(posTag));
    }

    public Set<String> getAllHypernymsForAllSense(String wordType, POS pos) {
        Set<String> allHyperNyms = new HashSet<String>();

        for (ISynset synset : getAllSynsets(wordType, pos)) {
            for (ISynsetID hyperSynsetId : getAllHypernyms(synset)) {
                List<IWord> words = dict.getSynset(hyperSynsetId).getWords();
                for (IWord hyperWord : words) {
                    allHyperNyms.add(hyperWord.getLemma());
                }
            }
            for (IWord iWord : synset.getWords()) {
                allHyperNyms.add(iWord.getLemma());
            }
        }
        return allHyperNyms;
    }

    public List<ISynset> getAllSynsets(String wordType, POS pos) {
        List<ISynset> synsets = new ArrayList<ISynset>();
        IIndexWord idxWord = dict.getIndexWord(wordType, pos);
        if (idxWord != null) {
            for (IWordID wordId : idxWord.getWordIDs()) {
                IWord word = dict.getWord(wordId);
                ISynset synset = word.getSynset();
                synsets.add(synset);
            }
        }
        return synsets;
    }

    private POS pennTreeTag2POS(String posTag) {
        if (posTag.startsWith("N")) {
            return POS.NOUN;
        } else if (posTag.startsWith("J")) {
            return POS.ADJECTIVE;
        } else if (posTag.startsWith("V")) {
            return POS.VERB;
        } else if (posTag.startsWith("R")) {
            return POS.ADVERB;
        } else {
            return POS.NOUN;
        }
    }


    public static void main(String[] argv) throws IOException {
        WordNetSearcher wns = new WordNetSearcher("/Users/zhengzhongliu/Documents/projects/data/wnDict");

//        System.out.println(wns.stem("advice", POS.VERB));
//        System.out.println(wns.stem("debater", POS.VERB));
//        System.out.println(wns.stem("injuries", POS.NOUN));
//        System.out.println(wns.stem("taxes", POS.NOUN));
//        System.out.println(wns.stem("sentencing", POS.NOUN));
//
//        System.out.println("Body part?");
//
//        System.out.println(wns.getAllNounHypernymsForAllSense("jaw"));
//
//        System.out.println("Money?");
//
//        System.out.println(wns.getAllNounHypernymsForAllSense("insurance"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("pension"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("revenue"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("bribe"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("bill"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("tax"));
//
//        System.out.println("Ownerships");
//
//        System.out.println(wns.getAllNounHypernymsForAllSense("name"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("jewelry"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("item"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("bracelet"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("gun"));
//
//        System.out.println("Profession");
//
//        System.out.println(wns.getAllNounHypernymsForAllSense("driver"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("senator"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("judge"));
//
//
//        System.out.println("Disease");
//
//        System.out.println(wns.getAllNounHypernymsForAllSense("injury"));
//        System.out.println(wns.getAllNounHypernymsForAllSense("have"));
//
//        System.out.println("Derivative words");
//
//        System.out.println(wns.getDerivations("injure", "V"));
//        System.out.println(wns.getDerivations("sentence", "V"));
        System.out.println(wns.getDerivations("have", "V"));
        System.out.println(wns.getDerivations("bruise", "N"));
        System.out.println(wns.getDerivations("growth", "N"));
    }
}