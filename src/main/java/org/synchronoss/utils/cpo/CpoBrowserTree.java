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
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.List;

public class CpoBrowserTree extends JTree  {
    /** Version Id for this class. */
    private static final long serialVersionUID=1L;
	private JPopupMenu menu;
  private AbstractCpoNode menuNode;            
  private static ImageIcon iconRed = new ImageIcon(CpoBrowserTree.class.getResource("/red.gif"));
  private static ImageIcon iconYellow = new ImageIcon(CpoBrowserTree.class.getResource("/yellow.gif"));
  private static ImageIcon iconGreen = new ImageIcon(CpoBrowserTree.class.getResource("/green.gif"));
  private Logger OUT = Logger.getLogger(this.getClass());

  public CpoBrowserTree() {
    this.setCellRenderer(new DefaultTreeCellRenderer() {
      /** Version Id for this class. */
      private static final long serialVersionUID = 1L;

      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        setText(value.toString());
        if (value instanceof AbstractCpoNode) {
          AbstractCpoNode node = (AbstractCpoNode)value;
          if (node.isDirty() || node.isRemove() || node.isNew()) {
            this.setIcon(iconRed);
          } else if (node.isChildDirty() || node.isChildRemove() || node.isChildNew()) {
            this.setIcon(iconYellow);
          } else {
            this.setIcon(iconGreen);
          }
        }
        if (hasFocus || sel) {
          this.setForeground(Color.red);
        } else {
          this.setForeground(Color.blue);
        }
        return this;
      }
    });
		menu = new JPopupMenu();
		addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          showMenu(e.getPoint());
        }
      }
    });
	}

  @Override
  public String getToolTipText(MouseEvent e) {
    StringBuffer sb = new StringBuffer();
    TreePath path = this.getPathForLocation( e.getX(), e.getY());
    if (path != null && path.getLastPathComponent() instanceof AbstractCpoNode) {
      sb.append("<html>");
      AbstractCpoNode node = (AbstractCpoNode)path.getLastPathComponent();
      if (node instanceof CpoClassNode) {
        sb.append(((CpoClassNode)node).getClassName());
        sb.append("<BR>");
      }
      sb.append("User: ");
      sb.append(node.getUserName());
      sb.append("<BR>Changed: ");
      sb.append(node.getCreateDate());
      sb.append("</html>");
    }
    return sb.toString();
  }

	private void showMenu(Point p) {
		TreePath path = getPathForLocation((int)p.getX(), (int)p.getY());
		if (path != null) {
			TreeNode node = (TreeNode)path.getLastPathComponent();
			buildMenu(node);
			menu.show(this, (int)p.getX(), (int)p.getY());
		}
	}

  private void buildMenu(TreeNode node) {
    CpoUtil.updateStatus("");
    menu.removeAll();
    menu.setLabel("");
    if (node instanceof AbstractCpoNode) {
      menuNode = (AbstractCpoNode)node;
      menu.setLabel(menuNode.toString());
      if (menuNode instanceof CpoServerNode) {
        if (menuNode.isChildDirty()
            || menuNode.isChildNew()
            || menuNode.isChildRemove()) {
          JMenuItem jMenuCommitChildren = new JMenuItem("Save Child Changes");
          jMenuCommitChildren.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              saveNodes();
            }
          });
          menu.add(jMenuCommitChildren);
        }        
        JMenuItem jMenuFqdnToggle = new JMenuItem("Toggle Classnames");
        jMenuFqdnToggle.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            toggleClassnames();
          }
        });
        menu.add(jMenuFqdnToggle);          
        JMenuItem jMenuAddClass = new JMenuItem("Add new Class");
        jMenuAddClass.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            createNewCpoClass();
          }
        });
        menu.add(jMenuAddClass);
        JMenuItem jMenuAddClassFromClass = new JMenuItem("Add new Class from .class");
        jMenuAddClassFromClass.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            createNewCpoClassFromClass();
          }
        });
        menu.add(jMenuAddClassFromClass);
        JMenuItem jMenuRefresh = new JMenuItem("Refresh from DB");
        jMenuRefresh.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            refreshFromDB();
          }
        });
        menu.add(jMenuRefresh);
        JMenuItem jMenuReloadCpo = new JMenuItem("Clear Meta Class Cache on Server");
        jMenuReloadCpo.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            clearMetaClassCache();
          }
        });
        menu.add(jMenuReloadCpo);

        JMenuItem jMenuSaveSqlDel = new JMenuItem("Export SQL for all of CPO");
        jMenuSaveSqlDel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            exportSqlAllCpo();
          }
        });
        menu.add(jMenuSaveSqlDel);
        JMenuItem jMenuReconnect = new JMenuItem("Reconnect to Server");
        jMenuReconnect.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            reconnectServer();
          }
        });
        menu.add(jMenuReconnect);
      }
      if (menuNode instanceof CpoClassNode) {
        JMenuItem jMenuExportSql = new JMenuItem("Export SQL for Class");
        jMenuExportSql.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            exportSql();
          }
        });
        menu.add(jMenuExportSql);
        JMenuItem jMenuReloadCpo = new JMenuItem("Clear Meta Class Cache on Server for this class");
        jMenuReloadCpo.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            clearMetaClassCacheForClass();
          }
        });
        menu.add(jMenuReloadCpo);
        JMenuItem jMenuRename = new JMenuItem("Rename Class");
        jMenuRename.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            renameCpoClassNode();
          }
        });
        menu.add(jMenuRename);
        JMenuItem jMenuGenerateClass = new JMenuItem("Generate Class Source");
        jMenuGenerateClass.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            generateClassSource();
          }
        });
        menu.add(jMenuGenerateClass);
      }
      if (menuNode instanceof CpoQueryGroupLabelNode) {
        JMenuItem jMenuAddGroup = new JMenuItem("Add Query Group to Class");
        jMenuAddGroup.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            createNewCpoQueryGroup();
          }
        });
        menu.add(jMenuAddGroup);
      }
      if (menuNode instanceof CpoQueryGroupNode) {
        JMenuItem jMenuAddQuery = new JMenuItem("Add Query to Group");
        jMenuAddQuery.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            addQueryToGroup();
          }
        });
        menu.add(jMenuAddQuery);
      }
      if (menuNode instanceof CpoQueryGroupNode) {
        JMenuItem jMenuRenameQG = new JMenuItem("Rename Query Group");
        jMenuRenameQG.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            renameQG();
          }
        });
        menu.add(jMenuRenameQG);
      }
      if (!(menuNode instanceof CpoServerNode) 
          && (!(menuNode instanceof CpoAttributeLabelNode))
          && (!(menuNode instanceof CpoQueryGroupLabelNode))) {
        JMenuItem jMenuRemove = new JMenuItem("Remove this object!");
        jMenuRemove.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            menuNode.setRemove(true);
          }
        });
        menu.add(jMenuRemove);
      }
    }
  }

  private void createNewCpoQueryGroup() {
    QueryGroupPanel cgp = new QueryGroupPanel(menuNode);
    int result = JOptionPane.showConfirmDialog(this, cgp, "Create new Query Group", JOptionPane.OK_CANCEL_OPTION);
    if (result == 0) {
      if (menuNode instanceof CpoQueryGroupLabelNode) {
        CpoQueryGroupNode cqgn = ((CpoQueryGroupLabelNode)menuNode).addNewQueryGroup(cgp.getGroupName().equals("") ? null : cgp.getGroupName(), cgp.getGroupType());

        // try to expand and select the node
        if (cqgn != null) {
          DefaultTreeModel model = (DefaultTreeModel)getModel();
          this.expandPath(new TreePath(model.getPathToRoot(cqgn)));
          CpoQueryNode cqn = cqgn.addNewQueryNode();
          if (cqn != null) {
            this.setSelectionPath(new TreePath(cqn));
          }
        }
      }
    }
  }

  private void addQueryToGroup() {
    if (menuNode instanceof CpoQueryGroupNode) {
      CpoQueryGroupNode cqgn = (CpoQueryGroupNode)menuNode;
      CpoQueryNode cqn = cqgn.addNewQueryNode();

      // try to expand and select the node
      if (cqn != null) {
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        this.expandPath(new TreePath(model.getPathToRoot(cqgn)));
        this.setSelectionPath(new TreePath(cqn));
      }
    }
  }

  private void saveNodes() {
    CpoSaveNodesPanel saveNodesPanel = new CpoSaveNodesPanel((CpoServerNode)menuNode);
    int result = JOptionPane.showConfirmDialog(this, saveNodesPanel, "Save Objects", JOptionPane.OK_CANCEL_OPTION);
    if (result == 0) {
      try {
        List<AbstractCpoNode> nodeList = saveNodesPanel.getSelectedNodes();
        menuNode.getProxy().saveNodes(nodeList);

        // bug 197 - force a refresh when someone saves
        if (!refreshFromDB())
          return;

        ExportChangedSwingWorker exporter = new ExportChangedSwingWorker(nodeList);
        exporter.start();

      } catch (Exception pe) {
        CpoUtil.showException(pe);
        return;
      }
//      OUT.debug ("Saved Objects");
    }
    CpoUtil.updateStatus("Persisted Nodes to DB");
  }

  private boolean refreshFromDB() {
    // figure out the index of this tab, so we only check unsaved data on this tab.
    int idx = -1;

    Component parent = this.getParent();
    while (idx == -1 && parent != null) {
      if (parent instanceof CpoBrowserPanel) {
        idx = CpoUtil.frame.jTabbedPane.indexOfComponent(parent);
      }
      parent = parent.getParent();
    }

    if (CpoUtil.checkUnsavedData("There is unsaved data, are you sure you wish to refresh over it??", idx))
      return false;
    
    menuNode.getProxy().removeObjectsFromAllCache();
    CpoServerNode csn = new CpoServerNode(menuNode.getProxy(),this);
//    menuNode.scrubNodes();
//    ((DefaultTreeModel)this.getModel()).nodeStructureChanged(menuNode);n
    ((DefaultTreeModel)this.getModel()).setRoot(csn);

    // force a selection on the root node
    TreePath tp = new TreePath(getModel().getRoot());
    this.setSelectionPath(tp);
    
    CpoUtil.updateStatus("Cleared Local Application Cache");
    return true;
  }

  private void createNewCpoClassFromClass() {
    OUT.debug("creating new class");
    boolean happy = false;
    String className = null;
    Method methods[] = null;
    while (!happy) {
      CpoNewClassClassPanel cnccp = new CpoNewClassClassPanel();
      if (menuNode.getProxy().getDefaultPackageName() != null)
        cnccp.jTextClassName.setText(menuNode.getProxy().getDefaultPackageName());
      OUT.debug("opening joption pane ...");
      int result = JOptionPane.showConfirmDialog(this,cnccp,"Create new CPO Class from Class", JOptionPane.OK_CANCEL_OPTION);
      if (result == 0) {
        className = cnccp.jTextClassName.getText();
        if (className.lastIndexOf(".") != -1)
          menuNode.getProxy().setDefaultPackageName(className.substring(0,className.lastIndexOf(".")));
        try {
          System.out.println("CpoUtil: " + menuNode.getProxy().getDefaultPackageName() + " "  + className);
          methods = CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass(className).getMethods();
          happy = true;
        } catch (Exception pe) {
          CpoUtil.showException(pe);
        }
      } else {
        /**
         * user wishes to cancel creation
         */
        CpoUtil.updateStatus("Aborted Class Creation");
        return;
      }
      
    }
    CpoClassNode ccn;
    try {
      ccn = new CpoClassNode(className,menuNode.getProxy().getNewGuid(),menuNode);
      menuNode.getProxy().getClasses(menuNode).add(ccn);
      menuNode.getProxy().getClassesById().put(ccn.getClassId(),ccn);
      OUT.debug("Menu Node Class Count: "+menuNode.getProxy().getClasses(menuNode).size());
      ccn.setNew(true);
      menuNode.getProxy().generateNewAttributeMap(ccn,methods);
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      this.refreshFromDB();
      CpoUtil.updateStatus("Class could not be added to the db, please report this error");
      return;
    }
    CpoUtil.updateStatus("Class ("+className+") successfully created");    
  }

  private void createNewCpoClass() {
    boolean happy = false;
    String className = null;
    String sql = null;
    String classString = null;
    while (!happy) {
      CpoNewClassPanel cncp = new CpoNewClassPanel();
      if (menuNode.getProxy().getDefaultPackageName() != null)
        cncp.jTextClassName.setText(menuNode.getProxy().getDefaultPackageName());
      int result = JOptionPane.showConfirmDialog(this,cncp,"Create new CPO Class", JOptionPane.OK_CANCEL_OPTION);
      if (result == 0) {
        className = cncp.jTextClassName.getText();
        if (className.lastIndexOf(".") != -1)
          menuNode.getProxy().setDefaultPackageName(className.substring(0,className.lastIndexOf(".")));
        sql = cncp.jTextAsql.getText();
        try {
          classString = menuNode.getProxy().makeClassOuttaSql(className, sql);
          happy = true;
        } catch (Exception pe) {
          CpoUtil.showException(pe);
        }
      } else {
        /**
         * user wishes to cancel creation
         */
        CpoUtil.updateStatus("Aborted Class Creation");
        return;
      }
    }
    saveClassSource(classString, className);
    CpoClassNode ccn;
    try {
      ccn = new CpoClassNode(className,menuNode.getProxy().getNewGuid(),menuNode);
      menuNode.getProxy().getClasses(menuNode).add(ccn);
      menuNode.getProxy().getClassesById().put(ccn.getClassId(),ccn);
      OUT.debug("Menu Node Class Count: "+menuNode.getProxy().getClasses(menuNode).size());
      ccn.setNew(true);
      menuNode.getProxy().generateNewAttributeMap(ccn,sql);
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      this.refreshFromDB();
      CpoUtil.updateStatus("Class could not be added to the db, please report this error");
    }
  }

  private void saveClassSource(String classString, String className) {
    String saveClassName = className;
    if (className.indexOf(".") != -1)
      saveClassName = className.substring(className.lastIndexOf(".")+1);
    JFileChooser jFile = new JFileChooser();
    if (menuNode.getProxy().getDefaultDir() != null)
      jFile.setCurrentDirectory(menuNode.getProxy().getDefaultDir());
    jFile.setDialogTitle("Choose a directory to save: "+saveClassName+".java");
    jFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int result = jFile.showSaveDialog(this);
    if (result != 0 || jFile.getSelectedFile() == null) {
      CpoUtil.updateStatus("Aborted Class Creation: file not saved");
      return;
    }
    menuNode.getProxy().setDefaultDir(jFile.getCurrentDirectory());
    try {
      FileWriter fw = new FileWriter(jFile.getSelectedFile()+File.separator+saveClassName+".java");
      fw.write(classString);
      fw.close();
    } catch (IOException ioe) {
      CpoUtil.showException(ioe);
      CpoUtil.updateStatus("Class not created: exception caught during save: "+ioe.getMessage());
      return;
    }
    CpoUtil.updateStatus("Class ("+className+") successfully saved");    
  }

  private void clearMetaClassCache() {
    try {
      menuNode.getProxy().clearMetaClassCache();
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      return;
    }
    CpoUtil.updateStatus("Meta Class Cache Cleared");
  }

  private void clearMetaClassCacheForClass() {
    try {
      menuNode.getProxy().clearMetaClassCache(((CpoClassNode)menuNode).getClassName());
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      return;
    }
    CpoUtil.updateStatus("Meta Class Cache Cleared for "+((CpoClassNode)menuNode).getClassName());
  }

  private void exportSqlAllCpo() {

    // bug 197 - force a refresh when someone saves
    if (!refreshFromDB())
        return;

    ExportAllSwingWorker exporter = new ExportAllSwingWorker(menuNode);
    exporter.start();
  }

  private void exportSql() {
    // bug 197 - force a refresh when someone saves
    if (!refreshFromDB())
      return;
    
    String sqlDir = menuNode.getProxy().getSqlDir();
    JFileChooser chooser = new JFileChooser();
    if (sqlDir != null) {
      // if there is a sql dir, use that
      chooser.setCurrentDirectory(new File(sqlDir));
    } else if (menuNode.getProxy().getDefaultDir() != null) {
      // if there wasn't a sql dir try the default dir
      chooser.setCurrentDirectory(menuNode.getProxy().getDefaultDir());
    }
    chooser.setApproveButtonText("Select");
    chooser.setDialogTitle("Select directory to save sql:");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int option = chooser.showSaveDialog(this);
    if (option == JFileChooser.APPROVE_OPTION) {
      File dir = chooser.getSelectedFile();
      
      if (OUT.isDebugEnabled())
        OUT.debug("Directory: " + dir.getPath());

      ExportClassSwingWorker exporter = new ExportClassSwingWorker(menuNode, dir);
      exporter.start();
    }
  }

  private void reconnectServer() {
    try {
      menuNode.getProxy().getConnection();
      int result = JOptionPane.showConfirmDialog(this,"Refresh From DB Too?",
          "Refresh from DB",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
      if (result == 0)
        refreshFromDB();
    } catch (ClassNotFoundException cnfe) {
      CpoUtil.setCustomClassPath("CPO Classes where not found, make sure ejb is in your classpath!");
      return;
    } catch (Exception pe) {
      CpoUtil.showException(pe);
      CpoUtil.updateStatus("Could not reconnect: "+pe.getMessage());
      return;
    }
    CpoUtil.updateStatus("Reconnected to Server!");
  }

  private void renameQG() {
    String result = (String)JOptionPane.showInputDialog(this,"Enter new group name","Edit Group Name",JOptionPane.INFORMATION_MESSAGE,null,null,((CpoQueryGroupNode)menuNode).getGroupName());
    if (result == null) return;
    if (result.equals("")) result = null;
    ((CpoQueryGroupNode)menuNode).setGroupName(result);
    CpoUtil.updateStatus("Changed Group Name to: "+result);
  }

  private void toggleClassnames() {
    menuNode.getProxy().toggleClassNames();
    menuNode.refreshMe();
  }

  private void renameCpoClassNode() {
    String result = (String)JOptionPane.showInputDialog(this,"Enter new class name","Edit Class Name",JOptionPane.INFORMATION_MESSAGE,null,null,((CpoClassNode)menuNode).getClassName());
    if (result == null) return;
    if (result.equals("")) result = null;
    ((CpoClassNode)menuNode).setClassName(result);
    CpoUtil.updateStatus("Changed Class Name to: "+result);    
  }

  private void generateClassSource() {
    try {
      String classString = menuNode.getProxy().makeClassOuttaNode((CpoClassNode)menuNode);
      this.saveClassSource(classString, ((CpoClassNode)menuNode).getClassName());
    } catch (Exception e) {
      CpoUtil.showException(e);
    }
  }
}