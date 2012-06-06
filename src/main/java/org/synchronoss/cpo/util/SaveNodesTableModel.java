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

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class SaveNodesTableModel extends AbstractTableModel {

  // Version Id for this class
  private static final long serialVersionUID = 1L;
  private List<AbstractCpoNode> changedObjects;
  private String[] columnNames = {"Object Type", "Object", "Trans Type"};
  private Object[] columnClasses = {String.class, String.class, String.class};

  public SaveNodesTableModel(CpoRootNode rootNode) {
    changedObjects = rootNode.getChangedNodes();
  }

  public int getRowCount() {
    return this.changedObjects.size();
  }

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
    return (columnIndex == 0);
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == 0) {
      String className = changedObjects.get(rowIndex).getClass().getName();
      return className.substring(className.lastIndexOf(".") + 1);
    } else if (columnIndex == 1) {
      return changedObjects.get(rowIndex).toString();
    } else if (columnIndex == 2) {
      if ((changedObjects.get(rowIndex)).isNew()) {
        return "New";
      } else if ((changedObjects.get(rowIndex)).isRemove()) {
        return "Delete";
      } else if ((changedObjects.get(rowIndex)).isDirty()) {
        return "Update";
      } else {
        return "This shouldn't be here!";
      }
    } else {
      return null;
    }
  }
}