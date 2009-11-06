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
import javax.swing.JPanel;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class CpoAttributeMapNode extends AbstractCpoNode {
  String attribute_id, class_id, column_name, attribute, column_type, transform, db_table, db_column;
  String type= "IN";
  
  public CpoAttributeMapNode(CpoAttributeLabelNode parent,String attribute_id, String class_id, String column_name, 
      String attribute, String column_type, String transform, String db_table, String db_column, String param_type) {
    this.attribute_id = attribute_id;
    this.class_id = class_id;
    this.column_name = column_name;
    this.attribute = attribute;
    this.column_type = column_type;
    this.transform = transform;
    this.db_table = db_table;
    this.db_column = db_column;
    this.parent = parent;
    this.type = param_type;
    this.addObserver(parent.getProxy());
    this.setProtected(parent.isProtected());
//    this.addObserver(parent);
  }
  public String getAttributeId() {
    return this.attribute_id;
  }
  public String getClassId() {
    return this.class_id;
  }
  public String getColumnName() {
    return this.column_name;
  }
  public String getAttribute() {
    return this.attribute;
  }
  public String getColumnType() {
    return this.column_type;
  }
  public String getType() {
    return this.type;
  }
  public String getTransformClass() {
    return this.transform;
  }
  public String getDbTable() {
    return this.db_table;
  }
  public String getDbColumn() {
    return this.db_column;
  }
  @Override
  public String toString() {
    return this.attribute;
  }
  public void setColumnName(String columnName) {
    if (columnName.equals(this.column_name)) return;
    this.column_name = columnName;
    this.setDirty(true);
  }
  public void setAttribute(String attribute) {
    if (attribute.equals(this.attribute)) return;
    this.attribute = attribute;
    this.setDirty(true);
  }
  public void setColumnType(String columnType) {
    if (columnType.equals(this.column_type)) return;
    this.column_type = columnType;
    this.setDirty(true);
  }
  public void setType(String _type) {
    this.type = _type;
  }

  public void setTransformClass(String transform) {
    if (transform.equals(this.transform)) return;
    this.transform = transform;
    this.setDirty(true);
  }

  public void setDbTable(String dbTable) {
    if (dbTable.equals(this.db_table)) return;
    this.db_table = dbTable;
    this.setDirty(true);
  }
  public void setDbColumn(String dbColumn) {
    if (dbColumn.equals(this.db_column)) return;
    this.db_column = dbColumn;
    this.setDirty(true);
  }
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CpoAttributeMapNode)) return false;
    if (((CpoAttributeMapNode)obj).getAttributeId().equals(this.getAttributeId())) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    return this.getAttributeId().hashCode();
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