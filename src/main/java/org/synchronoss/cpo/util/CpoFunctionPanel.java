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
import org.synchronoss.cpo.CpoException;
import org.synchronoss.cpo.meta.domain.CpoAttribute;
import org.synchronoss.cpo.parser.ExpressionParser;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class CpoFunctionPanel extends JPanel {

  private final static String expressionLineTooBigMsg = "The expression entered has lines containing more than 2000 characters.\n" +
                                                        "This might cause some tools such as sql plus not to be able to execute it.\n\n" +
                                                        "To solve this issue, add line breaks to the query.";

  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  // location of the divider
  private static int divLocation = -1;

  private static final long serialVersionUID = 1L;
  private CpoFunctionNode cpoFunctionNode;
  private CoreArgumentTableModel coreArgumentTM;

  private JLabel jLabName = new JLabel();
  private JTextField jTextName = new JTextField();

  private JLabel jLabExpression = new JLabel();
  private JTextArea jTextAExpression = new JTextArea();

  private JScrollPane jScrollExpression = new JScrollPane();
  private JTable jTableArgument;
  private TableCellEditor editor;
  private JComboBox jIOTypeBox;
  private JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

  public CpoFunctionPanel(CpoFunctionNode cpoFunctionNode, CoreArgumentTableModel coreArgumentTableModel) {
    this.cpoFunctionNode = cpoFunctionNode;
    this.coreArgumentTM = coreArgumentTableModel;
    try {
      jbInit();

      // adjust divider
      if (divLocation != -1) {
        jSplitPane.setDividerLocation(divLocation);
      }
    } catch (Exception e) {
      CpoUtil.showException(e);
    }

    jTextName.setText(cpoFunctionNode.getName());
    jTextAExpression.setText(cpoFunctionNode.getExpression());
  }

  private void jbInit() throws Exception {
    this.setLayout(new BorderLayout());

    this.add(jSplitPane, BorderLayout.CENTER);
    jSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        divLocation = jSplitPane.getDividerLocation();
      }
    });

    // the top panel
    JPanel northPanel = new JPanel();
    northPanel.setLayout(new GridBagLayout());

    jLabName.setText("Name:");
    northPanel.add(jLabName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextName.setMinimumSize(new Dimension(200, 20));
    jTextName.setPreferredSize(new Dimension(200,20));
    jTextName.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent ke) {
        cpoFunctionNode.setName(jTextName.getText());
      }
    });
    northPanel.add(jTextName, new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    jLabExpression.setText("Expression:");
    northPanel.add(jLabExpression, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextAExpression.setLineWrap(true);
    jTextAExpression.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent focusEvent) {
        checkExpressionLength();
      }
    });
    jTextAExpression.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent focusEvent) {
        checkExpressionLength();
      }
    });
    jTextAExpression.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent ke) {
        if (cpoFunctionNode.getExpression() != null) {
          checkExpression();
        }
      }
    });
    jTextAExpression.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.isMetaDown()) {
          // This is an ugly hack for a focus issue with JTable.  Selecting the cell at 0, 0
          // will cause the table to realize it needs to stop editing the selected cell and
          // will cause the cell editor to end removing the combo box
          jTableArgument.editCellAt(0, 0);
          if (cpoFunctionNode.getExpression() != null) {
            showMenu(e.getPoint());
          }
        }
      }
    });

    jScrollExpression.getViewport().add(jTextAExpression, null);
    jScrollExpression.getViewport().setMinimumSize(new Dimension(200,50));
    jScrollExpression.getViewport().setPreferredSize(new Dimension(200,100));
    northPanel.add(jScrollExpression, new GridBagConstraints(1, 1, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    jSplitPane.setTopComponent(northPanel);
    // the top panel

    // the bottom panel
    jTableArgument = new JTable(coreArgumentTM);

    jIOTypeBox = new JComboBox();
    jIOTypeBox.addItem("IN");
    jIOTypeBox.addItem("OUT");
    jIOTypeBox.addItem("BOTH");
    editor = new DefaultCellEditor(jIOTypeBox);

    JScrollPane jScrollTable = new JScrollPane();
    jScrollTable.getViewport().add(jTableArgument);

    jTableArgument.setDefaultEditor(CpoAttribute.class, new CpoArgumentAttributeEditor(coreArgumentTM));
    jTableArgument.setDefaultEditor(JComboBox.class, editor);

    jSplitPane.setBottomComponent(jScrollTable);
    // the bottom panel

  }

  private void checkExpressionLength() {
    String expression = jTextAExpression.getText();

    // check for length, over 2499 characters and sql plus won't handle the query
    boolean hasBigChunk = false;
    for (String chunk : expression.split("\n")) {
      if (chunk.length() > 2000) {
        hasBigChunk = true;
      }
    }

    if (hasBigChunk) {
      JOptionPane.showMessageDialog(this.getTopLevelAncestor(), expressionLineTooBigMsg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
  }

  private void checkExpression() {
    if (jTextAExpression.getText().length() < 1) {
      return;
    }

    try {
      ExpressionParser expressionParser = cpoFunctionNode.getProxy().getExpressionParser(jTextAExpression.getText());
      int tokenCount = expressionParser.countArguments();

      int attRowCount = coreArgumentTM.getNonRemovedRows();

      // need to add rows if
      if (tokenCount > attRowCount) {
        for (int i = tokenCount; i > attRowCount; i--) {
          coreArgumentTM.addNewRow();
        }
      }

      //need to mark rows deleted if
      if (tokenCount < attRowCount) {
        for (int i = attRowCount; i > tokenCount; i--) {
          coreArgumentTM.removeNewRow();
        }
      }
      String newExpression = jTextAExpression.getText();

      cpoFunctionNode.setExpression(newExpression);

      CpoUtil.getInstance().setStatusBarText("Expression Length: " + newExpression.length());
    } catch (CpoException ex) {
      CpoUtil.showException(ex);
    }
  }

  private void showMenu(Point p) {
    JPopupMenu menu = new JPopupMenu();
    menu.removeAll();
    menu.setLabel("Expression Menu");

    JMenuItem jMenuAddParams = new JMenuItem("Add Expression Params Here");
    jMenuAddParams.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        insertSQLparams();
      }
    });
    menu.add(jMenuAddParams);

    JMenuItem jMenuAddAttrs = new JMenuItem("Add All Attributes Here");
    jMenuAddAttrs.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        insertAllAttributes();
      }
    });
    menu.add(jMenuAddAttrs);

    JMenuItem jMenuGuessAttributes = new JMenuItem("Guess Attributes");
    jMenuGuessAttributes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        guessAttributes();
      }
    });
    menu.add(jMenuGuessAttributes);
    menu.show(jTextAExpression, (int)p.getX(), (int)p.getY());
  }

  private void insertSQLparams() {
    Enumeration<CpoArgumentNode> queryEnum = cpoFunctionNode.children();
    StringBuilder sbParams = new StringBuilder();
    while (queryEnum.hasMoreElements()) {
      CpoArgumentNode node = queryEnum.nextElement();
      sbParams.append(node.getCpoAttribute().getDataName());
      sbParams.append(",");
    }
    jTextAExpression.insert(sbParams.toString().substring(0, sbParams.toString().length() - 1), jTextAExpression.getCaretPosition());
    checkExpression();
  }

  private void insertAllAttributes() {
    CpoClassNode classNode = cpoFunctionNode.getParent().getParent().getParent();
    if (classNode != null) {
      String sql = jTextAExpression.getText();
      boolean isInsert = sql.toUpperCase().startsWith("INSERT");
      try {
        boolean first = true;
        StringBuilder buf = new StringBuilder();

        Enumeration e = classNode.getAttributeLabelNode().children();
        while (e.hasMoreElements()) {
          CpoAttributeNode att = (CpoAttributeNode)e.nextElement();
          String colName = att.getDataName();
          if (!first) {
            buf.append(", ");
          }
          buf.append(colName);
          if (!isInsert) {
            buf.append(" = ?");
          }
          first = false;
        }
        jTextAExpression.insert(buf.toString().substring(0, buf.toString().length()), jTextAExpression.getCaretPosition());
        checkExpression();
      } catch (Exception ex) {
        CpoUtil.showException(ex);
      }
    }
  }

  private void guessAttributes() {
    String query = jTextAExpression.getText().trim();

    Vector<String> errors = new Vector<String>();

    try {
      ExpressionParser expressionParser = cpoFunctionNode.getProxy().getExpressionParser(query);
      List<String> colList = expressionParser.parse();

      // if colList is null or empty, we're done
      if (colList == null || colList.isEmpty()) {
        return;
      }

      // at this point, the colList will only have columns that correspond to a ?
      if (OUT.isDebugEnabled()) {
        int count = 1;
        for (String col : colList) {
          OUT.debug("Column[" + count + "] = " + col);
          count++;
        }
      }

      // hash the attributes so we can do easier lookups
      HashMap<String, CpoAttribute> hash = new HashMap<String, CpoAttribute>();
      CpoClassNode classNode = cpoFunctionNode.getParent().getParent().getParent();
      if (classNode != null) {
        Enumeration e = classNode.getAttributeLabelNode().children();
        while (e.hasMoreElements()) {
          CpoAttributeNode att = (CpoAttributeNode)e.nextElement();
          String dataName = att.getDataName();
          if (dataName != null && att.getUserObject() != null) {
            hash.put(dataName.toUpperCase(), att.getUserObject());
          }
        }
      }

      int idx = 0;
      for (String col : colList) {
        // try to find the attribute that matches the column and fill it in the table
        CpoAttribute att = hash.get(col);
        if (att != null) {
          coreArgumentTM.setValueAt(att, idx, 1);
        } else {
          errors.add(col);
        }
        idx++;
      }

      if (!errors.isEmpty()) {
        StringBuilder buf = new StringBuilder();
        for (String s : errors) {
          buf.append("\n");
          buf.append(s);
        }
        JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Unable to guess the following fields: " + buf.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
      }
    } catch (ParseException ex) {
      CpoUtil.showMessage(ex.getMessage());
    } catch (Exception ex) {
      CpoUtil.showException(ex);
    }
  }
}
