/**
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
package org.synchronoss.cpo.util.jdbc;

import org.synchronoss.cpo.util.*;

import java.util.*;

public class JdbcAttributeTableModel extends CoreAttributeTableModel {

  public JdbcAttributeTableModel(CpoAttributeLabelNode cpoAttLabNode) {
    super(cpoAttLabNode);
  }

  public List<String> getColumnNames() {
    return Arrays.asList("Attribute", "Column Name", "Column Type", "DB Table", "DB Column", "Transform Class", "Modified?");
  }

  public List<Class<?>> getColumnClasses() {
    Class<?>[] columnClasses = {String.class, String.class, String.class, String.class, String.class, String.class, String.class};
    return Arrays.asList(columnClasses);
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return (columnIndex < 6);
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    CpoAttributeNode cpoAttributeNode = (CpoAttributeNode)cpoAttLabNode.getChildAt(rowIndex);

    if (!(cpoAttributeNode instanceof JdbcAttributeNode)) {
      // if it's not a jdbc attribute, return null here...should never happen
      return null;
    }

    JdbcAttributeNode jdbcAttributeNode = (JdbcAttributeNode)cpoAttributeNode;

    if (columnIndex == 0) {
      return jdbcAttributeNode.getJavaName();
    } else if (columnIndex == 1) {
      return jdbcAttributeNode.getDataName();
    } else if (columnIndex == 2) {
      return jdbcAttributeNode.getDataType();
    } else if (columnIndex == 3) {
      return jdbcAttributeNode.getDbTable();
    } else if (columnIndex == 4) {
      return jdbcAttributeNode.getDbColumn();
    } else if (columnIndex == 5) {
      return jdbcAttributeNode.getTransformClassName();
    } else if (columnIndex == 6) {
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

    CpoAttributeNode cpoAttributeNode = (CpoAttributeNode)cpoAttLabNode.getChildAt(rowIndex);

    if (!(cpoAttributeNode instanceof JdbcAttributeNode)) {
      // if it's not a jdbc attribute, return null here...should never happen
      return;
    }

    JdbcAttributeNode jdbcAttributeNode = (JdbcAttributeNode)cpoAttributeNode;

    String val = aValue.toString();
    if (columnIndex == 0) {
      jdbcAttributeNode.setAttribute(val);
    } else if (columnIndex == 1) {
      jdbcAttributeNode.setDataName(val);
    } else if (columnIndex == 2) {
      jdbcAttributeNode.setDataType(val);
    } else if (columnIndex == 3) {
      jdbcAttributeNode.setDbTable(val);
    } else if (columnIndex == 4) {
      jdbcAttributeNode.setDbColumn(val);
    } else if (columnIndex == 5) {
      jdbcAttributeNode.setTransformClass(val);
    }
  }
}