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
import org.synchronoss.cpo.meta.domain.CpoQueryText;
import org.synchronoss.cpo.meta.parser.ExpressionParser;
import org.synchronoss.cpo.meta.parser.jdbc.SQLExpressionParser;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class CpoQueryPanel extends JPanel {

  private Logger OUT = Logger.getLogger(this.getClass());

  // location of the divider
  private static int divLocation = -1;

  private static final long serialVersionUID = 1L;
  private BorderLayout borderLayout1 = new BorderLayout();
  private CpoQueryPanelNorth cpoQPnorth;
  private CpoQueryNode queryNode;
  private CpoQueryTableModel cpoQTM;
  private JTable jTableQueryParam;
  private JScrollPane jScrollTable = new JScrollPane();
  private TableCellEditor editor;
  private JComboBox jIOTypeBox;
  private JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

  private final static String sqlTooBigMsg = "The sql entered has contains more than 4000 characters.\n" +
                                             "This might cause some databases such as oracle not to be able to store it.\n\n" +
                                             "To solve this issue, shorten the query.";

  private final static String sqlLineTooBigMsg = "The sql entered has lines containing more than 2000 characters.\n" +
                                                 "This might cause some tools such as sql plus not to be able to execute it.\n\n" +
                                                 "To solve this issue, add line breaks to the query.";

  public CpoQueryPanel(CpoQueryNode queryNode) {
    this.queryNode = queryNode;
    try {
      jbInit();

      // adjust divider
      if (divLocation != -1)
        jSplitPane.setDividerLocation(divLocation);

      // if the query is used in more than one place, warn them
      CpoQueryText queryText = queryNode.getQueryText();
      if (queryText != null && queryText.getRefCount() > 1) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            CpoUtil.showMultipleQueryTextWarning();
          }
        });
      }
    } catch (Exception e) {
      CpoUtil.showException(e);
    }
  }

  private void jbInit() throws Exception {
    cpoQPnorth = new CpoQueryPanelNorth(queryNode);
    cpoQTM = new CpoQueryTableModel(queryNode);
    if (cpoQPnorth.jComQueryObject != null)
      cpoQPnorth.jComQueryObject.setSelectedItem(cpoQTM.attributeCpoClassNode);
    jTableQueryParam = new JTable(cpoQTM);
    jIOTypeBox = new JComboBox();
    jIOTypeBox.addItem("IN");
    jIOTypeBox.addItem("OUT");
    jIOTypeBox.addItem("BOTH");
    editor = new DefaultCellEditor(jIOTypeBox);
    jScrollTable.getViewport().add(jTableQueryParam);
    jTableQueryParam.setDefaultEditor(CpoAttributeMapNode.class, new CpoQueryAttributeEditor(cpoQTM));
    jTableQueryParam.setDefaultEditor(JComboBox.class, editor);
    this.setLayout(borderLayout1);
    this.add(jSplitPane, BorderLayout.CENTER);
    jSplitPane.setTopComponent(cpoQPnorth);
    jSplitPane.setBottomComponent(jScrollTable);
    jSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        divLocation = jSplitPane.getDividerLocation();
      }
    });

    cpoQPnorth.jTextSeq.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent ke) {
      }

      public void keyPressed(KeyEvent ke) {
      }

      public void keyReleased(KeyEvent ke) {
        try {
          int newSeqNo = Integer.parseInt(cpoQPnorth.jTextSeq.getText());
          queryNode.setSeqNo(newSeqNo);
        } catch (NumberFormatException nfe) {
          cpoQPnorth.jTextSeq.setText(Integer.toString(queryNode.getSeqNo()));
        }
      }
    });

    cpoQPnorth.jTextAdesc.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent ke) {
      }

      public void keyPressed(KeyEvent ke) {
      }

      public void keyReleased(KeyEvent ke) {
        queryNode.setDescription(cpoQPnorth.jTextAdesc.getText());
      }
    });

    cpoQPnorth.jTextASQL.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent focusEvent) {
      }

      public void focusLost(FocusEvent focusEvent) {
        checkSQLLength();
      }
    });

    cpoQPnorth.jTextASQL.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent ke) {
      }

      public void keyPressed(KeyEvent ke) {
      }

      public void keyReleased(KeyEvent ke) {
        if (queryNode.getQueryText() != null) {
          checkSQL();
        }
      }
    });
    cpoQPnorth.jTextASQL.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        if (e.isMetaDown()) {
          // This is an ugly hack for a focus issue with JTable.  Selecting the cell at 0, 0
          // will cause the table to realize it needs to stop editing the selected cell and
          // will cause the cell editor to end removing the combo box
          jTableQueryParam.editCellAt(0, 0);
          if (queryNode.getQueryText() != null)
            showMenu(e.getPoint());
        }
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }
    });
    cpoQPnorth.jTextASQL.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent focusEvent) {
      }

      public void focusLost(FocusEvent focusEvent) {
        checkSQLLength();
      }
    });
    cpoQPnorth.jComQueryText.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (cpoQPnorth.jComQueryText.getSelectedItem() != null) {
          queryNode.setQueryText(((CpoQueryText)cpoQPnorth.jComQueryText.getSelectedItem()));
          cpoQPnorth.jTextAdesc.setText(queryNode.getQueryText().getDescription());
          cpoQPnorth.jTextASQL.setText(queryNode.getQueryText().getSqlText());
          checkSQL();
        }
      }
    });
    cpoQPnorth.jComQueryText.setSelectedItem(queryNode.getQueryText());

    if (cpoQPnorth.jComQueryObject != null) {
      cpoQPnorth.jComQueryObject.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          if (cpoQPnorth.jComQueryObject.getSelectedItem() != null) {
            cpoQTM.attributeCpoClassNode = (CpoClassNode)cpoQPnorth.jComQueryObject.getSelectedItem();
          }
        }
      });
      cpoQPnorth.jComQueryObject.setSelectedItem(cpoQTM.attributeCpoClassNode);
    }
  }

  private void checkSQL() {
    if (cpoQPnorth.jTextASQL.getText().length() < 1)
      return;

    ExpressionParser expressionParser = new SQLExpressionParser();
    expressionParser.setExpression(cpoQPnorth.jTextASQL.getText());
    int tokenCount = expressionParser.countBindMarkers();

    int attRowCount = cpoQTM.getNonRemovedRows();

    // need to add rows if
    if (tokenCount > attRowCount) {
      for (int i = tokenCount; i > attRowCount; i--) {
        cpoQTM.addNewRow();
      }
    }

    //need to mark rows deleted if
    if (tokenCount < attRowCount) {
      for (int i = attRowCount; i > tokenCount; i--) {
        cpoQTM.removeNewRow();
      }
    }
    String newSql = cpoQPnorth.jTextASQL.getText();

    queryNode.setSqlText(newSql);

    CpoUtil.updateStatus("SQL Length: " + newSql.length());
  }

  private void checkSQLLength() {
    String sql = cpoQPnorth.jTextASQL.getText();

    // check for more than 4000 chars
    if (sql.length() > 4000) {
      JOptionPane.showMessageDialog(this, sqlTooBigMsg, "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }

    // check for length, over 2499 characters and sql plus won't handle the query
    boolean hasBigChunk = false;
    for (String chunk : sql.split("\n")) {
      if (chunk.length() > 2000)
        hasBigChunk = true;
    }

    if (hasBigChunk) {
      JOptionPane.showMessageDialog(this, sqlLineTooBigMsg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
  }

  private void showMenu(Point p) {
    JPopupMenu menu = new JPopupMenu();
    menu.removeAll();
    menu.setLabel("SQL Menu");
    JMenuItem jMenuAddParams = new JMenuItem("Add SQL Params Here");
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
    menu.show(cpoQPnorth.jTextASQL, (int)p.getX(), (int)p.getY());
  }

  private void insertSQLparams() {
    Enumeration<CpoQueryParameterNode> queryEnum = queryNode.children();
    StringBuilder sbParams = new StringBuilder();
    while (queryEnum.hasMoreElements()) {
      CpoQueryParameterNode node = queryEnum.nextElement();
      sbParams.append(node.getCpoAttributeMapBean().getColumnName());
      sbParams.append(",");
    }
    cpoQPnorth.jTextASQL.insert(sbParams.toString().substring(0, sbParams.toString().length() - 1), cpoQPnorth.jTextASQL.getCaretPosition());
    checkSQL();
  }

  private void insertAllAttributes() {
    CpoClassNode classNode = queryNode.getParent().getParent().getParent();
    if (classNode != null) {
      String sql = cpoQPnorth.jTextASQL.getText();
      boolean isInsert = sql.toUpperCase().startsWith("INSERT");
      try {
        boolean first = true;
        StringBuilder buf = new StringBuilder();
        List<CpoAttributeMapNode> attributeList = queryNode.getProxy().getAttributeMap(classNode);
        for (CpoAttributeMapNode att : attributeList) {
          String colName = att.getColumnName();
          if (!first)
            buf.append(", ");
          buf.append(colName);
          if (!isInsert)
            buf.append(" = ?");
          first = false;
        }
        cpoQPnorth.jTextASQL.insert(buf.toString().substring(0, buf.toString().length()), cpoQPnorth.jTextASQL.getCaretPosition());
        checkSQL();
      } catch (Exception ex) {
        CpoUtil.showException(ex);
      }
    }
  }

  private void guessAttributes() {
    String query = cpoQPnorth.jTextASQL.getText().trim();
    ExpressionParser expressionParser = new SQLExpressionParser();
    expressionParser.setExpression(query);

    Vector<String> errors = new Vector<String>();

    try {
      List<String> colList = expressionParser.parse();

      // if colList is null or empty, we're done
      if (colList == null || colList.isEmpty())
        return;

      // at this point, the colList will only have columns that correspond to a ?
      if (OUT.isDebugEnabled()) {
        int count = 1;
        for (String col : colList) {
          OUT.debug("Column[" + count + "] = " + col);
          count++;
        }
      }

      // hash the attributes so we can do easier lookups
      HashMap<String, CpoAttributeMapNode> hash = new HashMap<String, CpoAttributeMapNode>();
      CpoClassNode classNode = queryNode.getParent().getParent().getParent();
      if (classNode != null) {
        List<CpoAttributeMapNode> attributeList = queryNode.getProxy().getAttributeMap(classNode);
        for (CpoAttributeMapNode att : attributeList) {
          String colName = att.getColumnName();
          if (colName != null)
            hash.put(colName.toUpperCase(), att);
        }
      }

      int idx = 0;
      for (String col : colList) {
        // try to find the attribute that matches the column and fill it in the table
        CpoAttributeMapNode att = hash.get(col);
        if (att != null) {
          cpoQTM.setValueAt(att, idx, 1);
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
        JOptionPane.showMessageDialog(this, "Unable to guess the following fields: " + buf.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
      }
    } catch (ParseException ex) {
      CpoUtil.showMessage(ex.getMessage());
    } catch (Exception ex) {
      CpoUtil.showException(ex);
    }
  }
}
