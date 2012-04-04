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

public class CpoQueryTextLabelNode extends AbstractCpoNode {

  public CpoQueryTextLabelNode(CpoServerNode serverNode) {
    this.parent = serverNode;
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
    return new CpoQueryTextPanel((CpoServerNode)getParent());
  }

  @Override
  public void refreshChildren() {
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
  public String toString() {
    return "Query Text";
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