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

public class MainFrame extends JFrame {

  /** Version Id for this class. */
  private static final long serialVersionUID=1L;

  JLabel statusBar = new JLabel();
  private JMenuItem menuHelpAbout = new JMenuItem();
  private JMenu menuHelp = new JMenu();
  private JMenuItem menuFileBrowser = new JMenuItem();
  private JMenuItem menuFileClassPath = new JMenuItem();
  private JMenuItem menuFileExit = new JMenuItem();
  private JMenuItem menuFileEditCon = new JMenuItem();
  private JMenuItem menuFileUnloadLoader = new JMenuItem();
  private JMenu menuFile = new JMenu();
  private JMenuBar menuBar = new JMenuBar();
  private JPanel panelCenter = new JPanel();
  private BorderLayout layoutMain = new BorderLayout();
  JTabbedPane jTabbedPane = new JTabbedPane();
  private BorderLayout layoutPanel = new BorderLayout();
  private int tabCounter = 0;

  private static ImageIcon closeIcon = new ImageIcon(CpoBrowserTree.class.getResource("/close.png"));

  public MainFrame() {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    setIconImage(Toolkit.getDefaultToolkit().createImage(MainFrame.class.getResource("/sync-logo-sm.gif")));
    this.setJMenuBar(menuBar);
    this.getContentPane().setLayout(layoutMain);
    this.setSize(new Dimension(850, 650));
    this.setTitle("CPO Utility - "+CpoUtil.username+" logged in");
    menuFile.setText("File");
    panelCenter.setLayout(layoutPanel);
    menuFileBrowser.setText("CPO Browser");
    menuFileBrowser.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          menuFileBrowser_ActionPerformed(ae);
        }
      });
    menuFileClassPath.setText("Set Custom Classpath");
    menuFileClassPath.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          menuFileClassPath_ActionPerformed(ae);
        }
      });
    menuFileEditCon.setText("Edit Connections");
    menuFileEditCon.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          try {
            fileEditCon_ActionPerformed(ae);
          } catch (Exception e) {
            CpoUtil.showException(e);
          }
        }
      });
    menuFileUnloadLoader.setText("Unload Classloader");
    menuFileUnloadLoader.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          try {
            fileUnloadLoader_ActionPerformed(ae);
          } catch (Exception e) {
            CpoUtil.showException(e);
          }
        }
      });
    menuFileExit.setText("Exit");
    menuFileExit.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          try {
            fileExit_ActionPerformed(ae);
          } catch (Exception e) {
            CpoUtil.showException(e);
          }
        }
      });
    menuHelp.setText("Help");
    menuHelpAbout.setText("About");
    menuHelpAbout.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          helpAbout_ActionPerformed(ae);
        }
      });
    statusBar.setText("");
    menuFile.add(menuFileBrowser);
    menuFile.add(menuFileClassPath);
    menuFile.add(menuFileEditCon);
    menuFile.add(menuFileUnloadLoader);
    menuFile.add(menuFileExit);
    menuBar.add(menuFile);
    menuHelp.add(menuHelpAbout);
    menuBar.add(menuHelp);
    this.getContentPane().add(statusBar, BorderLayout.SOUTH);
    panelCenter.add(jTabbedPane, BorderLayout.CENTER);
    this.getContentPane().add(panelCenter, BorderLayout.CENTER);
  }

  void menuFileClassPath_ActionPerformed(ActionEvent e) {
    CpoUtil.setCustomClassPath("Set Your Custom Classpath, make sure to include the platform EJB!");
  }

  void menuFileBrowser_ActionPerformed(ActionEvent e) {
    try {
      final CpoBrowserPanel browserPanel = new CpoBrowserPanel();
      this.jTabbedPane.addTab(browserPanel.getServer(), null, browserPanel, browserPanel.getDatabaseName());
      this.statusBar.setText("Connected to: " + browserPanel.getServer() + " using " + browserPanel.getProxy().getConnectionClassName());
      this.jTabbedPane.setSelectedComponent(browserPanel);

      JButton tabCloseButton = new JButton(closeIcon);
      tabCloseButton.setContentAreaFilled(false);
      tabCloseButton.setBorderPainted(false);
      tabCloseButton.setActionCommand("" + tabCounter++);
      tabCloseButton.setMaximumSize(new Dimension(16, 16));
      tabCloseButton.setMinimumSize(new Dimension(16, 16));
      tabCloseButton.setPreferredSize(new Dimension(16, 16));

      ActionListener al = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          JButton btn = (JButton) ae.getSource();
          String s1 = btn.getActionCommand();
          for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
            JPanel pnl = (JPanel) jTabbedPane.getTabComponentAt(i);
            btn = (JButton) pnl.getComponent(1);
            String s2 = btn.getActionCommand();
            if (s1.equals(s2)) {
              if (!CpoUtil.checkUnsavedData("You have unsaved data, are you sure you wish to exit??", i)) {
                jTabbedPane.removeTabAt(i);
              }
              break;
            }
          }
        }
      };
      tabCloseButton.addActionListener(al);

      JPanel pnl = new JPanel(new GridBagLayout());
      pnl.setOpaque(false);
      pnl.add(new JLabel(browserPanel.getServer()), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      pnl.add(tabCloseButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      jTabbedPane.setTabComponentAt(jTabbedPane.getTabCount() - 1, pnl);
      jTabbedPane.setSelectedIndex(jTabbedPane.getTabCount() - 1);

    } catch(SqlDirRequiredException ex) {
      // the server selected doesn't have a sql dir selected, force the user to pick one
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
      CpoUtil.setNewJDBCConnection(ex.getServer());
    } catch(ClassNotFoundException cnfe) {
      CpoUtil.showException(cnfe);
      CpoUtil.setCustomClassPath("Make sure platform-ejb.jar and weblogic.jar is in your custom classpath!");
    } catch (Exception pe) {
      if (!pe.getMessage().equals("No Server Selected!"))
        CpoUtil.showException(pe);
      else
        this.statusBar.setText("No Server Selected!");
    }
  }

  void fileExit_ActionPerformed(ActionEvent e) {
      if (CpoUtil.checkUnsavedData("You have unsaved data, are you sure you wish to exit??"))
        return;
      System.exit(0);
  }

  void helpAbout_ActionPerformed(ActionEvent e) {
    JOptionPane.showMessageDialog(this, new MainFrame_AboutBoxPanel(), "About", JOptionPane.PLAIN_MESSAGE);
  }

  void fileNewJdbc_ActionPerformed(ActionEvent e) {
    CpoUtil.setNewJDBCConnection(null);
  }

  void fileNewWL_ActionPerformed(ActionEvent e) {
    CpoUtil.setNewWLConnection(null);
  }

  void fileEditCon_ActionPerformed(ActionEvent e) {
    CpoUtil.editConnection();
  }
  
  void fileUnloadLoader_ActionPerformed(ActionEvent e) {
    CpoUtilClassLoader.unloadLoader();
    try {
      CpoUtil.clearMetaClassCacheOnConnectedServers();
      CpoUtil.updateStatus("Classloader Unloaded - Meta Cache Refreshed On All Connected Servers");
    } catch (Exception ex) {
      CpoUtil.showException(ex);
    }
  }
}