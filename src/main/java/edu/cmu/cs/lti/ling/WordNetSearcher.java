package edu.cmu.cs.lti.ling;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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


    public List<String> getAllSynonyms(String wordType, String posTag) {
        return getAllSynonyms(wordType, pennTreeTag2POS(posTag));
    }

    public List<String> getAllSynonyms(String wordType, POS pos) {
        IIndexWord idxWord = dict.getIndexWord(wordType, pos);

        Set<String> synonyms = new HashSet<>();

        if (idxWord != null) {
            for (IWordID wordId : idxWord.getWordIDs()) {
                IWord word = dict.getWord(wordId);
                synonyms.addAll(word.getSynset().getWords().stream().map(IWord::getLemma).collect(Collectors.toList()));
            }
        }
        return new ArrayList<>(synonyms);
    }

    public List<ISynsetID> getAllHypernyms(ISynset synset) {
        List<ISynsetID> allHyperNyms = new ArrayList<>();

        List<ISynsetID> thisHypers = synset.getRelatedSynsets(Pointer.HYPERNYM);

        if (!thisHypers.isEmpty()) {
            for (ISynsetID thisHyper : thisHypers) {
                allHyperNyms.add(thisHyper);
                allHyperNyms.addAll(getAllHypernyms(dict.getSynset(thisHyper)));
            }
        }

        return allHyperNyms;
    }

    public List<Set<String>> getAllNounHypernymsForAllSense(String wordType) {
        return getAllHypernymsForAllSense(wordType, POS.NOUN);
    }

    public List<Set<String>> getAllHypernymsForAllSense(String wordType, String posTag) {
        return getAllHypernymsForAllSense(wordType, pennTreeTag2POS(posTag));
    }

    public List<Set<String>> getAllHypernymsForAllSense(String wordType, POS pos) {
        List<Set<String>> allHyperNyms = new ArrayList<>();

        IIndexWord idxWord = dict.getIndexWord(wordType, pos);

        if (idxWord == null) {
            return allHyperNyms;
        }

        for (IWordID wordId : idxWord.getWordIDs()) {
            IWord word = dict.getWord(wordId);
            ISynset synset = word.getSynset();

            Set<String> thisHyperNyms = new HashSet<>();
            List<ISynsetID> hyperNyms = getAllHypernyms(synset);

            for (ISynsetID sid : hyperNyms) {
                List<IWord> words = dict.getSynset(sid).getWords();
                for (IWord hyperWord : words) {
                    thisHyperNyms.add(hyperWord.getLemma());
                }
            }

            allHyperNyms.add(thisHyperNyms);
        }

        return allHyperNyms;
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

        System.out.println(wns.stem("advice", POS.VERB));
        System.out.println(wns.stem("debater", POS.VERB));
        System.out.println(wns.stem("injuries", POS.NOUN));
        System.out.println(wns.stem("taxes", POS.NOUN));


        System.out.println(wns.getAllNounHypernymsForAllSense("jaw"));

        System.out.println("Money?");

        System.out.println(wns.getAllNounHypernymsForAllSense("insurance"));
        System.out.println(wns.getAllNounHypernymsForAllSense("pension"));
        System.out.println(wns.getAllNounHypernymsForAllSense("revenue"));
        System.out.println(wns.getAllNounHypernymsForAllSense("bribe"));
        System.out.println(wns.getAllNounHypernymsForAllSense("bill"));
        System.out.println(wns.getAllNounHypernymsForAllSense("tax"));

        System.out.println("Ownerships");

        System.out.println(wns.getAllNounHypernymsForAllSense("name"));
        System.out.println(wns.getAllNounHypernymsForAllSense("jewelry"));
        System.out.println(wns.getAllNounHypernymsForAllSense("item"));
        System.out.println(wns.getAllNounHypernymsForAllSense("bracelet"));
        System.out.println(wns.getAllNounHypernymsForAllSense("gun"));

        System.out.println("Profession");

        System.out.println(wns.getAllNounHypernymsForAllSense("driver"));
        System.out.println(wns.getAllNounHypernymsForAllSense("senator"));
        System.out.println(wns.getAllNounHypernymsForAllSense("judge"));
    }
}