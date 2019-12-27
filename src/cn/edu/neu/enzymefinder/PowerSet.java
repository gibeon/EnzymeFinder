/**
 * This class is used to enumerate the power set of a given collection.
 * The source code is modified from http://cisinference.googlecode.com/files/PowerSet.java
 */
package cn.edu.neu.enzymefinder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class PowerSet<E> implements Iterable<Set<E>> {

    Collection<E> all;
    private Set<Long> availables = new HashSet<Long>();    // Added by WuGang 2012-12-26. Record those powerset elements that should not be skipped
    private boolean bUseAvailableSet = false;   // Added by WuGang 2012-12-26, indicate to use available set
    private boolean bSkipAvailableSet = false;  // Added by WuGang 2012-12-26, indicate to skip already known availables when we build the available set
    
    //
    private boolean bUseAvailableSet_backup = false;
    private boolean bSkipAvailableSet_backup = false;
    

    public PowerSet(Collection<E> all) throws EnzymeFinderException {
        if (all.size() > 62)
            throw new EnzymeFinderException("The power set size of this collection is too large to support here (2^63 - 1).");
//        BigInteger bi = new BigInteger("2");
//        BigInteger max = bi.pow(all.size());
//        if (max.compareTo(new BigInteger(Long.toString(Long.MAX_VALUE))) == 1) {
//            throw new EnzymeFinderException("The power set size of this collection is too large to support here.");
//        }

        this.all = all;
    }
    
    // Added by WuGang 2012-12-26
    public PowerSet(Collection<E> all, boolean bUseAvailableSet, boolean bSkipAvailableSet) throws EnzymeFinderException {
        this(all);
        this.bUseAvailableSet = bUseAvailableSet;
        this.bSkipAvailableSet = bSkipAvailableSet;
        backupStatus();
    }

    public void setbSkipAvailableSet(boolean bSkipAvailableSet) {
        this.bSkipAvailableSet = bSkipAvailableSet;
    }

    public void setbUseAvailableSet(boolean bUseAvailableSet) {
        this.bUseAvailableSet = bUseAvailableSet;
    }
    
    public void backupStatus(){
        this.bUseAvailableSet_backup = this.bUseAvailableSet;
        this.bSkipAvailableSet_backup = this.bSkipAvailableSet;       
    }
    
    public void restoreStatus(){
        this.bUseAvailableSet = this.bUseAvailableSet_backup;
        this.bSkipAvailableSet = this.bSkipAvailableSet_backup;       
    }

    /**
     * *
     * @return an iterator over elements of type Collection<E> which enumerates
     * the PowerSet of the collection used in the constructor
     */
    public PowerSetIterator<E> iterator() {
        return new PowerSetIterator<E>(this);
    }
    
    public Set<Long> getAvailables(){
        return availables;
    }

    public class PowerSetIterator<InE> implements Iterator<Set<InE>> {

        PowerSet<InE> powerSet;
        List<InE> canonicalOrder = new ArrayList<InE>();
        List<InE> mask = new ArrayList<InE>();  // the size increases until equal to the size of canonicalOrder
        boolean hasNext = true;
        private long id = 0; // Added by WuGang 2012-12-26. Use this field to identify current visiting powerset element.

        PowerSetIterator(PowerSet<InE> powerSet) {

            this.powerSet = powerSet;
            canonicalOrder.addAll(powerSet.all);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private boolean allOnes() {
            for (InE bit : mask) {
                if (bit == null) {
                    return false;
                }
            }
            return true;
        }

        private void increment() {
            int i = 0;
            while (true) {
                if (i < mask.size()) {
                    InE bit = mask.get(i);
                    if (bit == null) {  // 0 -> 1, we have finished. e.g. 1000 -> 1001, return at the first bit.
                        mask.set(i, canonicalOrder.get(i));
                        return;
                    } else {    // 1 -> 0, we should increment the next bit. e.g. 00000101 -> 00000110, return after 1 else and 1 if
                        mask.set(i, null);
                        i++;
                    }
                } else {    // we have arrived the top bit, so stop. e.g. 111 -> 1000. The fourth bit is copy from canonicalOrder
                    mask.add(canonicalOrder.get(i));
                    return;
                }
            }
        }

        public boolean hasNext() {
            return hasNext;
        }

        public Set<InE> next() {
            
            Set<InE> result = new HashSet<InE>();
            // Moved by WuGang 2012-12-26.
//            result.addAll(mask);
//            result.remove(null);
            
            // Added by WuGang 2012-12-26, skip if current id is not in the power set's availables
            if (bUseAvailableSet && !bSkipAvailableSet && !powerSet.availables.contains(new Long(id))){
                result = null;    // Return null means we should skip this power set element, because it is not in the availables (i.e. not skip the elements in the available set)
//                System.out.println("Skip id=" + id + ", mask=" + mask);
            }
            else {
                if (bUseAvailableSet && bSkipAvailableSet && powerSet.availables.contains(new Long(id)))
                    result = null;
                else {
                    result.addAll(mask);
                    result.remove(null);
//                    if (bUseAvailableSet && bSkipAvailableSet){
//                        System.out.println("Find an available element:" + id + ", mask=" + mask);
//                    }
                }
            }

            hasNext = mask.size() < powerSet.all.size() || !allOnes();

            if (hasNext) {
                increment();
            }
                
            id++;   // Added by WuGang 2012-12-26

            return result;
        }
        
        // Added by WuGang 2012-12-26, return the latest visited powerset element's id
        public long getId(){
            return id-1;
        }
        
        // Added by WuGang 2012-12-26, set the latest visited powerset element to be available
        // If bDerived is set, then we should inference all powerset elements that derived from the latest visited powerset element
        public void setToBeAvailable(boolean bDerived){
            if (!bDerived)
                powerSet.availables.add(getId());
            else{
                BigInteger bi = new BigInteger("2");
                BigInteger max = bi.pow(canonicalOrder.size());
                System.out.println("Max is " + max);
                for(long i = getId(); i < max.longValue(); i++) {
                    long temp = i & getId();
                    System.out.println("Temp=" +temp + ", i = " + i + ", id=" + getId());
                    if (temp == getId()){
                        powerSet.availables.add(i);
//                        System.out.println(i);
                    }
                }
            }
        }
        
    }
}
