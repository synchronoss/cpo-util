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
package org.synchronoss.cpo.util;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class CpoEditConnPanel extends JPanel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JComboBox jComConn = new JComboBox();
  private JButton jButNew = new JButton();
  private JButton jButEdit = new JButton();
  private JButton jButDelete = new JButton();
  Properties props;

  public CpoEditConnPanel(Properties props) {
    this.props = props;
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    jButNew.setText("New");
    jButNew.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButNew_actionPerformed(e);
      }
    });

    jButEdit.setText("Edit");
    jButEdit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButEdit_actionPerformed(e);
      }
    });

    jButDelete.setText("Delete");
    jButDelete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButDelete_actionPerformed(e);
      }
    });

    this.add(jComConn, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jButNew, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jButEdit, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jButDelete, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    populateCombo();
  }
  
  @SuppressWarnings("unchecked")
  private void populateCombo() {
    this.jComConn.removeAllItems();
    Enumeration propsEnum = props.keys();
    Vector<String> vals = new Vector<String>();
    while (propsEnum.hasMoreElements()) {
      String name = (String)propsEnum.nextElement();
      if (name.startsWith(Statics.PROP_JDBC_URL)) {
        String server = name.substring(Statics.PROP_JDBC_URL.length());
        vals.add(server+":*JDBC*");
      } else if (name.startsWith(Statics.PROP_WLSURL)) {
        String server = name.substring(Statics.PROP_WLSURL.length());
        vals.add(server+":*WEBLOGIC*");
      }
    }
    // sort the list
    Collections.sort(vals);
    jComConn.setModel(new DefaultComboBoxModel(vals));
  }

  private void jButNew_actionPerformed(ActionEvent e) {
    CpoUtil.setNewJDBCConnection(null);
    populateCombo();
  }

  private void jButEdit_actionPerformed(ActionEvent e) {
    String selectedVal = (String)jComConn.getSelectedItem();
    if (selectedVal == null) return;
    String server = selectedVal.substring(0,selectedVal.indexOf(":"));
    if (selectedVal.endsWith("*JDBC*")) {
      CpoUtil.setNewJDBCConnection(server);
    }else if (selectedVal.endsWith("*WEBLOGIC*")) {
      CpoUtil.setNewWLConnection(server);
    }
    populateCombo();
  }

  private void jButDelete_actionPerformed(ActionEvent e) {
    String selectedVal = (String)jComConn.getSelectedItem();
    if (selectedVal == null) return;
    String server = selectedVal.substring(0,selectedVal.indexOf(":"));
    CpoUtil.removeConnection(server);
    populateCombo();
  }
}