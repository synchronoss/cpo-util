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
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;

public class CpoBrowserPanel extends JPanel  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
  private Proxy prox;
  private BorderLayout borderLayout = new BorderLayout();
  private JPanel jPanelCenter = new JPanel();
  private JScrollPane jScrollWest = new JScrollPane();
  private CpoBrowserTree jTreeBrowser = new CpoBrowserTree();
  private JScrollPane jScrollCenter = new JScrollPane();
  private JSplitPane jSplitPane1 = new JSplitPane();
  
  public CpoBrowserPanel() throws Exception {
    String server = CpoUtil.getServerFromUser();
    prox = new Proxy(CpoUtil.props,server,jTreeBrowser);
    try {
      jbInit();
    }catch(Exception e) {
      CpoUtil.showException(e);
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout);
    this.setSize(new Dimension(800, 600));
//    jPanelCenter.add(jScrollCenter);
    jSplitPane1.add(jScrollWest, JSplitPane.LEFT);
    jSplitPane1.add(jScrollCenter, JSplitPane.RIGHT);
    this.add(jSplitPane1, BorderLayout.CENTER);
//    this.add(jScrollCenter, BorderLayout.CENTER);
//    this.add(jPanelCenter, BorderLayout.CENTER);
    jScrollWest.getViewport().add(jTreeBrowser);
    jScrollWest.getViewport().setPreferredSize(new Dimension(200,0));
//    this.add(jScrollWest, BorderLayout.WEST);
    jTreeBrowser.setModel(new DefaultTreeModel(new CpoServerNode(prox,jTreeBrowser)));
    jTreeBrowser.addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillExpand(TreeExpansionEvent tee) {
//        TreePath pathParent = tee.getPath().getParentPath();
//        Enumeration enum = jTreeBrowser.getExpandedDescendants(pathParent);
//        if (enum != null) {
//          while (enum.hasMoreElements()) {
//            jTreeBrowser.collapsePath((TreePath)enum.nextElement());
//          }
//        }
        Object pathComp = tee.getPath().getLastPathComponent();
        if (pathComp instanceof AbstractCpoNode) {
//          OUT.debug("Refreshing data in node: "+pathComp);
          ((AbstractCpoNode)pathComp).refreshChildren();
        }
      }
      public void treeWillCollapse(TreeExpansionEvent tee) {
        
      }
    });
    jTreeBrowser.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent tse) {
        Object pathComp = tse.getPath().getLastPathComponent();
        if (pathComp instanceof AbstractCpoNode) {
          jScrollCenter.getViewport().removeAll();
          JPanel panel = ((AbstractCpoNode)pathComp).getPanelForSelected();
//          OUT.debug ("Panel retreived for view: "+panel);
          if (panel != null)
            jScrollCenter.getViewport().add(panel,null);
          jScrollCenter.getViewport().revalidate();
          jScrollCenter.getViewport().repaint();
        }
      }
    });
//    ToolTipManager.sharedInstance().registerComponent(jScrollWest);
    ToolTipManager.sharedInstance().registerComponent(jTreeBrowser);
  }
  String getServer() {
    return this.prox.toString();
  }
  String getDatabaseName() {
    return this.prox.getDatabaseName();
  }
  Proxy getProxy() {
    return this.prox;
  }
}