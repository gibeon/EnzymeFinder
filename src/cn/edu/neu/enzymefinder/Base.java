/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gibeon
 */
public class Base {
    public static final byte BASE_ANY = (byte)0xff;   // 11111111
    public static final byte BASE_UNKNOWN = 0;    // 0000
    public static final byte BASE_A = 1;    //0001
    public static final byte BASE_T = 2;    //0010
    public static final byte BASE_C = 4;    //0100
    public static final byte BASE_G = 8;    //1000
    public static final byte BASE_R = BASE_A | BASE_G;    //1001
    public static final byte BASE_Y = BASE_C | BASE_T;    //0110
    public static final byte BASE_W = BASE_A | BASE_T;    //0011
    public static final byte BASE_S = BASE_G | BASE_C;    //1100
    public static final byte BASE_M = BASE_A | BASE_C;    //0101
    public static final byte BASE_K = BASE_G | BASE_T;    //1010
    public static final byte BASE_H = BASE_A | BASE_T | BASE_C; //0111
    public static final byte BASE_B = BASE_G | BASE_C | BASE_T; //1110
    public static final byte BASE_V = BASE_G | BASE_A | BASE_C; //1101
    public static final byte BASE_D = BASE_G | BASE_A | BASE_T; //1011
    
    public static byte[] toCodes(byte[] original, boolean bExtension){
        byte[] result = new byte[original.length];
        int i = 0;
        for(byte b : original){
            result[i++] = toCode((char)b, bExtension);
        }
        return result;
    }
    
    public static byte toCode(char name, boolean bExtension){
        byte result = BASE_UNKNOWN;
        // Since there should be only 4 types of base, i.e. A, T, G, C,
        // we need not to use regex which may be heavy and 
        // mismatch (char / byte) in this case.
        switch (name) {
            case (byte) 'a':
            case (byte) 'A':
                result = BASE_A;
                break;
            case (byte) 't':
            case (byte) 'T':
                result = BASE_T;
                break;
            case (byte) 'c':
            case (byte) 'C':
                result = BASE_C;
                break;
            case (byte) 'g':
            case (byte) 'G':
                result = BASE_G;
                break;
            default:
                if (!bExtension) {
                    break;
                } else {
                    switch (name) {
                        case (byte) 'N':
                        case (byte) 'X':
                            result = BASE_ANY;
                            break;
                        case (byte) 'R':
                            result = BASE_R;
                            break;
                        case (byte) 'Y':
                            result = BASE_Y;
                            break;
                        case (byte) 'W':
                            result = BASE_W;
                            break;
                        case (byte) 'S':
                            result = BASE_S;
                            break;
                        case (byte) 'M':
                            result = BASE_M;
                            break;
                        case (byte) 'K':
                            result = BASE_K;
                            break;
                        case (byte) 'H':
                            result = BASE_H;
                            break;
                        case (byte) 'B':
                            result = BASE_B;
                            break;
                        case (byte) 'V':
                            result = BASE_V;
                            break;
                        case (byte) 'D':
                            result = BASE_D;
                            break;
                        default:
                            break;
                    }
                }
        }
//        if (result == BASE_UNKNOWN) {
//            try {
//                throw new EnzymeFinderException("No such type of base");
//            } catch (EnzymeFinderException ex) {
//                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

        return result;
    }
    
    public static byte[] toPairCodes(byte[] codes) {
        if (codes == null)
            return null;
        byte[] pair = new byte[codes.length];

        for(int i = codes.length - 1, j = 0; i >= 0; i--){
            pair[i] = Base.toPairCode(codes[j++]);
        }
        
        return pair;
    }
    
    public static byte toPairCode(byte code){
        byte pair = BASE_UNKNOWN;
        switch (code){
            case BASE_A:
                pair = BASE_T;
                break;
            case BASE_T:
                pair = BASE_A;
                break;
            case BASE_C:
                pair = BASE_G;
                break;
            case BASE_G:
                pair = BASE_C;
                break;
            case BASE_R:
                pair = BASE_Y;
                break;
            case BASE_Y:
                pair = BASE_R;
                break;
            case BASE_W:
                pair = BASE_W;
                break;
            case BASE_S:
                pair = BASE_S;
                break;
            case BASE_M:
                pair = BASE_K;
                break;
            case BASE_K:
                pair = BASE_M;
                break;
            case BASE_H:
                pair = BASE_D;
                break;
            case BASE_B:
                pair = BASE_V;
                break;
            case BASE_V:
                pair = BASE_B;
                break;
            case BASE_D:
                pair = BASE_H;
                break;
            default:
                break;
        }
        return pair;
    }
    
    public static char[] toNames(byte[] code) {
        if (code == null)
            return (new String("")).toCharArray();
        char[] result = new char[code.length];
        int i = 0;
        for (byte b : code) {
            result[i++] = toName(b);
        }
        return result;
    }
        
        
    public static char toName(byte code){
        char name = 'X';    // means error
        switch (code) {
            case 0:
            case (byte) 0xff:
                name = 'N';
                break;
            case BASE_A:
                name = 'A';
                break;
            case BASE_T:
                name = 'T';
                break;
            case BASE_C:
                name = 'C';
                break;
            case BASE_G:
                name = 'G';
                break;
            case BASE_R:
                name = 'R';
                break;
            case BASE_Y:
                name = 'Y';
                break;
            case BASE_W:
                name = 'W';
                break;
            case BASE_S:
                name = 'S';
                break;
            case BASE_M:
                name = 'M';
                break;
            case BASE_K:
                name = 'K';
                break;
            case BASE_H:
                name = 'H';
                break;
            case BASE_B:
                name = 'B';
                break;
            case BASE_V:
                name = 'V';
                break;
            case BASE_D:
                name = 'D';
                break;
            default:
                name = 'X'; // means error
                break;
        }
        return name;
    }
}
