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

public class CpoEditConnPanel extends JPanel {

  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  // Version Id for this class
  private static final long serialVersionUID = 1L;
  private JComboBox connectionComboBox = new JComboBox();

  public CpoEditConnPanel() {
    refreshConnectionList();
    try {
      jbInit();
    } catch (Exception e) {
      OUT.error(e.getMessage(), e);
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(new GridBagLayout());

    this.add(connectionComboBox, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    JButton editButton = new JButton("Edit");
    editButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editConnection(e);
      }
    });
    this.add(editButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JButton deleteButton = new JButton("Delete");
    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteConnection(e);
      }
    });
    this.add(deleteButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  private void editConnection(ActionEvent e) {
    String connectionName = (String)connectionComboBox.getSelectedItem();
    if (connectionName == null) {
      return;
    }

    CpoUtil.getInstance().editConnection(connectionName);
    refreshConnectionList();
  }

  private void deleteConnection(ActionEvent e) {
    String connectionName = (String)connectionComboBox.getSelectedItem();
    if (connectionName == null) {
      return;
    }

    String message = "Are you sure you want to delete the connection: " + connectionName;
    int result = JOptionPane.showConfirmDialog(CpoUtil.getInstance(), message, "Delete Connection", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (result == JOptionPane.YES_OPTION) {
      CpoUtil.getInstance().removeConnection(connectionName);
      refreshConnectionList();
    }
  }

  protected void refreshConnectionList() {
    connectionComboBox.setModel(new DefaultComboBoxModel(CpoUtil.getInstance().getConnectionList()));
  }
}