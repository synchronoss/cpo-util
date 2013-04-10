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
import org.synchronoss.cpo.*;
import org.synchronoss.cpo.core.cpoCoreMeta.StFunctionGroupType;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.math.*;
import java.sql.Timestamp;
import java.text.*;
import java.util.*;

public class TesterPanel extends JPanel implements ClipboardOwner {

  // Version Id for this class.
  private static final long serialVersionUID = 1L;

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SS");
  private static SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd");

  private static final int COLUMN_PARAMETER = 4;
  private static final int COLUMN_ATTRIBUTE_NAME = 3;

  private JComboBox connectionComboBox = new JComboBox();
  private JComboBox jComFunctionGroup = new JComboBox();
  private JTable jTableParam = new JTable();
  private JButton jButExec = new JButton();
  private JCheckBox jCheckPersist = new JCheckBox();
  private JComboBox jComClassOut = new JComboBox();
  private JTable jTableResults = new JTable();

  private CpoClassNode cpoClassNode;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public TesterPanel(CpoClassNode cpoClassNode) {
    this.cpoClassNode = cpoClassNode;

    try {
      connectionComboBox.setModel(new DefaultComboBoxModel(cpoClassNode.getProxy().getConnectionList()));
      jbInit();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    // populate the list
    Enumeration enumFG = cpoClassNode.getFunctionGroupLabelNode().children();
    while (enumFG.hasMoreElements()) {
      jComFunctionGroup.addItem(enumFG.nextElement());
    }

    setTableCellSize();
  }

  /**
   * Creates the panel
   */
  private void jbInit() throws Exception {
    this.setLayout(new GridBagLayout());

    JLabel jLabConnction = new JLabel("Connection:");
    this.add(jLabConnction, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    this.add(connectionComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    jButExec.setText("Execute");
    jButExec.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeFunctionGroup();
      }
    });
    this.add(jButExec, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JLabel jLabFunctionGroup = new JLabel("Function Group:");
    this.add(jLabFunctionGroup, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jComFunctionGroup.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        functionGroupActionPerformed();
      }
    });
    this.add(jComFunctionGroup, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    jCheckPersist.setText("Persist");
    jCheckPersist.setEnabled(false);
    jCheckPersist.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkPersistActionPerformed();
      }
    });
    this.add(jCheckPersist, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JLabel jLabClassOut = new JLabel("Output Class: ");
    this.add(jLabClassOut, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    this.add(jComClassOut, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    jTableParam.setPreferredScrollableViewportSize(new Dimension(600, 200));
    jTableParam.setCellSelectionEnabled(true);
    jTableParam.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.isMetaDown()) {
          showMenu(e.getPoint());
        }
      }
    });

    JScrollPane jScrollParam = new JScrollPane();
    jScrollParam.getViewport().add(jTableParam, null);
    this.add(jScrollParam, new GridBagConstraints(0, 3, 4, 1, 0.5, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    jTableResults.setPreferredScrollableViewportSize(new Dimension(300, 300));
    jTableResults.setCellSelectionEnabled(true);
    jTableResults.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        keyTypedEvent(e);
      }
    });
    jTableResults.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.isMetaDown()) {
          showSouthMenu(e.getPoint());
        }
      }
    });

    JScrollPane jScrollResults = new JScrollPane();
    jScrollResults.getViewport().add(jTableResults, null);
    this.add(jScrollResults, new GridBagConstraints(0, 4, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Executes the selected function group
   */
  private void executeFunctionGroup() {
    if (jTableParam.isEditing()) {
      int row = jTableParam.getEditingRow();
      int column = jTableParam.getEditingColumn();
      jTableParam.getCellEditor(row, column).stopCellEditing();
    }

    CpoUtil.getInstance().setStatusBarText("");
    if (!((TesterParamModel)jTableParam.getModel()).isTableFilledOut()) {
      int result = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), "You need to completely fill out the Parameter values in the table before executing a Function Group!\nContinue Anyway or CANCEL?", "User Error", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.CANCEL_OPTION) {
        return;
      }
    }

    // find classname
    String cpoClassName = cpoClassNode.getUserObject().getName();
    String cpoClassNameReturnType = cpoClassName;
    CpoClassNode returnClassNode = cpoClassNode;
    CpoFunctionGroupNode cpoFunctionGroupNode = getSelectedCpoFunctionGroupNode();
    if (cpoFunctionGroupNode.getType().equals(StFunctionGroupType.LIST.toString())) {
      returnClassNode = (CpoClassNode)jComClassOut.getSelectedItem();
      cpoClassNameReturnType = returnClassNode.getUserObject().getName();
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Class name tester will use: " + cpoClassName + " and " + cpoClassNameReturnType + " as the return type class");
    }
    try {
      Class<?> cpoClass = CpoUtilClassLoader.getInstance(this.getClass().getClassLoader()).loadClass(cpoClassName);
      Object cpoObject = cpoClass.newInstance();
      Object cpoObjectReturnType = CpoUtilClassLoader.getInstance(this.getClass().getClassLoader()).loadClass(cpoClassNameReturnType).newInstance();
      Method[] methods = cpoClass.getMethods();
      TesterParamModel ctpm = (TesterParamModel)jTableParam.getModel();

      Hashtable<Object, Object> parameterMap = ctpm.getParameterMap();
      for (Object obj : parameterMap.keySet()) {
        String key = (String)obj;
        String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
        if (logger.isDebugEnabled()) {
          logger.debug("Method name looking for: " + methodName);
        }
        boolean found = false;
        for (Method method : methods) {
          if (method.getName().equals(methodName)) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1) {
              continue;
            }
            method.invoke(cpoObject, getMethObjFrStr(paramTypes[0], (String)parameterMap.get(key)));
            found = true;
          }
        }
        if (!found) {
          throw new CpoException("Could not find method: " + methodName);
        }
      }
      Collection<?> result = cpoClassNode.getProxy().executeFunctionGroup((String)connectionComboBox.getSelectedItem(), cpoObject, cpoObjectReturnType, cpoFunctionGroupNode.getUserObject(), jCheckPersist.isSelected());
      TableSorter ts = new TableSorter(new TesterResultsModel(result, returnClassNode));
      this.jTableResults.setModel(ts);
      ts.addMouseListenerToHeaderInTable(this.jTableResults);
      CpoUtil.getInstance().setStatusBarText("Function Executed! Rows Returned: " + result.size());
      setColumnSizes(200);
    } catch (NoClassDefFoundError cdfe) {
      CpoUtil.showException(cdfe);
      CpoUtil.getInstance().setCustomClasspath("Need path to: web-common, wls-startup and log4j!");
    } catch (Exception ex) {
      Throwable t = ex;
      while (t != null) {
        logger.error(ex.getMessage(), ex);
        t = t.getCause();
      }
      CpoUtil.showException(ex);
    }
  }

  private void setColumnSizes(int columnSize) {
    this.jTableResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    for (int i = 0; i < this.jTableResults.getColumnCount(); i++) {
      this.jTableResults.getColumnModel().getColumn(i).setPreferredWidth(columnSize);
    }
  }

  private Object getMethObjFrStr(Class<?> cls, String param) throws ParseException {
    if (param.equalsIgnoreCase("null") || param.equals("")) {
      return null;
    } else if (param.equalsIgnoreCase("guid")) {
      return GUID.getGUID();
    }
    try {
      if (cls == String.class) {
        return param;
      } else if (cls == int.class) {
        return Integer.valueOf(param);
      } else if (cls == Integer.class) {
        return Integer.valueOf(param);
      } else if (cls == float.class) {
        return Float.valueOf(param);
      } else if (cls == Float.class) {
        return Float.valueOf(param);
      } else if (cls == BigDecimal.class) {
        return new BigDecimal(param);
      } else if (cls == BigInteger.class) {
        return new BigInteger(param);
      } else if (cls == Date.class) {
        logger.debug("Found type Date");
        if (param.equalsIgnoreCase("sysdate")) {
          return new Date();
        }
        if (param.length() == 10) {
          return sdfDateOnly.parse(param);
        }
        return sdf.parse(param);
      } else if (cls == java.sql.Date.class) {
        logger.debug("Found type sql Date");
        if (param.equalsIgnoreCase("sysdate")) {
          return new java.sql.Date(new Date().getTime());
        }
        if (param.length() == 10) {
          return new java.sql.Date(sdfDateOnly.parse(param).getTime());
        }
        return new java.sql.Date(sdf.parse(param).getTime());
      } else if (cls == Timestamp.class) {
        logger.debug("Found type Timestamp");
        if (param.equalsIgnoreCase("sysdate")) {
          return new Timestamp(new Date().getTime());
        }
        if (param.length() == 10) {
          return new Timestamp(sdfDateOnly.parse(param).getTime());
        }
        return new Timestamp(sdf.parse(param).getTime());
      } else if (cls == byte[].class) {
        return param.getBytes();
      }
    } catch (ParseException e) {
      logger.error(e.getMessage(), e);
      throw e;
    }
    CpoUtil.showErrorMessage("Could not locate the type for param: " + param + " which is class: " + cls);
    return null;
  }

  private void keyTypedEvent(KeyEvent e) {
    int[] selectedRows = this.jTableResults.getSelectedRows();
    int[] selectedCols = this.jTableResults.getSelectedColumns();
    if (selectedRows.length < 1 || selectedCols.length < 1) {
      return;
    }
    Object valueO = this.jTableResults.getValueAt(selectedRows[0], selectedCols[0]);
    if (valueO == null) {
      return;
    }
    String value = valueO.toString();
    if ((e.getKeyCode() == KeyEvent.VK_COPY) ||
        ((e.getKeyCode() == KeyEvent.VK_C) && e.isControlDown()) ||
        ((e.getKeyCode() == KeyEvent.VK_C) && e.isMetaDown()) ||
        e.getKeyChar() == 3) {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(new StringSelection(value), this);
    }
  }

  @Override
  public void lostOwnership(Clipboard clip, Transferable trans) {
    logger.debug("Boohoo ...  clipboard lost ownership ...");
  }

  private void insertValueAt(String value) {
    int[] selectedRows = jTableParam.getSelectedRows();
    if (selectedRows.length != 1) {
      CpoUtil.getInstance().setStatusBarText("Select ONE Row");
      return;
    }
    jTableParam.setValueAt(value, selectedRows[0], COLUMN_PARAMETER);
  }

  private void insertValuesFromResults() {
    int[] selectedRows = this.jTableResults.getSelectedRows();
    if (selectedRows.length != 1) {
      CpoUtil.getInstance().setStatusBarText("Select ONE Row");
      return;
    }
    for (int i = 0; i < this.jTableResults.getColumnCount(); i++) {
      String setterName = this.jTableResults.getColumnName(i);
      int setColumn = getParamRowForSetter(setterName);
      if (setColumn != -1) {
        String value = this.jTableResults.getValueAt(selectedRows[0], i) == null ? null : this.jTableResults.getValueAt(selectedRows[0], i).toString();
        jTableParam.setValueAt(value, setColumn, COLUMN_PARAMETER);
      }
    }
  }

  private int getParamRowForSetter(String setter) {
    int returnVal = -1;
    for (int i = 0; i < jTableParam.getRowCount(); i++) {
      if (jTableParam.getValueAt(i, COLUMN_ATTRIBUTE_NAME).equals(setter)) {
        returnVal = i;
      }
    }
    return returnVal;
  }

  private void showMenu(Point p) {
    JPopupMenu menu = new JPopupMenu();
    menu.setLabel("Tester Menu");

    JMenuItem jMenuSetSysdate = new JMenuItem("Set Sysdate");
    jMenuSetSysdate.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        insertValueAt(sdf.format(new Date()));
      }
    });
    menu.add(jMenuSetSysdate);

    JMenuItem jMenuSetNewGuid = new JMenuItem("Set New Guid");
    jMenuSetNewGuid.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        insertValueAt(GUID.getGUID());
      }
    });
    menu.add(jMenuSetNewGuid);

    menu.show(jTableParam, (int)p.getX(), (int)p.getY());
  }

  private void showSouthMenu(Point p) {
    JPopupMenu menu = new JPopupMenu();
    menu.setLabel("Results Menu");

    JMenuItem jMenuPopulateNorth = new JMenuItem("Populate Function");
    jMenuPopulateNorth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        insertValuesFromResults();
      }
    });
    menu.add(jMenuPopulateNorth);

    menu.show(jTableResults, (int)p.getX(), (int)p.getY());
  }

  private void setTableCellSize() {
    if (jComFunctionGroup.getSelectedItem() != null) {
      jTableParam.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      jTableParam.getColumnModel().getColumn(0).setPreferredWidth(45);
      jTableParam.getColumnModel().getColumn(1).setPreferredWidth(45);
      jTableParam.getColumnModel().getColumn(2).setPreferredWidth(112);
      jTableParam.getColumnModel().getColumn(3).setPreferredWidth(112);
      jTableParam.getColumnModel().getColumn(4).setPreferredWidth(290);
    }
  }

  private void functionGroupActionPerformed() {
    if (((CpoFunctionGroupNode)jComFunctionGroup.getSelectedItem()).getType().equals(StFunctionGroupType.EXIST.toString())) {
      jCheckPersist.setEnabled(true);
    } else {
      jCheckPersist.setSelected(false);
      jCheckPersist.setEnabled(false);
    }
    if (((CpoFunctionGroupNode)jComFunctionGroup.getSelectedItem()).getType().equals(StFunctionGroupType.LIST.toString())) {
      CpoFunctionGroupNode cqgn = (CpoFunctionGroupNode)jComFunctionGroup.getSelectedItem();
      try {
        CpoRootNode cpoRootNode = cqgn.getParent().getParent().getParent();
        if (logger.isDebugEnabled()) {
          logger.debug("about to get classes for: " + cpoRootNode);
        }
        jComClassOut.removeAllItems();
        Enumeration children = cpoRootNode.children();
        while (children.hasMoreElements()) {
          jComClassOut.addItem(children.nextElement());
        }
        jComClassOut.setSelectedItem(cqgn.getParent().getParent());
      } catch (Exception ex) {
        CpoUtil.showException(ex);
      }
      jComClassOut.setEnabled(true);
    } else {
      jComClassOut.setEnabled(false);
    }
    jTableParam.setModel(new TesterParamModel((CpoFunctionGroupNode)jComFunctionGroup.getSelectedItem()));
    setTableCellSize();
  }

  private void checkPersistActionPerformed() {
    if (jCheckPersist.isSelected()) {
      try {
        CpoFunctionGroupNode selectedNode = (CpoFunctionGroupNode)jComFunctionGroup.getSelectedItem();

        CpoFunctionGroupNode node = getFunctionGroupNode(selectedNode.getGroupName(), StFunctionGroupType.CREATE.toString());

        if (node == null) {
          node = getFunctionGroupNode(selectedNode.getGroupName(), StFunctionGroupType.UPDATE.toString());
        }
        if (node == null) {
          JOptionPane.showMessageDialog(CpoUtil.getInstance(), "Can't locate a 'CREATE' or 'UPDATE' CPO Type with the Group Name of '" + selectedNode.getGroupName() + "'", "Sorry, no go", JOptionPane.INFORMATION_MESSAGE);
          jCheckPersist.setSelected(false);
          return;
        }
        jTableParam.setModel(new TesterParamModel(node));
      } catch (Exception ex) {
        CpoUtil.showException(ex);
      }
    } else {
      jTableParam.setModel(new TesterParamModel(getSelectedCpoFunctionGroupNode()));
    }
    setTableCellSize();
  }

  private CpoFunctionGroupNode getFunctionGroupNode(String groupName, String type) {
    Enumeration e = cpoClassNode.getFunctionGroupLabelNode().children();
    while (e.hasMoreElements()) {
      CpoFunctionGroupNode node = (CpoFunctionGroupNode)e.nextElement();
      if (((groupName == null && node.getGroupName() == null) || // the names are both null
           node.getGroupName() != null && groupName != null && node.getGroupName().equals(groupName) // the names match
          ) && node.getType().equals(type)) { // type has to match
        return node;
      }
    }
    return null;
  }

  private CpoFunctionGroupNode getSelectedCpoFunctionGroupNode() {
    return (CpoFunctionGroupNode)jComFunctionGroup.getSelectedItem();
  }
}
