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
package org.synchronoss.cpo.util.jdbc;

import org.synchronoss.cpo.util.*;

import javax.swing.*;

public class JdbcAttributeLabelNode extends CpoAttributeLabelNode {

  public JdbcAttributeLabelNode() {
    super();
  }

  @Override
  public JdbcClassNode getParent() {
    return (JdbcClassNode)super.getParent();
  }

  @Override
  public JPanel getPanelForSelected() {
    return new CpoAttributePanel(this, new JdbcAttributeTableModel(this));
  }

  @Override
  public JdbcNewAttributePanel getNewAttributePanel() {
    return new JdbcNewAttributePanel(this);
  }
}