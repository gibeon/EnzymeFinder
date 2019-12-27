/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

/**
 *
 * @author gibeon
 */
public class LocatingThread extends Thread{
    private EnzymeFinder finder;
    private JProgressBar findingProgressBar;
    private JLabel progressLabel;
    private JTextArea logTextArea;
    private JTextArea summaryTextArea;
    private EnzymeFinderWindow finderWindow;
    private byte[] subSequence;
    
    public LocatingThread(byte[] subSequence, EnzymeFinder finder, EnzymeFinderWindow finderWindow){
        this.subSequence = subSequence;
        this.finder = finder;
        this.findingProgressBar = finderWindow.getFindingProgressBar();
        this.progressLabel = finderWindow.getProgressLabel();
        this.logTextArea = finderWindow.getLogTextArea();
        this.summaryTextArea = finderWindow.getSummaryTextArea();
        finderWindow.clearFindingDialog();
        this.finderWindow = finderWindow;
    }

    @Override
    public void run() {
        finder.visualLocate(subSequence, findingProgressBar, progressLabel, logTextArea, summaryTextArea);
        finder.setbStop(true);
        JOptionPane.showMessageDialog(finderWindow.getFindingDialog(), "Double click the line to see the result");
    }
        
}
