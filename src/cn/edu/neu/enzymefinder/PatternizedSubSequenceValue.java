/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author gibeon
 */
public class PatternizedSubSequenceValue {
    protected HashMap<SubSequenceKey, HashSet<String>> subSequences = new HashMap<SubSequenceKey, HashSet<String>>();
    protected static Pattern pattern;
    
    protected PowerSet<Entry<SubSequenceKey, HashSet<String>>> possibleSubSequences = null;
    
    public static byte[] filterBases = null;    // {Base.BASE_R, Base.BASE_Y};
            
    
    public PatternizedSubSequenceValue() throws EnzymeFinderException{
        possibleSubSequences = new PowerSet<Entry<SubSequenceKey, HashSet<String>>>(subSequences.entrySet(), true, true);
    }
    
    public HashMap<SubSequenceKey, HashSet<String>> getSubSequences(){
        return subSequences;
    }
    
    public void insert(String id, byte[] subSequence, Pattern pattern) throws EnzymeFinderException{
        if(subSequence.length != pattern.length())
            throw new EnzymeFinderException("The length of sequence is inequal "
                    + "to the length of pattern.");
        
        // bit-wise AND at rand part to generate value
        for (int i = pattern.getHead().length; 
                i < pattern.head.length + pattern.getRandLength(); i++){  
            subSequence[i] &= pattern.full[i];
        }
        
        PatternizedSubSequenceValue.pattern = pattern;
        SubSequenceKey subSequenceKey = new SubSequenceKey(subSequence);

        if (subSequences.containsKey(subSequenceKey)){
            subSequences.get(subSequenceKey).add(id);
        }else{
            HashSet<String> ids = new HashSet<String>();
            ids.add(id);
            subSequences.put(subSequenceKey, ids);
        }
//        sources.add(id);
//        sequences.add(sequence);
    }
    
    public Set<String> getContainingSequences(){
        Set<String> allIds = new HashSet<String>();
        for(HashSet<String> ids : subSequences.values())
            allIds.addAll(ids);
        return allIds;
    }
    
    public int getFrequency(){
        return getContainingSequences().size();
    }
    
    public List<Integer> getFrequencies(){
        if (possibleSubSequences == null)
            return null;
        List<Integer> result = new ArrayList<Integer>();
        PowerSet.PowerSetIterator it = possibleSubSequences.iterator();
        while(it.hasNext){
            Set<Entry<SubSequenceKey, HashSet<String>>> powerSetElement = it.next();
            if (powerSetElement == null) // Indicate that we should skip this power set element
                continue;
            result.add(getFrequency(powerSetElement));
        }

        return result;
    }
    
    public static Set<String> getContainingSequences(Set<Entry<SubSequenceKey, HashSet<String>>> powerSetElement){
        Set<String> allIds = new HashSet<String>();
        for(Entry<SubSequenceKey, HashSet<String>> entry : powerSetElement ){
            allIds.addAll(entry.getValue());
        }
        return allIds;      
    }
    
    public static int getFrequency(Set<Entry<SubSequenceKey, HashSet<String>>> powerSetElement){
        return getContainingSequences(powerSetElement).size();
    }
    
    public byte[] getMergedValue(){
//        if(sequences.isEmpty())
//            return null;
        if (subSequences.keySet().isEmpty())
            return null;
        
        byte[] result = new byte[pattern.length()];
        
        // Merge all the sub sequences
//        for (byte[] sequence : sequences){
        for (SubSequenceKey subSequence : subSequences.keySet()){
            for (int i = 0; i < pattern.length(); i++){
                result[i] |= subSequence.getSubSequence()[i];
            }
        }
        return result;
    }
    
    public List<byte[]> getMergedValues(boolean bUseFilter){
        if (possibleSubSequences == null)
            return null;
        List<byte[]> result = new ArrayList<byte[]>();
        PowerSet.PowerSetIterator it = possibleSubSequences.iterator();
        while(it.hasNext){
            Set<Entry<SubSequenceKey, HashSet<String>>> powerSetElement = it.next();
            if (powerSetElement == null) // Indicate that we should skip this power set element
                continue;
            byte[] mergedBytes = getMergedValue(powerSetElement);
            if (mergedBytes != null)    // null means an invalid merged value
                result.add(mergedBytes);
        }

        return result;
    }    
    
