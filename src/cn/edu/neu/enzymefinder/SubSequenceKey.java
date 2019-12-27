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
public class SubSequenceKey {
    protected byte[] sequence;
    
    public SubSequenceKey(byte[] sequence){
        this.sequence = sequence;
    }

    public byte[] getSubSequence(){
        return sequence;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SubSequenceKey other = (SubSequenceKey) obj;
        if (!Arrays.equals(this.sequence, other.sequence)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Arrays.hashCode(this.sequence);
        return hash;
    }

    @Override
    public String toString() {
        return "SubSequenceKey{" + "sequence=" + new String(Base.toNames(sequence)) + '}';
    }
        
}
