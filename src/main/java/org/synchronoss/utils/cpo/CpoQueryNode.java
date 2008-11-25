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

import javax.swing.tree.TreeNode;
import javax.swing.JPanel;
import java.util.*;

public class CpoQueryNode extends AbstractCpoNode {
  private String queryId, groupId;
  private int seqNo;
  private CpoQueryTextNode cQTnode;
  private List<CpoQueryParameterNode> cpoQueryParam; // collection of CpoQueryParameterNode(s)
  
  public CpoQueryNode(String queryId, String groupId, int seqNo, CpoQueryTextNode cQTnode, AbstractCpoNode parent) {
    this.cQTnode = cQTnode;
    this.queryId = queryId;
    this.groupId = groupId;
    this.seqNo = seqNo;
    this.parent = parent;
    this.addObserver(parent.getProxy());
//    this.addObserver(parent);
  }

  @Override
  public JPanel getPanelForSelected() {
    return new CpoQueryPanel(this);
  }
  
  public TreeNode getChildAt(int childIndex) {
    if (childIndex >= cpoQueryParam.size() || childIndex < 0)
      return null;
    return cpoQueryParam.get(childIndex);
  }

  public int getChildCount() {
    return this.cpoQueryParam.size();
  }

  public int getIndex(TreeNode node) {
    if (this.cpoQueryParam == null)
      refreshChildren();
    return this.cpoQueryParam.indexOf(node);
  }

  public boolean getAllowsChildren() {
    return true;
  }

  public boolean isLeaf() {
    return true;
  }

  public Enumeration<CpoQueryParameterNode> children() {
    if (cpoQueryParam == null) // due to panel not being removed from center pane ... this should be fixed
      refreshChildren();
    return new Enumeration<CpoQueryParameterNode>() {
      Iterator<CpoQueryParameterNode> iter = cpoQueryParam.iterator();
      public CpoQueryParameterNode nextElement() {
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
      this.cpoQueryParam = getProxy().getQueryParameters(this);
    } catch (Exception pe) {
      CpoUtil.showException(pe);
    }        
  }
  @Override
  public String toString() {
    if (this.getQueryText() != null) {
      if (this.getQueryText().getDesc() == null || this.getQueryText().getDesc().equals(""))
        return this.getSeqNo()+" (NO DESCRIPTION)";
      
      return this.getSeqNo()+" ("+this.getQueryText().getDesc()+")";
    }
    return this.getSeqNo()+" (NO QUERY TEXT ASSOC)";
  }
  public String getGroupId() {
    return this.groupId;
  }
  public String getQueryId() {
    return this.queryId;
  }
  public int getSeqNo() {
    return this.seqNo;
  }
  public String getTextId() {
    return this.cQTnode.getTextId();
  }
  public CpoQueryTextNode getQueryText() {
    return this.cQTnode;
  }
  public void setQueryText(CpoQueryTextNode cQTnode) {
    if ((cQTnode == null && this.cQTnode == null) || (this.cQTnode != null && this.cQTnode.equals(cQTnode))) return;
    this.cQTnode = cQTnode;
    this.setDirty(true);
  }
  public void setSeqNo(int seqNo) {
    if (this.seqNo == seqNo) return;
    this.seqNo = seqNo;
    this.setDirty(true);
  }
}
