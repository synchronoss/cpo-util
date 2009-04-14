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
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.*;

public class CpoBrowserPanel extends JPanel  {
  /** Version Id for this class. */
  private static final long serialVersionUID=1L;
  private Proxy prox;
  private BorderLayout borderLayout = new BorderLayout();
  private JScrollPane jScrollWest = new JScrollPane();
  private CpoBrowserTree jTreeBrowser = new CpoBrowserTree();
  private JSplitPane jSplitPane = new JSplitPane();
  private JPanel emptyPanel = new JPanel();
  
  public CpoBrowserPanel() throws Exception {
    String server = CpoUtil.getServerFromUser();
    prox = new Proxy(CpoUtil.props,server,jTreeBrowser);

    // check to make sure they have a sql dir defined.  If they don't make them select one
    String sqlDirStr = prox.getSqlDir();
    if (sqlDirStr == null) {
      throw new SqlDirRequiredException("The selected server does not have a sql directory set.\nPlease select one now.", server);
    }

    File sqlDir = new File(sqlDirStr);
    if (!sqlDir.exists()) {
      if (!sqlDir.mkdirs()) {
        throw new SqlDirRequiredException("Unable to create directory: " + sqlDir.getPath(), server);
      }
    }

    if (!sqlDir.isDirectory()) {
      throw new SqlDirRequiredException("The sql dir is not a directory: " + sqlDir.getPath(), server);
    }

    if (!sqlDir.canWrite()) {
      throw new SqlDirRequiredException("Unable to write to directory: " + sqlDir.getPath(), server);
    }

    try {
      jbInit();
    }catch(Exception e) {
      CpoUtil.showException(e);
    }

    // if the directory is empty, prime it
    if (sqlDir.listFiles() == null || sqlDir.listFiles().length == 0) {
      ExportAllSwingWorker exporter = new ExportAllSwingWorker(prox.getServerNode());
      exporter.start();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout);
    this.setSize(new Dimension(800, 600));
    jSplitPane.add(jScrollWest, JSplitPane.LEFT);
    jSplitPane.add(emptyPanel, JSplitPane.RIGHT);

    jScrollWest.setMinimumSize(new Dimension(150,0));
    jScrollWest.setPreferredSize(new Dimension(200,0));
    emptyPanel.setMinimumSize(new Dimension(400,0));
    emptyPanel.setPreferredSize(new Dimension(400,0));

    this.add(jSplitPane, BorderLayout.CENTER);
    jScrollWest.getViewport().add(jTreeBrowser);
    jScrollWest.getViewport().setPreferredSize(new Dimension(200,0));
    jTreeBrowser.setModel(new DefaultTreeModel(new CpoServerNode(prox,jTreeBrowser)));
    jTreeBrowser.addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillExpand(TreeExpansionEvent tee) {
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
          Component rightComp = jSplitPane.getRightComponent();
          if (rightComp != null)
            jSplitPane.remove(rightComp);
          JPanel panel = ((AbstractCpoNode)pathComp).getPanelForSelected();
          if (panel == null)
            panel = emptyPanel;

          jSplitPane.setRightComponent(panel);
          panel.setMinimumSize(new Dimension(400,0));
          panel.setPreferredSize(new Dimension(400,0));
        }
      }
    });
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