    public List<String> getMergedValueNames(){
        if (possibleSubSequences == null)
            return null;
        List<String> result = new ArrayList<String>();
        PowerSet.PowerSetIterator it = possibleSubSequences.iterator();
        while(it.hasNext){
            Set<Entry<SubSequenceKey, HashSet<String>>> powerSetElement = it.next();
            if (powerSetElement == null) // Indicate that we should skip this power set element
                continue;
            byte[] mergedBytes = getMergedValue(powerSetElement);
            if (mergedBytes != null)    // null means an invalid merged value
                result.add(new String(Base.toNames(mergedBytes)));
            else
                result.add("");
        }

        return result;
    }    
    
    public static byte[] getMergedValue(Set<Entry<SubSequenceKey, HashSet<String>>> powerSetElement){
        if (powerSetElement.isEmpty())
            return null;
        
        byte[] result = new byte[pattern.length()];
        
        // Merge all the sub sequences
//        for (byte[] sequence : sequences){
        for (Entry<SubSequenceKey, HashSet<String>> entry : powerSetElement){
            for (int i = 0; i < pattern.length(); i++){
                result[i] |= entry.getKey().getSubSequence()[i];
            }
        }
        
        return result;
    }
    
    // Return true means we can reserve this power set element according to the preset filter bases, otherwise we should ignore the element
    public static boolean isMergedValueFilterable(Set<Entry<SubSequenceKey, HashSet<String>>> powerSetElement){
        byte[] mergedValueBytes = getMergedValue(powerSetElement);
         // if we set filterBases and it is not empty, we should filter out those merged values in whose x position has non-filter bases
        if ((filterBases != null) && (filterBases.length != 0)&&(mergedValueBytes != null)){
            for (int i = 0; i < pattern.length(); i++){
                if (pattern.isX(i)){
                    boolean currentXResult = false;
                    for (byte b : filterBases){
                        if (mergedValueBytes[i] == b)
                            currentXResult = true;
                    }
                    if (currentXResult == false)
                        return false;
                }
            }
        }
        return true;
    }

    public boolean mergedValueContains(byte[] subSequence){
        if (subSequence.length != pattern.length())
            return false;
        
        byte[] mergedValue = getMergedValue();
        
        if (mergedValue == null)
            return false;
        
        for(int i = 0; i < subSequence.length; i++){
            byte temp = (byte)(mergedValue[i] | subSequence[i]);
            if (temp != mergedValue[i])
                return false;
        }
        
        return true;
    }

    public static boolean mergedValueContains(Set<Entry<SubSequenceKey, HashSet<String>>> powerSetElement, byte[] subSequence){
        if (subSequence.length != pattern.length())
            return false;
        
        byte[] mergedValue = getMergedValue(powerSetElement);
        
//        System.out.println("Comparing Merge: " + new String(Base.toNames(mergedValue)) + " and " + new String(Base.toNames(subSequence)));
        
        if (mergedValue == null)
            return false;
        
        for(int i = 0; i < subSequence.length; i++){
            byte temp = (byte)(mergedValue[i] | subSequence[i]);
            if (temp != mergedValue[i])
                return false;
        }
        
        return true;
    }

    
    public PowerSet<Entry<SubSequenceKey, HashSet<String>>> getPossibleSubSequences() {
        return possibleSubSequences;
    }

    public static byte[] getFilterBases() {
        return filterBases;
    }

    public static void setFilterBases(byte[] filterBases) {
        PatternizedSubSequenceValue.filterBases = filterBases;
    }
    
    @Override
    public String toString() {
//        String strDetail = "PatternizedSubSequenceValue Detail:{" + subSequences + '}';
//        byte[] mergedValue = getMergedValue();
//        StringBuffer strMergedValue = new StringBuffer("");
//        for(byte pos : mergedValue){
//            strMergedValue.append(Base.toName(pos));
//        }
        
//        String strSummary = "PatternizedSubSequenceValue: " + "frequency=" + getFrequency() + ", mergedvalue=" + new String(Base.toNames(mergedValue));
//                + "\n" + strDetail;
        String strSummary;
        if (pattern.hasX()){
            strSummary = "PatternizedSubSequenceValue: " + "mergedvalues=" + getMergedValueNames() + ", frequencies=" + getFrequencies();
        }else{
            byte[] mergedValue = getMergedValue();
            strSummary = "PatternizedSubSequenceValue: " + "frequency=" + getFrequency() + ", mergedvalue=" + new String(Base.toNames(mergedValue));
        }
        
        return strSummary;
    }
    
    
}
