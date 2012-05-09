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

import org.synchronoss.cpo.meta.domain.CpoAttribute;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class CoreAttributeTableModel extends AbstractTableModel  {

  // Version Id for this class
  private static final long serialVersionUID=1L;

  protected CpoAttributeLabelNode cpoAttLabNode;

  public CoreAttributeTableModel(CpoAttributeLabelNode cpoAttLabNode) {
    this.cpoAttLabNode = cpoAttLabNode;
  }

  public List<String> getColumnNames() {
    return Arrays.asList("Attribute", "Data Name", "Data Type", "Transform Class", "Modified?");
  }

  public List<Class<?>> getColumnClasses() {
    Class<?>[] columnClasses = {String.class, String.class, String.class, String.class, String.class};
    return Arrays.asList(columnClasses);
  }

  public int getRowCount() {
    return cpoAttLabNode.getChildCount();
  }
  
  public int getColumnCount() {
    return getColumnNames().size();
  }

  @Override
  public String getColumnName(int columnIndex) {
    return getColumnNames().get(columnIndex);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return getColumnClasses().get(columnIndex);
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return (columnIndex < 4);
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    CpoAttributeNode cpoAttributeNode = (CpoAttributeNode)cpoAttLabNode.getChildAt(rowIndex);
    CpoAttribute att = cpoAttributeNode.getUserObject();

    if (columnIndex == 0) {
      return att.getJavaName();
    } else if (columnIndex == 1) {
      return att.getDataName();
    } else if (columnIndex == 2) {
      return att.getDataType();
    } else if (columnIndex == 3) {
      return att.getTransformClassName();
    } else if (columnIndex == 4) {
      if (cpoAttributeNode.isNew()) {
        return "New";
      } else if (cpoAttributeNode.isRemove()) {
        return "Removed";
      } else if (cpoAttributeNode.isDirty()) {
        return "Changed";
      } else {
        return "";
      }
    } else {
      return null;
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    String val = aValue.toString();
    CpoAttributeNode cpoAttributeNode = (CpoAttributeNode)cpoAttLabNode.getChildAt(rowIndex);
    if (columnIndex == 0) {
      cpoAttributeNode.setAttribute(val);
    } else if (columnIndex == 1) {
      cpoAttributeNode.setDataName(val);
    } else if (columnIndex == 2) {
      cpoAttributeNode.setDataType(val);
    } else if (columnIndex == 3) {
      cpoAttributeNode.setTransformClass(val);
    }
    this.fireTableDataChanged();
  }
}
