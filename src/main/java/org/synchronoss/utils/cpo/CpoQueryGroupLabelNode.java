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

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

public class CpoQueryGroupLabelNode extends AbstractCpoNode  {
  private List<CpoQueryGroupNode> qGroups; // contains CpoQueryGroupNode(s)
  
  public CpoQueryGroupLabelNode(CpoClassNode parent) {
    this.parent = parent;
    this.addObserver(parent.getProxy());
//    this.addObserver(parent);
  }

  @Override
  public JPanel getPanelForSelected() {
    return null;
  }

  @Override
  public void refreshChildren() {
//    OUT.debug ("Query Groups Label Refreshing Data");
    try {
      this.qGroups = getProxy().getQueryGroups(this);
    } catch (Exception pe) {
      CpoUtil.showException(pe);
    }    
  }

  public TreeNode getChildAt(int childIndex) {
    return (TreeNode)qGroups.get(childIndex);
  }

  public int getChildCount() {
//    OUT.debug ("Query Group Label Node Child Count: "+this.qGroups.size());
    return this.qGroups.size();
  }

  public int getIndex(TreeNode node) {
    return qGroups.indexOf(node);
  }

  public boolean getAllowsChildren() {
    return true;
  }

  public boolean isLeaf() {
    return false;
  }

  public Enumeration children() {
    if (this.qGroups == null)
      refreshChildren();
    return new Enumeration() {
      Iterator iter = qGroups.iterator();
      public Object nextElement() {
        return iter.next();
      }
      public boolean hasMoreElements() {
        return iter.hasNext();
      }
    };
  }
  public void addNewQueryGroup(String groupName, String groupType) {
    if (this.qGroups == null)
      this.refreshChildren();
    CpoQueryGroupNode cqgn;
    try {
      cqgn = new CpoQueryGroupNode(groupName,
          ((CpoClassNode)this.getParent()).getClassId(),
          this.getProxy().getNewGuid(), groupType, this);
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      return;
    }
    this.qGroups.add(cqgn);
    cqgn.setNew(true);
  }

  public String toString() {
    return "Query Groups";
  }
}