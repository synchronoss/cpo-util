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

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Method;
import java.util.*;

public class CpoTesterResultsModel extends AbstractTableModel {

  /** Version Id for this class. */
  private static final long serialVersionUID=1L;
  
  private Collection<?> results;
  private List<CpoAttributeMapNode> cpoAttMap; //CpoAttributeMapNode(s)
  private String[] columnNames;
  private Logger OUT = Logger.getLogger(this.getClass());
  
  public CpoTesterResultsModel(Collection<?> results, CpoClassNode cpoClassNode) throws Exception {
    this.results = results;
    this.cpoAttMap = cpoClassNode.getProxy().getAttributeMap(cpoClassNode);
  }
  
  public int getRowCount() {
    return results.size();
  }

  public int getColumnCount() {
    if (cpoAttMap == null)
      return 0;
    
    return cpoAttMap.size();
  }

  @Override
  public String getColumnName(int columnIndex) {
    if (this.columnNames != null) return this.columnNames[columnIndex];
    this.columnNames = new String[getColumnCount()];
    int columnCounter = 0;
    for (CpoAttributeMapNode camn : cpoAttMap) {
      columnNames[columnCounter] = camn.getAttribute();
      columnCounter++;
    }
    return this.columnNames[columnIndex];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    /**
     * Using the return type causes major issues when the return type is a byte[] or a char[]
     * We can cheat a little and just say it's always going to be a string
    String columnName = getColumnName(columnIndex);
    String methodName = "get"+columnName.substring(0,1).toUpperCase()+columnName.substring(1);
    for (Object obj : results) {
      for (Method method : obj.getClass().getMethods()) {
        if (method.getName().equals(methodName) && method.getParameterTypes().length == 0) {
          try {
            return method.getReturnType();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    OUT.debug("Did not find column class you are looking for - returning String.class!");
    **/
    return String.class;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    int row = 0;
    String columnName = getColumnName(columnIndex);
    String methodName = "get"+columnName.substring(0,1).toUpperCase()+columnName.substring(1);

    Object rowObj = null;
    for (Object obj : results) {
      if (row == rowIndex) {
        rowObj = obj;
        for (Method method : rowObj.getClass().getMethods()) {
          if (method.getName().equals(methodName) && method.getParameterTypes().length == 0) {
            try {
              Object returnObj = method.invoke(rowObj);
              return returnObj == null ? "" : returnObj.toString();
            } catch (Exception e) {
              e.printStackTrace();
              return e.getMessage();
            }
          }
        }
      }
      row++;
    }
    OUT.debug("Did not find column/row you are looking for - trying to return toString!");
    return rowObj == null ? "" : rowObj.toString();
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
  }
}