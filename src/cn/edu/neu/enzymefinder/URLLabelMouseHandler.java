/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.neu.enzymefinder;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

/**
 *
 * @author gibeon
 */
public class URLLabelMouseHandler extends MouseAdapter{
    private JLabel label;
    private String url;
    private String originalText;
    private boolean isSupported;
    
    public URLLabelMouseHandler(JLabel label, String url){
        this.label = label;
        this.originalText = label.getText();
        
        // if url is set to be null, then we'll use the value in tooltip as url.
        if (url == null){
            this.url = label.getToolTipText();
        }
        
        // test if the Desktop.Action.BROWSE is supported in this platform. 
        try {
            this.isSupported = Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
        } catch (Exception e) {
            this.isSupported = false;
        }
    }
    
    private void highlight(boolean b) {
        if (!b) {
//            label.setText("<html><font color=blue><u>" + this.originalText + "</u></font></html>");
            label.setText(this.originalText);
        } else {
//            label.setText("<html><font color=red><u>" + this.originalText + "</u></font></html>");
            label.setText("<html><font color=blue><u>" + this.originalText + "</u></font></html>");
        }
    }
    
    public void mouseEntered(MouseEvent e) {
        highlight(isSupported);
        if (isSupported) {
            label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    public void mouseExited(MouseEvent e) {
        highlight(false);
    }

    public void mouseClicked(MouseEvent e) {
        try {
            Desktop.getDesktop().browse(
                    new java.net.URI(url));
        } catch (Exception ex) {
        }
    }
}