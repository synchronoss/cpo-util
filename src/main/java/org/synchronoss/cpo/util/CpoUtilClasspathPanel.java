/*
 * Copyright (C) 2003-2012 David E. Berry, Michael A. Bellomo
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * A copy of the GNU Lesser General Public License may also be found at
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.synchronoss.cpo.util;

import org.slf4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class CpoUtilClasspathPanel extends JPanel  {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  // Version Id for this class
  private static final long serialVersionUID=1L;

  private JList jListClasspath = new JList();
  private Vector<File> classpathEntries = new Vector<File>();

  public CpoUtilClasspathPanel(List<File> classpathEntries) {
    this.classpathEntries.addAll(classpathEntries);
    try {
      jbInit();
    } catch(Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(515, 387));
    this.setLayout(new GridBagLayout());

    JLabel jLabClasspath = new JLabel("Currently in Classpath");
    this.add(jLabClasspath, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jListClasspath.setListData(classpathEntries);
    JScrollPane jScroll = new JScrollPane();
    jScroll.getViewport().add(jListClasspath);
    this.add(jScroll, new GridBagConstraints(0, 1, 1, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JButton jButAddClasspath = new JButton("Add");
    jButAddClasspath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addFile();
      }
    });
    this.add(jButAddClasspath, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    JButton jButRemove = new JButton("Remove");
    jButRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeFile();
      }
    });
    this.add(jButRemove, new GridBagConstraints(1, 2, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  private void addFile() {
    JFileChooser jFile = new JFileChooser();
    jFile.setMultiSelectionEnabled(true);
    jFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    int result = jFile.showOpenDialog(this);
    if (result == 1) return;
    classpathEntries.addAll(Arrays.asList(jFile.getSelectedFiles()));
    this.jListClasspath.setListData(classpathEntries);
  }

  private void removeFile() {
    Object[] selectedFiles = this.jListClasspath.getSelectedValues();
    for (Object selectedFile : selectedFiles) {
      classpathEntries.remove(selectedFile);
    }
    this.jListClasspath.setListData(classpathEntries);
  }

  public List<File> getClasspathEntries() {
    return classpathEntries;
  }
}