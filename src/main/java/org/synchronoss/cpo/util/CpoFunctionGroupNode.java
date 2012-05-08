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
import org.synchronoss.cpo.meta.domain.*;

import javax.swing.*;

public class CpoFunctionGroupNode extends AbstractCpoNode {

  public CpoFunctionGroupNode(CpoFunctionGroup functionGroup) {
    super(functionGroup);
  }

  @Override
  public CpoFunctionGroup getUserObject() {
    return (CpoFunctionGroup)super.getUserObject();
  }

  @Override
  public CpoFunctionGroupLabelNode getParent() {
    return (CpoFunctionGroupLabelNode)super.getParent();
  }

  @Override
  public boolean isProtected() {
    return getParent().isProtected();
  }

  @Override
  public JPanel getPanelForSelected() {
    return null;
  }

  @Override
  public void performRemove() throws CpoException {
    getProxy().removeFunctionGroup(this);
  }

  public String getGroupName() {
    return getUserObject().getName();
  }

  public String getType() {
    return getUserObject().getType();
  }

  public void setGroupName(String groupName) {
    if ((groupName == null && getUserObject().getName() == null) || (getUserObject().getName() != null && getUserObject().getName().equals(groupName))) {
      return;
    }

    getUserObject().setName(groupName);
    this.setDirty(true);
  }
}