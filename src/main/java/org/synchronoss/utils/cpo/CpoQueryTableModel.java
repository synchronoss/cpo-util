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
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;

public class CpoQueryTableModel extends AbstractTableModel {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private CpoQueryNode cpoQueryNode;
  private List<CpoQueryParameterNode> cpoQPBs; //CpoQueryParameterNode(s)
  private String[] columnNames = {"Seq Num","Attribute","Column Name","Column Type","In/Out/Both","DB Table", "DB Column","Get Transform Class","User","Date","Changed?"};
  private Object[] columnClasses = {String.class, CpoAttributeMapNode.class, String.class, String.class, JComboBox.class, String.class, String.class, String.class, String.class, Date.class, String.class};
  CpoClassNode attributeCpoClassNode;
  private Logger OUT = Logger.getLogger(this.getClass());
  
  public CpoQueryTableModel(CpoQueryNode cpoQueryNode) throws Exception {
    this.cpoQueryNode = cpoQueryNode;
    this.cpoQPBs = this.cpoQueryNode.getProxy().getQueryParameters(cpoQueryNode);
    if (this.cpoQPBs.size() > 0)
//      this.attributeCpoClassNode = (CpoClassNode)((CpoQueryParameterNode)this.cpoQPBs.get(0)).getParent().getParent().getParent().getParent();
      this.attributeCpoClassNode = (CpoClassNode)(this.cpoQPBs.get(0)).getCpoAttributeMapBean().getParent().getParent();
    else
      this.attributeCpoClassNode = (CpoClassNode)cpoQueryNode.getParent().getParent().getParent();
    
    for (CpoQueryParameterNode qpbs : cpoQPBs) {
      OUT.debug("qpbs: " + qpbs);
    }
    OUT.debug("ClassNode: "+attributeCpoClassNode);
//    OUT.debug ("Size of QPBs for queryNode: "+cpoQueryNode+" is "+cpoQPBs.size());
  }

  public int getRowCount() {
    return this.cpoQPBs.size();
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
    return (columnIndex == 1 || columnIndex==4);
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == 0)
      return new Integer((cpoQPBs.get(rowIndex)).getSeqNo());
    else if (columnIndex == 1)
      return (cpoQPBs.get(rowIndex)).getCpoAttributeMapBean();
    else if (columnIndex == 2)
      return (cpoQPBs.get(rowIndex)).getCpoAttributeMapBean()!=null
          ?(cpoQPBs.get(rowIndex)).getCpoAttributeMapBean().getColumnName():null;
    else if (columnIndex == 3)
      return (cpoQPBs.get(rowIndex)).getCpoAttributeMapBean()!=null
          ?(cpoQPBs.get(rowIndex)).getCpoAttributeMapBean().getColumnType():null;
    else if (columnIndex == 4)
      return (cpoQPBs.get(rowIndex)).getType();
    else if (columnIndex == 5)
      return (cpoQPBs.get(rowIndex)).getCpoAttributeMapBean()!=null
          ?(cpoQPBs.get(rowIndex)).getCpoAttributeMapBean().getDbTable():null;
    else if (columnIndex == 6)
      return (cpoQPBs.get(rowIndex)).getCpoAttributeMapBean()!=null
          ?(cpoQPBs.get(rowIndex)).getCpoAttributeMapBean().getDbColumn():null;
    else if (columnIndex == 7)
      return (cpoQPBs.get(rowIndex)).getCpoAttributeMapBean()!=null
          ?(cpoQPBs.get(rowIndex)).getCpoAttributeMapBean().getTransformClass():null;
    else if (columnIndex == 8)
      return (cpoQPBs.get(rowIndex)).getCpoAttributeMapBean()!=null
          ?(cpoQPBs.get(rowIndex)).getCpoAttributeMapBean().getUserName():null;
    else if (columnIndex == 9)
      return (cpoQPBs.get(rowIndex)).getCpoAttributeMapBean()!=null
          ?(cpoQPBs.get(rowIndex)).getCpoAttributeMapBean().getCreateDate():null;
    else if (columnIndex == 10)
      if ((cpoQPBs.get(rowIndex)).isNew()) return "New";
      else if ((cpoQPBs.get(rowIndex)).isRemove()) return "Removed";
      else if (cpoQPBs.get(rowIndex).isDirty()) return "Changed";
      else return "";
    else return null;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex == 1) {
      cpoQPBs.get(rowIndex).setCpoAttributeMap((CpoAttributeMapNode)aValue);
    }
    if (columnIndex == 4) {
      cpoQPBs.get(rowIndex).setType((String)aValue);
    }
    this.fireTableDataChanged();
  }
  public void removeNewRow() {
    int originalLength = this.getNonRemovedRows();
    ListIterator<CpoQueryParameterNode> iter = this.cpoQPBs.listIterator(this.cpoQPBs.size());
    while (iter.hasPrevious()) {
      CpoQueryParameterNode cqpn = iter.previous();
      if (cqpn.isNew()) {
//        this.cpoQPBs.remove(cqpn);
        cqpn.setRemove(true);
//        cpoQueryNode.getProxy().removeObjectFromAllCache(cqpn);
        this.fireTableDataChanged();
        break;
      }
    }
    if (this.getNonRemovedRows() == originalLength) {
      for (int i = this.cpoQPBs.size()-1 ; i >= 0 ; i--) {
        if (!(this.cpoQPBs.get(i)).isRemove()) {
          this.removeRow(i);
          break;
        }
      }
    }
  }
  public void removeRow(int rowIndex) {
    (this.cpoQPBs.get(rowIndex)).setRemove(true);
    this.fireTableDataChanged();
  }
  public void addNewRow() {
    int originalLength = this.getNonRemovedRows();
    for (CpoQueryParameterNode cqpn : cpoQPBs) {
      if (cqpn.isRemove()) {
        cqpn.setRemove(false);
        this.fireTableDataChanged();
        break;
      }
    }
    if (this.getNonRemovedRows() == originalLength) {
      try {
        CpoQueryParameterNode cqpn = new CpoQueryParameterNode(cpoQueryNode,
            this.cpoQPBs.size()+1,
//            (CpoAttributeMapNode)cpoQueryNode.getProxy().getAttributeMap((CpoClassNode)cpoQueryNode.getParent().getParent().getParent()).get(0));
            cpoQueryNode.getProxy().getAttributeMap(this.attributeCpoClassNode).get(0),
            "IN");
        cqpn.setNew(true);
        this.addRow(cqpn);
      } catch (Exception pe) {
        CpoUtil.showException(pe);
      }
    }
  }
  public void addRow(CpoQueryParameterNode cpoQPN) {
    this.cpoQPBs.add(cpoQPN);
    this.fireTableDataChanged();
  }
  public int getNonRemovedRows() {
    int count=0;
    for (CpoQueryParameterNode cqpn : cpoQPBs) {
      if (!cqpn.isRemove()) {
        count++;
      }
    }
    return count;
  }
}