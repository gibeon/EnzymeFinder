/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.io.*;
import java.util.*;

enum SequenceType{POSITIVE, NEGATIVE}
/**
 *
 * @author gibeon
 */
public class Sequence {
    protected List<Byte> originalSequence = new ArrayList<Byte>(); 
    protected List<Byte> pairSequence = new ArrayList<Byte>(); 
    protected String id = "";
    protected List<Integer> locatingPositions = new ArrayList<Integer>();
    protected List<Integer> pairLocatingPositions = new ArrayList<Integer>();
    protected byte[] locatingSubSequence = null;
    protected boolean bAlreadyPairized = false;
    
    public List<Byte> getOriginalSequence(boolean bPair){
        if (bPair)
            return pairSequence;
        else
            return originalSequence;
    }
    
    public String getId(){
        return id;
    }
    
    public List<Integer> getLocatingPositions(){
        return locatingPositions;
    }
    
    public List<Integer> getPairLocatingPositions(){
        return pairLocatingPositions;
    }
    
    public byte[] getLocatingSubSequence(){
        return locatingSubSequence;
    }
    
    public List<Integer> locatePos(byte[] subSequence, boolean bPair){
        assert(subSequence.length > 0);
        assert(originalSequence.size() > 0);
        
        if (subSequence.length > originalSequence.size())
            return null;

        List<Byte> originalORpairSequence = null;
        List<Integer> positions = null;
        if (!bPair){
            originalORpairSequence = originalSequence;
            positions = locatingPositions;
        }else{
            originalORpairSequence = pairSequence;
            positions = pairLocatingPositions;
        }
        
        
        positions.clear();
        this.locatingSubSequence = subSequence;
        Object[] sequence = originalORpairSequence.toArray();
        for (int i = 0; i <= sequence.length - subSequence.length; i++){
            boolean bFound = true;
            for (int j = 0; j < subSequence.length; j++){
                byte temp = (byte)(((Byte)sequence[i+j]).byteValue() & subSequence[j]);
                if (temp != ((Byte)sequence[i+j]).byteValue()){
                    bFound = false;
                    break;
                }
            }
            if (bFound)
                positions.add(i);
        }
        return positions;
    }
    
    public void loadFromFile(File file) throws IOException{
        assert(file!=null);
        assert(file.isFile());
        id = file.getAbsolutePath();
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(
                new FileInputStream(file)));
        while(in.available()>0){
            byte base = in.readByte();
            
            byte code = Base.toCode((char)base, false);
            if (code == Base.BASE_UNKNOWN)
                continue;
            originalSequence.add(code);
//            System.out.println(base);
        }
//        System.out.println("-----------");
        
        // for DNA double link
        pairize();
        
        in.close();
    }
    
    String getOriginalSequenceInNames(boolean bPair){
        StringBuffer buffer = new StringBuffer("");
        for (Byte b : (bPair ? pairSequence : originalSequence)){
            buffer.append(Base.toName(b));            
        }
        return buffer.toString();        
    }
    
    // See Base.toPairCodes.
    private void pairize(){
        if (bAlreadyPairized);
        else{
            pairSequence.clear();
            pairSequence.addAll(originalSequence);
            Collections.reverse(pairSequence);
            if (pairSequence.size() > 0){
                for (int i = 0; i < pairSequence.size(); i++){
                    pairSequence.set(i, Base.toPairCode(pairSequence.get(i)));
                }
            }
            bAlreadyPairized = true;
        }
    }

    @Override
    public String toString() {
        return new String(id + "=\n" + getOriginalSequenceInNames(false));
    }
    
}
