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

import org.synchronoss.cpo.meta.domain.CpoQueryGroup;
import org.synchronoss.cpo.util.CpoUtil;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

public class CpoQueryGroupLabelNode extends AbstractCpoNode  {

  private List<CpoQueryGroupNode> qGroups;

  public CpoQueryGroupLabelNode(CpoClassNode parent) {
    this.parent = parent;
    if (parent != null) {
      this.addObserver(parent.getProxy());
      this.setProtected(parent.isProtected());
    }
  }

  @Override
  public CpoClassNode getParent() {
    return (CpoClassNode)this.parent;
  }

  @Override
  public JPanel getPanelForSelected() {
    return null;
  }

  @Override
  public void refreshChildren() {
    try {
      this.qGroups = getProxy().getQueryGroups(getParent());
    } catch (Exception pe) {
      CpoUtil.showException(pe);
    }    
  }

  public TreeNode getChildAt(int childIndex) {
    return qGroups.get(childIndex);
  }

  public int getChildCount() {
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

  @Override
  public Enumeration<CpoQueryGroupNode> children() {
    if (this.qGroups == null)
      refreshChildren();
    return new Enumeration<CpoQueryGroupNode>() {
      Iterator<CpoQueryGroupNode> iter = qGroups.iterator();
      public CpoQueryGroupNode nextElement() {
        return iter.next();
      }
      public boolean hasMoreElements() {
        return iter.hasNext();
      }
    };
  }

  public CpoQueryGroupNode addNewQueryGroup(String groupName, String groupType) {
    if (this.qGroups == null)
      this.refreshChildren();
    CpoQueryGroupNode cqgn;
    try {
      CpoQueryGroup queryGroup = new CpoQueryGroup();
      queryGroup.setClassId(this.getParent().getCpoClass().getClassId());
      queryGroup.setGroupId(this.getProxy().getNewGuid());
      queryGroup.setName(groupName);
      queryGroup.setGroupType(groupType);
      queryGroup.setUserid(CpoUtil.getUserName());
      queryGroup.setCreatedate(Calendar.getInstance());

      cqgn = new CpoQueryGroupNode(queryGroup, this);
      cqgn.setProtected(this.isProtected());
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      return null;
    }
    this.qGroups.add(cqgn);
    cqgn.setNew(true);
    return cqgn;
  }

  @Override
  public String toString() {
    return "Query Groups";
  }

  @Override
  public String getUserName() {
    return "";
  }

  @Override
  public Calendar getCreateDate() {
    return Calendar.getInstance();
  }

  @Override
  public boolean isLabel() {
    return true;
  }
}