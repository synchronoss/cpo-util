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

import org.synchronoss.cpo.util.tree.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CpoSaveNodesPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollSave = new JScrollPane();
  private CpoSaveNodesTableModel model;

  public CpoSaveNodesPanel(CpoServerNode serverNode) {
    model = new CpoSaveNodesTableModel(serverNode);
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    this.add(jScrollSave, BorderLayout.CENTER);
    this.jScrollSave.getViewport().add(new JTable(model));
  }

  public List<AbstractCpoNode> getSelectedNodes() {
    return model.getSelectedNodes();
  }
}