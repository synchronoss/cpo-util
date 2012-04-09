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

import org.synchronoss.cpo.meta.domain.*;
import org.synchronoss.cpo.util.CpoUtil;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

public class CpoQueryGroupNode extends AbstractCpoNode {

  private CpoQueryGroup queryGroup;
  private List<CpoQueryNode> qNodes;

  public CpoQueryGroupNode(CpoQueryGroup queryGroup, CpoQueryGroupLabelNode parent) {
    this.queryGroup = queryGroup;
    this.parent = parent;
    if (parent != null) {
      this.addObserver(parent.getProxy());
      this.setProtected(parent.isProtected());
    }
  }

  @Override
  public CpoQueryGroupLabelNode getParent() {
    return (CpoQueryGroupLabelNode)this.parent;
  }

  @Override
  public JPanel getPanelForSelected() {
    return null;
  }

  public TreeNode getChildAt(int childIndex) {
    if (childIndex >= qNodes.size())
      return null;
    return qNodes.get(childIndex);
  }

  public int getChildCount() {
    return this.qNodes.size();
  }

  public int getIndex(TreeNode node) {
    return this.qNodes.indexOf(node);
  }

  public boolean getAllowsChildren() {
    return true;
  }

  public boolean isLeaf() {
    return false;
  }

  @Override
  public Enumeration<CpoQueryNode> children() {
    if (qNodes == null) 
      refreshChildren();

    return new Enumeration<CpoQueryNode>() {
      Iterator<CpoQueryNode> iter = qNodes.iterator();

      public CpoQueryNode nextElement() {
        return iter.next();
      }

      public boolean hasMoreElements() {
        return iter.hasNext();
      }
    };
  }

  @Override
  public void refreshChildren() {
    try {
      this.qNodes = getProxy().getQueries(this);
    } catch (Exception pe) {
      CpoUtil.showException(pe);
    }
  }

  @Override
  public String toString() {
    return queryGroup.getName() + " (" + queryGroup.getGroupType() + ")";
  }
  
  public CpoQueryGroup getCpoQueryGroup() {
    return queryGroup;
  }

  public String getClassId() {
    return queryGroup.getClassId();
  }

  public String getGroupId() {
    return queryGroup.getGroupId();
  }

  public String getGroupName() {
    return queryGroup.getName();
  }

  public String getType() {
    return queryGroup.getGroupType();
  }

  @Override
  public String getUserName() {
    return queryGroup.getUserid();
  }

  @Override
  public Calendar getCreateDate() {
    return queryGroup.getCreatedate();
  }

  public CpoQueryNode addNewQueryNode() {
    if (this.qNodes == null)
      this.refreshChildren();
    String queryId;
    int seqNo;
    try {
      queryId = this.getProxy().getNewGuid();
      seqNo = this.getNextQuerySeqNo();
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      return null;
    }
    
    CpoQuery query = new CpoQuery();
    query.setQueryId(queryId);
    query.setGroupId(getGroupId());
    query.setSeqNo(seqNo);
    query.setUserid(CpoUtil.getUserName());
    query.setCreatedate(Calendar.getInstance());
    
    CpoQueryNode cqn = new CpoQueryNode(query, this);
    this.qNodes.add(cqn);
    cqn.setNew(true);
    return cqn;
  }

  private int getNextQuerySeqNo() {
    int nextSeqNo = 0;
    for (CpoQueryNode cqn : qNodes) {
      if (nextSeqNo <= cqn.getSeqNo()) {
        nextSeqNo = cqn.getSeqNo() + 1;
      }
    }
    return nextSeqNo;
  }

  public void setGroupName(String groupName) {
    if ((groupName == null && queryGroup.getName() == null) || (queryGroup.getName() != null && queryGroup.getName().equals(groupName))) {
      return;
    }
    
    queryGroup.setName(groupName);
    this.setDirty(true);
  }
}