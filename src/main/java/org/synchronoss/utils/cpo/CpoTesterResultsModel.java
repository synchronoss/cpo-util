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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Category;

public class CpoTesterResultsModel extends AbstractTableModel {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private Collection results;
  private ArrayList cpoAttMap; //CpoAttributeMapNode(s)
  private int columnCount;
  private String[] columnNames;
  private Category OUT = Category.getInstance(this.getClass());
  
  public CpoTesterResultsModel(Collection results, CpoClassNode cpoClassNode) throws Exception {
    this.results = results;
    this.cpoAttMap = cpoClassNode.getProxy().getAttributeMap(cpoClassNode);
  }
  public int getRowCount() {
//    OUT.debug ("Getting row size");
    return results.size();
  }
  public int getColumnCount() {
//    OUT.debug ("Getting column count");
    if (this.columnCount != 0) return this.columnCount;
    Iterator iter = cpoAttMap.iterator();
    while (iter.hasNext()) {
      iter.next();
      this.columnCount++;
    }
    return this.columnCount;
  }

  public String getColumnName(int columnIndex) {
//    OUT.debug ("Getting column name: "+columnIndex);
    if (this.columnNames != null) return this.columnNames[columnIndex];
    this.columnNames = new String[getColumnCount()];
    int columnCounter = 0;
    Iterator iter = cpoAttMap.iterator();
    while (iter.hasNext()) {
      CpoAttributeMapNode camn = (CpoAttributeMapNode)iter.next();
      columnNames[columnCounter] = camn.getAttribute();
      columnCounter++;
    }
    return this.columnNames[columnIndex];
  }

  public Class getColumnClass(int columnIndex) {
//    OUT.debug ("Getting return type for column: "+columnIndex);
    String columnName = getColumnName(columnIndex);
    String methodName = "get"+columnName.substring(0,1).toUpperCase()+columnName.substring(1);
//    OUT.debug("getting return type for method: "+methodName);
    Iterator iter = results.iterator();
    if (iter.hasNext()) {
      Object obj = iter.next();
      Method[] methods = obj.getClass().getMethods();
      for (int i = 0 ; i < methods.length ; i++) {
        if (methods[i].getName().equals(methodName) && methods[i].getParameterTypes().length == 0) {
          try {
//            OUT.debug(methods[i].getReturnType());
            return methods[i].getReturnType();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    OUT.debug("Did not find column class you are looking for - returning String.class!");
    return String.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
//    OUT.debug ("Getting value at: "+rowIndex+","+columnIndex);
    int row = 0;
    String columnName = getColumnName(columnIndex);
    String methodName = "get"+columnName.substring(0,1).toUpperCase()+columnName.substring(1);
//    OUT.debug("getting return type for method: "+methodName);
    Object rowObj = null;
    Iterator iter = results.iterator();
    while (iter.hasNext()) {
      Object obj = iter.next();
      if (row == rowIndex) {
        rowObj = obj;
        Method[] methods = obj.getClass().getMethods();
        for (int i = 0 ; i < methods.length ; i++) {
          if (methods[i].getName().equals(methodName) && methods[i].getParameterTypes().length == 0) {
            try {
//              OUT.debug(methods[i].invoke(obj,null));
              return methods[i].invoke(obj,null);
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
    return rowObj==null?"":rowObj.toString();
  }
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
  }
}