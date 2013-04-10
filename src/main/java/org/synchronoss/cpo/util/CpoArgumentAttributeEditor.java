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

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.*;

public class CpoArgumentAttributeEditor implements TableCellEditor {

  private TableCellEditor editor;
  private CoreArgumentTableModel cpoFTM;

  public CpoArgumentAttributeEditor(CoreArgumentTableModel cpoFTM) {
    editor = new DefaultCellEditor(new JComboBox());
    this.cpoFTM = cpoFTM;
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    JComboBox attEdBox;
    if (value instanceof CpoAttribute) {
      CpoAttribute currentCpoAtt = (CpoAttribute)value;
      try {
        CpoAttributeLabelNode attributeLabelNode = cpoFTM.getCpoFunctionNode().getParent().getParent().getParent().getAttributeLabelNode();
        Enumeration e = attributeLabelNode.children();
        Vector<CpoAttribute> atts = new Vector<CpoAttribute>();
        while (e.hasMoreElements()) {
          CpoAttributeNode cpoAttributeNode = (CpoAttributeNode)e.nextElement();
          atts.add(cpoAttributeNode.getUserObject());
        }

        attEdBox = new JComboBox(atts);
        attEdBox.setSelectedItem(currentCpoAtt);
      } catch (Exception pe) {
        CpoUtil.showException(pe);
        attEdBox = new JComboBox();
      }
    } else {
      attEdBox = new JComboBox();
    }
    editor = new DefaultCellEditor(attEdBox);
    return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
  }

  public Object getCellEditorValue() {
    return editor.getCellEditorValue();
  }

  public boolean isCellEditable(EventObject anEvent) {
    return editor.isCellEditable(anEvent);
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    return editor.shouldSelectCell(anEvent);
  }

  public boolean stopCellEditing() {
    return editor.stopCellEditing();
  }

  public void cancelCellEditing() {
    editor.cancelCellEditing();
  }

  public void addCellEditorListener(CellEditorListener l) {
    editor.addCellEditorListener(l);
  }

  public void removeCellEditorListener(CellEditorListener l) {
    editor.removeCellEditorListener(l);
  }
}