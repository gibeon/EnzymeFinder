/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gibeon
 */
public class Pattern {

    byte[] head;
    byte randLength;
    byte[] tail;
    byte[] full;    // full is only used to compute hash code

    // only 0xff and 0 are valid values. so please do not construct this directly
    // call Patterns.loadFromFile() instead.
    public Pattern(byte[] head, byte randLength, byte[] tail) throws EnzymeFinderException {
        for (byte hb : head){
            if ((hb != (byte)0xff)&&(hb != (byte)0))
                throw new EnzymeFinderException("Not a valid head part of pattern: " + hb);
        }
        
        for (byte hb:tail){
            if ((hb != (byte)0xff)&&(hb != (byte)0))
                throw new EnzymeFinderException("Not a valid tail part of pattern: " + hb);
        }
        
        this.head = head;
        this.randLength = randLength;
        this.tail = tail;

        // full is composed by concating head, rand, tail where rand and the X position is set to be zero
        full = new byte[head.length + randLength + tail.length];
        System.arraycopy(head, 0, full, 0, head.length);
        for (int i = head.length; i < head.length + randLength; i++) {
            full[i] = 0;
        }
        System.arraycopy(tail, 0, full, head.length + randLength, tail.length);
    }

    public boolean hasX(){
        for (int pos = 0; pos < length(); pos++){
            if (isX(pos))
                return true;
        }
        return false;
    }
    
    /**
     * Test if it is X at the position pos
     *
     * @param pos 0 based
     * @return true if it is X, otherwise false
     */
    public boolean isX(int pos) {
        byte mask = 1;
        if (pos < head.length) {    // at head part
            return ((head[pos] == 0));
        }
        if ((pos >= head.length + randLength)
                && (pos < this.length()) && // at tail part
                (tail[pos - head.length - randLength] == 0)) {
            return true;
        } else {
            return false;
        }
    }

    public int length() {
        return head.length + randLength + tail.length;
    }

    public byte[] getHead() {
        return head;
    }

    public byte getRandLength() {
        return randLength;
    }

    public byte[] getTail() {
        return tail;
    }

    public byte[] getFull() {
        return full;
    }

    public void setHead(byte[] head) {
        this.head = head;
    }

    public void setRand(byte randLength) {
        this.randLength = randLength;
    }

    public void setTail(byte[] tail) {
        this.tail = tail;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            for (int i = 0; i < getHead().length; i++) {
                if (getHead()[i] == 0) {
                    buffer.append("X");
                } else if (getHead()[i] == (byte) 0xff) {
                    buffer.append("N");
                } else {
                    throw new EnzymeFinderException("Encounter an unrecognizable value in Head: "
                            + getHead()[i]);
                }
            }

            buffer.append(":");

            buffer.append(String.valueOf(getRandLength()));

            buffer.append(":");

            for (int i = 0; i < getTail().length; i++) {
                if (getTail()[i] == 0) {
                    buffer.append("X");
                } else if (getTail()[i] == (byte) 0xff) {
                    buffer.append("N");
                } else {
                    throw new EnzymeFinderException("Encounter an unrecognizable value in Head: "
                            + getTail()[i]);
                }
            }
        } catch (EnzymeFinderException ex) {
            Logger.getLogger(Patterns.class.getName()).log(Level.SEVERE, null, ex);
        }
        return buffer.toString();
    }
}
