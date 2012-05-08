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
package org.synchronoss.cpo.util.jdbc;

import org.synchronoss.cpo.jdbc.JdbcCpoAttribute;
import org.synchronoss.cpo.util.CpoAttributeNode;

public class JdbcAttributeNode extends CpoAttributeNode {

  public JdbcAttributeNode(JdbcCpoAttribute attribute) {
    super(attribute);
  }

  @Override
  public JdbcCpoAttribute getUserObject() {
    return (JdbcCpoAttribute)super.getUserObject();
  }

  public String getDbTable() {
    return getUserObject().getDbTable();
  }

  public String getDbColumn() {
    return getUserObject().getDbColumn();
  }

  public void setDbTable(String dbTable) {
    if (dbTable.equals(this.getDbTable()))
      return;
    getUserObject().setDbTable(dbTable);
    this.setDirty(true);
  }

  public void setDbColumn(String dbColumn) {
    if (dbColumn.equals(this.getDbColumn()))
      return;
    getUserObject().setDbColumn(dbColumn);
    this.setDirty(true);
  }
}