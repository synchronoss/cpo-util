/**
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

import org.slf4j.*;
import org.synchronoss.cpo.CpoException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public abstract class AbstractCpoNode extends DefaultMutableTreeNode {

  private static Logger OUT = LoggerFactory.getLogger(AbstractCpoNode.class);

  private boolean dirty = false;
  private boolean remove = false;
  private boolean isnew = false;
  private boolean isProtected = false;

  protected Set<AbstractCpoNode> dirtyChildren = new HashSet<AbstractCpoNode>();
  protected Set<AbstractCpoNode> newChildren = new HashSet<AbstractCpoNode>();
  protected Set<AbstractCpoNode> removeChildren = new HashSet<AbstractCpoNode>();

  public AbstractCpoNode() {
    super();
  }

  public AbstractCpoNode(Object userObject) {
    super(userObject);
  }

  public AbstractCpoNode(Object userObject, boolean allowsChildren) {
    super(userObject, allowsChildren);
  }

  @Override
  public AbstractCpoNode getParent() {
    return (AbstractCpoNode)super.getParent();
  }

  @Override
  public CpoRootNode getRoot() {
    return (CpoRootNode)super.getRoot();
  }

  public Proxy getProxy() {
    return getRoot().getUserObject();
  }

  public abstract JPanel getPanelForSelected();

  public String getToolTipText() {
    return this.toString();
  }

  public boolean isProtected() {
    return isProtected;
  }

  public void setProtected(boolean b) {
    isProtected = b;
  }

  public void setDirty(boolean dirty) {
    if (this.dirty == dirty) {
      return;
    }
    this.dirty = dirty;
    getProxy().nodeChanged(this, this);
  }

  public boolean isDirty() {
    return this.dirty;
  }

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

  public boolean isChildDirty() {
    return (!dirtyChildren.isEmpty());
  }

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

  public boolean isRemove() {
    return this.remove;
  }

  public void setChildRemove(AbstractCpoNode childNode) {
    boolean notify = false;
    if (this.isChildRemove() != childNode.isRemove()) {
      notify = true;
    }
    if (childNode.isRemove() && childNode.isNew()) {
      removeChildren.remove(childNode);
      newChildren.remove(childNode);
      dirtyChildren.remove(childNode);
    } else if (childNode.isRemove()) {
      removeChildren.add(childNode);
    } else if (!childNode.isRemove()) {
      removeChildren.remove(childNode);
    }
    if (notify) {
      getProxy().nodeChanged(this, childNode);
    }
  }

  public boolean isChildRemove() {
    return (!removeChildren.isEmpty());
  }

  public void setNew(boolean isnew) {
    if (this.isnew == isnew) {
      return;
    }
    this.isnew = isnew;
    getProxy().nodeChanged(this, this);
  }

  public boolean isNew() {
    return this.isnew;
  }

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

  public boolean isChildNew() {
    return (!newChildren.isEmpty());
  }
}
