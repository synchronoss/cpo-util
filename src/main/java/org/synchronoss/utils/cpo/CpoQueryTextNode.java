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
import java.util.Enumeration;
import javax.swing.tree.TreeNode;

public class CpoQueryTextNode extends AbstractCpoNode  {
  String textId, sql, desc;
  int usageCount;
  
  public CpoQueryTextNode(String textId, String sql, String desc, AbstractCpoNode parent) {
    this.parent = parent;
    this.textId = textId;
    this.sql = sql;
    this.desc = desc;
    this.addObserver(parent.getProxy());
//    this.addObserver(parent);
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
  public String getDesc() {
    return this.desc;
  }
  public String getTextId() {
    return this.textId;
  }
  public String getSQL() {
    return this.sql;
  }
  public void setSQL(String sql) {
    if (this.sql != null && this.sql.equals(sql)) return;
    this.sql = sql;
    this.setDirty(true);
  }
  public void setDesc(String desc) {
    if (this.desc != null && this.desc.equals(desc)) return;
    this.desc = desc;
    this.setDirty(true);
  }
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CpoQueryTextNode)) {return false;}
    //PJD for HSQLDB 
    if (((CpoQueryTextNode)obj).getTextId() == null) {
      return false;
    }
    if (((CpoQueryTextNode)obj).getTextId().equals(this.getTextId()))
      return true;
    
    return false;
  }
  @Override
  public int hashCode() {
    //PJD for HSQLDB 
    if (this.getTextId() == null) {
      return -1;
    }
    return this.getTextId().hashCode();
  }
  @Override
  public String toString() {
    return this.hashCode()+" -- "+this.getDesc();
  }
  public void setUsageCount(int usageCount) {
    this.usageCount = usageCount;
  }
  public int getUsageCount() {
    return this.usageCount;
  }
}