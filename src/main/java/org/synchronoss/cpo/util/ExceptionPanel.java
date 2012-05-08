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

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class ExceptionPanel extends JPanel {

  // Version Id for this class
  private static final long serialVersionUID = 1L;

  private JScrollPane jScrollExc = new JScrollPane();
  private JTextArea jTextExc = new JTextArea();

  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  public ExceptionPanel(Throwable ex) {
    try {
      jbInit(ex);
    } catch (Exception e) {
      OUT.error(e.getMessage(), e);
    }
  }

  private void jbInit(Throwable e) throws Exception {
    this.setBorder(BorderFactory.createEtchedBorder());
    this.setSize(new Dimension(600, 450));
    this.setPreferredSize(new Dimension(600, 450));
    this.setLayout(new BorderLayout());
    jScrollExc.setBounds(new Rectangle(190, 145, 3, 3));
    jScrollExc.getViewport().add(jTextExc, null);
    this.add(jScrollExc, null);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    jTextExc.setText(sw.toString());
  }
}