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

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class CpoBrowserTree extends JTree {

  /**
   * Version Id for this class.
   */
  private static final long serialVersionUID = 1L;

  private JPopupMenu menu;
  private static ImageIcon iconRed = new ImageIcon(CpoBrowserTree.class.getResource("/images/red.gif"));
  private static ImageIcon iconYellow = new ImageIcon(CpoBrowserTree.class.getResource("/images/yellow.gif"));
  private static ImageIcon iconGreen = new ImageIcon(CpoBrowserTree.class.getResource("/images/green.gif"));
  private static ImageIcon iconBlue = new ImageIcon(CpoBrowserTree.class.getResource("/images/blue.gif"));

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public CpoBrowserTree() {

    // single select
    TreeSelectionModel treeSelectionModel = new DefaultTreeSelectionModel();
    treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.setSelectionModel(treeSelectionModel);

    this.setCellRenderer(new DefaultTreeCellRenderer() {
      // Version Id for this class
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
          } else if (node.isProtected()) {
            this.setIcon(iconBlue);
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
  public DefaultTreeModel getModel() {
    return (DefaultTreeModel)super.getModel();
  }

  protected CpoRootNode getRoot() {
    return (CpoRootNode)getModel().getRoot();
  }

  @Override
  public String getToolTipText(MouseEvent e) {
    TreePath path = this.getPathForLocation(e.getX(), e.getY());
    if (path != null && path.getLastPathComponent() instanceof AbstractCpoNode) {
      AbstractCpoNode node = (AbstractCpoNode)path.getLastPathComponent();
      return (node.getToolTipText());
    }
    return null;
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
    CpoUtil.getInstance().setStatusBarText("");
    menu.removeAll();
    menu.setLabel("");
    if (node instanceof AbstractCpoNode) {
      final AbstractCpoNode menuNode = (AbstractCpoNode)node;
      menu.setLabel(menuNode.toString());
      if (menuNode instanceof CpoRootNode) {
        if (menuNode.isChildDirty() || menuNode.isChildNew() || menuNode.isChildRemove()) {
          JMenuItem jMenuCommitChildren = new JMenuItem("Save Changes");
          jMenuCommitChildren.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              save(null);
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

        JMenuItem jMenuRefresh = new JMenuItem("Reload Config");
        jMenuRefresh.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            refreshFromXml(null);
          }
        });
        menu.add(jMenuRefresh);
      } else if (menuNode instanceof CpoClassNode) {
        final CpoClassNode cpoClassNode = (CpoClassNode)menuNode;
        JMenuItem jMenuRename = new JMenuItem("Rename Class");
        jMenuRename.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            renameCpoClassNode(cpoClassNode);
          }
        });
        menu.add(jMenuRename);

        JMenuItem jMenuGenerateClass = new JMenuItem("Generate Class Source");
        jMenuGenerateClass.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            generateClassSource(cpoClassNode);
          }
        });
        menu.add(jMenuGenerateClass);
      } else if (menuNode instanceof CpoFunctionGroupLabelNode) {
        JMenuItem jMenuAddGroup = new JMenuItem("Add Function Group to Class");
        jMenuAddGroup.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            addFunctionGroup((CpoFunctionGroupLabelNode)menuNode);
          }
        });
        menu.add(jMenuAddGroup);
      } else if (menuNode instanceof CpoFunctionGroupNode) {
        final CpoFunctionGroupNode cpoFunctionGroupNode = (CpoFunctionGroupNode)menuNode;
        JMenuItem jMenuAddQuery = new JMenuItem("Add Function to Group");
        jMenuAddQuery.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            addFunctionToGroup(cpoFunctionGroupNode);
          }
        });
        menu.add(jMenuAddQuery);

        JMenuItem jMenuRenameQG = new JMenuItem("Rename Query Group");
        jMenuRenameQG.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            renameFunctionGroup(cpoFunctionGroupNode);
          }
        });
        menu.add(jMenuRenameQG);
      } else if (menuNode instanceof CpoFunctionNode) {
        final CpoFunctionNode functionNode = (CpoFunctionNode)menuNode;

        JMenuItem jMenuMoveUp = new JMenuItem("Move Up");
        // only available if it's not the first child
        jMenuMoveUp.setEnabled(functionNode.getSeqNo() > 1);
        jMenuMoveUp.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            moveFunctionUp(functionNode);
          }
        });
        menu.add(jMenuMoveUp);

        JMenuItem jMenuMoveDown = new JMenuItem("Move Down");
        // only available if it's not the last child
        jMenuMoveDown.setEnabled(functionNode.getSeqNo() < functionNode.getParent().getChildCount());
        jMenuMoveDown.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            moveFunctionDown(functionNode);
          }
        });
        menu.add(jMenuMoveDown);
      }

      if (!(menuNode instanceof CpoRootNode) && (!(menuNode instanceof CpoAttributeLabelNode)) && (!(menuNode instanceof CpoFunctionGroupLabelNode))) {
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

  private void addFunctionGroup(CpoFunctionGroupLabelNode functionGroupLabelNode) {
    FunctionGroupPanel fgp = new FunctionGroupPanel();
    int result = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), fgp, "Create new Function Group", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      try {
        Proxy proxy = getRoot().getProxy();

        String groupName = fgp.getGroupName().equals("") ? null : fgp.getGroupName();

        CpoFunctionGroupNode cpoFunctionGroupNode = proxy.addFunctionGroup(functionGroupLabelNode.getParent(), groupName, fgp.getGroupType());

        // create a function to the group
        addFunctionToGroup(cpoFunctionGroupNode);

      } catch (CpoException ex) {
        CpoUtil.showException(ex);
      }
    }
  }

  private void addFunctionToGroup(CpoFunctionGroupNode cpoFunctionGroupNode) {
    try {
      Proxy proxy = getRoot().getProxy();

      CpoFunctionNode cpoFunctionNode = proxy.addFunction(cpoFunctionGroupNode);

      // try to expand and select the node
      DefaultTreeModel model = getModel();
      this.expandPath(new TreePath(model.getPathToRoot(cpoFunctionGroupNode)));
      this.setSelectionPath(new TreePath(cpoFunctionNode));

    } catch (CpoException ex) {
      CpoUtil.showException(ex);
    }
  }

  protected void save(File file) {
    CpoRootNode rootNode = getRoot();
    SaveNodesPanel saveNodesPanel = new SaveNodesPanel(rootNode);
    int result = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), saveNodesPanel, "Save Objects", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      try {
        rootNode.getProxy().save(file);

        refreshFromXml(file);

        CpoUtil.getInstance().setStatusBarText("Xml saved successfully");
      } catch (Exception pe) {
        CpoUtil.showException(pe);
      }
    }
  }

  protected void refreshFromXml(File file) {
    // figure out the index of this tab, so we only check unsaved data on this tab.
    int idx = -1;

    Component parent = this.getParent();
    while (idx == -1 && parent != null) {
      if (parent instanceof CpoBrowserPanel) {
        idx = CpoUtil.getInstance().jTabbedPane.indexOfComponent(parent);
      }
      parent = parent.getParent();
    }

    // should never happen
    if (idx == -1) {
      CpoUtil.showErrorMessage("Unable to refresh");
      return;
    }

    if (CpoUtil.getInstance().checkUnsavedData("There is unsaved data, are you sure you wish to refresh over it??", idx)) {
      return;
    }

    Proxy proxy = getRoot().getProxy();

    try {
      ProxyFactory.getInstance().refreshMetaDescriptor(proxy, file);

      this.setModel(proxy.createTreeModel());

      // force a toggle - this will sort the names
      toggleClassnames();

      // force a selection on the root node
      TreePath tp = new TreePath(getModel().getRoot());
      this.setSelectionPath(tp);

      CpoUtil.getInstance().setStatusBarText("Reloaded " + proxy);
    } catch (CpoException ex) {
      CpoUtil.showException(ex);
    }
  }

  private void createNewCpoClassFromClass() {
    logger.debug("creating new class from class");

    Proxy proxy = getRoot().getProxy();

    String className = null;
    List<CpoAttribute> attributes = new ArrayList<CpoAttribute>();

    boolean happy = false;
    while (!happy) {
      CpoNewClassClassPanel cnccp = new CpoNewClassClassPanel(proxy);
      int result = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), cnccp, "Create new CPO Class from Class", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        try {
          className = cnccp.getClassName();
          attributes = proxy.createAttributesFromClass(className);
          happy = true;
        } catch (Exception pe) {
          CpoUtil.showException(pe);
        }
      } else {
        /**
         * user wishes to cancel creation
         */
        CpoUtil.getInstance().setStatusBarText("Aborted Class Creation");
        return;
      }
    }

    createNewClass(className, attributes);
  }

  private void createNewCpoClass() {
    logger.debug("creating new class");

    Proxy proxy = getRoot().getProxy();

    String className = null;
    List<CpoAttribute> attributes = new ArrayList<CpoAttribute>();

    boolean happy = false;
    while (!happy) {
      CpoNewClassPanel cncp = new CpoNewClassPanel(proxy);
      int result = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), cncp, "Create new CPO Class", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        try {
          className = cncp.getClassName();
          attributes = proxy.createAttributesFromExpression(cncp.getConnection(), cncp.getExpression());
          happy = true;
        } catch (Exception pe) {
          CpoUtil.showException(pe);
        }
      } else {
        /**
         * user wishes to cancel creation
         */
        CpoUtil.getInstance().setStatusBarText("Aborted Class Creation");
        return;
      }
    }

    createNewClass(className, attributes);
  }

  private void createNewClass(String className, List<CpoAttribute> attributes) {
    try {
      Proxy proxy = getRoot().getProxy();
      CpoClassNode cpoClassNode = proxy.addClass(className);
      proxy.addAttributes(cpoClassNode, attributes);

      saveClassSource(cpoClassNode);

      // try to expand and select the node
      DefaultTreeModel model = getModel();
      this.expandPath(new TreePath(model.getPathToRoot(cpoClassNode)));
      this.setSelectionPath(new TreePath(cpoClassNode));

      CpoUtil.getInstance().setStatusBarText("Class (" + cpoClassNode.getUserObject().getClass().getName() + ") successfully created");
    } catch (Exception pe) {
      CpoUtil.showException(pe);
    }
  }

  private void saveClassSource(CpoClassNode cpoClassNode) {
    Proxy proxy = getRoot().getProxy();

    String className = cpoClassNode.getUserObject().getName();

    String saveClassName = className;
    if (className.contains(".")) {
      saveClassName = className.substring(className.lastIndexOf(".") + 1);
    }
    JFileChooser jFile = new JFileChooser();
    if (proxy.getLastDir() != null) {
      jFile.setCurrentDirectory(proxy.getLastDir());
    }
    jFile.setDialogTitle("Choose a directory to save: " + saveClassName + ".java");
    jFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int result = jFile.showSaveDialog(this.getTopLevelAncestor());
    if (result != JFileChooser.APPROVE_OPTION || jFile.getSelectedFile() == null) {
      CpoUtil.getInstance().setStatusBarText("Aborted Class Creation: file not saved");
      return;
    }

    proxy.setLastDir(jFile.getCurrentDirectory());
    try {
      FileWriter fw = new FileWriter(jFile.getSelectedFile() + File.separator + saveClassName + ".java");
      fw.write(proxy.generateSourceCode(cpoClassNode));
      fw.close();
    } catch (IOException ioe) {
      CpoUtil.showException(ioe);
      CpoUtil.getInstance().setStatusBarText("Class not created: exception caught during save: " + ioe.getMessage());
      return;
    }
    CpoUtil.getInstance().setStatusBarText("Class (" + className + ") successfully saved");
  }

  private void renameFunctionGroup(CpoFunctionGroupNode cpoFunctionGroupNode) {
    String result = (String)JOptionPane.showInputDialog(this.getTopLevelAncestor(), "Enter new group name", "Edit Group Name", JOptionPane.INFORMATION_MESSAGE, null, null, cpoFunctionGroupNode.getGroupName());
    if (result == null) {
      return;
    }
    if (result.equals("")) {
      result = null;
    }
    cpoFunctionGroupNode.setGroupName(result);
    CpoUtil.getInstance().setStatusBarText("Changed Group Name to: " + result);
  }

  public void toggleClassnames() {
    CpoRootNode rootNode = getRoot();

    // toggle the class name display
    rootNode.getProxy().toggleClassNames();

    List<CpoClassNode> nodes = new ArrayList<CpoClassNode>();
    for (int i = 0; i < rootNode.getChildCount(); i++) {
      CpoClassNode node = (CpoClassNode)rootNode.getChildAt(i);

      // add to the list
      nodes.add(node);
    }

    // sort them - note that this sorts by display name so it will work both for long and short names
    Collections.sort(nodes);

    // unlink from the root
    rootNode.removeAllChildren();

    // link them back into the tree
    for (CpoClassNode node : nodes) {
      // relink
      rootNode.add(node);
    }

    this.getModel().nodeStructureChanged(rootNode);
  }

  private void renameCpoClassNode(CpoClassNode cpoClassNode) {
    String result = (String)JOptionPane.showInputDialog(this.getTopLevelAncestor(), "Enter new class name", "Edit Class Name", JOptionPane.INFORMATION_MESSAGE, null, null, cpoClassNode.getUserObject().getName());
    if (result == null) {
      return;
    }
    if (result.equals("")) {
      result = null;
    }
    cpoClassNode.setClassName(result);
    CpoUtil.getInstance().setStatusBarText("Changed Class Name to: " + result);
  }

  private void generateClassSource(CpoClassNode cpoClassNode) {
    try {
      this.saveClassSource(cpoClassNode);
    } catch (Exception e) {
      CpoUtil.showException(e);
    }
  }

  private void moveFunctionUp(CpoFunctionNode functionNode) {
    functionNode.getProxy().moveFunctionUp(functionNode);
  }

  private void moveFunctionDown(CpoFunctionNode functionNode) {
    functionNode.getProxy().moveFunctionDown(functionNode);
  }
}