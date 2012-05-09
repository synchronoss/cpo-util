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

import org.synchronoss.cpo.CpoException;
import org.synchronoss.cpo.jdbc.JdbcCpoAttribute;
import org.synchronoss.cpo.util.CpoNewAttributePanel;

import javax.swing.*;
import java.awt.*;

public class JdbcNewAttributePanel extends CpoNewAttributePanel {

  // Version Id for this class
  private static final long serialVersionUID = 1L;

  protected JTextField jTextDBTable = new JTextField();
  protected JTextField jTextDBColumn = new JTextField();

  public JdbcNewAttributePanel(JdbcAttributeLabelNode cpoAttributeLabelNode) {
    super(cpoAttributeLabelNode);

    try {
      jbInit();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void jbInit() throws Exception {
    JLabel jLabDBTable = new JLabel();
    jLabDBTable.setText("DB Table:");
    this.add(jLabDBTable, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jTextDBTable.setMinimumSize(new Dimension(100, 22));
    jTextDBTable.setPreferredSize(new Dimension(100, 22));
    this.add(jTextDBTable, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JLabel jLabDBColumn = new JLabel();
    jLabDBColumn.setText("DB Column:");
    this.add(jLabDBColumn, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jTextDBColumn.setPreferredSize(new Dimension(100, 22));
    jTextDBColumn.setMinimumSize(new Dimension(100, 22));
    this.add(jTextDBColumn, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  @Override
  public JdbcCpoAttribute createCpoAttribute() throws CpoException {
    JdbcCpoAttribute attribute = (JdbcCpoAttribute)super.createCpoAttribute();
    attribute.setDbTable(jTextDBTable.getText());
    attribute.setDbColumn(jTextDBColumn.getText());
    return attribute;
  }
}