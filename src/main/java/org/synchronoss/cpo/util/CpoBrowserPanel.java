/**
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

import org.synchronoss.cpo.CpoException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;

public class CpoBrowserPanel extends JPanel {

  // Version Id for this class
  private static final long serialVersionUID = 1L;
  private Proxy proxy;
  private BorderLayout borderLayout = new BorderLayout();
  private JScrollPane jScrollWest = new JScrollPane();
  private CpoBrowserTree jTreeBrowser = new CpoBrowserTree();
  private JSplitPane jSplitPane = new JSplitPane();
  private JPanel emptyPanel = new JPanel();

  public CpoBrowserPanel(Proxy proxy) throws CpoException {
    this.proxy = proxy;

    try {
      jbInit();
    } catch (Exception e) {
      CpoUtil.showException(e);
    }

    jTreeBrowser.setModel(this.proxy.createTreeModel());

    // force a toggle - this will sort the names
    jTreeBrowser.toggleClassnames();

    // force a selection on the root node
    TreePath tp = new TreePath(jTreeBrowser.getRoot());
    jTreeBrowser.setSelectionPath(tp);
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout);
    //this.setSize(new Dimension(800, 600));
    jSplitPane.add(jScrollWest, JSplitPane.LEFT);
    jSplitPane.add(emptyPanel, JSplitPane.RIGHT);

    jScrollWest.setMinimumSize(new Dimension(300, 0));
    jScrollWest.setPreferredSize(new Dimension(300, 0));
    emptyPanel.setMinimumSize(new Dimension(400, 0));
    emptyPanel.setPreferredSize(new Dimension(400, 0));

    this.add(jSplitPane, BorderLayout.CENTER);
    jScrollWest.getViewport().add(jTreeBrowser);
    jScrollWest.getViewport().setPreferredSize(new Dimension(200, 0));
    jTreeBrowser.addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillExpand(TreeExpansionEvent tee) {
        Object pathComp = tee.getPath().getLastPathComponent();
        // if the node is a protected class node, warn them
        if (pathComp instanceof CpoClassNode) {
          if (((CpoClassNode)pathComp).isProtected()) {
            CpoUtil.showMessage("This is a protected class.  Be careful what you do.");
          }
        }
      }

      public void treeWillCollapse(TreeExpansionEvent tee) {
      }
    });
    jTreeBrowser.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent tse) {
        if (tse.isAddedPath()) {
          AbstractCpoNode acn = (AbstractCpoNode)tse.getPath().getLastPathComponent();
          Component rightComp = jSplitPane.getRightComponent();

          // save the divider location
          int divLoc = jSplitPane.getDividerLocation();

          if (rightComp != null) {
            jSplitPane.remove(rightComp);
          }
          JPanel panel = acn.getPanelForSelected();
          if (panel == null) {
            panel = emptyPanel;
          }

          jSplitPane.setRightComponent(panel);

          // reset the divider location
          jSplitPane.setDividerLocation(divLoc);
        }
      }
    });
    ToolTipManager.sharedInstance().registerComponent(jTreeBrowser);
  }

  Proxy getProxy() {
    return this.proxy;
  }

  public void save(File file) {
    jTreeBrowser.save(file);
  }

  public boolean hasUnsavedData() {
    CpoRootNode root = (CpoRootNode)jTreeBrowser.getModel().getRoot();
    return root.isUnsaved();
  }
}