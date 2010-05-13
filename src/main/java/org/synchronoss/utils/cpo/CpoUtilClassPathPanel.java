/*
 *  Copyright (C) 2006  Jay Colson
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
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class CpoUtilClassPathPanel extends JPanel  {

  /** Version Id for this class. */
  private static final long serialVersionUID=1L;
  
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabClasspath = new JLabel();
  private JList jListClasspath = new JList();
  private JButton jButAddClassPath = new JButton();
  private JButton jButRemove = new JButton();
  private JScrollPane jScroll = new JScrollPane();
  private List<File> files;
  
  public CpoUtilClassPathPanel(List<File> files) {
    this.files = files;
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(515, 387));
    this.setLayout(gridBagLayout1);
    jLabClasspath.setText("Currently in Classpath");
    jListClasspath.setListData(new Vector<File>(files));
    jButAddClassPath.setText("Add");
    jButAddClassPath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addFile();
      }
    });
    jButRemove.setText("Remove");
    jButRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeFile();
      }
    });
    this.jScroll.getViewport().add(jListClasspath);
    this.add(jLabClasspath, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jScroll, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jButAddClassPath, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jButRemove, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  private void addFile() {
    JFileChooser jFile = new JFileChooser();
    jFile.setMultiSelectionEnabled(true);
    jFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    int result = jFile.showOpenDialog(this);
    if (result == 1) return;
    files.addAll(Arrays.asList(jFile.getSelectedFiles()));
    this.jListClasspath.setListData(new Vector<File>(files));
  }

  private void removeFile() {
    Object[] selectedFiles = this.jListClasspath.getSelectedValues();
    for (Object selectedFile : selectedFiles) {
      files.remove(selectedFile);
    }
    this.jListClasspath.setListData(new Vector<File>(files));
  }
}