/**
 *
 */
package edu.cmu.cs.lti.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author zhengzhongliu
 */
public class StringUtils {
    private final static DiffMatchPatch diffMatchPatch = new DiffMatchPatch();

    public static String text2CsvField(String text) {
        return text.replace(",", ".").replace(":", "_").replace("\n", " ");
    }

    public static String removeEnd(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static List<String> characterSkipBigram(String str) {
        List<String> skipBigrams = new ArrayList<String>();

        if (str.length() < 2) {
            return skipBigrams;
        }

        for (int i = 0; i < str.length() - 2; i++) {
            char[] nonskip = new char[2];
            char[] skip = new char[2];

            nonskip[0] = str.charAt(i);
            nonskip[1] = str.charAt(i + 1);

            skip[0] = str.charAt(i);
            skip[1] = str.charAt(i + 2);

            skipBigrams.add(new String(nonskip));
            skipBigrams.add(new String(skip));
        }

        char[] lastone = new char[2];
        lastone[0] = str.charAt(str.length() - 2);
        lastone[1] = str.charAt(str.length() - 1);

        skipBigrams.add(new String(lastone));

        return skipBigrams;
    }

    public static double strDice(Map<String, Double> featureMap1, Map<String, Double> featureMap2) {
        int length1 = 0;
        int length2 = 0;

        double intersect = 0;

        for (Entry<String, Double> feature1 : featureMap1.entrySet()) {
            String featureName = feature1.getKey();
            double val1 = feature1.getValue();
            length1 += val1;
            if (featureMap2.containsKey(featureName)) {
                double val2 = featureMap2.get(featureName);
                intersect += val1 > val2 ? val2 : val1;
            }
        }

        for (Entry<String, Double> feature2 : featureMap2.entrySet()) {
            double val2 = feature2.getValue();
            length2 += val2;
        }

        if (length1 == 0 || length2 == 0) {
            return 0;
        }

        // System.out.println(intersect + " " + length1 + " " + length2);

        return 2 * intersect / (length1 + length2);
    }

    /**
     * Match the altered string to the base one, compute an offset value for each character of the altered text.
     *
     * @param target The source String.
     * @param source The target String.
     * @return An integer array contains the mapping. The map allow you to get the target index from source index.
     */
    public static int[] translateOffset(String target, String source) {
        int[] offsets = new int[source.length()];

        LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diffMain(target, source);

        int targetPointer = 0;
        int sourcePointer = 0;

        int availableSpaces = 0;

        for (DiffMatchPatch.Diff diff : diffs) {
            if (diff.operation.equals(DiffMatchPatch.Operation.EQUAL)) {
                int textLength = diff.text.length();
                // Move two pointers together.
                for (int i = 0; i < textLength; i++) {
                    offsets[sourcePointer + i] = targetPointer + i;
                }
                targetPointer += textLength;
                sourcePointer += textLength;
                availableSpaces = 0;
            } else if (diff.operation.equals(DiffMatchPatch.Operation.DELETE)) {
                int textLength = diff.text.length();
                // Move the target pointer only.
                targetPointer += textLength;
                availableSpaces = textLength;
            } else if (diff.operation.equals(DiffMatchPatch.Operation.INSERT)) {
                int textLength = diff.text.length();
                // Move the source pointer only.
                for (int i = 0; i < textLength; i++) {
                    if (availableSpaces > 0) {
                        offsets[sourcePointer + i] = targetPointer - availableSpaces;
                        offsets[sourcePointer + i] = targetPointer - availableSpaces;
                        availableSpaces--;
                    } else {
                        int adjustedTargetPointer = targetPointer > 0 ? targetPointer - 1 : 0;
                        offsets[sourcePointer + i] = adjustedTargetPointer;
                    }
                }
                sourcePointer += textLength;
            }
        }

//        offsets[alteredPointer] = basePointer;

        return offsets;
    }

    /**
     * Make the altered text to be the same length as the base by padding spaces, match corresponding characters as
     * much as possible. The altered text must be shorter than the base.
     *
     * @param base
     * @param altered
     * @return
     */
    public static String matchText(String base, String altered) {
        int[] offsetMap = translateOffset(base, altered);

        char[] charSeq = new char[base.length()];

        for (int i = 0; i < charSeq.length; i++) {
            charSeq[i] = ' ';
        }
        for (int alterIndex = 0; alterIndex < offsetMap.length; alterIndex++) {
            int baseIndex = offsetMap[alterIndex];
//            System.out.println("matchText:" + alterIndex + " -> " + baseIndex + " " + altered.charAt(alterIndex));
            charSeq[baseIndex] = altered.charAt(alterIndex);
        }

        return new String(charSeq);
    }

    public static void main(String[] args) {

        String xmlEscaped = "&lt;body xmlns=\"http://www.w3.org/1999/xhtml\"&gt;&lt;p class=\"XHFounderPOuter\"&gt;";
        String xmlUnescaped = StringEscapeUtils.unescapeXml(xmlEscaped);
        String missHeader = xmlUnescaped.substring(1);

        System.out.println(String.format("Origin: [%s], length %d.", xmlEscaped, xmlEscaped.length()));
        System.out.println(String.format("Unescaped: [%s], length %d.", xmlUnescaped, xmlUnescaped.length()));
        System.out.println(String.format("No head: [%s], length %d.", missHeader, missHeader.length()));

//        String matchedText = matchText(xmlEscaped, xmlUnescaped);
//        String reverseMatch = matchText(xmlUnescaped, xmlEscaped);
//        System.out.println(String.format("Matched: [%s], length %d.", matchedText, matchedText.length()));
//        System.out.println(String.format("Reverse Matched: [%s], length %d.", reverseMatch, reverseMatch.length()));

        System.out.println("Matching no header.");
        String matchMisshead = matchText(missHeader, xmlEscaped);

        System.out.println(String.format("Match No Head [%s], length %d.", matchMisshead, matchMisshead.length()));
    }
}
