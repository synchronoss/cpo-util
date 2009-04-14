/*
 *  Copyright (C) 2006  Jay Colson
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *  
 *  A copy of the GNU Lesser General Public License may also be found at 
 *  http://www.gnu.org/licenses/lgpl.txt
 */
package org.synchronoss.utils.cpo;

import java.awt.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.Border;

public class MainFrame_AboutBoxPanel extends JPanel {
  
  /** Version Id for this class. */
  private static final long serialVersionUID=1L;

  private Border border = BorderFactory.createEtchedBorder();
  private GridBagLayout layoutMain = new GridBagLayout();
  private JLabel labelCompany = new JLabel();
  private JLabel labelCopyright = new JLabel();
  private JLabel labelAuthor = new JLabel();
  private JLabel labelTitle = new JLabel();
  private JLabel labelVersion = new JLabel();
  private ResourceBundle resources;

  public MainFrame_AboutBoxPanel() {
    try {
      resources = ResourceBundle.getBundle("cpoutil");
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(layoutMain);
    this.setBorder(border);
    labelTitle.setText("Title: " + resources.getString("cpoutil.title"));
    labelVersion.setText("Version: " + resources.getString("cpoutil.version"));
    labelAuthor.setText("Author: " + resources.getString("cpoutil.author"));
    labelCopyright.setText("Copyright: " + resources.getString("cpoutil.copyright"));
    labelCompany.setText("Company: " + resources.getString("cpoutil.company"));
    this.add(labelTitle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 0, 15), 0, 0));
    this.add(labelVersion, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
    this.add(labelAuthor, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
    this.add(labelCopyright, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
    this.add(labelCompany, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 5, 15), 0, 0));
  }
}