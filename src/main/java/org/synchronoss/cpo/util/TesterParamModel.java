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
import org.synchronoss.cpo.meta.domain.CpoAttribute;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class TesterParamModel extends AbstractTableModel {

  // Version Id for this class
  private static final long serialVersionUID = 1L;
  public static int COLUMN_PARAMETER = 4;
  public static int COLUMN_ATTRIBUTE_NAME = 3;
  private String[] columnNames = {"Function Seq Num", "Argument Seq Num", "Data Name", "Attribute Name", "Parameter"};
  private Object[] columnClasses = {Integer.class, String.class, String.class, String.class, String.class};
  private CpoFunctionGroupNode cpoFGnode;
  private Hashtable<Object, Object> parameterMap = new Hashtable<Object, Object>();
  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  public TesterParamModel(CpoFunctionGroupNode cpoQGnode) {
    this.cpoFGnode = cpoQGnode;
  }

  @Override
  public int getRowCount() {
    int rowCount = 0;
    Enumeration<CpoFunctionNode> enumQueries = cpoFGnode.children();
    while (enumQueries.hasMoreElements()) {
      CpoFunctionNode node = enumQueries.nextElement();
      rowCount = rowCount + node.getChildCount();
    }
    return rowCount;
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int columnIndex) {
    return columnNames[columnIndex];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return (Class<?>)columnClasses[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return (columnIndex == 4);
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    int rowCount = 0;
    Enumeration<CpoFunctionNode> enumQueries = cpoFGnode.children();
    while (enumQueries.hasMoreElements()) {
      CpoFunctionNode node = enumQueries.nextElement();
      Enumeration<CpoArgumentNode> enumArguments = node.children();
      while (enumArguments.hasMoreElements()) {
        CpoArgumentNode argumentNode = enumArguments.nextElement();
        if (rowCount == rowIndex) {
          if (columnIndex == 0) {
            return node.getSeqNo();
          } else if (columnIndex == 1) {
            return argumentNode.getSeqNo();
          } else if (columnIndex == 2) {
            return argumentNode.getUserObject().getAttribute().getDataName();
          } else if (columnIndex == 3) {
            return argumentNode.getUserObject().getAttribute().getJavaName();
          } else if (columnIndex == 4) {
            CpoAttribute attribute = argumentNode.getUserObject().getAttribute();
            return this.parameterMap.get(attribute != null ? attribute.getJavaName() : "");
          }
        }
        rowCount++;
      }
    }
    return null;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex == 4) {
      OUT.debug(getValueAt(rowIndex, 3) + " : " + aValue);
      if (aValue == null) {
        this.parameterMap.remove(getValueAt(rowIndex, 3));
      }

      this.parameterMap.put(getValueAt(rowIndex, 3), aValue);
      this.fireTableDataChanged();
    }
  }

  boolean isTableFilledOut() {
    int rows = getRowCount();
    for (int i = 0; i < rows; i++) {
      if (getValueAt(i, 4) == null || getValueAt(i, 4).equals("")) {
        return false;
      }
    }
    return true;
  }

  public Hashtable<Object, Object> getParameterMap() {
    return parameterMap;
  }
}