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

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CpoAttributeMapPanel extends JPanel  {

  /** Version Id for this class. */
  private static final long serialVersionUID=1L;
  private BorderLayout borderLayout1 = new BorderLayout();
  private CpoAttributeLabelNode cpoAttLabNode;
  private JTable jTableAttMap;
  private JScrollPane jScrollTable = new JScrollPane();
  private CpoAttMapTableModel model;
  TableSorter ts;
  private JPopupMenu menu = new JPopupMenu();
  private int menuRow;
  private Logger OUT = Logger.getLogger(this.getClass());
  
  public CpoAttributeMapPanel(CpoAttributeLabelNode cpoAttLabelNode) {
    this.cpoAttLabNode = cpoAttLabelNode;
    try {
      jbInit();
    } catch (Exception e) {
      CpoUtil.showException(e);
    }
  }

  private void jbInit() throws Exception {
    model = new CpoAttMapTableModel(cpoAttLabNode);
    ts = new TableSorter(model);
    jTableAttMap = new JTable(ts);
    ts.addMouseListenerToHeaderInTable(jTableAttMap);
    jTableAttMap.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          showMenu(e.getPoint());
        }
      }
    });
    jTableAttMap.setDefaultEditor(String.class,new CpoAttMapTableEditor(cpoAttLabNode.getProxy().getCpoSqlTypes()));
    jScrollTable.getViewport().add(jTableAttMap);
    this.setLayout(borderLayout1);
    this.add(jScrollTable,BorderLayout.CENTER);
  }
  public void showMenu(Point point) {
    menuRow = jTableAttMap.rowAtPoint(point);
//    OUT.debug ("Row selected: "+menuRow);
    buildMenu();
    menu.show(jTableAttMap, (int)point.getX(), (int)point.getY());
  }
  public void buildMenu() {
    menu.removeAll();
    menu.setLabel(jTableAttMap.getValueAt(menuRow,0).toString());
    JMenuItem jMenuRemove = new JMenuItem("Remove");
    jMenuRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        model.removeRow(ts.getTrueRow(menuRow));
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
    CpoClassNode ccn = (CpoClassNode)cpoAttLabNode.getParent();
    CpoNewClassPanel cncp = new CpoNewClassPanel();
    if (ccn.getClassName() != null)
      cncp.jTextClassName.setText(ccn.getClassName());
    OUT.debug(cncp.jTextClassName.getText());
    int result = JOptionPane.showConfirmDialog(this, cncp, "Add New Attributes based on SQL", JOptionPane.OK_CANCEL_OPTION);
    if (result == 0) {
      String className = cncp.jTextClassName.getText();
      if (className.lastIndexOf(".") != -1)
        CpoUtil.setDefaultPackageName(className.substring(0, className.lastIndexOf(".")));
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
}