/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.uncommons.maths.combinatorics.CombinationGenerator;

/**
 *
 * @author gibeon
 */
public class Test {
    
    public static void main(String[] args){
        Set<String> ss = new HashSet<String>();
        ss.add("a");
        ss.add("b");
        ss.add("c");
        ss.add("d");
        ss.add("e");
        
        CombinationGenerator<String> combs = new CombinationGenerator<String>(ss, 4);
        System.out.println("Total: " + combs.getTotalCombinations());
        Iterator<List<String>> it = combs.iterator();
        while(it.hasNext()){
            System.out.println(it.next());
        }
    }
}
