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

import org.synchronoss.cpo.CpoException;
import org.synchronoss.cpo.meta.domain.CpoClass;

import javax.swing.*;
import java.util.Enumeration;

public class CpoClassNode extends AbstractCpoNode implements Comparable<CpoClassNode> {

  private boolean displayShortClassName = false;

  public CpoClassNode(CpoClass cpoClass) {
    super(cpoClass);
    createLabelNodes();
  }

  protected void createLabelNodes() {
    // create the attribute label node
    this.add(new CpoAttributeLabelNode());

    // create the function group label node
    this.add(new CpoFunctionGroupLabelNode());
  }

  @Override
  public CpoClass getUserObject() {
    return (CpoClass)super.getUserObject();
  }

  @Override
  public CpoRootNode getParent() {
    return (CpoRootNode)super.getParent();
  }

  @Override
  public JPanel getPanelForSelected() {
    return new TesterPanel(this);
  }

  public CpoAttributeLabelNode getAttributeLabelNode() {
    return (CpoAttributeLabelNode)this.getChildAt(0);
  }

  public CpoFunctionGroupLabelNode getFunctionGroupLabelNode() {
    return (CpoFunctionGroupLabelNode)this.getChildAt(1);
  }

  public boolean attributeExists(String name) {
    Enumeration e = getAttributeLabelNode().children();
    while (e.hasMoreElements()) {
      CpoAttributeNode cpoAttributeNode = (CpoAttributeNode)e.nextElement();
      if (cpoAttributeNode.getJavaName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return this.getDisplayClassName();
  }

  @Override
  public String getToolTipText() {
    return getUserObject().toString();
  }

  @Override
  public void performRemove() throws CpoException {
    getProxy().removeClass(this);
  }

  public void toggleClassNames() {
    this.displayShortClassName = !displayShortClassName;
  }

  public String getDisplayClassName() {
    String className = getUserObject().getName();
    if (displayShortClassName && className.lastIndexOf(".") != -1 && className.length() > className.lastIndexOf(".") + 1) {
      return className.substring(className.lastIndexOf(".") + 1);
    }
    return className;
  }

  public void setClassName(String className) {
    CpoClass cpoClass = getUserObject();
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
