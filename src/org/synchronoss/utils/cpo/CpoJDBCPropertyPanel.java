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
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

public class CpoJDBCPropertyPanel extends JPanel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;

  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabJdbcUrl = new JLabel();
  private JLabel jLabJdbcDriver = new JLabel();
  JTextField jTextJdbcUrl = new JTextField();
  JTextField jTextJdbcDriver = new JTextField();
  private JLabel jLabCpoUtilName = new JLabel();
  JTextField jTextCpoUtilName = new JTextField();
  private JLabel jLabJdbcParams = new JLabel();
  private JScrollPane jScrollParams = new JScrollPane();
  JTextArea jTextAJDBCParams = new JTextArea();

  public CpoJDBCPropertyPanel() {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    this.setSize(new Dimension(431, 258));
    jLabJdbcUrl.setText("JDBC URL:");
    jLabJdbcDriver.setText("JDBC Driver");
    jTextJdbcUrl.setText("jdbc:oracle:thin:[USER]/[PASS]@[HOSTNAME]:1521:[INSTANCE]");
    jTextJdbcUrl.setPreferredSize(new Dimension(300, 17));
    jTextJdbcUrl.setMinimumSize(new Dimension(300, 17));
    jTextJdbcDriver.setText("oracle.jdbc.driver.OracleDriver");
    jTextJdbcDriver.setMinimumSize(new Dimension(300, 17));
    jTextJdbcDriver.setPreferredSize(new Dimension(300, 17));
    jLabCpoUtilName.setText("Cpo Util Name:");
    jTextCpoUtilName.setText("MyNewServer");
    jTextCpoUtilName.setMinimumSize(new Dimension(300, 17));
    jTextCpoUtilName.setPreferredSize(new Dimension(300, 17));
    jLabJdbcParams.setText("JDBC Params");
    jScrollParams.setPreferredSize(new Dimension(300, 100));
    jScrollParams.setMinimumSize(new Dimension(300, 100));
    this.add(jLabJdbcUrl, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabJdbcDriver, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextJdbcUrl, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextJdbcDriver, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabCpoUtilName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextCpoUtilName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabJdbcParams, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jScrollParams.getViewport().add(jTextAJDBCParams, null);
    this.add(jScrollParams, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
}