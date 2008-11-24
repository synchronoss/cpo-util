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
  private List cpoQueryText;
  private List cpoQueryTextFiltered;
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
    else
      return this.cpoQueryText.size();
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
    return false;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    List workingQueryList = cpoQueryText;
    if (cpoQueryTextFiltered != null) {
      workingQueryList = cpoQueryTextFiltered;
    }
    if (columnIndex == 0)
      return ((CpoQueryTextNode)workingQueryList.get(rowIndex)).getDesc();
    else if (columnIndex == 1)
      return ((CpoQueryTextNode)workingQueryList.get(rowIndex)).getSQL();
    else if (columnIndex == 2)
      return new Integer(((CpoQueryTextNode)workingQueryList.get(rowIndex)).getUsageCount());
    else if (columnIndex == 3)
      return ((CpoQueryTextNode)workingQueryList.get(rowIndex)).getUserName();
    else if (columnIndex == 4)
      return ((CpoQueryTextNode)workingQueryList.get(rowIndex)).getCreateDate();      
    else if (columnIndex == 5) {
      if (((CpoQueryTextNode)workingQueryList.get(rowIndex)).isNew()) return "New";
      else if (((CpoQueryTextNode)workingQueryList.get(rowIndex)).isRemove()) return "Removed";
      else if (((CpoQueryTextNode)workingQueryList.get(rowIndex)).isDirty()) return "Changed";
      else return "";
    }
    else return null;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
//    this.fireTableDataChanged();
  }

  public void removeRows(int[] rowIndex) {
    /**
     * this should be handled in the db, but I don't think it is, so here goes
     */
    List workingQueryList = cpoQueryText;
    if (cpoQueryTextFiltered != null) {
      workingQueryList = cpoQueryTextFiltered;
    }
    for (int i = 0 ; i < rowIndex.length ; i++) {
      String textId = ((CpoQueryTextNode)workingQueryList.get(rowIndex[i])).getTextId();
      int usageCount;
      try {
        usageCount = serverNode.getProxy().getQueryTextUsageCount(textId);
      } catch (Exception pe) {
        CpoUtil.showException(pe);
        return;
      }
      if (usageCount == 0) {
        ((CpoQueryTextNode)workingQueryList.get(rowIndex[i])).setRemove(true);
        this.filter();
      }
      else {
        CpoUtil.showException(new Exception("You can not remove a node that has dependencies.\n"
            +"This text id is used in "+usageCount+" cpo query(ies), please change them first!"));
      }
    }
  }

  public void addNewRow(String description) {
    String textId;
    try {
      textId = serverNode.getProxy().getNewGuid();
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      return;
    }
    CpoQueryTextNode cQTnode = new CpoQueryTextNode(textId, "", description, serverNode.getProxy().getCpoQueryTextLabelNode(serverNode));
    cpoQueryText.add(cQTnode);
    cQTnode.setNew(true);
    this.filter();
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
    cpoQueryTextFiltered = new ArrayList();
    Iterator queryIt = cpoQueryText.iterator();
    while (queryIt.hasNext()) {
      CpoQueryTextNode node = (CpoQueryTextNode)queryIt.next();
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
      return (CpoQueryTextNode)cpoQueryTextFiltered.get(row);
    else
      return (CpoQueryTextNode)cpoQueryText.get(row);
  }
}