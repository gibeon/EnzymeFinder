/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 *
 * @author gibeon
 */
public class Sequences {
    protected List<Sequence> sequences = new ArrayList<Sequence>();
    protected HashMap<PatternizedSubSequenceKey, PatternizedSubSequenceValue> subSequences 
        = new HashMap<PatternizedSubSequenceKey, PatternizedSubSequenceValue>();

    public List<Sequence> getSequences() {
        return sequences;
    }
    
    public Collection<PatternizedSubSequenceValue> getSubSequenceValues(){
        return subSequences.values();
    }
    
    public HashMap<PatternizedSubSequenceKey, PatternizedSubSequenceValue> getSubSequences(){
        return subSequences;
    }
        
    public int loadFromDir(File file) throws IOException{
        assert (file!=null);
        sequences.clear();  // Clear the sequences
        if(file.isFile()){
            Sequence seq = new Sequence();
            seq.loadFromFile(file);
            add(seq);
        }else{
            for(File fileInDir : file.listFiles()){
                Sequence seq = new Sequence();
                seq.loadFromFile(fileInDir);
                add(seq);
            }
        }
        return sequences.size();
    }

    /**
     * Print statistics information. It is only meaningful after calling patternize.
     * @return the sparse rate
     */
    public double printStatistics(){
        System.out.println("There are " + sequences.size() + " sequences.");
        System.out.println("There are " + subSequences.keySet().size() + " different subsequences.");
        Iterator<PatternizedSubSequenceValue> it = subSequences.values().iterator();
        int totalSubSequences = 0;
        while(it.hasNext()){
            totalSubSequences += it.next().getFrequency();
        }
        System.out.println("The frequency of these subsequences in files are: " + totalSubSequences);
        int totalBits = sequences.size()*subSequences.keySet().size();
        System.out.println("However, it will need bits: " + sequences.size() + " X " + subSequences.keySet().size() + " = " + totalBits);
        System.out.println("Therefore, the sparse rate is: " + totalSubSequences + " / " + totalBits + " = " + (totalSubSequences + 0.0)/totalBits);
        return (totalSubSequences + 0.0)/totalBits;
    }
    
    public Set<String> getAllIds(){
        Set<String> results = new HashSet<String>();
        for(Sequence seq : sequences){
            results.add(seq.id);
        }
        return results;
    }
    
    public void add(Sequence seq){
        sequences.add(seq);
    }
    
    public void patternize(Pattern pattern, List<Patterns.PatternFilter> filters) throws EnzymeFinderException{
        subSequences.clear();
        patternize(pattern, filters, false); // Patternize the original sequence
        patternize(pattern, filters, true);  // Patternize the pair sequence
    }

    /**
     * A sliding window style patternize
     * @param pattern According to which to patternize all the sequences
     * @param filters We can use a reg expression to constrain the head or tail of the sequece 
     * @param bPair If true means we will patternize a reverse pair of the sequence
     * @throws EnzymeFinderException 
     */
    private void patternize(Pattern pattern, List<Patterns.PatternFilter> filters, boolean bPair) throws EnzymeFinderException{
        // Move clear to patternize(Pattern pattern)
//        subSequences.clear(); 
        
        for (Sequence seq : sequences){
            if (pattern.length() > seq.getOriginalSequence(bPair).size()) {
                return;
            }

            int sequenceSize = seq.getOriginalSequence(bPair).size();
            int patternSize = pattern.length();
//            System.out.println("Output subsequences of sequence: " + seq.id);
//            System.out.println("seq.getOriginalSequence().size(): " + seq.getOriginalSequence().size());
//            System.out.println("pattern.length(): " + pattern.length());
            for (int i = 0; i <= sequenceSize - patternSize; i++) {
                byte[] subSequence = new byte[pattern.length()];
//                System.arraycopy(seq.getOriginalSequence().toArray(), i, subSequence, 0, pattern.length());
                for(int j = i; j < i + pattern.length(); j++){
                    subSequence[j-i] = seq.getOriginalSequence(bPair).get(j);
                }
//                System.out.println("subsequence: " + new String(Base.toNames(subSequence)));
                
                // Added by WuGang 2013-2-27, Use the PatternFilter to filter out subSequence
                char[] strSubSequence = Base.toNames(subSequence);
                String strHead = new String(strSubSequence, 0, pattern.getHead().length);
                String strRand = new String(strSubSequence, 0 + pattern.getHead().length, pattern.getRandLength());
                String strTail = new String(strSubSequence, 0 + pattern.getHead().length + pattern.getRandLength(), pattern.getTail().length);
                boolean bFilterOut = false;
                for (Patterns.PatternFilter filter : filters){
                    if (filter.head!=null){
                        Matcher m = filter.head.matcher(strHead);
                        if (!m.matches()){
                            bFilterOut = true;
                            continue;  // This subsequence does not satisfy this pattern filter, then try next filter
                        }else
                            bFilterOut = false;
                    }
                    if (filter.rand!=null){
                        Matcher m = filter.rand.matcher(strRand);
                        if (!m.matches()){
                            bFilterOut = true;
                            continue;  // This subsequence does not satisfy this pattern filter, then try next filter
                        }else
                            bFilterOut = false;
                    }
                    if (filter.tail!=null){
                        Matcher m = filter.tail.matcher(strTail);
                        if (!m.matches()){
                            bFilterOut = true;
                            continue;  // This subsequence does not satisfy this pattern filter, then try next filter
                        }else
                            bFilterOut = false;
                    }
                    
                    if (!bFilterOut)
                        break;  // the subsequence could be kept for future finding!!!
                }
                if (bFilterOut) // This subsequence will not be considered in the future.
                    continue;
                
                // The subsequence will be considered below.
                PatternizedSubSequenceKey key = new PatternizedSubSequenceKey(subSequence, pattern);    // subSequence is not modified
                if (!subSequences.containsKey(key)){
                    PatternizedSubSequenceValue value = new PatternizedSubSequenceValue();
                    value.insert(seq.id, subSequence, pattern); // subSequence is modified
                    subSequences.put(key, value);
                }
                else{
                    subSequences.get(key).insert(seq.id, subSequence, pattern); // subSequence is modified
                }
            }
        }
    }
    
    public static void main(String [] args){
        Sequences positives = new Sequences();
        try {
            positives.loadFromDir(new File("D:\\Work\\ChenKai\\positives"));
            for (Sequence seq : positives.getSequences()){
                System.out.println(seq.getId());
                System.out.println(seq);
            }
        } catch (IOException ex) {
            Logger.getLogger(Sequences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
