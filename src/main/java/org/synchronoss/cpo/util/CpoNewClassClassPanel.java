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

public class CpoNewClassClassPanel extends JPanel {

  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  // Version Id for this class
  private static final long serialVersionUID = 1L;
  private JLabel jLabClassName = new JLabel();
  private JTextField jTextClassName = new JTextField();

  public CpoNewClassClassPanel(Proxy proxy) {
    try {
      jbInit();
    } catch (Exception e) {
      OUT.error(e.getMessage(), e);
    }

    if (proxy.getDefaultPackageName() != null) {
      jTextClassName.setText(proxy.getDefaultPackageName());
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(new GridBagLayout());

    jLabClassName.setText("New class Name:");
    this.add(jLabClassName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jTextClassName.setMinimumSize(new Dimension(300, 20));
    jTextClassName.setPreferredSize(new Dimension(300, 20));
    this.add(jTextClassName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  public String getClassName() {
    return jTextClassName.getText();
  }
}