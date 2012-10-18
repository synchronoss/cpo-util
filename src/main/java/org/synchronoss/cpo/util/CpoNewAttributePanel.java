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
import org.synchronoss.cpo.CpoException;
import org.synchronoss.cpo.meta.domain.CpoAttribute;

import javax.swing.*;
import java.awt.*;

public class CpoNewAttributePanel extends JPanel {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  // Version Id for this class
  private static final long serialVersionUID = 1L;

  protected JTextField jTextAtt = new JTextField();
  protected JTextField jTextDataName = new JTextField();
  protected JComboBox jComDataType;
  protected JTextField jTextTransform = new JTextField();

  protected CpoAttributeLabelNode cpoAttributeLabelNode;

  public CpoNewAttributePanel(CpoAttributeLabelNode cpoAttributeLabelNode) {
    this.cpoAttributeLabelNode = cpoAttributeLabelNode;
    this.jComDataType = new JComboBox(cpoAttributeLabelNode.getProxy().getAllowableDataTypes());
    try {
      jbInit();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(382, 281));
    this.setLayout(new GridBagLayout());

    JLabel jLabAtt = new JLabel();
    jLabAtt.setText("Attribute:");
    this.add(jLabAtt, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jTextAtt.setPreferredSize(new Dimension(100, 22));
    jTextAtt.setMinimumSize(new Dimension(100, 22));
    this.add(jTextAtt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JLabel jLabDataName = new JLabel();
    jLabDataName.setText("Data Name:");
    this.add(jLabDataName, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jTextDataName.setMinimumSize(new Dimension(100, 22));
    jTextDataName.setPreferredSize(new Dimension(100, 22));
    this.add(jTextDataName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JLabel jLabDataType = new JLabel();
    jLabDataType.setText("Data Type:");
    this.add(jLabDataType, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    this.add(jComDataType, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JLabel jLabTransform = new JLabel();
    jLabTransform.setText("Transform Class:");
    this.add(jLabTransform, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jTextTransform.setMinimumSize(new Dimension(100, 22));
    jTextTransform.setPreferredSize(new Dimension(100, 22));
    this.add(jTextTransform, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  public CpoAttribute createCpoAttribute() throws CpoException {

    String javaName = jTextAtt.getText();

    if (cpoAttributeLabelNode.getParent().attributeExists(javaName)) {
      CpoUtil.showErrorMessage("An attribute with that name already exists.");
      return null;
    }

    Proxy proxy = cpoAttributeLabelNode.getProxy();
    CpoAttribute attribute = proxy.createCpoAttribute();
    attribute.setJavaName(javaName);
    attribute.setDataName(jTextDataName.getText());
    attribute.setDataType((String)jComDataType.getSelectedItem());
    attribute.setTransformClassName(jTextTransform.getText());

    // figure out the java type
    attribute.setJavaType(proxy.getJavaTypeName(attribute));

    return attribute;
  }
}