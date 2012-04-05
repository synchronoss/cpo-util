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

import org.slf4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class CpoQueryPanelNorth extends JPanel  {

  /** Version Id for this class. */
  private static final long serialVersionUID=1L;
  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  private JLabel jLabSeq = new JLabel();
  JTextField jTextSeq = new JTextField();
  private JLabel jLabQueryText = new JLabel();
  private JTextField jTextQuerySearch = new JTextField();
  JComboBox jComQueryText = new JComboBox();
  private JButton newQueryTextButton = new JButton();
  private JLabel jLabDesc = new JLabel();
  JTextArea jTextAdesc = new JTextArea();
  private JScrollPane jScrollDesc = new JScrollPane();
  private JLabel jLabSQL = new JLabel();
  JTextArea jTextASQL = new JTextArea();
  private JScrollPane jScrollSQL = new JScrollPane();

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
    List<CpoQueryTextNode> queryText;
    try {
      queryText = cpoQueryNode.getProxy().getQueryText((CpoServerNode)cpoQueryNode.getParent().getParent().getParent().getParent());
      if (cpoQueryNode.getParent() instanceof CpoQueryGroupNode) {
        OUT.debug("cpoQueryNode's parent is a queryGroupNode");
        CpoQueryGroupNode cgNode = (CpoQueryGroupNode)cpoQueryNode.getParent();
        if (cgNode.getType().equals("LIST")) {
          OUT.debug("LIST server: "+cgNode.getParent().getParent().getParent());
          List<CpoClassNode> al = cgNode.getProxy().getClasses(cgNode.getParent().getParent().getParent());
          jComQueryObject = new JComboBox(new Vector<CpoClassNode>(al));
        }
      }
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      return;
    }

    setLayout(new GridBagLayout());

    jLabSeq.setText("Sequence No:");
    add(jLabSeq, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jTextSeq.setText(Integer.toString(cpoQueryNode.getSeqNo()));
    jTextSeq.setMinimumSize(new Dimension(50, 22));
    jTextSeq.setPreferredSize(new Dimension(50, 22));
    add(jTextSeq, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jLabQueryText.setText("Query Text:");
    add(jLabQueryText, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jTextQuerySearch.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent ke) { }
      public void keyPressed(KeyEvent ke) { }
      public void keyReleased(KeyEvent ke) {
        try {
          jComQueryText.removeAllItems();
          List<CpoQueryTextNode> qts = cpoQueryNode.getProxy().getQueryTextMatches((CpoServerNode)cpoQueryNode.getParent().getParent().getParent().getParent(), jTextQuerySearch.getText());
          for (CpoQueryTextNode qt : qts) {
            jComQueryText.addItem(qt.getCpoQueryText());
          }
        } catch (Exception pe) {
          CpoUtil.showException(pe);
        }
        try {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              jTextQuerySearch.requestFocus();
            }
          });
        } catch (Exception e) {
          // ignore
        }
      }
    });
    add(jTextQuerySearch, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    newQueryTextButton.setText("New Query Text");
    newQueryTextButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        String description = JOptionPane.showInputDialog(newQueryTextButton,"Please type the description of your new query.","New Query", JOptionPane.PLAIN_MESSAGE);
        CpoQueryTextNode cQTnode = cpoQueryNode.getProxy().addQueryText(description);
        if (cQTnode != null) {
          jComQueryText.addItem(cQTnode.getCpoQueryText());
          jComQueryText.setSelectedItem(cQTnode.getCpoQueryText());
        }
      }
    });
    add(newQueryTextButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    for (CpoQueryTextNode qt : queryText) {
      jComQueryText.addItem(qt.getCpoQueryText());
    }
    add(jComQueryText, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    jLabDesc.setText("Description:");
    add(jLabDesc, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jTextAdesc.setLineWrap(true);
    jScrollDesc.getViewport().add(jTextAdesc, null);
    jScrollDesc.getViewport().setMinimumSize(new Dimension(200,20));
    jScrollDesc.getViewport().setPreferredSize(new Dimension(200,20));
    add(jScrollDesc, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 230, 25));

    jLabSQL.setText("SQL:");
    add(jLabSQL, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    jTextASQL.setLineWrap(true);
    jScrollSQL.getViewport().add(jTextASQL, null);
    jScrollSQL.getViewport().setMinimumSize(new Dimension(200,50));
    jScrollSQL.getViewport().setPreferredSize(new Dimension(200,100));
    add(jScrollSQL, new GridBagConstraints(1, 5, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 230, 45));
  }
}