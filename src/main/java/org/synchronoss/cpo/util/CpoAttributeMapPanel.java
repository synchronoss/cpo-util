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

import org.slf4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CpoAttributeMapPanel extends JPanel  {

  /** Version Id for this class. */
  private static final long serialVersionUID=1L;
  private JPanel buttonPanel = new JPanel();
  private JButton addButton = new JButton();
  private JButton addFromSqlButton = new JButton();
  private JButton removeButton = new JButton();

  private CpoAttributeLabelNode cpoAttLabNode;
  private JTable jTableAttMap;
  private JScrollPane jScrollTable = new JScrollPane();
  private CpoAttMapTableModel model;
  TableSorter ts;
  private JPopupMenu menu = new JPopupMenu();
  private Logger OUT = LoggerFactory.getLogger(this.getClass());
  
  public CpoAttributeMapPanel(CpoAttributeLabelNode cpoAttLabelNode) {
    this.cpoAttLabNode = cpoAttLabelNode;
    try {
      jbInit();
    } catch (Exception e) {
      CpoUtil.showException(e);
    }
  }

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

    addFromSqlButton.setText("New From SQL");
    addFromSqlButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        addAttrToClassFromSQL();
      }
    });
    buttonPanel.add(addFromSqlButton);

    removeButton.setText("Remove");
    removeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        removeAttribute();
      }
    });
    buttonPanel.add(removeButton);

    this.add(buttonPanel, BorderLayout.NORTH);
    model = new CpoAttMapTableModel(cpoAttLabNode);
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
    jTableAttMap.setDefaultEditor(String.class,new CpoAttMapTableEditor(cpoAttLabNode.getProxy().getCpoSqlTypes()));
    jScrollTable.getViewport().add(jTableAttMap);
    this.add(jScrollTable,BorderLayout.CENTER);
  }

  public void showMenu(Point point) {
    buildMenu();
    menu.show(jTableAttMap, (int)point.getX(), (int)point.getY());
  }

  public void buildMenu() {
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
    JMenuItem jMenuAddAttrWithSql = new JMenuItem("Add New Attributes based on SQL");
    jMenuAddAttrWithSql.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        addAttrToClassFromSQL();
      }
    });
    menu.add(jMenuAddAttrWithSql);   
  }

  public void addNewAttribute() {
    CpoNewAttributePanel cnap = new CpoNewAttributePanel(cpoAttLabNode.getProxy().getCpoSqlTypes());
    int result = JOptionPane.showConfirmDialog(this,cnap,"Add New Attribute",JOptionPane.OK_CANCEL_OPTION);
    OUT.debug ("Result: "+result);
    if (result == 0)
      model.addNewAttribute(cnap.jTextColName.getText(), cnap.jTextAtt.getText(),
          (String)cnap.jComColType.getSelectedItem(),cnap.jTextTransform.getText(),cnap.jTextDBTable.getText(),cnap.jTextDBColumn.getText());
  }

  private void addAttrToClassFromSQL() {
    CpoClassNode ccn = cpoAttLabNode.getParent();
    CpoNewClassPanel cncp = new CpoNewClassPanel();
    if (ccn.getCpoClass().getName() != null)
      cncp.jTextClassName.setText(ccn.getCpoClass().getName());
    OUT.debug(cncp.jTextClassName.getText());
    int result = JOptionPane.showConfirmDialog(this, cncp, "Add New Attributes based on SQL", JOptionPane.OK_CANCEL_OPTION);
    if (result == 0) {
      String className = cncp.jTextClassName.getText();
      if (className.lastIndexOf(".") != -1)
        cpoAttLabNode.getProxy().setDefaultPackageName(className.substring(0, className.lastIndexOf(".")));
      String sql = cncp.jTextAsql.getText();
      OUT.debug(sql);
      try {
        cpoAttLabNode.getProxy().addToAttributeMapFromSQL(model, sql);
      } catch (Exception e) {
        CpoUtil.showException(e);
      }
    } else {
      /**
       * user wishes to cancel creation
       */
      CpoUtil.updateStatus("Aborted Attribute Creation");
    }
  }

  private void removeAttribute() {
    int[] selectedRows = jTableAttMap.getSelectedRows();
    if (selectedRows.length < 1) {
      CpoUtil.showErrorMessage("Please select an attribute to remove.");
    } else {
      model.removeRows(ts.getTrueRows(selectedRows));
    }
  }
}