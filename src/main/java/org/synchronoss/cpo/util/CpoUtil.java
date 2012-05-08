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

import org.apache.xmlbeans.*;
import org.slf4j.*;
import org.synchronoss.cpo.CpoException;
import org.synchronoss.cpo.core.cpoCoreConfig.CtDataSourceConfig;
import org.synchronoss.cpo.meta.domain.CpoClass;
import org.synchronoss.cpo.util.cpoUtilConfig.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.List;

public class CpoUtil extends JFrame {

  private static Logger OUT = LoggerFactory.getLogger(CpoUtil.class);

  private static URL mainIcon = CpoBrowserTree.class.getResource("/images/sync-logo-sm.gif");
  private static ImageIcon closeIcon = new ImageIcon(CpoBrowserTree.class.getResource("/images/close.png"));

  private static final String CPOUTIL_CONFIG_DIRNAME = ".cpoutil";
  private static final File CPOUTIL_CONFIG_DIR = new File(System.getProperties().getProperty("user.home"), CPOUTIL_CONFIG_DIRNAME);
  private static final String CPOUTIL_CONFIG_FILE = "CpoUtilConfig.xml";
  private static final String CPOUTIL_PROPERTIES_FILE = "cpoutil.properties";

  private static final String BOOTSTRAP_URL_PROP = "cpoutil.bootstrapUrl";
  private static final String PROTECTED_CLASS_PROP = "cpoutil.protectedClasses";

  public static final String TITLE = "cpoutil.title";
  public static final String VERSION = "cpoutil.version";
  public static final String AUTHOR = "cpoutil.author";
  public static final String COPYRIGHT = "cpoutil.copyright";
  public static final String COMPANY = "cpoutil.company";
  public static final String MINIMUM_VERSION = "cpoutil.minimumVersion";


  // config
  private static CtCpoUtilConfig cpoUtilConfig = null;

  private File configFile = new File(CPOUTIL_CONFIG_DIR, CPOUTIL_CONFIG_FILE);
  private Properties props = new Properties();

  // reference to the CpoUtil frame
  private static CpoUtil cpoUtil;

  // protected classes
  private HashSet<String> protectedClasses = new HashSet<String>();

  private JLabel statusBar = new JLabel();

  private JPanel panelCenter = new JPanel();
  protected JTabbedPane jTabbedPane = new JTabbedPane();
  private int tabCounter = 0;

  public static CpoUtil getInstance() {
    return cpoUtil;
  }

