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

import org.synchronoss.cpo.meta.domain.CpoQueryText;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class CpoQueryTextTableModel extends AbstractTableModel  {

  /** Version Id for this class. */
  private static final long serialVersionUID=1L;
  private CpoServerNode serverNode;
  private String[] columnNames = {"Description","SQL","UsageCount","User","Date","Modified?"};
  private Object[] columnClasses = {String.class, String.class, Integer.class, String.class, String.class, String.class};
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

    CpoQueryTextNode cqtn = workingQueryList.get(rowIndex);
    CpoQueryText queryText = cqtn.getCpoQueryText();

    if (columnIndex == 0) {
      return queryText.getDescription();
    } else if (columnIndex == 1) {
      return queryText.getSqlText();
    } else if (columnIndex == 2) {
      return queryText.getRefCount();
    } else if (columnIndex == 3) {
      return queryText.getUserid();
    } else if (columnIndex == 4) {
      String createDate = "";
      if (queryText.getCreatedate() != null) {
        SimpleDateFormat df = new SimpleDateFormat();
        createDate = df.format(queryText.getCreatedate().getTime());
      }
      return createDate;
    } else if (columnIndex == 5) {
      if (cqtn.isNew()) {
        return "New";
      } else if (cqtn.isRemove()) {
        return "Removed";
      } else if (cqtn.isDirty()) {
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

    List<CpoQueryTextNode> nodesToRemove = new ArrayList<CpoQueryTextNode>();
    for (int i : rowIndex) {
      CpoQueryTextNode cqtn = workingQueryList.get(i);
      CpoQueryText text = cqtn.getCpoQueryText();
      int usageCount = text.getRefCount();
      if (usageCount == 0) {
        nodesToRemove.add(cqtn);
      } else {
        CpoUtil.showErrorMessage("You can not remove a node that has dependencies.\n" + "This text id is used in " + usageCount + " cpo query(ies), please change them first!");
      }
    }

    for (CpoQueryTextNode node : nodesToRemove) {
      node.setRemove(true);
    }
    this.filter();
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
      if (node.getDesc() != null && node.getDesc().toLowerCase().contains(this.filter.toLowerCase())) {
        cpoQueryTextFiltered.add(node);
      } else if (node.getSQL() != null && node.getSQL().toLowerCase().contains(this.filter.toLowerCase())) {
        cpoQueryTextFiltered.add(node);
      }
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