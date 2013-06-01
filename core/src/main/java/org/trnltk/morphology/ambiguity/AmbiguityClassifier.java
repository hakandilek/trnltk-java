package org.trnltk.morphology.ambiguity;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.io.Files;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONException;
import org.trnltk.morphology.ambiguity.model.ParseResult;
import org.trnltk.morphology.ambiguity.model.ParseResultDifference;
import org.trnltk.morphology.ambiguity.model.WordParseResultEntry;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AmbiguityClassifier {

    public static void main(String[] args) throws IOException, JSONException {
        int numberOfWords = 0;
        int numberOfParseResults = 0;
        final Multiset<ParseResultDifference> differenceSet = HashMultiset.create();
        final Multiset<ParseResultDifference> differenceSetWithoutRootDifferences = HashMultiset.create();

        final File folder = new File("D:\\devl\\data\\1MSentences\\split");

        final File[] files = folder.listFiles();

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
            File file = files[fileIndex];
            System.out.println("Processing file " + file);
//            final BufferedReader reader = new BufferedReader(new FileReader(file));
//            while (reader.ready()) {
//                reader.readLine();
//            }
            final ParseResultReader parseResultReader = new ParseResultReader();
            final ParseResultDiffTool parseResultDiffTool = new ParseResultDiffTool();

            final List<WordParseResultEntry> parseResultEntries = parseResultReader.getParseResultEntries(Files.newReader(file, Charsets.UTF_8));
            numberOfWords += parseResultEntries.size();
            for (int parseResultEntryIndex = 0; parseResultEntryIndex < parseResultEntries.size(); parseResultEntryIndex++) {
                WordParseResultEntry parseResultEntry = parseResultEntries.get(parseResultEntryIndex);
                final List<ParseResult> parseResults = parseResultEntry.getParseResults();
                numberOfParseResults += parseResults.size();
                for (int i = 0; i < parseResults.size(); i++) {
                    final ParseResult leftParseResult = parseResults.get(i);
                    for (int j = i + 1; j < parseResults.size(); j++) {
                        final ParseResult rightParseResult = parseResults.get(j);

                        final ParseResultDifference difference = parseResultDiffTool.findDifference(leftParseResult, rightParseResult);
                        final boolean added = differenceSet.add(difference);
                        if (added && difference.hasNoRootDifference() && difference.hasPartDifference())
                            differenceSetWithoutRootDifferences.add(difference);
                    }
                }
            }
            if (fileIndex == 0)
                break;
        }

        stopWatch.stop();
        final long time = stopWatch.getTime();
        System.out.println(stopWatch);
        System.out.println(Long.valueOf(time).doubleValue() / (51));

        System.out.println("Number of words : " + numberOfWords);
        System.out.println("Number of parseResults : " + numberOfParseResults);
        System.out.println("Number of distinct differences : " + differenceSet.elementSet().size());
        System.out.println("numberOfDistinctDifferencesWithoutRootDifference : " + differenceSetWithoutRootDifferences.elementSet().size());

        final ImmutableMultiset<ParseResultDifference> sortedDifferenceSetWithoutRootDifferences = Multisets.copyHighestCountFirst(differenceSetWithoutRootDifferences);
        for (ParseResultDifference parseResultDifference : sortedDifferenceSetWithoutRootDifferences.elementSet()) {
            final int count = sortedDifferenceSetWithoutRootDifferences.count(parseResultDifference);
            if (count > 100) {
                System.out.println(count);
                System.out.println(parseResultDifference);
            }
        }

    }

}