  public CpoUtil() {
    cpoUtil = this;

    loadConfig();
    checkKillSwitch();
    loadProtectedClasses();

    try {
      jbInit();
    } catch (Exception e) {
      OUT.error(e.getMessage(), e);
    }

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = cpoUtil.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        exit();
      }
    });
    this.setVisible(true);
  }

  private void jbInit() throws Exception {
    setIconImage(Toolkit.getDefaultToolkit().createImage(mainIcon));

    this.getContentPane().setLayout(new BorderLayout());
    this.setSize(new Dimension(1000, 800));
    this.setTitle("CPO Utility");
    panelCenter.setLayout(new BorderLayout());

    // menu bar
    JMenuBar menuBar = new JMenuBar();
    this.setJMenuBar(menuBar);

    // file menu
    JMenu menuFile = new JMenu("File");
    menuBar.add(menuFile);

    JMenu newMenu = new JMenu("New");
    menuFile.add(newMenu);

    for (final SupportedType type : SupportedType.values()) {
      JMenuItem typeMenu = new JMenuItem(type.toString());
      newMenu.add(typeMenu);
      typeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          newMetaFile(type);
        }
      });
    }

    JMenuItem menuOpen = new JMenuItem("Open");
    menuOpen.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        openActionPerformed(ae);
      }
    });
    menuFile.add(menuOpen);

    JMenuItem menuSave = new JMenuItem("Save");
    menuSave.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          saveActionPerformed(ae);
        } catch (Exception e) {
          CpoUtil.showException(e);
        }
      }
    });
    menuFile.add(menuSave);

    JMenuItem menuSaveAs = new JMenuItem("Save As");
    menuSaveAs.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          saveAsActionPerformed(ae);
        } catch (Exception e) {
          CpoUtil.showException(e);
        }
      }
    });
    menuFile.add(menuSaveAs);

    JMenuItem menuExit = new JMenuItem("Exit");
    menuExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        exit();
      }
    });
    menuFile.add(menuExit);

    // Connections Menu
    JMenu menuConnections = new JMenu("Connections");
    menuBar.add(menuConnections);

    JMenu newConnectionMenu = new JMenu("New");
    menuConnections.add(newConnectionMenu);

    for (final SupportedType type : SupportedType.values()) {
      JMenuItem typeMenu = new JMenuItem(type.toString());
      newConnectionMenu.add(typeMenu);
      typeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          newConnection(type);
        }
      });
    }

    JMenuItem menuEditCon = new JMenuItem("Edit Connections");
    menuEditCon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          editConActionPerformed(ae);
        } catch (Exception e) {
          CpoUtil.showException(e);
        }
      }
    });
    menuConnections.add(menuEditCon);

    // tools menu
    JMenu menuTools = new JMenu("Tools");
    menuBar.add(menuTools);

    JMenuItem menuClasspath = new JMenuItem("Set Custom Classpath");
    menuClasspath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setCustomClasspath("Set Custom Classpath");
      }
    });
    menuTools.add(menuClasspath);

    JMenuItem menuUnloadLoader = new JMenuItem("Unload Classloader");
    menuUnloadLoader.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        unloadCustomClassLoader();
      }
    });
    menuTools.add(menuUnloadLoader);

    // help menu
    JMenu menuHelp = new JMenu("Help");
    menuBar.add(menuHelp);

    JMenuItem menuHelpAbout = new JMenuItem("About");
    menuHelpAbout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        aboutActionPerformed(ae);
      }
    });
    menuHelp.add(menuHelpAbout);

    statusBar.setText("");
    this.getContentPane().add(statusBar, BorderLayout.SOUTH);

    panelCenter.add(jTabbedPane, BorderLayout.CENTER);
    this.getContentPane().add(panelCenter, BorderLayout.CENTER);
  }

  private void newMetaFile(SupportedType type) {
    JFileChooser jFileChooser = new JFileChooser();
    jFileChooser.setMultiSelectionEnabled(false);
    jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    jFileChooser.setFileFilter(new FileFilter() {
      public String getDescription() {
        return "XML files";
      }

      public boolean accept(File f) {
        return (f.getName().toLowerCase().endsWith(".xml"));
      }
    });
    int result = jFileChooser.showSaveDialog(this);
    if (result == JFileChooser.CANCEL_OPTION) {
      return;
    }

    try {
      Proxy proxy = ProxyFactory.getInstance().newProxy(jFileChooser.getSelectedFile(), type);
      createNewBrowser(proxy);
    } catch (CpoException ex) {
      CpoUtil.showException(ex);
    }
  }

  private void openActionPerformed(ActionEvent e) {

    JFileChooser jFileChooser = new JFileChooser();
    jFileChooser.setMultiSelectionEnabled(false);
    jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    jFileChooser.setFileFilter(new FileFilter() {
      public String getDescription() {
        return "XML files";
      }

      public boolean accept(File f) {
        return (f.getName().toLowerCase().endsWith(".xml"));
      }
    });
    int result = jFileChooser.showOpenDialog(this);
    if (result == JFileChooser.CANCEL_OPTION) {
      return;
    }

    // make sure the file is readable
    File cpoMetaXml = jFileChooser.getSelectedFile();
    if (cpoMetaXml == null || !cpoMetaXml.canRead()) {
      CpoUtil.showErrorMessage("Invalid file selected.");
      return;
    }

    // make sure this file isn't already open
    int tabCount = jTabbedPane.getTabCount();
    for (int i = 0; i < tabCount; i++) {
      CpoBrowserPanel panel = (CpoBrowserPanel)jTabbedPane.getComponentAt(i);
      if (cpoMetaXml.equals(panel.getProxy().getCpoMetaXml())) {
        CpoUtil.showErrorMessage("The selected file is already open: " + cpoMetaXml.getAbsolutePath());
        return;
      }
    }

    try {
      Proxy proxy = ProxyFactory.getInstance().getProxy(cpoMetaXml);
      createNewBrowser(proxy);
    } catch (CpoException ex) {
      CpoUtil.showException(ex);
    }
  }

  private void createNewBrowser(Proxy proxy) {
    try {
      CpoBrowserPanel browserPanel = new CpoBrowserPanel(proxy);
      this.jTabbedPane.addTab(browserPanel.getProxy().toString(), null, browserPanel, browserPanel.getProxy().toString());
      this.jTabbedPane.setSelectedComponent(browserPanel);

      JButton tabCloseButton = new JButton(closeIcon);
      tabCloseButton.setContentAreaFilled(false);
      tabCloseButton.setBorderPainted(false);
      tabCloseButton.setActionCommand(Integer.toString(tabCounter++));
      tabCloseButton.setMaximumSize(new Dimension(16, 16));
      tabCloseButton.setMinimumSize(new Dimension(16, 16));
      tabCloseButton.setPreferredSize(new Dimension(16, 16));

      ActionListener al = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          JButton btn = (JButton)ae.getSource();
          String s1 = btn.getActionCommand();
          for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
            JPanel pnl = (JPanel)jTabbedPane.getTabComponentAt(i);
            btn = (JButton)pnl.getComponent(1);
            String s2 = btn.getActionCommand();
            if (s1.equals(s2)) {
              if (!checkUnsavedData("You have unsaved data, are you sure you wish to exit??", i)) {
                jTabbedPane.removeTabAt(i);
              }
              break;
            }
          }
        }
      };
      tabCloseButton.addActionListener(al);

      // tab w/ close button
      JPanel pnl = new JPanel(new GridBagLayout());
      pnl.setOpaque(false);
      pnl.add(new JLabel(browserPanel.getProxy().toString()), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      pnl.add(tabCloseButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      jTabbedPane.setTabComponentAt(jTabbedPane.getTabCount() - 1, pnl);
      jTabbedPane.setSelectedIndex(jTabbedPane.getTabCount() - 1);
    } catch (CpoException ex) {
      CpoUtil.showException(ex);
    }
  }

  private void saveActionPerformed(ActionEvent e) {
    int index = jTabbedPane.getSelectedIndex();
    if (index != -1) {
      save(null);
    }
  }

  private void saveAsActionPerformed(ActionEvent e) {
    int index = jTabbedPane.getSelectedIndex();
    if (index != -1) {
      JFileChooser jFileChooser = new JFileChooser();
      jFileChooser.setMultiSelectionEnabled(false);
      jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jFileChooser.setFileFilter(new FileFilter() {
        public String getDescription() {
          return "XML files";
        }

        public boolean accept(File f) {
          return (f.getName().toLowerCase().endsWith(".xml"));
        }
      });
      int result = jFileChooser.showSaveDialog(this);
      if (result == JFileChooser.CANCEL_OPTION) {
        return;
      }

      save(jFileChooser.getSelectedFile());
    }
  }

  private void save(File file) {
    int index = jTabbedPane.getSelectedIndex();
    if (index != -1) {
      CpoBrowserPanel cbp = (CpoBrowserPanel)jTabbedPane.getComponentAt(index);
      if (cbp != null) {
        cbp.save(file);
      }
    }
  }

  private void aboutActionPerformed(ActionEvent e) {
    JOptionPane.showMessageDialog(this, new AboutBoxPanel(), "About", JOptionPane.PLAIN_MESSAGE);
  }

  private void newConnection(SupportedType type) {
    try {
      AbstractConnectionPanel panel = type.getConnectionPanelClass().newInstance();
      panel.newDataSourceConfig();
      String title = "Create " + panel.getTitle();

      int result = 0;
      boolean complete = false;
      while (result == JOptionPane.OK_OPTION && !complete) {
        result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
          try {
            CtDataSourceConfig dataSourceConfig = panel.createDataSourceConfig();
            if (!cpoUtilConfig.isSetDataConfigs()) {
              cpoUtilConfig.addNewDataConfigs();
            }
            CtDataSourceConfig dsc = cpoUtilConfig.getDataConfigs().addNewDataConfig();
            dsc.set(dataSourceConfig);
            saveConfig();
            complete = true;
          } catch (CpoException ex) {
            showErrorMessage(ex.getMessage());
          }
        }
      }
    } catch (Exception ex) {
      showException(ex);
    }
  }

  protected void editConnection(String connectionName) {

    CtDataSourceConfig dataSourceConfig = getDataSourceConfig(connectionName);
    if (dataSourceConfig == null) {
      showErrorMessage("Can't find data source config for connection: " + connectionName);
      return;
    }

    try {
      SupportedType type = SupportedType.getTypeForConnection(dataSourceConfig);
      AbstractConnectionPanel panel = type.getConnectionPanelClass().newInstance();
      panel.setDataSourceConfig(dataSourceConfig);
      String title = "Edit " + panel.getTitle();

      int result = 0;
      boolean complete = false;
      while (result == JOptionPane.OK_OPTION && !complete) {
        result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
          try {
            CtDataSourceConfig panelDsc = panel.createDataSourceConfig();
            for (CtDataSourceConfig dsc : cpoUtilConfig.getDataConfigs().getDataConfigArray()) {
              // lookup based on the old name, so if it changed, we replace it
              if (dsc.getName().equals(dataSourceConfig.getName())) {
                dsc.set(panelDsc);
                complete = true;
                saveConfig();
              }
            }

            if (!complete) {
              throw new CpoException("Saving connection failed, could not update connection: " + connectionName);
            }

          } catch (CpoException ex) {
            showErrorMessage(ex.getMessage());
          }
        }
      }
    } catch (Exception ex) {
      showException(ex);
    }
  }

  protected boolean editConnection(AbstractConnectionPanel panel, String title) {
    int result = 0;
    boolean complete = false;
    while (result == JOptionPane.OK_OPTION && !complete) {
      result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        try {
          CtDataSourceConfig dataSourceConfig = panel.createDataSourceConfig();
          complete = true;
          saveConfig();
        } catch (CpoException ex) {
          showErrorMessage(ex.getMessage());
        }
      }
    }
    return complete;
  }

  private void editConActionPerformed(ActionEvent e) {
    CpoEditConnPanel cecp = new CpoEditConnPanel();
    JOptionPane.showConfirmDialog(this, cecp, "Edit Connections", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
  }

  private void unloadCustomClassLoader() {
    CpoUtilClassLoader.unloadLoader();
    setStatusBarText("Classloader Unloaded - Meta Cache Refreshed On All Connected Servers");
  }

  public void setStatusBarText(String txt) {
    statusBar.setText(txt);
  }

  public String getProperty(String propertyName) {
    return props.getProperty(propertyName);
  }

  protected void loadConfig() {
    try {
      // user config
      CpoUtilConfigDocument cpoUtilConfigDocument;
      if (!configFile.exists()) {
        // file doesn't exist, let's make one
        cpoUtilConfigDocument = CpoUtilConfigDocument.Factory.newInstance();
        saveConfig();
      } else {
        cpoUtilConfigDocument = CpoUtilConfigDocument.Factory.parse(configFile);
      }

      cpoUtilConfig = cpoUtilConfigDocument.getCpoUtilConfig();

      // read default props
      InputStream is = getClass().getResourceAsStream("/" + CPOUTIL_PROPERTIES_FILE);
      if (is == null) {
        throw new IOException("Could not find properties file!\nCPU Util will exit now.");
      }
      props.load(is);

      // bootstrapping
      String bootstrapUrl = props.getProperty(BOOTSTRAP_URL_PROP);
      if (bootstrapUrl != null && !bootstrapUrl.isEmpty()) {
        if (OUT.isDebugEnabled()) {
          OUT.debug("Bootstrap Url: " + bootstrapUrl);
        }
        try {
          UrlLoader loader = new UrlLoader(bootstrapUrl);
          Thread thread = new Thread(loader);
          thread.start();
          thread.join(3000);

          InputStream in = loader.getInputStream();
          if (in != null) {
            props.load(in);
            in.close();
          }
        } catch (Exception ex) {
          OUT.error("Exception caught reading bootstrap properties: " + ex);
        }
      }
    } catch (MalformedURLException mue) {
      showException(mue);
      System.exit(1);
    } catch (IOException ex) {
      showException(ex);
      System.exit(1);
    } catch (XmlException ex) {
      showException(ex);
      System.exit(1);
    }
  }

  public void saveConfig() {
    CpoUtilConfigDocument doc = CpoUtilConfigDocument.Factory.newInstance();
    CtCpoUtilConfig config = doc.addNewCpoUtilConfig();
    config.set(cpoUtilConfig);

    XmlOptions xo = new XmlOptions();
    xo.setCharacterEncoding("utf-8");
    xo.setSaveAggressiveNamespaces();
    xo.setSaveNamespacesFirst();
    xo.setSavePrettyPrint();
    xo.setUseDefaultNamespace();

    try {
      doc.save(configFile, xo);
    } catch (Exception ex) {
      showException(ex);
    }
  }

  protected void checkKillSwitch() {
    String minimumVersion = props.getProperty(MINIMUM_VERSION);
    if (OUT.isDebugEnabled()) {
      OUT.debug("Minimum version: " + minimumVersion);
    }
    if (minimumVersion != null) {
      // if it's a snapshot, strip the -SNAPSHOT
      String version = props.getProperty(VERSION);
      if (version.endsWith("-SNAPSHOT")) {
        version = version.substring(0, version.length() - 9);
      }

      // if the version is something like 2.9.1, use 2.9 as the version, the .1 is a minor rev
      int idxFirstDot = version.indexOf(".");
      if (idxFirstDot != -1) {
        int idxSecondDot = version.indexOf(".", idxFirstDot + 1);
        if (idxSecondDot != -1) {
          version = version.substring(0, idxSecondDot);
        }
      }

      if (OUT.isDebugEnabled()) {
        OUT.debug("Version: " + version);
      }

      double min = 0;
      try {
        min = Double.parseDouble(minimumVersion);
      } catch (NumberFormatException ex) {
        OUT.error(ex.getMessage());
      }

      double actual = 0;
      try {
        actual = Double.parseDouble(version);
      } catch (NumberFormatException ex) {
        OUT.error(ex.getMessage());
      }

      if (actual < min) {
        // kill it
        showMessage("Your version (" + props.getProperty(VERSION) + ") is outdated.\nPlease upgrade to at least version " + minimumVersion);
        System.exit(2);
      }
    }
  }

  private void loadProtectedClasses() {
    if (props == null) {
      return;
    }

    String url = props.getProperty(PROTECTED_CLASS_PROP);
    if (OUT.isDebugEnabled()) {
      OUT.debug("Protected Classes Url: " + url);
    }

    Set<String> protClasses = loadProtectedClassesFromUrl(url);
    if (protClasses != null) {
      // updated from url, use them and save them locally
      OUT.debug("Connected to url, loading classes");
      protectedClasses.addAll(protClasses);
      saveProtectedClasses();
    } else {
      // if the set was null, it couldn't be read, so use the local copy
      OUT.debug("Couldn't connect to url, so loading local copy");
      if (cpoUtilConfig.isSetProtectedClasses()) {
        protectedClasses.addAll(Arrays.asList(cpoUtilConfig.getProtectedClasses().getProtectedClassArray()));
      }
    }
  }

  /**
   * Returns the set of protected classes from the url provided.
   * This will return null if there was a timeout issue, otherwise a set of
   * classes will be returned.
   *
   * @param url The url
   *
   * @return the set of classes
   */
  private Set<String> loadProtectedClassesFromUrl(String url) {

    if (url == null) {
      return null;
    }

    Set<String> result = new HashSet<String>();
    try {
      if (OUT.isDebugEnabled()) {
        OUT.debug("Url: " + url);
      }
      UrlLoader loader = new UrlLoader(url);
      Thread thread = new Thread(loader);
      thread.start();
      thread.join(3000);

      InputStream in = loader.getInputStream();
      if (in != null) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String inputLine;
        while ((inputLine = br.readLine()) != null) {
          result.add(inputLine);
        }
        br.close();
        in.close();
      }
      return result;
    } catch (Exception ex) {
      OUT.debug("Exception caught reading from url: " + ex);
    }

    return null;
  }

  protected void saveProtectedClasses() {
    if (protectedClasses == null) {
      return;
    }

    if (cpoUtilConfig.isSetProtectedClasses()) {
      cpoUtilConfig.unsetProtectedClasses();
    }
    cpoUtilConfig.addNewProtectedClasses();

    for (String pc : protectedClasses) {
      cpoUtilConfig.getProtectedClasses().addProtectedClass(pc);
    }

    // always save
    saveConfig();
  }

  public static List<File> getCustomClasspathEntries() {
    // Custom classpath
    List<File> files = new ArrayList<File>();
    if (cpoUtilConfig.isSetCustomClasspath()) {
      for (String classpathEntry : cpoUtilConfig.getCustomClasspath().getClasspathEntryArray()) {
        files.add(new File(classpathEntry));
      }
    }

    return files;
  }

  public void setCustomClasspath(String message) {
    CpoUtilClasspathPanel cpp = new CpoUtilClasspathPanel(getCustomClasspathEntries());
    int result = JOptionPane.showConfirmDialog(this, cpp, message, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      List<File> classpathEntries = cpp.getClasspathEntries();

      // remove everything
      cpoUtilConfig.unsetCustomClasspath();

      // if entries exist, add them
      if (!classpathEntries.isEmpty()) {
        cpoUtilConfig.addNewCustomClasspath();

        for (File f : classpathEntries) {
          cpoUtilConfig.getCustomClasspath().addClasspathEntry(f.getAbsolutePath());
        }
      }

      // always save
      saveConfig();

      // refresh the class loader
      unloadCustomClassLoader();
    }
  }

  public List<CtDataSourceConfig> getDataSourceConfigs() {
    List<CtDataSourceConfig> result = new ArrayList<CtDataSourceConfig>();
    if (cpoUtilConfig.isSetDataConfigs()) {
      result.addAll(Arrays.asList(cpoUtilConfig.getDataConfigs().getDataConfigArray()));
    }
    return result;
  }

  public CtDataSourceConfig getDataSourceConfig(String name) {
    if (cpoUtilConfig.isSetDataConfigs()) {
      for (CtDataSourceConfig dataSourceConfig : cpoUtilConfig.getDataConfigs().getDataConfigArray()) {
        if (dataSourceConfig.getName().equals(name)) {
          return dataSourceConfig;
        }
      }
    }
    return null;
  }

  protected Vector<String> getConnectionList() {
    Vector<String> result = new Vector<String>();
    for (CtDataSourceConfig dataSourceConfig : getDataSourceConfigs()) {
      result.add(dataSourceConfig.getName());
    }
    Collections.sort(result, new CaseInsensitiveStringComparator());
    return result;
  }

  protected void addConnection(CtDataSourceConfig dataSourceConfig) {
    if (!cpoUtilConfig.isSetDataConfigs()) {
      cpoUtilConfig.addNewDataConfigs();
    }
    CtDataSourceConfig cdsc = cpoUtilConfig.getDataConfigs().addNewDataConfig();
    cdsc.set(dataSourceConfig);
    saveConfig();
  }

  protected void removeConnection(String connectionName) {
    int index = 0;
    int connectionNameIndex = -1;

    for (CtDataSourceConfig dataSourceConfig : cpoUtilConfig.getDataConfigs().getDataConfigArray()) {
      if (dataSourceConfig.getName().equals(connectionName)) {
        connectionNameIndex = index;
      }
      index++;
    }
    cpoUtilConfig.getDataConfigs().removeDataConfig(connectionNameIndex);
    saveConfig();
  }

  /**
   * Returns true if there is any unsaved data.
   *
   * @param message Message to display if there is unsaved data.
   */
  public boolean checkUnsavedData(String message) {
    return checkUnsavedData(message, -1);
  }

  /**
   * Returns true if there is any unsaved data on the tab at the specified index.
   *
   * @param message Message to display if there is unsaved data.
   * @param tabIdx The index of the tab to consider, if this is -1, all tabs will be checked
   */
  public boolean checkUnsavedData(String message, int tabIdx) {
    boolean unsavedData = false;
    int tabCount = jTabbedPane.getTabCount();
    for (int i = 0; i < tabCount; i++) {
      CpoBrowserPanel panel = (CpoBrowserPanel)jTabbedPane.getComponentAt(i);
      if ((tabIdx == -1 || tabIdx == i) && panel.hasUnsavedData()) {
        unsavedData = true;
      }
    }
    if (unsavedData) {
      int result = JOptionPane.showConfirmDialog(this, message, "Cpo Utility - Unsaved Data", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (result == JOptionPane.YES_OPTION) {
        unsavedData = false;
      }
    }
    return unsavedData;
  }

  /**
   * Returns true if a class is protected.  This will match exact class names as well as packages
   * for example:
   * org.synchronoss.utils.cpo.CpoClassNode - would match that exact class
   * org.synchronoss.utils.cpo - would match all classes in the cpo package
   */
  public boolean isClassProtected(CpoClass cpoClass) {

    String className = cpoClass.getName();

    // exact match
    if (protectedClasses.contains(className)) {
      return true;
    }

    // partial match
    for (String s : protectedClasses) {
      if (className.startsWith(s)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Exits CpoUtil
   */
  private void exit() {
    if (checkUnsavedData("You have unsaved data, are you sure you wish to exit??")) {
      return;
    }

    System.exit(0);
  }

  // Some static helper methods for displaying messages
  public static void showException(Throwable e) {
    if (OUT.isDebugEnabled()) {
      OUT.debug("Exception caught", e);
    }
    JOptionPane.showMessageDialog(CpoUtil.getInstance(), new ExceptionPanel(e), "Exception Caught!", JOptionPane.PLAIN_MESSAGE);
  }

  public static void showMessage(String message) {
    JOptionPane.showMessageDialog(CpoUtil.getInstance(), message);
  }

  public static void showErrorMessage(String message) {
    JOptionPane.showMessageDialog(CpoUtil.getInstance(), message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Main method to run CpoUtil
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      OUT.error(e.getMessage(), e);
    }
    Policy.setPolicy(new Policy() {
      @Override
      public PermissionCollection getPermissions(CodeSource codesource) {
        Permissions perms = new Permissions();
        perms.add(new AllPermission());
        return (perms);
      }

      @Override
      public void refresh() {
      }
    });
    new CpoUtil();
  }
}