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

import org.synchronoss.cpo.meta.domain.CpoQueryText;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

public class CpoQueryTextNode extends AbstractCpoNode {

  CpoQueryText queryText;

  public CpoQueryTextNode(CpoQueryText queryText, AbstractCpoNode parent) {
    this.queryText = queryText;
    this.parent = parent;
    if (parent != null) {
      this.addObserver(parent.getProxy());
    }
  }

  @Override
  public void refreshChildren() {
  }

  @Override
  public JPanel getPanelForSelected() {
    return null;
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

  public CpoQueryText getCpoQueryText() {
    return queryText;
  }

  public String getDesc() {
    return queryText.getDescription();
  }

  public String getTextId() {
    return queryText.getTextId();
  }

  public int getRefCount() {
    return queryText.getRefCount();
  }

  public String getSQL() {
    return queryText.getSqlText();
  }

  @Override
  public String getUserName() {
    return queryText.getUserid();
  }

  @Override
  public Calendar getCreateDate() {
    return queryText.getCreatedate();
  }

  public void setSQL(String sql) {

    if (sql == null) {
      sql = "";
    } else {
      sql = sql.trim();
    }

    // strip sql of CRs
    sql = sql.replaceAll("\\r\\n\\s*\\r\\n", "\r\n");
    sql = sql.replaceAll("\\n\\s*\\n", "\n");

    if (queryText.getSqlText() != null && queryText.getSqlText().equals(sql))
      return;

    queryText.setSqlText(sql);
    this.setDirty(true);
  }

  public void setDesc(String desc) {
    if (queryText.getDescription() != null && queryText.getDescription().equals(desc))
      return;

    queryText.setDescription(desc);
    this.setDirty(true);
  }

  public void setRefCount(int refCount) {
    queryText.setRefCount(refCount);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CpoQueryTextNode)) {
      return false;
    }
    //PJD for HSQLDB 
    if (((CpoQueryTextNode) obj).getTextId() == null) {
      return false;
    }
    if (((CpoQueryTextNode) obj).getTextId().equals(this.getTextId()))
      return true;

    return false;
  }

  @Override
  public int hashCode() {
    //PJD for HSQLDB 
    if (queryText.getTextId() == null) {
      return -1;
    }
    return queryText.getTextId().hashCode();
  }

  @Override
  public String toString() {
    //return this.hashCode() + " -- " + this.getDesc();
    return queryText.toString();
  }
}