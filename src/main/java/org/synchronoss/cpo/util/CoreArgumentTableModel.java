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

import org.synchronoss.cpo.meta.domain.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;

public class CoreArgumentTableModel extends AbstractTableModel {

  // Version Id for this class
  private static final long serialVersionUID = 1L;
  protected CpoFunctionNode cpoFunctionNode;

  public CoreArgumentTableModel(CpoFunctionNode cpoFunctionNode) {
    this.cpoFunctionNode = cpoFunctionNode;
  }

  public void initTableEditors(JTable table) {
    table.setDefaultEditor(CpoAttribute.class, new CpoArgumentAttributeEditor(this));
  }

  public CpoFunctionNode getCpoFunctionNode() {
    return cpoFunctionNode;
  }

  public List<String> getColumnNames() {
    return Arrays.asList("Seq Num", "Attribute", "Data Name", "Data Type", "Transform Class", "Changed?");
  }

  public List<Class<?>> getColumnClasses() {
    Class<?>[] columnClasses = {String.class, CpoAttribute.class, String.class, String.class, String.class, String.class};
    return Arrays.asList(columnClasses);
  }

  @Override
  public int getRowCount() {
    return cpoFunctionNode.getChildCount();
  }

  @Override
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
    return (columnIndex == 1 || columnIndex==4);
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    CpoArgumentNode cpoArgumentNode = (CpoArgumentNode)cpoFunctionNode.getChildAt(rowIndex);

    CpoArgument argument = cpoArgumentNode.getUserObject();
    CpoAttribute att = argument.getAttribute();

    if (columnIndex == 0) {
      return cpoFunctionNode.getIndex(cpoArgumentNode);
    } else if (columnIndex == 1) {
      return cpoArgumentNode.getCpoAttribute();
    } else if (columnIndex == 2) {
      return att != null ? att.getDataName() : null;
    } else if (columnIndex == 3) {
      return att != null ? att.getDataType() : null;
    } else if (columnIndex == 4) {
      return att != null ? att.getTransformClassName() : null;
    } else if (columnIndex == 5) {
      if (cpoArgumentNode.isNew()) {
        return "New";
      } else if (cpoArgumentNode.isRemove()) {
        return "Removed";
      } else if (cpoArgumentNode.isDirty()) {
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
    if (columnIndex == 1) {
      CpoArgumentNode cpoArgumentNode = (CpoArgumentNode)cpoFunctionNode.getChildAt(rowIndex);
      cpoArgumentNode.setCpoAttribute((CpoAttribute)aValue);
    }
    this.fireTableDataChanged();
  }

  public void removeNewRow() {
    int originalLength = this.getNonRemovedRows();

    for (int i = cpoFunctionNode.getChildCount() - 1; i >= 0; i--) {
      CpoArgumentNode cpoArgumentNode = (CpoArgumentNode)cpoFunctionNode.getChildAt(i);
      if (cpoArgumentNode.isNew()) {
        cpoArgumentNode.setRemove(true);
        cpoFunctionNode.remove(cpoArgumentNode);
        this.fireTableDataChanged();
        return;
      }
    }

    if (this.getNonRemovedRows() == originalLength) {
      for (int i = cpoFunctionNode.getChildCount() - 1; i >= 0; i--) {
        CpoArgumentNode cpoArgumentNode = (CpoArgumentNode)cpoFunctionNode.getChildAt(i);
        if (!cpoArgumentNode.isRemove()) {
          this.removeRow(i);
          return;
        }
      }
    }
  }

  public void removeRow(int rowIndex) {
    CpoArgumentNode cpoArgumentNode = (CpoArgumentNode)cpoFunctionNode.getChildAt(rowIndex);
    cpoArgumentNode.setRemove(true);
    this.fireTableDataChanged();
  }

  public void addNewRow() {
    int originalLength = this.getNonRemovedRows();
    Enumeration e = cpoFunctionNode.children();
    while (e.hasMoreElements()) {
      CpoArgumentNode cpoArgumentNode = (CpoArgumentNode)e.nextElement();
      if (cpoArgumentNode.isRemove()) {
        cpoArgumentNode.setRemove(false);
        this.fireTableDataChanged();
        return;
      }
    }

    if (this.getNonRemovedRows() == originalLength) {
      try {
        CpoAttributeLabelNode attributeLabelNode = cpoFunctionNode.getParent().getParent().getParent().getAttributeLabelNode();

        CpoAttribute firstAttribute = null;
        if (attributeLabelNode.getChildCount() > 0) {
          CpoAttributeNode firstAttributeNode = (CpoAttributeNode)attributeLabelNode.getFirstChild();
          firstAttribute = firstAttributeNode.getUserObject();
        }

        CpoArgumentNode cpoArgumentNode = cpoFunctionNode.getProxy().addArgument(cpoFunctionNode, firstAttribute);

        // tell the table it's changed
        this.fireTableDataChanged();
      } catch (Exception pe) {
        CpoUtil.showException(pe);
      }
    }
  }

  public int getNonRemovedRows() {
    int count = 0;
    Enumeration e = cpoFunctionNode.children();
    while (e.hasMoreElements()) {
      CpoArgumentNode cpoArgumentNode = (CpoArgumentNode)e.nextElement();
      if (!cpoArgumentNode.isRemove()) {
        count++;
      }
    }
    return count;
  }
}