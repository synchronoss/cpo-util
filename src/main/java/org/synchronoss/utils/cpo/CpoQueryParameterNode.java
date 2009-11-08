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

public class CpoQueryParameterNode extends AbstractCpoNode {
  private int seqNo;
  private String inOutType = "IN";
  private CpoAttributeMapNode cpoAMB;
  
  public CpoQueryParameterNode(CpoQueryNode qNode, int seqNo, CpoAttributeMapNode cpoAMB, String type) {
    this.parent = qNode;
    this.seqNo = seqNo;
    this.cpoAMB = cpoAMB;
    this.inOutType = type;
    this.addObserver(parent.getProxy());
    
//    this.addObserver(parent);
  }
  public String getType() {
    return this.inOutType;
  }

  public void setType(String value) {
    if (this.inOutType == null && value == null)
      return;
    if (this.inOutType == null || value == null || !value.equals(this.inOutType)) {
      this.inOutType = value;
      this.setDirty(true);
    }
  }

  public String getAttributeId() {
    return this.cpoAMB.getAttributeId();
  }

  public String getQueryId() {
    return ((CpoQueryNode)this.parent).getQueryId();
  }

  public int getSeqNo() {
    return this.seqNo;
  }

  public CpoAttributeMapNode getCpoAttributeMapBean() {
    return this.cpoAMB;
  }

  public void setCpoAttributeMap(CpoAttributeMapNode cpoAMB) {
    if (this.cpoAMB == null && cpoAMB == null) return;
    if (this.cpoAMB == null || cpoAMB == null || !cpoAMB.equals(this.cpoAMB)) {
      this.cpoAMB = cpoAMB;
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
    if (!(obj instanceof CpoQueryParameterNode)) return false;
    if (((CpoQueryParameterNode)obj).getQueryId().equals(this.getQueryId())
        && ((CpoQueryParameterNode)obj).getSeqNo() == this.getSeqNo()) return true;
    return false;
  }
  
  @Override
  public int hashCode() {
    return this.getQueryId().hashCode();
  }
  
  @Override
  public String toString() {
    return this.getQueryId()+" - "+this.getSeqNo();
  }
}