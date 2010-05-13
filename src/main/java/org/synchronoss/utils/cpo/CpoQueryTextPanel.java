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
import java.awt.event.*;
import java.util.List;

public class CpoQueryTextPanel extends JPanel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollQT = new JScrollPane();
  private CpoServerNode serverNode;
  private CpoQueryTextTableModel model;
  private TableSorter ts;
  private JTable queryTextJtable;
  private JPopupMenu menu = new JPopupMenu();
  private int menuRow;
  private int[] selectedRows;
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
    this.setLayout(borderLayout1);
    this.add(jScrollQT, BorderLayout.CENTER);
  }
  public void showMenu(Point point) {
    menuRow = queryTextJtable.rowAtPoint(point);
    selectedRows = queryTextJtable.getSelectedRows();
//    OUT.debug ("Row selected: "+menuRow);
    buildMenu();
    menu.show(queryTextJtable, (int)point.getX(), (int)point.getY());
  }
  public void buildMenu() {
    menu.removeAll();
    try {
      menu.setLabel((String)queryTextJtable.getValueAt(menuRow,0));
      JMenuItem jMenuRemove = new JMenuItem("Remove");
      jMenuRemove.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          if (selectedRows.length < 1)
            model.removeRows(new int[]{ts.getTrueRow(menuRow)});
          else
            model.removeRows(ts.getTrueRows(selectedRows));
        }
      });
      menu.add(jMenuRemove);
    } catch (Exception e) {
      // ignore
    }
    JMenuItem jMenuAddQT = new JMenuItem("Add New Query Text");
    jMenuAddQT.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        addNewRow();
      }
    });
    menu.add(jMenuAddQT);
    JMenuItem jMenuFilter = new JMenuItem("Filter Queries");
    jMenuFilter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        filterQueries();
      }
    });
    menu.add(jMenuFilter);
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
    StringBuffer sb = new StringBuffer();
    int rowAtPoint = queryTextJtable.rowAtPoint(p);
    if (rowAtPoint == this.tipTextRow) return this.tipText;
    this.tipTextRow = rowAtPoint;
    CpoQueryTextNode node = model.getQueryNodeAt(ts.getTrueRow(rowAtPoint));
    try {
      sb.append("<html>");
      sb.append("<bold>");
      sb.append(node.getDesc());
      sb.append("</bold><BR>");
      List<CpoQueryGroupNode> queryGroups = node.getProxy().getQueryGroups(node);
      for (CpoQueryGroupNode qGnode : queryGroups) {
        CpoClassNode classNode = node.getProxy().getClassNode(qGnode.getClassId());
        sb.append("Class: ");
        sb.append(classNode.getClassName());
        sb.append(" -- Query Group: ");
        sb.append(qGnode.getGroupName());
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