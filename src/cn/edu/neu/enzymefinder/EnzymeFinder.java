/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import org.uncommons.maths.combinatorics.CombinationGenerator;

/**
 *
 * @author gibeon
 */
public class EnzymeFinder {

    public static final String FIND_OUTPUT_NAME = "EnzymeFinderFind.txt";
    public static final String LOCATE_OUTPUT_NAME = "EnzymeFinderLocate.txt";
    private Patterns patterns = new Patterns();
    private Sequences positives = new Sequences();
    private Sequences negatives = new Sequences();
    private int positiveNoiseThresold = 0;  // remove positives which cannot ensure smaller (and equal to) the positives noise thresold
    private int negativeNoiseThresold = 0;  // remove negatives from positives which ensure larger (not equal to) the negative noise thresold
    private int combinationLength = 1;    // Added by Wugang 2013-07-01, indicate how many combinations are supported
    private boolean bStop = false;  // this is used to stop the finding progress
    private List<Sequence> allSequencesList = new ArrayList<Sequence>();
    private Map<SubSequenceKey, Set<String>> uniqueCandidates = new HashMap<SubSequenceKey, Set<String>>();
    private List<List<Entry<SubSequenceKey, Set<String>>>> finalResults = new ArrayList<List<Entry<SubSequenceKey, Set<String>>>>();
//    private float totalSparseRate = 0;
    
    public boolean isbStop() {
        return bStop;
    }

    public void setbStop(boolean bStop) {
        this.bStop = bStop;
    }

    public void setNegativeNoise(int negativeNoise) {
        this.negativeNoiseThresold = negativeNoise;
    }

    public void setPositiveNoise(int positiveNoise) {
        this.positiveNoiseThresold = positiveNoise;
    }

    public int getNegativeNoise() {
        return negativeNoiseThresold;
    }

    public int getPositiveNoise() {
        return positiveNoiseThresold;
    }

    public int getCombinationLength() {
        return combinationLength;
    }

    public void setCombination(int combinationLength) {
        this.combinationLength = combinationLength;
    }

    public List<Sequence> getAllSequencesList() {
        return allSequencesList;
    }

    public void loadPatterns(File file) throws IOException, EnzymeFinderException {
        patterns.loadFromFile(file);
    }

    public int loadPositiveSequences(File file) throws IOException {
        int size = positives.loadFromDir(file);
//        setPositiveNoise(size); // usually we should set positive noise to be equal to the size of samples in the positives.
        return size;
    }

    public int loadNegativeSequences(File file) throws IOException {
        int size = negatives.loadFromDir(file);
        return size;
    }

