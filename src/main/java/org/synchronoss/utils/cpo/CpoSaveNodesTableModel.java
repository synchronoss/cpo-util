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
import java.util.*;

public class CpoSaveNodesTableModel extends AbstractTableModel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private CpoServerNode serverNode;
  private List<AbstractCpoNode> dontSave = new ArrayList<AbstractCpoNode>();
  private List<AbstractCpoNode> changedObjects = new ArrayList<AbstractCpoNode>();
  private String[] columnNames = {"Save","Object Type","Object","Trans Type"};
  private Object[] columnClasses = {Boolean.class, String.class, String.class, String.class};
  
  public CpoSaveNodesTableModel(CpoServerNode serverNode) {
    this.serverNode = serverNode;
    this.changedObjects = serverNode.getProxy().getAllChangedObjects();
  }
  
  public CpoServerNode getServerNode() {
    return serverNode;
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
    if (columnIndex == 0)
      return !dontSave.contains(changedObjects.get(rowIndex));
    else if (columnIndex == 1) {
      String className = changedObjects.get(rowIndex).getClass().getName();
      return className.substring(className.lastIndexOf("."));
    }
    else if (columnIndex == 2)
      return changedObjects.get(rowIndex).toString();
    else if (columnIndex == 3) {
      if ((changedObjects.get(rowIndex)).isNew())
        return "New";
      else if ((changedObjects.get(rowIndex)).isRemove())
        return "Delete";
      else if ((changedObjects.get(rowIndex)).isDirty())
        return "Update";
      else return "This shouldn't be here!";
    }
    else return null;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex == 0) {
      if (aValue instanceof Boolean) {
        Boolean newVal = (Boolean)aValue;
        if (newVal) {
          if (dontSave.contains(changedObjects.get(rowIndex)))
            dontSave.remove(changedObjects.get(rowIndex));
        } else {
          if (!dontSave.contains(changedObjects.get(rowIndex)))
            dontSave.add(changedObjects.get(rowIndex));
        }
      }
    }
    this.fireTableDataChanged();
  }

  public List<AbstractCpoNode> getSelectedNodes() {
    List<AbstractCpoNode> al = new ArrayList<AbstractCpoNode>();
    for (AbstractCpoNode node : changedObjects) {
      if (!dontSave.contains(node))
        al.add(node);
    }
    return al;
  }
}