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

import org.synchronoss.cpo.meta.domain.CpoQueryParameter;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

public class CpoQueryParameterNode extends AbstractCpoNode {

  private CpoQueryParameter queryParameter;
  private CpoAttributeMapNode attributeMapNode;

  public CpoQueryParameterNode(CpoQueryParameter queryParameter, CpoAttributeMapNode attributeMapNode, CpoQueryNode parent) {
    this.queryParameter = queryParameter;
    this.attributeMapNode = attributeMapNode;
    this.parent = parent;
    if (parent != null) {
      this.addObserver(parent.getProxy());
    }
  }

  @Override
  public CpoQueryNode getParent() {
    return (CpoQueryNode)this.parent;
  }

  public String getType() {
    return queryParameter.getParamType();
  }

  public void setType(String value) {
    if (this.getType() == null && value == null)
      return;

    if (this.getType() == null || value == null || !value.equals(this.getType())) {
      queryParameter.setParamType(value);
      this.setDirty(true);
    }
  }

  public CpoQueryParameter getCpoQueryParameter() {
    return queryParameter;
  }

  public String getAttributeId() {
    return queryParameter.getAttributeId();
  }

  public String getQueryId() {
    return queryParameter.getQueryId();
  }

  public int getSeqNo() {
    return queryParameter.getSeqNo();
  }

  @Override
  public String getUserName() {
    return queryParameter.getUserid();
  }

  @Override
  public Calendar getCreateDate() {
    return queryParameter.getCreatedate();
  }

  public CpoAttributeMapNode getCpoAttributeMapBean() {
    return attributeMapNode;
  }

  public void setCpoAttributeMap(CpoAttributeMapNode cpoAMB) {
    if (this.attributeMapNode == null && cpoAMB == null) return;
    if (this.attributeMapNode == null || cpoAMB == null || !cpoAMB.equals(this.attributeMapNode)) {
      this.attributeMapNode = cpoAMB;
      if (attributeMapNode != null) {
        queryParameter.setAttributeId(attributeMapNode.getAttributeId());
      }
      this.setDirty(true);
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

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;

    if (!(obj instanceof CpoQueryParameterNode))
      return false;

    if (((CpoQueryParameterNode) obj).getQueryId().equals(this.getQueryId()) && ((CpoQueryParameterNode) obj).getSeqNo() == this.getSeqNo())
      return true;
    
    return false;
  }
  
  @Override
  public int hashCode() {
    return this.getQueryId().hashCode();
  }
  
  @Override
  public String toString() {
    return this.getQueryId() + " - " + this.getSeqNo();
  }
}