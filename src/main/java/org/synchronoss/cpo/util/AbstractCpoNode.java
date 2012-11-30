/*
 * Copyright (C) 2003-2012 David E. Berry, Michael A. Bellomo
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * A copy of the GNU Lesser General Public License may also be found at
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.synchronoss.cpo.util;

import org.synchronoss.cpo.CpoException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public abstract class AbstractCpoNode extends DefaultMutableTreeNode {

  private boolean dirty = false;
  private boolean remove = false;
  private boolean isnew = false;
  private boolean isProtected = false;

  protected Set<AbstractCpoNode> dirtyChildren = new HashSet<AbstractCpoNode>();
  protected Set<AbstractCpoNode> newChildren = new HashSet<AbstractCpoNode>();
  protected Set<AbstractCpoNode> removeChildren = new HashSet<AbstractCpoNode>();

  /**
   * @see DefaultMutableTreeNode#DefaultMutableTreeNode()
   */
  public AbstractCpoNode() {
    super();
  }

  /**
   * @see DefaultMutableTreeNode#DefaultMutableTreeNode(Object userObject);
   */
  public AbstractCpoNode(Object userObject) {
    super(userObject);
  }

  /**
   * @see DefaultMutableTreeNode#DefaultMutableTreeNode(Object userObject, boolean allowsChildren)
   */
  public AbstractCpoNode(Object userObject, boolean allowsChildren) {
    super(userObject, allowsChildren);
  }

  /**
   * @see DefaultMutableTreeNode#getParent()
   */
  @Override
  public AbstractCpoNode getParent() {
    return (AbstractCpoNode)super.getParent();
  }

  /**
   * @see DefaultMutableTreeNode#getRoot()
   */
  @Override
  public CpoRootNode getRoot() {
    return (CpoRootNode)super.getRoot();
  }

  /**
   * @return The Proxy for this node
   */
  public Proxy getProxy() {
    return getRoot().getUserObject();
  }

  /**
   * @return The JPanel to display for this node
   */
  public abstract JPanel getPanelForSelected();

  /**
   * @return Text to display for a tooltip for this node
   */
  public String getToolTipText() {
    return this.toString();
  }

  /**
   * @return True if this node is protected, false otherwise
   */
  public boolean isProtected() {
    return isProtected;
  }

  /**
   * Sets if this node is protected.
   *
   * @param b true if protected, false otherwise
   */
  public void setProtected(boolean b) {
    isProtected = b;
  }

  /**
   * Sets if this node is dirty
   * @param dirty True if this node is being changed
   */
  public void setDirty(boolean dirty) {
    if (this.dirty == dirty) {
      return;
    }
    this.dirty = dirty;
    getProxy().nodeChanged(this, this);
  }

  /**
   * @return True if this node is dirty, false otherwise
   */
  public boolean isDirty() {
    return this.dirty;
  }

  /**
   * Called to inform the node that the supplied child is dirty
   */
  public void setChildDirty(AbstractCpoNode childNode) {
    boolean notify = false;
    if (this.isChildDirty() != childNode.isDirty()) {
      notify = true;
    }

    if (childNode.isDirty()) {
      dirtyChildren.add(childNode);
    } else if (!childNode.isDirty()) {
      dirtyChildren.remove(childNode);
    }
    if (notify) {
      getProxy().nodeChanged(this, childNode);
    }
  }

  /**
   * @return True if this node has any dirty children, false otherwise
   */
  public boolean isChildDirty() {
    return (!dirtyChildren.isEmpty());
  }

  /**
   * Sets if this node is removed
   * @param remove True if this node is being removed
   */
  public void setRemove(boolean remove) {
    if (this.remove == remove) {
      return;
    }
    this.remove = remove;
    getProxy().nodeChanged(this, this);
  }

  public void performRemove() throws CpoException {
    // do nothing
  }

  /**
   * @return True if this node is removed, false otherwise
   */
  public boolean isRemove() {
    return this.remove;
  }

  /**
   * Called to inform the node that the supplied child is removed
   */
  public void setChildRemove(AbstractCpoNode childNode) {
    if (childNode.isRemove()) {
      removeChildren.add(childNode);
      if (childNode.isNew()) {
        newChildren.remove(childNode);
        dirtyChildren.remove(childNode);
      }
    } else if (!childNode.isRemove()) {
      removeChildren.remove(childNode);
    }
    getProxy().nodeChanged(this, childNode);
  }

  /**
   * @return True if this node has any removed children, false otherwise
   */
  public boolean isChildRemove() {
    return (!removeChildren.isEmpty());
  }

  /**
   * Sets If this node is new
   * @param isnew True if this node is new, false otherwise
   */
  public void setNew(boolean isnew) {
    if (this.isnew == isnew) {
      return;
    }
    this.isnew = isnew;
    getProxy().nodeChanged(this, this);
  }

  /**
   * @return True if this node is new, false otherwise
   */
  public boolean isNew() {
    return this.isnew;
  }

  /**
   * Called to inform the node that the supplied child is new
   */
  public void setChildNew(AbstractCpoNode childNode) {
    boolean notify = false;
    if (this.isChildNew() != childNode.isNew()) {
      notify = true;
    }
    if (childNode.isNew()) {
      newChildren.add(childNode);
    } else if (!childNode.isNew()) {
      newChildren.remove(childNode);
    }
    if (notify) {
      getProxy().nodeChanged(this, childNode);
    }
  }

  /**
   * @return True if this node has any new children, false otherwise
   */
  public boolean isChildNew() {
    return (!newChildren.isEmpty());
  }
}
