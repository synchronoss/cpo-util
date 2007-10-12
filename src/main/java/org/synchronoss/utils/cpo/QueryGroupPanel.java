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
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JComboBox;

public class QueryGroupPanel extends JPanel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabName = new JLabel();
  private JLabel jLabType = new JLabel();
  private JTextField jTextName = new JTextField();
  private JComboBox jComType = new JComboBox();
  private AbstractCpoNode cpoNode;

  public QueryGroupPanel(AbstractCpoNode cpoNode) {
    this.cpoNode = cpoNode;
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    jLabName.setText("Name:");
    jLabType.setText("Type:");
    this.jComType.addItem(Statics.CPO_TYPE_CREATE);
    this.jComType.addItem(Statics.CPO_TYPE_DELETE);
    this.jComType.addItem(Statics.CPO_TYPE_LIST);
    this.jComType.addItem(Statics.CPO_TYPE_RETRIEVE);
    this.jComType.addItem(Statics.CPO_TYPE_UPDATE);
    this.jComType.addItem(Statics.CPO_TYPE_EXIST);
    this.jComType.addItem(Statics.CPO_TYPE_EXECUTE);
    this.add(jLabName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabType, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jComType, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
  public String getGroupName() {
    return this.jTextName.getText();
  }
  public String getGroupType() {
    return (String)this.jComType.getSelectedItem();
  }
}