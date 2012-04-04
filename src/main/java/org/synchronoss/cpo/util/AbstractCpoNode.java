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
package org.synchronoss.cpo.util;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

public abstract class AbstractCpoNode extends Observable implements TreeNode { //, Observer
  
  private boolean dirty = false;
  private boolean remove = false;
  private boolean isnew = false;
  private boolean isProtected = false;

  protected Proxy prox;
  protected JTree jtree;
  protected AbstractCpoNode parent;

  private List<AbstractCpoNode> dirtyChildren = new ArrayList<AbstractCpoNode>();
  private List<AbstractCpoNode> newChildren = new ArrayList<AbstractCpoNode>();
  private List<AbstractCpoNode> removeChildren = new ArrayList<AbstractCpoNode>();

  public abstract void refreshChildren();
  public abstract JPanel getPanelForSelected();

  public Proxy getProxy() {
    if (this.parent != null)
      return parent.getProxy();
    
    return this.prox;
  }

  public JTree getJtree() {
    if (this.jtree != null)
      return this.jtree;
    else if (parent != null)
      return parent.getJtree();
    else
      return null;
  }
  
  public TreeNode getParent() {
    return parent;
  }

  public boolean isProtected() {
    return isProtected;
  }

  public void setProtected(boolean b) {
    isProtected = b;
  }

  public void setDirty(boolean dirty) {
    if (this.dirty == dirty)
      return;
    this.dirty = dirty;
    this.setChanged();
    this.notifyObservers(this);
  }
  
  public boolean isDirty() {
    return this.dirty;
  }

  public void setChildDirty(AbstractCpoNode childNode) {
    boolean notify = false;
    if (this.isChildDirty() != childNode.isDirty())
      notify = true;
    if (childNode.isDirty() && !dirtyChildren.contains(childNode)) {
      dirtyChildren.add(childNode);
    }
    else if (!childNode.isDirty() && dirtyChildren.contains(childNode)) {
      dirtyChildren.remove(childNode);
    }
    if (notify) {
      this.setChanged();
      this.notifyObservers(childNode);      
    }
  }

  public boolean isChildDirty() {
    return (dirtyChildren.size() > 0);
  }

  public void setRemove(boolean remove) {
    if (this.remove == remove)
      return;
    this.remove = remove;
    this.setChanged();
    this.notifyObservers(this);
  }

  public boolean isRemove() {
    return this.remove;
  }

  public void setChildRemove(AbstractCpoNode childNode) {
    boolean notify = false;
    if (this.isChildRemove() != childNode.isRemove())
      notify = true;
    if (childNode.isRemove() && childNode.isNew()) {
      if (removeChildren.contains(childNode))
        removeChildren.remove(childNode);
      if (newChildren.contains(childNode))
        newChildren.remove(childNode);
      if (dirtyChildren.contains(childNode))
        dirtyChildren.remove(childNode);
    } else if (childNode.isRemove() && !removeChildren.contains(childNode)) {
      removeChildren.add(childNode);
    } else if (!childNode.isRemove() && removeChildren.contains(childNode)) {
      removeChildren.remove(childNode);
    }
    if (notify) {
      this.setChanged();
      this.notifyObservers(childNode);
    }
  }

  public boolean isChildRemove() {
    return (removeChildren.size() > 0);
  }

  public void setNew(boolean isnew) {
    if (this.isnew == isnew)
      return;
    this.isnew = isnew;
    this.setChanged();
    this.notifyObservers(this);
  }

  public boolean isNew() {
    return this.isnew;
  }

  public void setChildNew(AbstractCpoNode childNode) {
    boolean notify = false;
    if (this.isChildNew() != childNode.isNew())
      notify = true;
    if (childNode.isNew() && !newChildren.contains(childNode)) {
      newChildren.add(childNode);
    } else if (!childNode.isNew() && newChildren.contains(childNode)) {
      newChildren.remove(childNode);
    }
    if (notify) {
      this.setChanged();
      this.notifyObservers(childNode);      
    }
  }

  public boolean isChildNew() {
    return (newChildren.size() > 0);
  }

  public void refreshMe() {
    ((DefaultTreeModel)this.jtree.getModel()).nodeStructureChanged(this);
  }

  public abstract String getUserName();

  public abstract Calendar getCreateDate();

  public boolean isLabel() {
    return this.getClass().getName().toLowerCase().indexOf("label") != -1;
  }
  
  public abstract Enumeration<? extends AbstractCpoNode> children();
}