    public Map<SubSequenceKey, Set<String>> find(Pattern pattern) throws EnzymeFinderException {
        if ((patterns == null) || (positives == null) || (negatives == null)) {
            return null;
        }

        // Modified by WuGang 2013-2-27, Use new patternize API with PatternFilter
        positives.patternize(pattern, patterns.getPatternFilter());
        negatives.patternize(pattern, patterns.getPatternFilter());
//        totalSparseRate += positives.printStatistics();

        // remove negatives from positives which ensure larger (not equal to) than the negative noise thresold
        for (Entry<PatternizedSubSequenceKey, PatternizedSubSequenceValue> entryNegative : negatives.getSubSequences().entrySet()) {
            if (positives.getSubSequences().containsKey(entryNegative.getKey())) {  // a negative entry key exists in the positive subsequences
                PatternizedSubSequenceValue negativeValue = entryNegative.getValue();
                PatternizedSubSequenceValue positiveValue = positives.getSubSequences().get(entryNegative.getKey());

                // Remove keys that exactly exist in the negatives. After this, the size of positiveValue.getSubSequences() may be zero!!!
                if (entryNegative.getValue().getFrequency() > negativeNoiseThresold) {  // If the frequecey of the negative value corresponding to the key exceed the negativeNoise
//                    System.out.println("Before: " + positiveValue.getSubSequences().keySet());
                    positiveValue.getSubSequences().keySet().removeAll(negativeValue.getSubSequences().keySet());   // Remove the key-value from the positiveValue.
//                    System.out.println("After: " + positiveValue.getSubSequences().keySet());
                }
            }   // else just skip those negative keys that do not appear in the positives
        }


        Map<SubSequenceKey, Set<String>> currentCandidates = new HashMap<SubSequenceKey, Set<String>>();
//        Set<SubSequenceKey> result = new HashSet<SubSequenceKey>(); // Use the set to remove pair result
        // remove positives which cannot ensure smaller (not equal to) than the positives noise thresold
        for (Iterator<Entry<PatternizedSubSequenceKey, PatternizedSubSequenceValue>> itPositive = positives.getSubSequences().entrySet().iterator(); itPositive.hasNext();) {
            Entry<PatternizedSubSequenceKey, PatternizedSubSequenceValue> entryPositive = itPositive.next();
            PatternizedSubSequenceValue negativeValue = negatives.getSubSequences().get(entryPositive.getKey());
            PatternizedSubSequenceValue positiveValue = entryPositive.getValue();

//            if (entryPositive.getValue().getFrequency() < positiveNoiseThresold) {
//                // cannot use the following code which may result in "java.util.ConcurrentModificationException"
//                //positives.getSubSequences().remove(entry.getKey());
//                // so we use iterator's remove
//                itPositive.remove();
//            } else {
                if (pattern.hasX()) {
                    positiveValue.getPossibleSubSequences().setbUseAvailableSet(false);
                    positiveValue.getPossibleSubSequences().setbSkipAvailableSet(false);
                    PowerSet.PowerSetIterator psPositiveIt = positiveValue.getPossibleSubSequences().iterator();
//                    System.out.println(positiveValue.getPossibleSubSequences().getAvailables());
                    boolean bEmpty = true;
                    while (psPositiveIt.hasNext()) {
                        Set<Entry<SubSequenceKey, HashSet<String>>> psPositiveElement = psPositiveIt.next();
                        if ((psPositiveElement == null)|| 
//                                (PatternizedSubSequenceValue.getFrequency(psPositiveElement) < positiveNoiseThresold)||
                                ((PatternizedSubSequenceValue.getFilterBases() != null) && (!PatternizedSubSequenceValue.isMergedValueFilterable(psPositiveElement)))) // Indicate that we should skip this power set element
                            continue;
                        byte[] mergedBytes = PatternizedSubSequenceValue.getMergedValue(psPositiveElement);
                        SubSequenceKey mergedValue = new SubSequenceKey(mergedBytes);
                        SubSequenceKey pairMergedValue = new SubSequenceKey(Base.toPairCodes(mergedBytes));
                        
                        boolean bAvailable = true;
                        if (negativeValue != null){
                            int negativeNoise = 0;
                            for (SubSequenceKey negativeValueKey : negativeValue.getSubSequences().keySet()) {
                                    if (PatternizedSubSequenceValue.mergedValueContains(psPositiveElement, negativeValueKey.getSubSequence())){
                                        negativeNoise += negativeValue.getSubSequences().get(negativeValueKey).size();
                                    }
                            }
                            if (negativeNoise > negativeNoiseThresold)
                                bAvailable = false;
                        }
                        if (!bAvailable)
                            continue;
                        else
                            bEmpty = false;
                        
                        
                        if (currentCandidates.keySet().contains(mergedValue) || currentCandidates.keySet().contains(pairMergedValue)) {
//                            psIt.setToBeAvailable(false);
//                      System.out.println("remove a key from positives " + entry.getKey());
                        } else 
                            currentCandidates.put(mergedValue, PatternizedSubSequenceValue.getContainingSequences(psPositiveElement));
                        
                    }
                    if (bEmpty)
                        itPositive.remove();
                } else {
                    SubSequenceKey mergedValue = new SubSequenceKey(entryPositive.getValue().getMergedValue());
                    SubSequenceKey pairMergedValue = new SubSequenceKey(Base.toPairCodes(entryPositive.getValue().getMergedValue()));
                    if (currentCandidates.keySet().contains(mergedValue) || currentCandidates.keySet().contains(pairMergedValue)) {
//                        itPositive.remove();
//                  System.out.println("remove a key from positives " + entry.getKey());
                    } else 
                        currentCandidates.put(mergedValue, entryPositive.getValue().getContainingSequences());
//                  System.out.println("MergedValue = " + mergedValue);
//                  System.out.println("PairMergedValue = " + pairMergedValue);
                }
//            }
        }
        
//        System.out.println("3: " + positives.getSubSequenceValues());

        uniqueCandidates.putAll(currentCandidates);
        
//        System.out.println(currentCandidates.size() + ": ");


//        return positives.getSubSequenceValues();
        return currentCandidates;
    }

