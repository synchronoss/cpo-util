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
import org.synchronoss.cpo.meta.domain.CpoFunction;

import javax.swing.*;

public class CpoFunctionNode extends AbstractCpoNode {

  public CpoFunctionNode(CpoFunction function) {
    super(function);
  }

  @Override
  public CpoFunction getUserObject() {
    return (CpoFunction)super.getUserObject();
  }

  @Override
  public CpoFunctionGroupNode getParent() {
    return (CpoFunctionGroupNode)super.getParent();
  }

  @Override
  public JPanel getPanelForSelected() {
    return new CpoFunctionPanel(this, new CoreArgumentTableModel(this));
  }

  @Override
  public void performRemove() throws CpoException {
    getProxy().removeFunction(this);
  }

  @Override
  public boolean isProtected() {
    return getParent().isProtected();
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public String toString() {
    int seqNo = getParent().getIndex(this) + 1;

    if (this.getName() == null || this.getName().equals("")) {
      return seqNo + " (NO NAME)";
    }

    return seqNo + " (" + this.getName() + ")";
  }

  public String getExpression() {
    return getUserObject().getExpression();
  }

  public void setExpression(String expression) {
    if (getUserObject().getExpression().equals(expression)) {
      return;
    }

    // if the expression changed, mark the node dirty
    getUserObject().setExpression(expression);
    setDirty(true);
  }

  public String getName() {
    return getUserObject().getName();
  }

  public void setName(String name) {
    if ((name == null && getName() == null) || (getName() != null && getName().equals(name))) {
      return;
    }

    getUserObject().setName(name);
    setDirty(true);
  }

  /**
   * Returns the index of this node in relation
   */
  public int getSeqNo() {
    return getParent().getIndex(this) + 1;
  }
}
