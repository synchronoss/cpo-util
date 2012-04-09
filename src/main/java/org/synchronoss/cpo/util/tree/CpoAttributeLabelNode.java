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

import org.synchronoss.cpo.util.*;

import javax.swing.tree.TreeNode;
import javax.swing.JPanel;
import java.util.*;

public class CpoAttributeLabelNode extends AbstractCpoNode  {
  
  private List<CpoAttributeMapNode> cpoAttMap; // contains collection of CpoAttributeMapNode(s)
  
  public CpoAttributeLabelNode(CpoClassNode parent) {
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
    return new CpoAttributeMapPanel(this);
  }

  @Override
  public void refreshChildren() {
    try {
      this.cpoAttMap = getProxy().getAttributeMap(this);
    } catch (Exception pe) {
      CpoUtil.showException(pe);
    }    
  }

  public TreeNode getChildAt(int childIndex) {
    if (childIndex >= cpoAttMap.size()) 
      return null;
    
    return cpoAttMap.get(childIndex);
  }

  public int getChildCount() {
    return this.cpoAttMap.size();
  }

  public int getIndex(TreeNode node) {
    if (this.cpoAttMap == null)
      refreshChildren();
    return this.cpoAttMap.indexOf(node);
  }

  public boolean getAllowsChildren() {
    return true;
  }

  public boolean isLeaf() {
    return true;
  }

  @Override
  public Enumeration<CpoAttributeMapNode> children() {
    if (cpoAttMap == null) // due to panel not being removed from center pane ... this should be fixed
      refreshChildren();
    
    return new Enumeration<CpoAttributeMapNode>() {
      Iterator<CpoAttributeMapNode> iter = cpoAttMap.iterator();
      public CpoAttributeMapNode nextElement() {
        return iter.next();
      }
      public boolean hasMoreElements() {
        return iter.hasNext();
      }
    };
  }
  
  @Override
  public String toString() {
    return "Attribute Map";
  }

  @Override
  public String getUserName() {
    return "";
  }

  @Override
  public Calendar getCreateDate() {
    return Calendar.getInstance();
  }
}