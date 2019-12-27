/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gibeon
 */
public class Patterns {
    // Only one TYPE is allowed
    public static String PATTERN_PROPERTY_KEY_TYPE = "TYPE";        // value is the name of the type, e.g., type I RM.
    public static String PATTERN_PROPERTY_VALUE_TYPE_I_RM = "type-I-RM";    // This is specific for type I RM.
    // Multiple FILTERs section (composed of HEAD or BODY or TAIL) are allowed, and they are in parallel.
    // However, one FILTER can only have at most one HEAD, one RAND, one TAIL followed (while the order is not important).
    public static String PATTERN_PROPERTY_KEY_FILTER = "FILTER";    // value is an id for the filter
    public static String PATTERN_PROPERTY_KEY_HEAD = "HEAD";        // value is a regular expression
    public static String PATTERN_PROPERTY_KEY_RAND = "RAND";        // value is a regular expression
    public static String PATTERN_PROPERTY_KEY_TAIL = "TAIL";        // value is a regular expression
    
    class PatternFilter{
        public java.util.regex.Pattern head;
        public java.util.regex.Pattern rand;
        public java.util.regex.Pattern tail;
    }

    private List <Pattern> patterns = new ArrayList<Pattern>();
    private Properties patternProperties = new Properties();
    private List <PatternFilter> filters = new ArrayList<PatternFilter>();
    
    public void clear(){
        patterns.clear();
        patternProperties.clear();
        filters.clear();
    }
    
    public int size(){
        return patterns.size();
    }
    
    public void add(Pattern pattern){
        patterns.add(pattern);
    }
    
    public void remove(int pos){
        patterns.remove(pos);
    }
    
    public Pattern get(int pos){
        return patterns.get(pos);
    }
    
    public Properties getConfig(){
        return patternProperties;
    }
    
    public List<PatternFilter> getPatternFilter(){
        return this.filters;
    }
    
    public void loadFromFile(File file) throws IOException, EnzymeFinderException{
        assert(file!=null);
        assert (file.isFile());
        clear();   // Clear the patterns and configs
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String patternLine = reader.readLine();
        
        // Added by Wu Gang 2013-2-25
        // Process config line which is identified with '#' at the beginning of the line.
        while ((patternLine != null)&&(patternLine.charAt(0) == '#')){
            StringReader configReader = new StringReader(patternLine.substring(1));
            Properties p = new Properties();
            p.load(configReader);
            patternProperties.putAll(p);    // add the property to patternProperties
            if (p.containsKey(Patterns.PATTERN_PROPERTY_KEY_TYPE)){
                // TODO: here we should generate a set of Patterns.PatternFilter
                //       objects according to the type value (type name)
                
                // Added by WuGang 2013-02-27, Support type-I-RM
                String typeValue = p.getProperty(Patterns.PATTERN_PROPERTY_KEY_TYPE);
                if (typeValue.equalsIgnoreCase(PATTERN_PROPERTY_VALUE_TYPE_I_RM)){
                    PatternFilter pf1 = new PatternFilter();
                    pf1.head = java.util.regex.Pattern.compile(".*A+.*");
                    pf1.tail = java.util.regex.Pattern.compile(".*T+.*");
                    filters.add(pf1);
                    PatternFilter pf2 = new PatternFilter();
                    pf2.head = java.util.regex.Pattern.compile(".*T+.*");
                    pf2.tail = java.util.regex.Pattern.compile(".*A+.*");
                    filters.add(pf2);
                }
                patternLine = reader.readLine();
            }else{
                if (p.containsKey(Patterns.PATTERN_PROPERTY_KEY_FILTER)){
                    // Here we should generate a Patterns.PatternFilter object 
                    // to store the following read lines
                    patternLine = reader.readLine();
                    if (patternLine == null) {
                        throw new EnzymeFinderException("Errors in parsing configurations of the patterns file.");
                    } else {
                        PatternFilter pf = new PatternFilter();
                        while ((patternLine != null) && (patternLine.charAt(0) == '#')) {
                            // Here, we should read either
                            StringReader filterReader = new StringReader(patternLine.substring(1));
                            Properties fp = new Properties();
                            fp.load(filterReader);
                            String head = fp.getProperty(Patterns.PATTERN_PROPERTY_KEY_HEAD);
                            if (head == null) {
                                String rand = fp.getProperty(Patterns.PATTERN_PROPERTY_KEY_RAND);
                                if (rand == null) {
                                    String tail = fp.getProperty(Patterns.PATTERN_PROPERTY_KEY_TAIL);
                                    if (tail == null) {
                                        break;
                                    } else {
                                        pf.tail = java.util.regex.Pattern.compile(tail);
                                    }
                                } else {
                                    pf.rand = java.util.regex.Pattern.compile(rand);
                                }
                            } else {
                                pf.head = java.util.regex.Pattern.compile(head);
                            }
                            // read next line
                            patternLine = reader.readLine();
                        }// end of while
                        filters.add(pf);
                    }// end of else
                }else
                    throw new EnzymeFinderException("Errors in parsing configurations of the patterns file.");
            }// end of else
        }// end of while
        

        // '#' is not allowed at the beginning of the lines from now on.
        while (patternLine != null) {
            String [] parts = patternLine.split(":");
            assert(parts.length == 3);  // Composed of 3 parts: head, rand, tail
            
            byte[] head = new byte[parts[0].length()];
            for (int i = 0; i < parts[0].length(); i++){
                if ((parts[0].charAt(i) =='N')||(parts[0].charAt(i) =='n'))
                    head[i] = (byte)0xff;
                else if ((parts[0].charAt(i) =='X')||(parts[0].charAt(i) =='x'))
                    head[i] = 0;
                else
                    throw new EnzymeFinderException("Encounter an unrecognizable type in head: " 
                            + parts[0].charAt(i));
            }
            
            byte randLength = new Byte(parts[1]).byteValue();
            
            byte[] tail = new byte[parts[2].length()];
            for (int i = 0; i < parts[2].length(); i++){
                if ((parts[2].charAt(i) =='N')||(parts[2].charAt(i) =='n'))
                    tail[i] = (byte)0xff;
                else if ((parts[2].charAt(i) =='X')||(parts[2].charAt(i) =='x'))
                    tail[i] = 0;
                else
                    throw new EnzymeFinderException("Encounter an unrecognizable type in tail: " 
                            + parts[2].charAt(i));
            }
            
            Pattern pattern = new Pattern(head, randLength, tail);
            patterns.add(pattern);
            
            patternLine = reader.readLine();
        }
        reader.close();
    }
    
    public void saveToFile(File file) throws IOException{
        assert(file!=null);
        assert (file.isFile());
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        writer.write(this.toString());

        writer.close();
    }
    

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Pattern pattern : patterns) {
            buffer.append(pattern.toString());
            buffer.append("\n");
        }
        return "patterns=\n" + buffer.toString();
    }
    
    
    
    public static void main(String [] args){
        try {
            Patterns patterns = new Patterns();
            patterns.loadFromFile(new File("D:\\Work\\ChenKai\\patterns.txt"));
            System.out.println(patterns);
            for (int i = 0; i < patterns.get(0).length(); i++)
                System.out.println(patterns.get(0).isX(i));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Patterns.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Patterns.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Patterns.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
