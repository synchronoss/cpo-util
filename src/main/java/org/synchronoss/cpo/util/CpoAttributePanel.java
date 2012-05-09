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
import org.synchronoss.cpo.CpoException;
import org.synchronoss.cpo.meta.domain.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * JPanel for displaying attributes
 */
public class CpoAttributePanel extends JPanel  {

  // Version Id for this class
  private static final long serialVersionUID = 1L;
  private JPanel buttonPanel = new JPanel();
  private JButton addButton = new JButton();
  private JButton addFromExpressionButton = new JButton();
  private JButton removeButton = new JButton();

  private CpoAttributeLabelNode cpoAttLabNode;
  private JTable jTableAttMap;
  private JScrollPane jScrollTable = new JScrollPane();
  private CoreAttributeTableModel model;
  private TableSorter ts;
  private JPopupMenu menu = new JPopupMenu();
  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  /**
   * Constructs the panel
   *
   * @param cpoAttLabelNode The attribute label node
   * @param model The AttributeTableModel to use to render the table
   */
  public CpoAttributePanel(CpoAttributeLabelNode cpoAttLabelNode, CoreAttributeTableModel model) {
    this.cpoAttLabNode = cpoAttLabelNode;
    this.model = model;

    try {
      jbInit();
    } catch (Exception e) {
      CpoUtil.showException(e);
    }
  }

  /**
   * Builds panel
   */
  private void jbInit() throws Exception {
    this.setLayout(new BorderLayout());

    buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    addButton.setText("New");
    addButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        addNewAttribute();
      }
    });
    buttonPanel.add(addButton);

    addFromExpressionButton.setText("New From Expression");
    addFromExpressionButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        addAttrToClassFromExpression();
      }
    });
    buttonPanel.add(addFromExpressionButton);

    removeButton.setText("Remove");
    removeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        removeAttribute();
      }
    });
    buttonPanel.add(removeButton);

    this.add(buttonPanel, BorderLayout.NORTH);
    ts = new TableSorter(model);
    jTableAttMap = new JTable(ts);
    ts.addMouseListenerToHeaderInTable(jTableAttMap);
    jTableAttMap.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          ListSelectionModel selectionModel = jTableAttMap.getSelectionModel();
          int row = jTableAttMap.rowAtPoint(e.getPoint());
          selectionModel.addSelectionInterval(row, row);
          showMenu(e.getPoint());
        }
      }
    });
    jTableAttMap.setDefaultEditor(String.class,new CpoAttributeTableEditor(cpoAttLabNode.getProxy().getAllowableDataTypes()));
    jScrollTable.getViewport().add(jTableAttMap);
    this.add(jScrollTable,BorderLayout.CENTER);
  }

  /**
   * Builds the right click menu
   */
  public void showMenu(Point point) {
    menu.removeAll();
    JMenuItem jMenuRemove = new JMenuItem("Remove");
    jMenuRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        removeAttribute();
      }
    });
    menu.add(jMenuRemove);
    JMenuItem jMenuAdd = new JMenuItem("Add New Attribute");
    jMenuAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        addNewAttribute();
      }
    });
    menu.add(jMenuAdd);
    JMenuItem jMenuAddAttrWithSql = new JMenuItem("Add New Attributes based on expression");
    jMenuAddAttrWithSql.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        addAttrToClassFromExpression();
      }
    });
    menu.add(jMenuAddAttrWithSql);

    // show it
    menu.show(jTableAttMap, (int)point.getX(), (int)point.getY());
  }

  /**
   * Prompts the user to add a new attribute
   */
  public void addNewAttribute() {
    CpoClassNode ccn = cpoAttLabNode.getParent();
    CpoNewAttributePanel cnap = cpoAttLabNode.getNewAttributePanel();

    int result = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), cnap, "Add New Attribute", JOptionPane.OK_CANCEL_OPTION);
    OUT.debug("Result: " + result);
    if (result == JOptionPane.OK_OPTION) {
      try {
        CpoAttribute newAttribute = cnap.createCpoAttribute();
        boolean added = cpoAttLabNode.getProxy().addAttribute(ccn, newAttribute);
        if (added) {
          model.fireTableDataChanged();
        }
      } catch (CpoException ex) {
        CpoUtil.showException(ex);
      }
    }
  }

  /**
   * Prompts the user to add a new attribute based on an expression
   */
  private void addAttrToClassFromExpression() {
    CpoClassNode ccn = cpoAttLabNode.getParent();
    CpoNewClassPanel cncp = new CpoNewClassPanel(ccn.getProxy(), ccn.getUserObject().getName());
    int result = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), cncp, "Add New Attributes based on Expression", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      try {
        List<CpoAttribute> attributes = cpoAttLabNode.getProxy().createAttributesFromExpression(cncp.getConnection(), cncp.getExpression());
        boolean added = cpoAttLabNode.getProxy().addAttributes(ccn, attributes);
        if (added) {
          model.fireTableDataChanged();
        }
      } catch (Exception e) {
        CpoUtil.showException(e);
      }
    } else {
      // user wishes to cancel creation
      CpoUtil.getInstance().setStatusBarText("Aborted Attribute Creation");
    }
  }

  /**
   * Removes an attribute
   */
  private void removeAttribute() {
    int[] selectedRows = jTableAttMap.getSelectedRows();
    if (selectedRows.length < 1) {
      CpoUtil.showErrorMessage("Please select an attribute to remove.");
      return;
    }

    int[] trueRows = ts.getTrueRows(selectedRows);
    List<CpoAttributeNode> nodesToRemove = new ArrayList<CpoAttributeNode>();
    for (int i : trueRows) {
      CpoAttributeNode cpoAttributeNode = (CpoAttributeNode)cpoAttLabNode.getChildAt(i);

      // don't let them delete attributes that are used by functions
      List<CpoFunctionGroup> functionGroups = cpoAttributeNode.getProxy().getFunctionGroupsUsingAttribute(cpoAttributeNode);
      if (!functionGroups.isEmpty()) {
        StringBuilder msg = new StringBuilder();
        msg.append(cpoAttributeNode.getUserObject().getJavaName());
        msg.append(" cannot be removed because it is used by function(s):");
        for (CpoFunctionGroup functionGroup : functionGroups) {
          msg.append("\n");
          msg.append(functionGroup);
        }
        CpoUtil.showErrorMessage(msg.toString());
        return;
      }

      nodesToRemove.add(cpoAttributeNode);
    }


    for (CpoAttributeNode node : nodesToRemove) {
      node.setRemove(true);

      if (node.isNew() && node.isRemove()) {
        // if it was new and is being removed, remove it from the table
        cpoAttLabNode.remove(node);
      }
    }

    model.fireTableDataChanged();
  }
}