    public void printFind() throws EnzymeFinderException {
        for (int i = 0; i < patterns.size(); i++) {
            System.out.println(find(patterns.get(i)));
        }

        System.out.println("Unique finding result are as follows:");
        for (SubSequenceKey key : uniqueCandidates.keySet()) {
            System.out.println(key);
        }
    }

    public File visualFind(JProgressBar progress, JLabel progressLabel, JTextArea log, JTextArea summary) {
        // Start compute time
        Long startTime = System.currentTimeMillis();
        
        long finalResultNumber = 0;
        File f = null;
        FileOutputStream fos = null;
        try {
            f = new File(FIND_OUTPUT_NAME);
            fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos, true);

            int total = patterns.size();
            progress.setMaximum(total);
            uniqueCandidates.clear();
            for (int i = 0; i < total; i++) {
                if (bStop) {
                    break;
                }
                try {
                    // porgress bar & textarea show
                    progress.setValue(i + 1);
                    progressLabel.setText("Finding Progress: - candidates " + Math.round(progress.getPercentComplete()*100) + "%");
                    String line = "\"" + patterns.get(i).toString() + "\" has candidates: " +find(patterns.get(i)).size();
//                    find(patterns.get(i));
//                    String line = patterns.get(i).toString();
                    log.append(line + "\n");

                    // file output
                    ps.println(line);
                } catch (EnzymeFinderException ex) {
                    Logger.getLogger(EnzymeFinder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            ps.println("Unique finding result are as follows:");
//            ps.println("Average sparse rate = " + totalSparseRate + "/" + patterns.size() + "=" + totalSparseRate/patterns.size());
//            for (SubSequenceKey key : uniqueCandidates.keySet()) {
//                ps.println(key);
//            }
            
            progressLabel.setText("Finding Progress: - waiting for CombinationGenerator");
            ps.println("Finding Progress: - waiting for CombinationGenerator");
            progress.setValue(0);
            log.append("Combination length: " + getCombinationLength() + "-------------------\n");
            ps.println("-------------------");
            int combLength = getCombinationLength();
            CombinationGenerator<Entry<SubSequenceKey, Set<String>>> combs = new CombinationGenerator<Entry<SubSequenceKey, Set<String>>>(uniqueCandidates.entrySet(), combLength);
            long totalCombinations = combs.getTotalCombinations();
            log.append("Total combinations: " + totalCombinations + "-------------------\n");
            ps.println("-------------------");
            progress.setMaximum((int)totalCombinations);
            Iterator<List<Entry<SubSequenceKey, Set<String>>>> combIt = combs.iterator();
            int i = 0;
            while (combIt.hasNext()) {
                progress.setValue((int)(++i));
                progressLabel.setText("Finding Progress: - results " + Math.round(progress.getPercentComplete()*100) + "%");
                
                List<Entry<SubSequenceKey, Set<String>>> comb = combIt.next();

                Set<String> candidateSet = new HashSet<String>();
                for (Entry<SubSequenceKey, Set<String>> candidate : comb) {
                    candidateSet.addAll(candidate.getValue());
                }
                if (candidateSet.size() >= positiveNoiseThresold) {
//                    finalResults.add(comb);
                    finalResultNumber++;
                    log.append(Integer.toString(i) + ": ");
                    ps.print(i + ": ");
                    StringBuffer strOneFinalResult = new StringBuffer("");
                    for (Entry<SubSequenceKey, Set<String>> entry : comb){
                        strOneFinalResult.append(entry.getKey().toString());
                        strOneFinalResult.append(": ");
                    }
                    log.append(strOneFinalResult.toString());
                    ps.print(strOneFinalResult);
                    ps.append("\n");
                    log.append("\n");
//                    System.out.println("A possible combination: " + comb);
                }
            }
        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EnzymeFinder.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(EnzymeFinder.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                // End compute time
                Long endTime = System.currentTimeMillis(); 
//                String strSummary = "Find " + uniqueCandidates.size() + " unique results in " + (endTime - startTime)/1000.0 + " seconds " + "as follows:\n";
                String strSummary = "Find " + finalResultNumber + " results in " + (endTime - startTime)/1000.0 + " seconds " + "as follows:\n";
                StringBuffer sb = new StringBuffer("");
//                for (SubSequenceKey key : uniqueCandidates.keySet()) {
//                    sb.append(key.toString());
//                    sb.append("\n");
//                }
//                for (List<Entry<SubSequenceKey, Set<String>>> oneResult: finalResults){
//                    for (Entry<SubSequenceKey, Set<String>> entry: oneResult){
//                        sb.append(entry.getKey().toString());
//                        sb.append("; ");
//                    }
//                    sb.append("\n");
//                }
                strSummary += sb.toString();
                summary.setText(strSummary);
            }
        }
        return f;
    }

    public File outputFind() throws EnzymeFinderException {
        File f = null;
        FileOutputStream fos = null;
        try {
            f = new File(FIND_OUTPUT_NAME);
            fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos, true);
            for (int i = 0; i < patterns.size(); i++) {
                ps.println(patterns.get(i).toString() + find(patterns.get(i)));
            }

            ps.println("Unique finding result are as follows:");
            for (SubSequenceKey key : uniqueCandidates.keySet()) {
                ps.println(key);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EnzymeFinder.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(EnzymeFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return f;
    }

    public void visualLocate(byte[] subSequence, JProgressBar progress, JLabel progressLabel, JTextArea log, JTextArea summary) {
//        Map<Sequence,List<Integer>> result = new HashMap<Sequence,List<Integer>>(); 
        if ((subSequence == null) || (subSequence.length == 0)) {
            return;
        }

        // Start compute time
        Long startTime = System.currentTimeMillis();
        
        allSequencesList.clear();

        int total = positives.getSequences().size() + negatives.getSequences().size();
        progress.setMaximum(total);
        int progressPos = 0;

        int positiveCount = 0;
        int negativeCount = 0;
        
        // Locate in positive files
        if (positives != null) {
            for (Sequence seq : positives.getSequences()) {
                progress.setValue(++progressPos);
                progressLabel.setText("Finding Progress: " +  Math.round(progress.getPercentComplete()*100) + "%");
                List<Integer> locations = seq.locatePos(subSequence, false);    // Locating in original sequence
                List<Integer> pairLocations = seq.locatePos(subSequence, true); // locating in pair sequence
                log.append(seq.getId() + " : " + locations.toString() + " Pair: " + pairLocations.toString() + "\n");
                allSequencesList.add(seq);
                if ((locations.size() + pairLocations.size()) > 0)
                    positiveCount++;
                if (bStop) {
                    return;
                }
            }
        }

        // Locate in negative files
        if (negatives != null) {
            for (Sequence seq : negatives.getSequences()) {
                progress.setValue(++progressPos);
                progressLabel.setText("Finding Progress: " +  Math.round(progress.getPercentComplete()*100) + "%");
                List<Integer> locations = seq.locatePos(subSequence, false);    // Locating in original sequence
                List<Integer> pairLocations = seq.locatePos(subSequence, true); // locating in pair sequence
                log.append(seq.getId() + " : " + locations.toString() + " Pair: " + pairLocations.toString() + "\n");
                allSequencesList.add(seq);
                if ((locations.size() + pairLocations.size()) > 0) 
                    negativeCount++;
                if (bStop) {
                    return;
                }
            }
        }
        
        // End compute time
        Long endTime = System.currentTimeMillis();
        String strSummary = 
                "Found " + positiveCount + " in " + positives.getSequences().size() + " positives.\n"
                + "Found " + negativeCount + " in " + negatives.getSequences().size() + " negatives.\n"
                + "Totle time is " + (endTime - startTime)/1000.0 + " seconds";
        summary.setText(strSummary);
    }

    public void locate(byte[] subSequence) {
        if ((subSequence == null) || (subSequence.length == 0)) {
            return;
        }

        allSequencesList.clear();

        // Locate in positive files
        if (positives != null) {
            for (Sequence seq : positives.getSequences()) {
                allSequencesList.add(seq);
            }
        }

        // Locate in negative 
        if (negatives != null) {
            for (Sequence seq : negatives.getSequences()) {
                allSequencesList.add(seq);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            EnzymeFinder finder = new EnzymeFinder();
            finder.loadPositiveSequences(new File("D:\\Work\\ChenKai\\positives"));
            finder.loadNegativeSequences(new File("D:\\Work\\ChenKai\\negatives"));
            finder.loadPatterns(new File("D:\\Work\\ChenKai\\allcomb.txt"));
            finder.printFind();
        } catch (IOException ex) {
            Logger.getLogger(EnzymeFinder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EnzymeFinderException ex) {
            Logger.getLogger(EnzymeFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
