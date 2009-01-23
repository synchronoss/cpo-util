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

import javax.swing.*;
import java.awt.*;

public class CpoQueryPanelNorth extends JPanel  {
  /** Version Id for this class. */
  private static final long serialVersionUID=1L;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabSeq = new JLabel();
  JTextField jTextSeq = new JTextField();
  private JLabel jLabDesc = new JLabel();
  private JScrollPane jScrollDesc = new JScrollPane();
  JTextArea jTextAdesc = new JTextArea();
  private JLabel jLabSQL = new JLabel();
  private JScrollPane jScrollSQL = new JScrollPane();
  JTextArea jTextASQL = new JTextArea();
  private CpoQueryNode cpoQueryNode;
  JComboBox jComQueryObject;

  public CpoQueryPanelNorth(CpoQueryNode cpoQueryNode) {
    this.cpoQueryNode = cpoQueryNode;
    try {
      jbInit();
    } catch(Exception e) {
      CpoUtil.showException(e);
    }
  }

  private void jbInit() throws Exception {
//    this.setSize(new Dimension(175, 300));
    
    this.setLayout(gridBagLayout1);
    
    jLabSeq.setText("Sequence No:");
    this.add(jLabSeq, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    
    jTextSeq.setText(Integer.toString(cpoQueryNode.getSeqNo()));
    jTextSeq.setMinimumSize(new Dimension(30, 22));
    jTextSeq.setPreferredSize(new Dimension(30, 22));
    this.add(jTextSeq, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    
    jLabDesc.setText("Description:");
    this.add(jLabDesc, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    
    jTextAdesc.setLineWrap(true);
    jTextAdesc.setText(cpoQueryNode.getDesc());
    jScrollDesc.getViewport().add(jTextAdesc, null);
    jScrollDesc.getViewport().setMinimumSize(new Dimension(200,20));
    jScrollDesc.getViewport().setPreferredSize(new Dimension(200,20));
    this.add(jScrollDesc, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 230, 25));
    
    jLabSQL.setText("SQL:");
    this.add(jLabSQL, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    
    jTextASQL.setLineWrap(true);
    jTextASQL.setText(cpoQueryNode.getSQL());
    jScrollSQL.getViewport().add(jTextASQL, null);
    jScrollSQL.getViewport().setMinimumSize(new Dimension(200,50));
    jScrollSQL.getViewport().setPreferredSize(new Dimension(200,100));
    this.add(jScrollSQL, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 230, 45));
  }
}