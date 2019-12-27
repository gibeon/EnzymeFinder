/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.util.Arrays;

/**
 *
 * @author gibeon
 */
public class PatternizedSubSequenceKey extends SubSequenceKey{
    protected static Pattern pattern;

    public PatternizedSubSequenceKey(byte[] sequence, Pattern pattern) throws EnzymeFinderException {
        super(sequence);    // construct SubSequenceKey object
        
        if(sequence.length != pattern.length())
            throw new EnzymeFinderException("The length of sequence is inequal to the length of pattern.");
        
//        System.out.println("Original sequence is:");
//        System.out.println(Base.toNames(sequence));
//        System.out.println("The pattern is:");
//        System.out.println(pattern);
        
        this.sequence = new byte[pattern.length()];
        System.arraycopy(sequence, 0, this.sequence, 0, pattern.length());
        for (int i = 0; i < sequence.length; i++){  // bit-wise AND to generate key
            this.sequence[i] &= pattern.full[i];
        }
        PatternizedSubSequenceKey.pattern = pattern;
        
//        System.out.println("The generated key is:");
//        System.out.println(Base.toNames(this.sequence));
    }

    @Override
    public String toString() {
        return "PatternizedSubSequenceKey{" + "sequence=" + new String(Base.toNames(sequence)) + '}';
    }
}
