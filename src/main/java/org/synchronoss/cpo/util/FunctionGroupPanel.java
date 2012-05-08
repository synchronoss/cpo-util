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

import org.slf4j.*;
import org.synchronoss.cpo.core.cpoCoreMeta.StFunctionGroupType;

import javax.swing.*;
import java.awt.*;

public class FunctionGroupPanel extends JPanel {

  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  // Version Id for this class
  private static final long serialVersionUID = 1L;

  private JLabel jLabName = new JLabel();
  private JLabel jLabType = new JLabel();
  private JTextField jTextName = new JTextField();
  private JComboBox jComType = new JComboBox();

  public FunctionGroupPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      OUT.error(e.getMessage(), e);
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(new GridBagLayout());

    jLabName.setText("Name:");
    this.add(jLabName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jLabType.setText("Type:");
    this.add(jLabType, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    this.add(jTextName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    this.jComType.addItem(StFunctionGroupType.CREATE);
    this.jComType.addItem(StFunctionGroupType.DELETE);
    this.jComType.addItem(StFunctionGroupType.LIST);
    this.jComType.addItem(StFunctionGroupType.RETRIEVE);
    this.jComType.addItem(StFunctionGroupType.UPDATE);
    this.jComType.addItem(StFunctionGroupType.EXIST);
    this.jComType.addItem(StFunctionGroupType.EXECUTE);
    this.add(jComType, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
  }

  public String getGroupName() {
    return this.jTextName.getText();
  }

  public String getGroupType() {
    return this.jComType.getSelectedItem().toString();
  }
}