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
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class CpoAttMapTableModel extends AbstractTableModel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private String[] columnNames = {"Attribute","Column Name","Column Type","DB Table", "DB Column","Transform Class","User","Date","Modified?"};
  private Object[] columnClasses = {String.class, String.class, String.class, String.class, String.class,  String.class, String.class, Date.class, String.class};
  private CpoAttributeLabelNode cpoAttLabNode;
  private ArrayList attMap; //CpoAttributeMapNode(s)

  public CpoAttMapTableModel(CpoAttributeLabelNode cpoAttLabNode) throws Exception {
    this.cpoAttLabNode = cpoAttLabNode;
    attMap = cpoAttLabNode.getProxy().getAttributeMap(cpoAttLabNode);
  }
  public int getRowCount() {
    return this.attMap.size();
  }
  
  public int getColumnCount() {
    return columnNames.length;
  }

  public String getColumnName(int columnIndex) {
    return columnNames[columnIndex];
  }

  public Class getColumnClass(int columnIndex) {
    return (Class)columnClasses[columnIndex];
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex < 6)
      return true;
    return false;
  }
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == 0)
      return ((CpoAttributeMapNode)attMap.get(rowIndex)).getAttribute();
    else if (columnIndex == 1)
      return ((CpoAttributeMapNode)attMap.get(rowIndex)).getColumnName();
    else if (columnIndex == 2)
      return ((CpoAttributeMapNode)attMap.get(rowIndex)).getColumnType();
    else if (columnIndex == 3)
      return ((CpoAttributeMapNode)attMap.get(rowIndex)).getDbTable();
    else if (columnIndex == 4)
      return ((CpoAttributeMapNode)attMap.get(rowIndex)).getDbColumn();
    else if (columnIndex == 5)
      return ((CpoAttributeMapNode)attMap.get(rowIndex)).getTransformClass();
    else if (columnIndex == 6)
      return ((CpoAttributeMapNode)attMap.get(rowIndex)).getUserName();
    else if (columnIndex == 7)
      return ((CpoAttributeMapNode)attMap.get(rowIndex)).getCreateDate();    
    else if (columnIndex == 8) {
      if (((CpoAttributeMapNode)attMap.get(rowIndex)).isNew()) return "New";
      else if (((CpoAttributeMapNode)attMap.get(rowIndex)).isRemove()) return "Removed";
      else if (((CpoAttributeMapNode)attMap.get(rowIndex)).isDirty()) return "Changed";
      else return "";
    }
    else return null;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (aValue instanceof String) {
      CpoAttributeMapNode node = (CpoAttributeMapNode)attMap.get(rowIndex);
      if (columnIndex == 0)
        node.setAttribute((String)aValue);
      else if (columnIndex == 1)
        node.setColumnName((String)aValue);
      else if (columnIndex == 2)
        node.setColumnType((String)aValue);
      else if (columnIndex == 3)
        node.setDbTable((String)aValue);
      else if (columnIndex == 4)
        node.setDbColumn((String)aValue);
      else if (columnIndex == 5)
        node.setTransformClass((String)aValue);
    }
  }
  public void removeRow(int rowIndex) {
    ((CpoAttributeMapNode)attMap.get(rowIndex)).setRemove(true);
    this.fireTableDataChanged();
  }
  public void addNewAttribute(String columnName, String attribute, 
      String columnType, String transform, String dbTable, String dbColumn) {
    CpoClassNode ccn = (CpoClassNode)cpoAttLabNode.getParent();
    String attributeId;
    try {
      attributeId = cpoAttLabNode.getProxy().getNewGuid();
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      return;
    }
    CpoAttributeMapNode camn = new CpoAttributeMapNode(cpoAttLabNode,attributeId,
        ccn.getClassId(),columnName,attribute,columnType,transform, dbTable,dbColumn,"IN");
    attMap.add(camn);
    camn.setNew(true);
    this.fireTableDataChanged();
  }
  public boolean attributeExists(String columnName) {
    Iterator itrAttr = attMap.iterator();
    while (itrAttr.hasNext()) {
        CpoAttributeMapNode attr = (CpoAttributeMapNode)itrAttr.next();
        if (attr.getAttribute().equals(columnName)) return true;
    }
    return false;  
  }
}