/*
 *  Copyright (C) 2008
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  A copy of the GNU Lesser General Public License may also be found at
 *  http://www.gnu.org/licenses/lgpl.txt
 */
package org.synchronoss.utils.cpo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * This class is used to present the user with an "Estimated" progress box.  By
 * estimated we mean that the progress bar will move forward based on a timer, not
 * based on any actual work done.  The progress bar will be updated every <i>updateSeconds</i>,
 * which defaults to two.
 */
public class ProgressFrame extends JFrame implements ActionListener, ProgressEventListener {

    private javax.swing.JLabel label;
    private javax.swing.JProgressBar pbar;

    /**
     * Holds value of property counterValue.
     */
    private int counterValue;
    private int maxValue;

    private ProgressEvent max_event       = null ;

    int	local_counter = 0 ;
    int wait_counter = 0 ;
    boolean active = false ;

    /**
     * Default constructor.  Causes a progress frame with message = "Progress" and
     * an estimated execution time of 20 seconds.
     */
    public ProgressFrame() {
        this("Progress", 20);
    }

    /**
     * Creates new form ProgressFrame
     *
     * @param message The text message to appear above the progress bar.
     * @param max The number of "increments" that the background
     * process will take.  If this value is -1 then an indeterminate
     * progress box will be shown.
     */
    public ProgressFrame(String message, int max) {

        counterValue = 0;
        if (max == -1)
            maxValue = 10;
        else
            maxValue = max;

        initComponents();

        setTitle("Working");
        label.setText(message);

        pbar.setValue(counterValue);

        if (max == -1)
            pbar.setIndeterminate(true);

        pack();
    }

    public ProgressFrame(String message) {
        this(message, -1);
    }

    /**
     * The ProgressEventListener method.
     */
    public void progressMade(ProgressEvent pe) {
        if ( pe.isMaxEvent() ) {
            max_event = pe ; // just hold this for now
            wait_counter = max_event.getMax() / 10 ;
            active = false ;
        } else {
            if ( max_event != null ) {
                // max has been delayed, see if we have enough now
                local_counter += pe.getValue() ;
                if ( local_counter > wait_counter ) {
                    setMaxValue(max_event.getMax()) ;
                    max_event = null ;
                    increment(wait_counter) ;
                    active = true ;
                }
            }

            if ( active ) {
                increment(pe.getValue()) ;
            }
        }
    }

    public void setMaxValue(int newMax) {
        maxValue = newMax;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                pbar.setIndeterminate(false);
                pbar.setMaximum(maxValue);
            }
        });

        setCounterValue(0);
    }

    public void stop() {
        final ProgressFrame _this = this;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //System.out.println("ProgressFrame disposing") ;
                _this.dispose();
            }
        });
    }

    /**
     * Start the timer, which will begin to update the progress
     * bar.  Once the timer is started it would be unwise to alter
     * any of the properties.
     */
    public void start() {
        //System.out.println("progressbar starting: " + pbar) ;
        final ProgressFrame _this = this ;

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    Dimension frameSize = _this.getSize();
                    if (frameSize.height > screenSize.height) {
                        frameSize.height = screenSize.height;
                    }
                    if (frameSize.width > screenSize.width) {
                        frameSize.width = screenSize.width;
                    }
                    _this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
                    _this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    _this.setVisible(true);
                }
            }) ;
        } catch ( Exception ex ) {
        }
    }

    public void increment() {
        increment(1);
    }

    public synchronized void increment(int val) {

        counterValue += val;
        if (counterValue > maxValue)
            counterValue = maxValue;

        //System.out.println("counterValue now " + counterValue + "; max " + maxValue) ;

        if (pbar.isIndeterminate())
            return;

        try {
            //SwingUtilities.invokeAndWait(new Runnable() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //System.out.println("update running") ;
                    pbar.setValue(counterValue);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initComponents() {
        label = new javax.swing.JLabel();
        pbar = new javax.swing.JProgressBar(1, maxValue);

        getContentPane().setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;

        setPreferredSize(new java.awt.Dimension(200, 100));
        label.setText("Progress");
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(label, gridBagConstraints1);

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(5, 5, 5, 5);
        gridBagConstraints1.weightx = 1.0;
        getContentPane().add(pbar, gridBagConstraints1);

        pack();
    }

    /**
     * Getter for property counterValue.
     *
     * @return Value of property counterValue.
     */
    public int getCounterValue() {
        return counterValue;
    }

    /**
     * Setter for property counterValue.
     *
     * @param counterValue New value of property counterValue.
     */
    public void setCounterValue(int counterValue) {
        this.counterValue = counterValue;
    }

    public void setLabel(String l) {
        label.setText(l);
    }

    /**
     * ActionEvents arrive as they are triggered by the timer.  When each
     * event occurs, execute an instance of Update on the Swing event thread.
     * *
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        //SwingUtilities.invokeLater(new Update()) ;
    }
}
