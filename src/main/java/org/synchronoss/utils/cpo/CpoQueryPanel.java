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
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class CpoQueryPanel extends JPanel {

    /**
     * Version Id for this class.
     */
    private static final long serialVersionUID = 1L;
    private BorderLayout borderLayout1 = new BorderLayout();
    private CpoQueryPanelNorth cpoQPnorth;
    private CpoQueryNode queryNode;
    private CpoQueryTableModel cpoQTM;
    private JTable jTableQueryParam;
    private JScrollPane jScrollTable = new JScrollPane();
    private JPopupMenu menu = new JPopupMenu();
    private TableCellEditor editor;
    private JComboBox jIOTypeBox;

    private Logger OUT = Logger.getLogger(this.getClass());

    public CpoQueryPanel(CpoQueryNode queryNode) {
        this.queryNode = queryNode;
        try {
            jbInit();
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
//    this.setSize(new Dimension(200, 500));
        this.setLayout(borderLayout1);
        this.add(cpoQPnorth, BorderLayout.NORTH);
        this.add(jScrollTable, BorderLayout.CENTER);
        cpoQPnorth.jTextSeq.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ke) {
            }

            public void keyPressed(KeyEvent ke) {
            }

            public void keyReleased(KeyEvent ke) {
                int newSeqNo = queryNode.getSeqNo();
                try {
                    newSeqNo = Integer.parseInt(cpoQPnorth.jTextSeq.getText());
                } catch (NumberFormatException nfe) {
                    cpoQPnorth.jTextSeq.setText(Integer.toString(queryNode.getSeqNo()));
                    return;
                }
                queryNode.setSeqNo(newSeqNo);
            }
        });
        cpoQPnorth.jTextAdesc.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ke) {
            }

            public void keyPressed(KeyEvent ke) {
            }

            public void keyReleased(KeyEvent ke) {
                queryNode.getQueryText().setDesc(cpoQPnorth.jTextAdesc.getText());
            }
        });
        cpoQPnorth.jTextASQL.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ke) {
            }

            public void keyPressed(KeyEvent ke) {
            }

            public void keyReleased(KeyEvent ke) {
//        queryNode.getQueryText().setDirty(true);
                checkSQLAttributes();
/*        try {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              cpoQPnorth.jTextASQL.requestFocus();
            }
          });
        } catch (Exception e) {}
*/
            }
        });
        cpoQPnorth.jTextASQL.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.isMetaDown()) {
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
        cpoQPnorth.jComQueryText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (cpoQPnorth.jComQueryText.getSelectedItem() != null) {
                    queryNode.setQueryText((CpoQueryTextNode)cpoQPnorth.jComQueryText.getSelectedItem());
                    cpoQPnorth.jTextAdesc.setText(queryNode.getQueryText().getDesc());
                    cpoQPnorth.jTextASQL.setText(queryNode.getQueryText().getSQL());
                    checkSQLAttributes();
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

    private void checkSQLAttributes() {
        if (cpoQPnorth.jTextASQL.getText().length() < 1)
            return;
        int index = -1, tokenCount = 0;
        while ((index = cpoQPnorth.jTextASQL.getText().indexOf("?", index + 1)) != -1) {
//      OUT.debug ("index: "+index);
            tokenCount++;
        }
        int attRowCount = cpoQTM.getNonRemovedRows();
//    OUT.debug ("tokens: "+tokenCount+" and rows: "+attRowCount);
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
        queryNode.getQueryText().setSQL(cpoQPnorth.jTextASQL.getText());
//    jTableQueryParam.revalidate();
    }

    private void showMenu(Point p) {
        menu.removeAll();
        menu.setLabel("SQL Menu");
        JMenuItem jMenuAddParams = new JMenuItem("Add SQL Params Here");
        jMenuAddParams.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                insertSQLparams();
            }
        });
        menu.add(jMenuAddParams);
        JMenuItem jMenuGuessAttributes = new JMenuItem("Guess Attributes (Experimental)");
        jMenuGuessAttributes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                guessAttributes();
            }
        });
        menu.add(jMenuGuessAttributes);
        menu.show(cpoQPnorth.jTextASQL, (int)p.getX(), (int)p.getY());
    }

    private void insertSQLparams() {
        Enumeration queryEnum = queryNode.children();
        StringBuffer sbParams = new StringBuffer();
        while (queryEnum.hasMoreElements()) {
            CpoQueryParameterNode node = (CpoQueryParameterNode)queryEnum.nextElement();
            sbParams.append(node.getCpoAttributeMapBean().getColumnName());
            sbParams.append(",");
        }
        cpoQPnorth.jTextASQL.insert(sbParams.toString().substring(0, sbParams.toString().length() - 1), cpoQPnorth.jTextASQL.getCaretPosition());
        checkSQLAttributes();
    }

    private void guessAttributes() {
        String query = cpoQPnorth.jTextASQL.getText().trim();
        QueryParser parser = new QueryParser();

        Vector<String> errors = new Vector<String>();

        try {
            List<String> colList = parser.parse(query);

            // if colList is null or empty, we're done
            if (colList == null || colList.isEmpty())
                return;

            // hash the attributes so we can do easier lookups
            HashMap<String, CpoAttributeMapNode> hash = new HashMap<String, CpoAttributeMapNode>();
            CpoClassNode classNode = (CpoClassNode)queryNode.getParent().getParent().getParent();
            if (classNode != null) {
                ArrayList attributeList = queryNode.getProxy().getAttributeMap(classNode);
                Iterator iterator = attributeList.iterator();
                while (iterator.hasNext()) {
                    CpoAttributeMapNode att = (CpoAttributeMapNode)iterator.next();
                    String colName = att.getColumnName();
                    hash.put(colName, att);
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