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

import org.slf4j.*;
import org.synchronoss.cpo.util.*;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

public class CpoServerNode extends AbstractCpoNode {
  private List<CpoClassNode> classes; // CpoClassNode(s)
  private CpoQueryTextLabelNode cpoQTLN;
  private Logger OUT = LoggerFactory.getLogger(this.getClass());
  
  public CpoServerNode(Proxy prox, JTree jtree) {
    this.jtree = jtree;
    this.prox = prox;
    this.addObserver(prox);
  }

  @Override
  public JPanel getPanelForSelected() {
    return null;
  }
  
  public TreeNode getChildAt(int childIndex) {
    if (childIndex >= classes.size() + 1 || childIndex < 0)
      return null;
    else if (childIndex == 0)
      return this.cpoQTLN;
    
    return classes.get(childIndex-1);
  }

  public int getChildCount() {
    if (classes == null)
      refreshChildren();
    return this.classes.size()+1;
  }

  public int getIndex(TreeNode node) {
    if (this.classes == null) refreshChildren();
    if (node.equals(this.cpoQTLN)) return 0;
    for (int i = 0 ; i < classes.size() ; i++) {
      if (node.equals(classes.get(i))) return i+1;
    }
    return -1;
  }

  public boolean getAllowsChildren() {
    return true;
  }

  public boolean isLeaf() {
    return false;
  }

  @Override
  public Enumeration<AbstractCpoNode> children() {
    if (this.classes == null || this.cpoQTLN == null) refreshChildren();
    return new Enumeration<AbstractCpoNode>() {
      int count = 0;
      public AbstractCpoNode nextElement() {
        if (count == 0) {
          count++;
          return cpoQTLN;
        }
        AbstractCpoNode child = classes.get(count - 1);
        count++;
        return child;
      }
      public boolean hasMoreElements() {
        // classes + the query text node
        return (count < (classes.size() + 1));
      }
    };
  }

  @Override
  public String toString() {
    return this.prox.getServer();
  }
  @Override
  public void refreshChildren() {
    OUT.debug ("CpoServerNode refreshing data");
    try {
      this.classes = prox.getClasses(this);
    } catch (Exception pe) {
      CpoUtil.showException(pe);
    }
    this.cpoQTLN = new CpoQueryTextLabelNode(this);
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
