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
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JComboBox;

public class CpoNewAttributePanel extends JPanel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabAtt = new JLabel();
  JTextField jTextAtt = new JTextField();
  private JLabel jLabColName = new JLabel();
  JTextField jTextColName = new JTextField();
  private JLabel jLabColType = new JLabel();
  JComboBox jComColType;
  private JLabel jLabDBTable = new JLabel();
  JTextField jTextDBTable = new JTextField();
  private JLabel jLabTransform = new JLabel();
  JTextField jTextTransform = new JTextField();
  private JLabel jLabDBColumn = new JLabel();
  JTextField jTextDBColumn = new JTextField();

  public CpoNewAttributePanel(String[] cpoSqlTypes) {
    this.jComColType = new JComboBox(cpoSqlTypes);
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(382, 281));
    this.setLayout(gridBagLayout1);
    jLabAtt.setText("Attribute:");
    jTextAtt.setPreferredSize(new Dimension(100, 22));
    jTextAtt.setMinimumSize(new Dimension(100, 22));
    jLabColName.setText("Column Name:");
    jTextColName.setMinimumSize(new Dimension(100, 22));
    jTextColName.setPreferredSize(new Dimension(100, 22));
    jLabColType.setText("Column Type:");
    jLabTransform.setText("Transform Class:");
    jTextTransform.setMinimumSize(new Dimension(100, 22));
    jTextTransform.setPreferredSize(new Dimension(100, 22));
    jLabDBTable.setText("DB Table:");
    jTextDBTable.setMinimumSize(new Dimension(100, 22));
    jTextDBTable.setPreferredSize(new Dimension(100, 22));
    jLabDBColumn.setText("DB Column:");
    jTextDBColumn.setPreferredSize(new Dimension(100, 22));
    jTextDBColumn.setMinimumSize(new Dimension(100, 22));
    this.add(jLabAtt, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextAtt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabColName, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextColName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabColType, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jComColType, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabTransform, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextTransform, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabDBTable, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextDBTable, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabDBColumn, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextDBColumn, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
}