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

import org.synchronoss.cpo.meta.domain.CpoClass;
import org.synchronoss.cpo.util.CpoTesterPanel;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

public class CpoClassNode extends AbstractCpoNode implements Comparable<CpoClassNode> {

  private CpoClass cpoClass;
  private CpoQueryGroupLabelNode queryGroupLabel;
  private CpoAttributeLabelNode attributeLabel;

  public CpoClassNode(CpoClass cpoClass, AbstractCpoNode parent) {
    this.cpoClass = cpoClass;
    this.parent = parent;
    if (parent != null) {
      this.addObserver(parent.getProxy());
    }
  }

  @Override
  public CpoServerNode getParent() {
    return (CpoServerNode)this.parent;
  }

  @Override
  public JPanel getPanelForSelected() {
    return new CpoTesterPanel(this);
  }

  @Override
  public TreeNode getChildAt(int childIndex) {
    if (childIndex == 0) {
      return this.queryGroupLabel;
    } else if (childIndex == 1) {
      return this.attributeLabel;
    } else {
      return null;
    }
  }

  @Override
  public int getChildCount() {
    return 2;
  }

  @Override
  public int getIndex(TreeNode node) {
    if (node == this.queryGroupLabel) {
      return 0;
    } else if (node == this.attributeLabel) {
      return 1;
    } else {
      return -1;
    }
  }

  @Override
  public boolean getAllowsChildren() {
    return true;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public Enumeration<AbstractCpoNode> children() {
    if (queryGroupLabel == null || attributeLabel == null)
      refreshChildren();

    return new Enumeration<AbstractCpoNode>() {
      int count = 0;

      public AbstractCpoNode nextElement() {
        if (count++ == 0) {
          return queryGroupLabel;
        }
        return attributeLabel;
      }

      public boolean hasMoreElements() {
        return (count <= 1);
      }
    };
  }

  @Override
  public String toString() {
    return this.getDisplayClassName();
  }

  public CpoClass getCpoClass() {
    return cpoClass;
  }

  @Override
  public String getUserName() {
    return cpoClass.getUserid();
  }

  @Override
  public Calendar getCreateDate() {
    return cpoClass.getCreatedate();
  }

  public String getDisplayClassName() {
    String className = cpoClass.getName();
    if (!this.getProxy().getClassNameToggle() && className.lastIndexOf(".") != -1 && className.length() > className.lastIndexOf(".") + 1) {
      return className.substring(className.lastIndexOf(".") + 1);
    }
    return className;
  }

  @Override
  public void refreshChildren() {
    if (this.queryGroupLabel == null || this.attributeLabel == null) {
      this.queryGroupLabel = new CpoQueryGroupLabelNode(this);
      this.attributeLabel = new CpoAttributeLabelNode(this);
    }
  }

  public void setClassName(String className) {
    if ((className == null && cpoClass.getName() == null) || (cpoClass.getName() != null && cpoClass.getName().equals(className))) {
      return;
    }
    cpoClass.setName(className);
    this.setDirty(true);
  }

  @Override
  public int compareTo(CpoClassNode ccn) {
    return getDisplayClassName().compareTo(ccn.getDisplayClassName());
  }
}
