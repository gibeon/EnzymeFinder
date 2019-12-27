/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author gibeon
 */
public class FindingThread extends Thread{
    private EnzymeFinder finder;
    private JProgressBar findingProgressBar;
    private JLabel progressLabel;
    private JTextArea logTextArea;
    private JTextArea summaryTextArea;    
    private EnzymeFinderWindow finderWindow;
    
    public FindingThread(EnzymeFinder finder, EnzymeFinderWindow finderWindow){
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
            File f = finder.visualFind(findingProgressBar, progressLabel, logTextArea, summaryTextArea);
            finder.setbStop(true);
            if (f != null)
                JOptionPane.showMessageDialog(finderWindow.getFindingDialog(), "Please find the output results in " + f.getAbsolutePath());
//            finderWindow.getFindingDialog().setVisible(false);
    }
    
    
}
