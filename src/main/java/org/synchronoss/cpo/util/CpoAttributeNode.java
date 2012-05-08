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
import org.synchronoss.cpo.meta.domain.CpoAttribute;

import javax.swing.*;

public class CpoAttributeNode extends AbstractCpoNode {

  public CpoAttributeNode(CpoAttribute attribute) {
    super(attribute);
  }

  @Override
  public CpoAttribute getUserObject() {
    return (CpoAttribute)super.getUserObject();
  }

  @Override
  public CpoAttributeLabelNode getParent() {
    return (CpoAttributeLabelNode)super.getParent();
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
    getProxy().removeAttribute(this);
  }

  public String getJavaName() {
    return getUserObject().getJavaName();
  }

  public String getDataName() {
    return getUserObject().getDataName();
  }

  public String getDataType() {
    return getUserObject().getDataType();
  }

  public String getTransformClassName() {
    return getUserObject().getTransformClassName();
  }

  public void setDataName(String dataName) {
    if (dataName.equals(this.getDataName()))
      return;
    this.getUserObject().setDataName(dataName);
    this.setDirty(true);
  }

  public void setAttribute(String attribute) {
    if (attribute.equals(this.getJavaName()))
      return;
    this.getUserObject().setJavaName(attribute);
    this.setDirty(true);
  }

  public void setDataType(String dataType) {
    if (dataType.equals(this.getDataType()))
      return;
    this.getUserObject().setDataType(dataType);
    this.setDirty(true);
  }

  public void setTransformClass(String transform) {
    if (transform.equals(this.getTransformClassName()))
      return;
    this.getUserObject().setTransformClassName(transform);
    this.setDirty(true);
  }
}