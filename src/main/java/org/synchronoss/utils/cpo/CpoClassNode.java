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
import java.util.Enumeration;

public class CpoClassNode extends AbstractCpoNode {
  private String name,class_id;
  private CpoQueryGroupLabelNode queryGroupLabel;
  private CpoAttributeLabelNode attributeLabel;
  
  public CpoClassNode(String name, String class_id, AbstractCpoNode parent) {
    this.name = name;
    this.class_id = class_id;
    this.parent = parent;
    if (parent != null)
      this.addObserver(parent.getProxy());
//    this.addObserver(parent);
  }

  public JPanel getPanelForSelected() {
    return new CpoTesterPanel(this);
  }
    
  public TreeNode getChildAt(int childIndex) {
    if (childIndex == 0) return this.queryGroupLabel;
    else if (childIndex == 1) return this.attributeLabel;
    else return null;
  }

  public int getChildCount() {
    return 2;
  }

  public int getIndex(TreeNode node) {
    if (node == this.queryGroupLabel) return 0;
    else if (node == this.attributeLabel) return 1;
    else return -1;
  }

  public boolean getAllowsChildren() {
    return true;
  }

  public boolean isLeaf() {
    return false;
  }

  public Enumeration children() {
    if (queryGroupLabel == null || attributeLabel == null) refreshChildren();
    return new Enumeration() {
      int count = 0;
      public Object nextElement() {
        if (count++==0) return queryGroupLabel;
        else return attributeLabel;
      }
      public boolean hasMoreElements() {
        if (count > 1) return false;
          else return true;
      }
    };
  }

  public String toString() {
    return this.getDisplayClassName();
  }
  public String getClassId() {
    return this.class_id;
  }
  public String getClassName() {
    return this.name;
  }
  public String getDisplayClassName() {
    if (!this.getProxy().getClassNameToggle() && getClassName().lastIndexOf(".") != -1 && getClassName().length() > getClassName().lastIndexOf(".")+1) {
      return getClassName().substring(getClassName().lastIndexOf(".")+1);
    }
    else {
      return getClassName();
    }
  }
  public void refreshChildren() {
    if (this.queryGroupLabel == null || this.attributeLabel == null) {
//      OUT.debug("CpoClassNode refreshing data");
      this.queryGroupLabel = new CpoQueryGroupLabelNode(this);
      this.attributeLabel = new CpoAttributeLabelNode(this);
    }
  }
  public void setClassName(String className) {
    if ((className == null && this.name == null) || (this.name != null && this.name.equals(className))) return;
    this.name = className;
    this.setDirty(true);
  }
}
