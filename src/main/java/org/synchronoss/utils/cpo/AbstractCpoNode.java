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
import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;
import java.sql.Timestamp;

public abstract class AbstractCpoNode extends Observable implements TreeNode { //, Observer
  private boolean dirty = false;
  private boolean remove = false;
  private boolean isnew = false;
  protected Proxy prox;
  protected JTree jtree;
  protected AbstractCpoNode parent;
  private ArrayList dirtyChildren = new ArrayList();
  private ArrayList newChildren = new ArrayList();
  private ArrayList removeChildren = new ArrayList();
  public abstract void refreshChildren();
  public abstract JPanel getPanelForSelected();
  private String userName;
  private Date createDate;

  public Proxy getProxy() {
    if (this.parent != null)
      return parent.getProxy();
    else return this.prox;
  }
  public JTree getJtree() {
    if (this.jtree != null)
     return this.jtree;
    else if (parent != null)
      return parent.getJtree();
    else return null;
  }
  public TreeNode getParent() {
    return parent;
  }
  public void setDirty(boolean dirty) {
    if (this.dirty == dirty) return;
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
    return dirtyChildren.size() > 0 ? true : false;
  }
  public void setRemove(boolean remove) {
    if (this.remove == remove) return;
    this.remove = remove;
    this.setChanged();
    this.notifyObservers(this);
  }
  public boolean isRemove() {
    return this.remove;
  }
  public void setChildRemove(AbstractCpoNode childNode) {
//  OUT.debug ("SETCHILDREMOVE: "+this+", "+childNode+" isremove>"+childNode.isRemove()+" isnew>"+childNode.isNew());
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
    }
    else if (childNode.isRemove() && !removeChildren.contains(childNode)) {
      removeChildren.add(childNode);
    }
    else if (!childNode.isRemove() && removeChildren.contains(childNode)) {
      removeChildren.remove(childNode);
    }
    if (notify) {
      this.setChanged();
      this.notifyObservers(childNode);
    }
  }
  public boolean isChildRemove() {
    return removeChildren.size() > 0 ? true : false;
  }
  public void setNew(boolean isnew) {
    if (this.isnew == isnew) return;
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
//      OUT.debug (this+" adding new for "+childNode);
    }
    else if (!childNode.isNew() && newChildren.contains(childNode)) {
      newChildren.remove(childNode);
//      OUT.debug (this+" removing new for "+childNode);
    }
    if (notify) {
      this.setChanged();
      this.notifyObservers(childNode);      
    }
  }
  public boolean isChildNew() {
    return newChildren.size() > 0 ? true : false;
  }
  public void refreshMe() {
    ((DefaultTreeModel)this.jtree.getModel()).nodeStructureChanged(this);
  }
/*
  public void update(Observable obs, Object obj) {
//    OUT.debug (this+" was just updated by "+obs+" with object "+obj+".  About to notify "+this.countObservers()+" more observers");
    if (obj instanceof AbstractCpoNode) {
      this.setChildDirty((AbstractCpoNode)obj);
      this.setChildNew((AbstractCpoNode)obj);
      this.setChildRemove((AbstractCpoNode)obj);
      // need to notify tree that the model has possibly changed
//      if (this.getParent() == null)
      OUT.debug ("object: "+((AbstractCpoNode)obj).getParent().getClass().getName()
          +" is new?: "+((AbstractCpoNode)obj).isNew());
      int index = ((AbstractCpoNode)obs).getParent().getIndex((TreeNode)obs);
      OUT.debug ("parent class name: "+((AbstractCpoNode)obs).getParent().getClass().getName()
          +" child: "+obs.getClass().getName()+" index: "+index+" new?: "+((AbstractCpoNode)obs).isNew());
//      if (index > 0)
        if (((AbstractCpoNode)obs).isRemove()) {
          if (((AbstractCpoNode)obs).isNew()
              && !((AbstractCpoNode)obs).getParent().isLeaf()) {        
            ((DefaultTreeModel)this.getJtree().getModel()).nodesWereRemoved(((AbstractCpoNode)obs).getParent(),new int[]{index},new Object[]{obs});
          }
        }
        else if (((AbstractCpoNode)obs).isNew())
          ((DefaultTreeModel)this.getJtree().getModel()).nodesWereInserted(((AbstractCpoNode)obs).getParent(),new int[]{index});
        else 
          ((DefaultTreeModel)this.getJtree().getModel()).nodesChanged(((AbstractCpoNode)obs).getParent(),new int[]{index});
//      ((DefaultTreeModel)this.getJtree().getModel()).nodeStructureChanged(((AbstractCpoNode)obj).getParent());
    }
    else {
      OUT.debug ("Really shouldn't be in this segment of code ....");
      ((DefaultTreeModel)this.getJtree().getModel()).nodeStructureChanged(this);
    }
  }
*/
/*
  public void scrubNodes() {
    this.dirtyChildren.clear();
    this.newChildren.clear();
    this.removeChildren.clear();
    this.dirty = false;
    this.isnew = false;
    this.remove = false;
    Enumeration enum = this.children();
    if (enum != null) {
      while (enum.hasMoreElements()) {
        AbstractCpoNode workingNode = (AbstractCpoNode)enum.nextElement();
        if (workingNode != null)
          workingNode.scrubNodes();
      }
    }
  }
*/
  public String getUserName() {
    return this.userName;
  }
  public void setUserName(String userName) {
    this.userName = userName;
  }
  public Date getCreateDate() {
    return this.createDate;
  }
  public void setCreateDate(Timestamp createDate) {
    if (createDate == null) return;
    this.createDate = new Date(createDate.getTime());
  }
  public boolean isLabel() {
    return this.getClass().getName().toLowerCase().indexOf("label") != -1;
  }
}