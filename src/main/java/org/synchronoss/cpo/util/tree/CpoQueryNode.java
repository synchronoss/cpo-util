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
import org.synchronoss.cpo.util.*;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

public class CpoQueryNode extends AbstractCpoNode {

  private CpoQuery query;
  private List<CpoQueryParameterNode> cpoQueryParam;

  public CpoQueryNode(CpoQuery query, CpoQueryGroupNode parent) {
    this.query = query;
    this.parent = parent;
    if (parent != null) {
      this.addObserver(parent.getProxy());
      this.setProtected(parent.isProtected());
    }
  }

  @Override
  public CpoQueryGroupNode getParent() {
    return (CpoQueryGroupNode)this.parent;
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

  @Override
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
      if (this.getQueryText().getDescription() == null || this.getQueryText().getDescription().equals(""))
        return this.getSeqNo() + " (NO DESCRIPTION)";

      return this.getSeqNo() + " (" + this.getQueryText().getDescription() + ")";
    }
    return this.getSeqNo() + " (NO QUERY TEXT ASSOC)";
  }
  
  public CpoQuery getCpoQuery() {
    return query;
  }

  public String getGroupId() {
    return query.getGroupId();
  }

  public String getQueryId() {
    return query.getQueryId();
  }

  public int getSeqNo() {
    return query.getSeqNo();
  }

  public String getTextId() {
    return query.getTextId();
  }

  public CpoQueryText getQueryText() {
    return query.getQueryText();
  }

  @Override
  public String getUserName() {
    return query.getUserid();
  }

  @Override
  public Calendar getCreateDate() {
    return query.getCreatedate();
  }

  public void setQueryText(CpoQueryText queryText) {
    if ((queryText == null && query.getQueryText() == null) || (query.getQueryText() != null && query.getQueryText().equals(queryText))) {
      return;
    }

    query.setQueryText(queryText);
    this.setDirty(true);
  }

  public void setSeqNo(int seqNo) {
    if (getSeqNo() == seqNo) {
      return;
    }

    query.setSeqNo(seqNo);
    this.setDirty(true);
  }

  public void setDescription(String desc) {
    if (query.getQueryText().getDescription().equals(desc)) {
      return;
    }

    query.getQueryText().setDescription(desc);
    setDirty(true);
  }

  public void setSqlText(String sql) {
    if (query.getQueryText().getSqlText().equals(sql)) {
      return;
    }

    // if the sql changed, mark the node dirty
    query.getQueryText().setSqlText(sql);
    setDirty(true);
  }
}
