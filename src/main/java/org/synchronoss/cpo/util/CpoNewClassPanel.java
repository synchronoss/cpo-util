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

import javax.swing.*;
import java.awt.*;

public class CpoNewClassPanel extends JPanel {

  private static Logger OUT = LoggerFactory.getLogger(CpoNewClassPanel.class);

  // Version Id for this class
  private static final long serialVersionUID = 1L;
  private JTextField jTextClassName = new JTextField();
  private JTextArea jTextAreaExpression = new JTextArea();
  private JComboBox connectionComboBox = new JComboBox();

  public CpoNewClassPanel(Proxy proxy) {
    this(proxy, null);
  }

  public CpoNewClassPanel(Proxy proxy, String className) {
    try {
      connectionComboBox.setModel(new DefaultComboBoxModel(proxy.getConnectionList()));
      jbInit();
    } catch (Exception e) {
      OUT.error(e.getMessage(), e);
    }

    if (className != null) {
      jTextClassName.setText(className);
    } else if (proxy.getDefaultPackageName() != null) {
      jTextClassName.setText(proxy.getDefaultPackageName());
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(new GridBagLayout());

    JLabel jLabClassName = new JLabel("Class Name:");
    this.add(jLabClassName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextClassName.setMinimumSize(new Dimension(300, 20));
    jTextClassName.setPreferredSize(new Dimension(300, 20));
    this.add(jTextClassName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    JLabel jLabConnection = new JLabel("Connection:");
    this.add(jLabConnection, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    this.add(connectionComboBox, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    JLabel jLabExpression = new JLabel("Initialization Expression:");
    this.add(jLabExpression, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextAreaExpression.setLineWrap(true);
    JScrollPane jScrollPane = new JScrollPane();
    jScrollPane.getViewport().add(jTextAreaExpression, null);
    jScrollPane.setPreferredSize(new Dimension(300, 200));
    this.add(jScrollPane, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0)); //169, 112));
  }

  public String getClassName() {
    return jTextClassName.getText();
  }

  public String getExpression() {
    return jTextAreaExpression.getText();
  }

  public String getConnection() {
    if (connectionComboBox.getSelectedIndex() != -1) {
      return (String)connectionComboBox.getSelectedItem();
    }

    return null;
  }
}