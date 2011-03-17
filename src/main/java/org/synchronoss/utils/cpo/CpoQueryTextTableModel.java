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

public class CpoQueryTextTableModel extends AbstractTableModel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private CpoServerNode serverNode;
  private String[] columnNames = {"Description","SQL","UsageCount","User","Date","Modified?"};
  private Object[] columnClasses = {String.class, String.class, Integer.class, String.class, Date.class, String.class};
  private List<CpoQueryTextNode> cpoQueryText;
  private List<CpoQueryTextNode> cpoQueryTextFiltered;
  private String filter;

  public CpoQueryTextTableModel(CpoServerNode serverNode) {
    this.serverNode = serverNode;
    try {
      cpoQueryText = serverNode.getProxy().getQueryText(serverNode);
    } catch (Exception pe) {
      CpoUtil.showException(pe);
    }
  }

  public int getRowCount() {
    if (this.cpoQueryTextFiltered != null)
      return this.cpoQueryTextFiltered.size();
    
    return this.cpoQueryText.size();
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
    return false;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    List<CpoQueryTextNode> workingQueryList = cpoQueryText;
    if (cpoQueryTextFiltered != null) {
      workingQueryList = cpoQueryTextFiltered;
    }
    if (columnIndex == 0) {
      return (workingQueryList.get(rowIndex)).getDesc();
    } else if (columnIndex == 1) {
      return (workingQueryList.get(rowIndex)).getSQL();
    } else if (columnIndex == 2) {
      return (workingQueryList.get(rowIndex)).getUsageCount();
    } else if (columnIndex == 3) {
      return (workingQueryList.get(rowIndex)).getUserName();
    } else if (columnIndex == 4) {
      return (workingQueryList.get(rowIndex)).getCreateDate();      
    } else if (columnIndex == 5) {
      if ((workingQueryList.get(rowIndex)).isNew()) return "New";
      else if ((workingQueryList.get(rowIndex)).isRemove()) return "Removed";
      else if ((workingQueryList.get(rowIndex)).isDirty()) return "Changed";
      else return "";
    } else {
      return null;
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    //this.fireTableDataChanged();
  }

  public void removeRows(int[] rowIndex) {
    /**
     * this should be handled in the db, but I don't think it is, so here goes
     */
    List<CpoQueryTextNode> workingQueryList = cpoQueryText;
    if (cpoQueryTextFiltered != null) {
      workingQueryList = cpoQueryTextFiltered;
    }

    for (int i : rowIndex) {
      String textId = workingQueryList.get(i).getTextId();
      int usageCount;
      try {
        usageCount = serverNode.getProxy().getQueryTextUsageCount(textId);
      } catch (Exception pe) {
        CpoUtil.showException(pe);
        return;
      }
      if (usageCount == 0) {
        (workingQueryList.get(i)).setRemove(true);
        this.filter();
      }
      else {
        CpoUtil.showErrorMessage("You can not remove a node that has dependencies.\n"
            +"This text id is used in "+usageCount+" cpo query(ies), please change them first!");
      }
    }
  }

  public void addNewRow(String description) {
    CpoQueryTextNode cQTnode = serverNode.getProxy().addQueryText(description);
    if (cQTnode != null) {
      this.filter();
    }
  }

  public void addFilter(String filter) {
    this.filter = filter;
    this.filter();
  }

  private void filter() {
    if (this.filter == null) {
      this.removeFilter();
      return;
    }
    cpoQueryTextFiltered = new ArrayList<CpoQueryTextNode>();
    for (CpoQueryTextNode node : cpoQueryText) {
      if (node.getDesc() != null && node.getDesc().toLowerCase().indexOf(this.filter.toLowerCase()) != -1)
        cpoQueryTextFiltered.add(node);
      else if (node.getSQL() != null && node.getSQL().toLowerCase().indexOf(this.filter.toLowerCase()) != -1)
        cpoQueryTextFiltered.add(node);
    }
    this.fireTableDataChanged();
  }

  public void removeFilter() {
    this.cpoQueryTextFiltered = null;
    this.fireTableDataChanged();
  }

  public CpoQueryTextNode getQueryNodeAt(int row) {
    if (cpoQueryTextFiltered != null)
      return cpoQueryTextFiltered.get(row);
    
    return cpoQueryText.get(row);
  }
}