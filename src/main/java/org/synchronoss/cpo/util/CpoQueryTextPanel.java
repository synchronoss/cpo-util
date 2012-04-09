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

import org.synchronoss.cpo.meta.domain.CpoQueryGroup;
import org.synchronoss.cpo.util.tree.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CpoQueryTextPanel extends JPanel  {

  /** Version Id for this class. */
  private static final long serialVersionUID=1L;

  private JPanel buttonPanel = new JPanel();
  private JButton filterButton = new JButton();
  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();
  private JScrollPane jScrollQT = new JScrollPane();
  private CpoServerNode serverNode;
  private CpoQueryTextTableModel model;
  private TableSorter ts;
  private JTable queryTextJtable;
  private JPopupMenu menu = new JPopupMenu();
  private int tipTextRow = -1;
  private String tipText;
  
  public CpoQueryTextPanel(CpoServerNode serverNode) {
    this.serverNode = serverNode;
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {

    this.setLayout(new BorderLayout());

    buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    addButton.setText("New Query Text");
    addButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        addNewRow();
      }
    });
    buttonPanel.add(addButton);

    removeButton.setText("Remove");
    removeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        removeRows();
      }
    });
    buttonPanel.add(removeButton);

    filterButton.setText("Filter Queries");
    filterButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        filterQueries();
      }
    });
    buttonPanel.add(filterButton);

    this.add(buttonPanel, BorderLayout.NORTH);

    model = new CpoQueryTextTableModel(serverNode);
    ts = new TableSorter(model);
    queryTextJtable = new JTable(ts) {
        /** Version Id for this class. */
        private static final long serialVersionUID=1L;

      @Override
      public String getToolTipText(MouseEvent e) {
        return getTipText(e.getPoint());
      }
    };
    ToolTipManager.sharedInstance().registerComponent(queryTextJtable);
    ts.addMouseListenerToHeaderInTable(queryTextJtable);
		queryTextJtable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          ListSelectionModel selectionModel = queryTextJtable.getSelectionModel();
          int row = queryTextJtable.rowAtPoint(e.getPoint());
          selectionModel.addSelectionInterval(row, row);
          showMenu(e.getPoint());
        }
      }
    });
    queryTextJtable.getTableHeader().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          showMenu(e.getPoint());
        }
      }
    });
    this.jScrollQT.getViewport().add(queryTextJtable);
    this.add(jScrollQT, BorderLayout.CENTER);
  }

  public void showMenu(Point point) {
    buildMenu();
    menu.show(queryTextJtable, (int)point.getX(), (int)point.getY());
  }

  public void buildMenu() {
    menu.removeAll();
    try {
      JMenuItem jMenuRemove = new JMenuItem("Remove");
      jMenuRemove.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          removeRows();
        }
      });
      menu.add(jMenuRemove);
    } catch (Exception e) {
      // ignore
    }
    JMenuItem jMenuAddQT = new JMenuItem("New");
    jMenuAddQT.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        addNewRow();
      }
    });
    menu.add(jMenuAddQT);
    JMenuItem jMenuFilter = new JMenuItem("Filter");
    jMenuFilter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        filterQueries();
      }
    });
    menu.add(jMenuFilter);
  }

  private void removeRows() {
    int[] selectedRows = queryTextJtable.getSelectedRows();
    if (selectedRows.length < 1) {
      CpoUtil.showErrorMessage("Please select a query text to remove.");
    } else {
      model.removeRows(ts.getTrueRows(selectedRows));
    }
  }

  private void addNewRow() {
    String description = JOptionPane.showInputDialog(this,"Please type the description of your new query.","New Query",JOptionPane.PLAIN_MESSAGE);
    if (description == null || description.equals("")) return;
    model.addNewRow(description);
  }

  private void filterQueries() {
    String filter = JOptionPane.showInputDialog(this,"Please type string to filter on.","Filter Queries",JOptionPane.PLAIN_MESSAGE);
    if (filter == null || filter.equals(""))
      model.removeFilter();
    else
      model.addFilter(filter);
  }

  private String getTipText(Point p) {
    StringBuilder sb = new StringBuilder();
    int rowAtPoint = queryTextJtable.rowAtPoint(p);
    if (rowAtPoint == this.tipTextRow) return this.tipText;
    this.tipTextRow = rowAtPoint;
    CpoQueryTextNode node = model.getQueryNodeAt(ts.getTrueRow(rowAtPoint));
    try {
      sb.append("<html>");
      sb.append("<bold>");
      sb.append(node.getDesc());
      sb.append("</bold><BR>");
      List<CpoQueryGroup> queryGroups = node.getProxy().getQueryGroups(node);
      for (CpoQueryGroup queryGroup : queryGroups) {
        CpoClassNode classNode = node.getProxy().getClassNode(queryGroup.getClassId());
        sb.append("Class: ");
        sb.append(classNode.getCpoClass().getName());
        sb.append(" -- Query Group: ");
        sb.append(queryGroup.getName());
        sb.append("<BR>");
      }
      sb.append("</html>");
    } catch (Exception e) {
      CpoUtil.showException(e);
      return null;
    }
    this.tipText = sb.toString();
    return this.tipText;
  }
}