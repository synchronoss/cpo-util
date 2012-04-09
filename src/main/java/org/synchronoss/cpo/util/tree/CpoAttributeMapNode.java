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
package org.synchronoss.cpo.util.tree;

import org.synchronoss.cpo.meta.domain.CpoAttribute;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

public class CpoAttributeMapNode extends AbstractCpoNode {

  private CpoAttribute attribute;

  public CpoAttributeMapNode(CpoAttribute attribute, CpoAttributeLabelNode parent) {
    this.attribute = attribute;
    this.parent = parent;
    if (parent != null) {
      this.addObserver(parent.getProxy());
      this.setProtected(parent.isProtected());
    }
  }

  @Override
  public CpoAttributeLabelNode getParent() {
    return (CpoAttributeLabelNode)this.parent;
  }

  public CpoAttribute getCpoAttribute() {
    return attribute;
  }

  public String getAttributeId() {
    return attribute.getAttributeId();
  }

  public String getClassId() {
    return attribute.getClassId();
  }

  public String getColumnName() {
    return attribute.getColumnName();
  }

  public String getAttribute() {
    return attribute.getAttribute();
  }

  public String getColumnType() {
    return attribute.getColumnType();
  }

  public String getTransformClass() {
    return attribute.getTransformClass();
  }

  public String getDbTable() {
    return attribute.getDbTable();
  }

  public String getDbColumn() {
    return attribute.getDbColumn();
  }

  @Override
  public String getUserName() {
    return attribute.getUserid();
  }

  @Override
  public Calendar getCreateDate() {
    return attribute.getCreatedate();
  }

  @Override
  public String toString() {
    return attribute.getAttribute();
  }

  public void setColumnName(String columnName) {
    if (columnName.equals(this.getColumnName()))
      return;
    this.attribute.setColumnName(columnName);
    this.setDirty(true);
  }

  public void setAttribute(String attribute) {
    if (attribute.equals(this.getAttribute()))
      return;
    this.attribute.setAttribute(attribute);
    this.setDirty(true);
  }

  public void setColumnType(String columnType) {
    if (columnType.equals(this.getColumnType()))
      return;
    this.attribute.setColumnType(columnType);
    this.setDirty(true);
  }

  public void setTransformClass(String transform) {
    if (transform.equals(this.getTransformClass()))
      return;
    this.attribute.setTransformClass(transform);
    this.setDirty(true);
  }

  public void setDbTable(String dbTable) {
    if (dbTable.equals(this.getDbTable()))
      return;
    this.attribute.setDbTable(dbTable);
    this.setDirty(true);
  }

  public void setDbColumn(String dbColumn) {
    if (dbColumn.equals(this.getDbColumn()))
      return;
    this.attribute.setDbColumn(dbColumn);
    this.setDirty(true);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CpoAttributeMapNode))
      return false;

    if (((CpoAttributeMapNode) obj).getCpoAttribute().getAttributeId().equals(attribute.getAttributeId())) {
      return true;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return attribute.getAttributeId().hashCode();
  }

  public TreeNode getChildAt(int childIndex) {
    return null;
  }

  public int getChildCount() {
    return 0;
  }

  public int getIndex(TreeNode node) {
    return 0;
  }

  public boolean getAllowsChildren() {
    return false;
  }

  public boolean isLeaf() {
    return true;
  }

  @Override
  public Enumeration<AbstractCpoNode> children() {
    return null;
  }

  @Override
  public void refreshChildren() {
  }

  @Override
  public JPanel getPanelForSelected() {
    return null;
  }
}