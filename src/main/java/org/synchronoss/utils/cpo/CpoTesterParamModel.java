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
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.log4j.*;

public class CpoTesterParamModel extends AbstractTableModel {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  public static int COLUMN_PARAMETER = 4;
  public static int COLUMN_ATTRIBUTE_NAME = 3;
  private String[] columnNames = {"Query Seq Num","Param Seq Num","Column Name","Attribute Name","Parameter"};
  private Object[] columnClasses = {Integer.class, String.class, String.class, String.class, String.class};
  CpoQueryGroupNode cpoQGnode;
  Hashtable parameter = new Hashtable();
  private Category OUT = Category.getInstance(this.getClass());
  
  public CpoTesterParamModel(CpoQueryGroupNode cpoQGnode) {
    this.cpoQGnode = cpoQGnode;
  }
  public int getRowCount() {
    int rowCount = 0;
    Enumeration enumQueries = cpoQGnode.children();
    while (enumQueries.hasMoreElements()) {
      CpoQueryNode node = (CpoQueryNode)enumQueries.nextElement();
      Enumeration enumQueryParams = node.children();
      while (enumQueryParams.hasMoreElements()) {
        CpoQueryParameterNode qpNode = (CpoQueryParameterNode)enumQueryParams.nextElement();
        rowCount++;
      }
    }
    return rowCount;
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
    if (columnIndex == 4)
      return true;
    else
      return false;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    int rowCount = 0;
    Enumeration enumQueries = cpoQGnode.children();
    while (enumQueries.hasMoreElements()) {
      CpoQueryNode node = (CpoQueryNode)enumQueries.nextElement();
      Enumeration enumQueryParams = node.children();
      while (enumQueryParams.hasMoreElements()) {
        CpoQueryParameterNode qpNode = (CpoQueryParameterNode)enumQueryParams.nextElement();
        if (rowCount == rowIndex) {
          if (columnIndex == 0) {
            return new Integer(node.getSeqNo());
          } else if (columnIndex == 1) {
            return new Integer(qpNode.getSeqNo());
          } else if (columnIndex == 2) {
            return qpNode.getCpoAttributeMapBean()!=null?qpNode.getCpoAttributeMapBean().getColumnName():null;
          } else if (columnIndex == 3) {
            return qpNode.getCpoAttributeMapBean()!=null?qpNode.getCpoAttributeMapBean().getAttribute():null;
          } else if (columnIndex == 4) {
            return this.parameter.get(qpNode.getCpoAttributeMapBean()!=null?qpNode.getCpoAttributeMapBean().getAttribute():"");
          }
        }
        rowCount++;
      }
    }
    return null;
  }
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex == 4) {
      OUT.debug(getValueAt(rowIndex,3)+" : "+aValue);
      if (aValue == null) this.parameter.remove(getValueAt(rowIndex,3));
      else this.parameter.put(getValueAt(rowIndex,3),aValue);
      this.fireTableDataChanged();
    }
  }
  
  boolean isTableFilledOut() {
    int rows = getRowCount();
    for (int i = 0 ; i < rows ; i++) {
      if (getValueAt(i,4) == null || getValueAt(i,4).equals(""))
        return false;
    }
    return true;
  }
}