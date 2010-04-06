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

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

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
  private JLabel jLabQueryText = new JLabel();
  private JTextField jTextQuerySeach = new JTextField();
  JComboBox jComQueryText = new JComboBox();
  JComboBox jComQueryObject;
  private JLabel jLabListQ = new JLabel();
  private Logger OUT = Logger.getLogger(this.getClass());

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
    List<CpoQueryTextNode> queryText;
    try {
      queryText = cpoQueryNode.getProxy().getQueryText((CpoServerNode)cpoQueryNode.getParent().getParent().getParent().getParent());
      if (cpoQueryNode.getParent() instanceof CpoQueryGroupNode) {
        OUT.debug("cpoQueryNode's parent is a queryGroupNode");
        CpoQueryGroupNode cgNode = (CpoQueryGroupNode)cpoQueryNode.getParent();
        if (cgNode.getType().equals("LIST")) {
          OUT.debug("LIST server: "+cgNode.getParent().getParent().getParent());
          List<CpoClassNode> al = cgNode.getProxy().getClasses((AbstractCpoNode)cgNode.getParent().getParent().getParent());
          jComQueryObject = new JComboBox(new Vector<CpoClassNode>(al));
          //populate jComQueryObject
        }
      }
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      return;
    }
    
    for (CpoQueryTextNode qt : queryText) {
      this.jComQueryText.addItem(qt);
    }
    
    this.jTextQuerySeach.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent ke) {
      }
      public void keyPressed(KeyEvent ke) {
      }
      public void keyReleased(KeyEvent ke) {
        try {
          jComQueryText.removeAllItems();
          List<CpoQueryTextNode> qts = cpoQueryNode.getProxy().getQueryTextMatches(
              (CpoServerNode)cpoQueryNode.getParent().getParent().getParent().getParent(), jTextQuerySeach.getText());
          
          for (CpoQueryTextNode qt : qts) {
            jComQueryText.addItem(qt);
          }
        } catch (Exception pe) {
          CpoUtil.showException(pe);
        }
        try {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              jTextQuerySeach.requestFocus();
            }
          });
        } catch (Exception e) {
          // ignore
        }
      }
    });
    this.setLayout(gridBagLayout1);
    jLabSeq.setText("Sequence No:");
    jTextSeq.setText(Integer.toString(cpoQueryNode.getSeqNo()));
    jTextSeq.setMinimumSize(new Dimension(30, 22));
    jTextSeq.setPreferredSize(new Dimension(30, 22));
    jLabDesc.setText("Description:");
//    jTextAdesc.setText(cpoQueryNode.getDesc());
    jTextAdesc.setLineWrap(true);
    jLabSQL.setText("SQL:");
//    jTextASQL.setText(cpoQueryNode.getSQL());
    jTextASQL.setLineWrap(true);
    jLabQueryText.setText("Query Text:");
    jTextQuerySeach.setMinimumSize(new Dimension(200, 22));
    jTextQuerySeach.setPreferredSize(new Dimension(200, 22));
    jLabListQ.setText("LIST Query Class:");
    this.add(jLabSeq, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextSeq, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabDesc, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jScrollDesc.getViewport().add(jTextAdesc, null);
    jScrollDesc.getViewport().setMinimumSize(new Dimension(200,20));
    jScrollDesc.getViewport().setPreferredSize(new Dimension(200,20));
    this.add(jScrollDesc, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 230, 25));
    this.add(jLabSQL, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jScrollSQL.getViewport().add(jTextASQL, null);
    jScrollSQL.getViewport().setMinimumSize(new Dimension(200,50));
    jScrollSQL.getViewport().setPreferredSize(new Dimension(200,100));
    this.add(jScrollSQL, new GridBagConstraints(1, 5, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 230, 45));
    this.add(jLabQueryText, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jTextQuerySeach, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jComQueryText, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
/*    if (jComQueryObject != null) {
      this.add(jLabListQ, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      this.add(jComQueryObject, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }*/
  }
}