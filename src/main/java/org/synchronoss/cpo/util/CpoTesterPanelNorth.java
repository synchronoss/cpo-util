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

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class CpoTesterPanelNorth extends JPanel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabQueryGroup = new JLabel();
  private JLabel jLabClassOut = new JLabel("Output Class: ");
  JComboBox jComQueryGroup = new JComboBox();
  private JScrollPane jScrollParam = new JScrollPane();
  JTable jTableParam = new JTable();
  private CpoClassNode cpoClassNode;
  JButton jButExec = new JButton();
  JCheckBox jCheckPersist = new JCheckBox();
  private CpoTesterParamModel cpoTesterParamModel;
  JComboBox jComClassOut = new JComboBox();
  private Logger OUT = Logger.getLogger(this.getClass());

  public CpoTesterPanelNorth(CpoClassNode cpoClassNode) {
    this.cpoClassNode = cpoClassNode;
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.jCheckPersist.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (jCheckPersist.isSelected()) {
          try {
            CpoQueryGroupNode selectedNode = (CpoQueryGroupNode)jComQueryGroup.getSelectedItem();
            CpoQueryGroupNode node = cpoClassNode.getProxy().getQueryGroupNode(cpoClassNode,selectedNode.getGroupName(),Statics.CPO_TYPE_CREATE);
            if (node == null) {
              node = cpoClassNode.getProxy().getQueryGroupNode(cpoClassNode,selectedNode.getGroupName(),Statics.CPO_TYPE_UPDATE);
            }
            if (node == null) {
              JOptionPane.showMessageDialog(CpoUtil.frame,"Can't locate a 'CREATE' or 'UPDATE' CPO Type with the Group Name of '"+selectedNode.getGroupName()+"'",
                  "Sorry, no go",JOptionPane.INFORMATION_MESSAGE);
              jCheckPersist.setSelected(false);
              return;
            }
            cpoTesterParamModel = new CpoTesterParamModel(node);
            jTableParam.setModel(cpoTesterParamModel);
          } catch (Exception ex) {
            CpoUtil.showException(ex);
          }
        }
        else {
          cpoTesterParamModel = new CpoTesterParamModel((CpoQueryGroupNode)jComQueryGroup.getSelectedItem());
          jTableParam.setModel(cpoTesterParamModel);
        }
        setTableCellSize();
      }
    });
    this.jComQueryGroup.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (((CpoQueryGroupNode)jComQueryGroup.getSelectedItem()).getType().equals(Statics.CPO_TYPE_EXIST))
          jCheckPersist.setEnabled(true);
        else {
          jCheckPersist.setSelected(false);
          jCheckPersist.setEnabled(false);
        }
        if (((CpoQueryGroupNode)jComQueryGroup.getSelectedItem()).getType().equals(Statics.CPO_TYPE_LIST)) {
          CpoQueryGroupNode cqgn = (CpoQueryGroupNode)jComQueryGroup.getSelectedItem();
          try {
            CpoServerNode cpoServerNode = (CpoServerNode)cqgn.getParent().getParent().getParent();
            OUT.debug("about to get classes for: "+cpoServerNode);
            List<CpoClassNode> alClasses = cqgn.getProxy().getClasses(cpoServerNode);
            jComClassOut.removeAllItems();
            for (CpoClassNode ccn : alClasses) {
              jComClassOut.addItem(ccn);
            }
            jComClassOut.setSelectedItem(cqgn.getParent().getParent());
          } catch (Exception ex) {
            CpoUtil.showException(ex);
          }
          jComClassOut.setEnabled(true);
        } else {
          jComClassOut.setEnabled(false);
        }
        jTableParam.setModel(new CpoTesterParamModel((CpoQueryGroupNode)jComQueryGroup.getSelectedItem()));
        setTableCellSize();
      }
    });
    Enumeration<AbstractCpoNode> enumClassChildren = this.cpoClassNode.children();
    while (enumClassChildren.hasMoreElements()) {
      AbstractCpoNode node = enumClassChildren.nextElement();
      if (node instanceof CpoQueryGroupLabelNode) {
        Enumeration<? extends AbstractCpoNode> enumQG = node.children();
        while (enumQG.hasMoreElements()) {
          this.jComQueryGroup.addItem(enumQG.nextElement());
        }
      }
    }
//    this.setSize(new Dimension(750, 400));
    this.setLayout(gridBagLayout1);
    jLabQueryGroup.setText("QueryGroup:");
    jTableParam.setPreferredScrollableViewportSize(new Dimension(600,200));
    jButExec.setText("Execute");
    jCheckPersist.setText("Persist");
    jCheckPersist.setEnabled(false);
    this.add(jLabQueryGroup, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jComQueryGroup, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    jScrollParam.getViewport().add(jTableParam, null);
    this.add(jScrollParam, new GridBagConstraints(0, 2, 5, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jButExec, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jCheckPersist, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabClassOut, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));    
    this.add(jComClassOut, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));    
    this.jTableParam.setCellSelectionEnabled(true);
    setTableCellSize();
  }
  private void setTableCellSize() {
    if (this.jComQueryGroup.getSelectedItem() != null) {
      this.jTableParam.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      this.jTableParam.getColumnModel().getColumn(0).setPreferredWidth(45);
      this.jTableParam.getColumnModel().getColumn(1).setPreferredWidth(45);
      this.jTableParam.getColumnModel().getColumn(2).setPreferredWidth(112);
      this.jTableParam.getColumnModel().getColumn(3).setPreferredWidth(112);
      this.jTableParam.getColumnModel().getColumn(4).setPreferredWidth(290);
    }
  }

}