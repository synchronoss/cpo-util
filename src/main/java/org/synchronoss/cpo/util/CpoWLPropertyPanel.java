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
package org.synchronoss.cpo.util;
import javax.swing.*;
import java.awt.*;

public class CpoWLPropertyPanel extends JPanel  {

  /** Version Id for this class. */
  private static final long serialVersionUID=1L;

  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabWLURL = new JLabel();
  JTextField jTextWLURL = new JTextField();
  private JLabel jLabWLUser = new JLabel();
  private JLabel jLabWLPass = new JLabel();
  private JLabel jLabWLDefInitCtx = new JLabel();
  private JLabel jLabConnPool = new JLabel();
  private JLabel jLabCPOjndi = new JLabel();
  JTextField jTextWLUser = new JTextField();
  JTextField jTextWLPass = new JTextField();
  JTextField jTextDefInitCtx = new JTextField();
  JTextField jTextConPool = new JTextField();
  JTextField jTextCpoJndi = new JTextField();
  private JLabel jLabCpoUtilName = new JLabel();
  JTextField jTextCpoUtilName = new JTextField();

  public CpoWLPropertyPanel() {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    jLabWLURL.setText("WebLogic URL:");
    jTextWLURL.setMinimumSize(new Dimension(200, 17));
    jTextWLURL.setPreferredSize(new Dimension(200, 17));
    jTextWLURL.setText("t3://[HOSTNAME]:7001");
    jLabWLUser.setText("WebLogic User:");
    jLabWLPass.setText("WebLogic Pass:");
    jLabWLDefInitCtx.setText("Default Init Ctx:");
    jLabConnPool.setText("Conn Pool:");
    jLabCPOjndi.setText("CPO JNDI:");
    jTextWLUser.setText("wamu_data");
    jTextWLUser.setMinimumSize(new Dimension(200, 17));
    jTextWLUser.setPreferredSize(new Dimension(200, 17));
    jTextWLPass.setText("wamu_data");
    jTextWLPass.setPreferredSize(new Dimension(200, 17));
    jTextWLPass.setMinimumSize(new Dimension(200, 17));
    jTextDefInitCtx.setText("weblogic.jndi.WLInitialContextFactory");
    jTextDefInitCtx.setMinimumSize(new Dimension(200, 17));
    jTextDefInitCtx.setPreferredSize(new Dimension(200, 17));
    jTextConPool.setText("actnowConnectionPool");
    jTextConPool.setMinimumSize(new Dimension(200, 17));
    jTextConPool.setPreferredSize(new Dimension(200, 17));
    jTextCpoJndi.setText("ejbCpoManager");
    jTextCpoJndi.setMinimumSize(new Dimension(200, 17));
    jTextCpoJndi.setPreferredSize(new Dimension(200, 17));
    jLabCpoUtilName.setText("Cpo Util Name:");
    jTextCpoUtilName.setText("MyNewServer");
    jTextCpoUtilName.setMinimumSize(new Dimension(200, 17));
    jTextCpoUtilName.setPreferredSize(new Dimension(200, 17));
    this.add(jLabWLURL, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextWLURL, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabWLUser, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabWLPass, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabWLDefInitCtx, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabConnPool, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabCPOjndi, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextWLUser, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextWLPass, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextDefInitCtx, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextConPool, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextCpoJndi, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabCpoUtilName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextCpoUtilName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
}