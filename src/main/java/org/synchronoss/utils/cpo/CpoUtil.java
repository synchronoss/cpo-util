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
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.List;

public class CpoUtil {

  public static final String PROPERTIES_FILE = ".cpoutil.properties";
  public static final String PROTECTED_CLASS_FILE = ".cpoutil.protected";
  
  static Properties props = new Properties();
  static Properties localProps = new Properties();
  static MainFrame frame;
  static List<File> files = new ArrayList<File>();
  static String username;

  // globally protected classes
  static HashSet<String> globallyProtectedClasses = new HashSet<String>();

  private static Logger OUT = Logger.getLogger(CpoUtil.class);
  
  public CpoUtil(String propsLocation) {
    loadProps(propsLocation);
    checkKillSwitch();
    loadGloballyProtectedClasses();

    username = System.getProperty("user.name");
    //makeSysTray();
    frame = new MainFrame();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        if (CpoUtil.checkUnsavedData("You have unsaved data, are you sure you wish to exit??"))
          return;
//        frame.setVisible(false);
        System.exit(0);      
      }
    });
    frame.setVisible(true);
  }

  protected void checkKillSwitch() {
    String minimumVersion = props.getProperty("cpoutil.minimumVersion");
    if (OUT.isDebugEnabled()) OUT.debug("Minimum version: " + minimumVersion);
    if (minimumVersion != null) {
      // if it's a snapshot, strip the -SNAPSHOT
      String version = props.getProperty("cpoutil.version");
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

      if (OUT.isDebugEnabled()) OUT.debug("Version: " + version);
      try {
        double min = Double.parseDouble(minimumVersion);
        double actual = Double.parseDouble(version);
        if (actual < min) {
          // kill it
          showMessage("Your version (" + props.getProperty("cpoutil.version") + ") is outdated.\nPlease upgrade to at least version " + minimumVersion);
          System.exit(2);
        }
      } catch (NumberFormatException ex) {
        // ignore
      }
    }
  }

  private void loadGloballyProtectedClasses() {
    if (props == null)
        return;

    String url = props.getProperty("cpoutil.protectedClasses");
    if (OUT.isDebugEnabled()) OUT.debug("Globally Protected Classes Url: " + url);

    if (url != null) {
      Set<String> protClasses = loadProtectedClassesFromUrl(url);
      if (protClasses != null) {
        // updated from url, use them and save them locally
        OUT.debug("Connected to url, loading classes");
        globallyProtectedClasses.addAll(protClasses);
        saveGloballyProtectedClasses();
      } else {
        // if the set was null, it couldn't be read, so use the local copy
        OUT.debug("Couldn't connect to url, so loading local copy");
        try {
          File protFile = new File(System.getProperties().getProperty("user.home") + File.separator + PROTECTED_CLASS_FILE);
          BufferedReader br = new BufferedReader(new FileReader(protFile));
          String line;
          while ((line = br.readLine()) != null) {
            globallyProtectedClasses.add(line);
          }
          br.close();
        } catch (IOException ioe) {
          showException(ioe);
        }
      }
    }
  }

  /**
   * Returns the set of protected classes from the url provided.
   * This will return null if there was a timeout issue, otherwise a set of
   * classes will be returned.
   *
   * @param url The url
   * @return the set of classes
   */
  public static Set<String> loadProtectedClassesFromUrl(String url) {

    Set<String> result = new HashSet<String>();

    if (url == null)
      return result;

    try {
      if (OUT.isDebugEnabled()) OUT.debug("Url: " + url);
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

  public static void main(String[] args) {
    //try {
      //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    //} catch(Exception e) {
      //e.printStackTrace();
    //}
    Policy.setPolicy(new Policy() {
      @Override
      public PermissionCollection getPermissions(CodeSource codesource) {
        Permissions perms = new Permissions();
        perms.add(new AllPermission());
        return(perms);
      }
      @Override
      public void refresh() {}
    });
    String propsLocation = null;
    if (args.length > 0)
      propsLocation = args[0];
    new CpoUtil(propsLocation);
  }
  
  static void showException(Throwable e) {
    if (OUT.isDebugEnabled())
      OUT.debug("Exception caught", e);
    
    JOptionPane.showMessageDialog(frame, new ExceptionPanel(e), "Exception Caught!", JOptionPane.PLAIN_MESSAGE);
  }

  static void updateStatus(String status) {
    frame.statusBar.setText(status);
  }
  
  private void loadProps(String propsLocation) {
    try {
      InputStream is;
      if (propsLocation == null)
        is = getClass().getResourceAsStream("/cpoutil.properties");
      else
        is = new URL(propsLocation).openStream();
      if (is == null) {
        throw new IOException("Could not find properties file!  propsLocation passed to me: "+propsLocation+"\nCPU Util will exit now.");
      }
      props.load(is);
      File propsFile = new File(System.getProperties().getProperty("user.home") + File.separator + PROPERTIES_FILE);
      if (propsFile.exists() && propsFile.canRead()) {
        is = new FileInputStream(propsFile);
        localProps.load(is);
      }

      // bootstrapping
      String bootstrapUrl = props.getProperty("cpoutil.bootstrapUrl");
      if (bootstrapUrl != null) {
        if (OUT.isDebugEnabled()) OUT.debug("Bootstrap Url: " + bootstrapUrl);
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
      frame = new MainFrame();
      showException(mue);
      System.exit(1);
    } catch (IOException ioe) {
      frame = new MainFrame();
      showException(ioe);
      System.exit(1);
    }
    String classpath = localProps.getProperty(Statics.LPROP_CLASSPATH);
    if (classpath != null) {
      StringTokenizer st = new StringTokenizer(classpath,File.pathSeparator);
      while (st.hasMoreTokens()) {
        String file = st.nextToken();
        files.add(new File(file));
      }
    }
  }
  
  public static String getServerFromUser() {
    Vector<String> vec = new Vector<String>();
    // get default provided properties
    Enumeration<?> propsEnum = CpoUtil.props.propertyNames();
    while (propsEnum.hasMoreElements()) {
      String name = (String)propsEnum.nextElement();
      if (name.startsWith(Statics.PROP_WLSURL)) {
        String server = name.substring(Statics.PROP_WLSURL.length());
        vec.add(server+":"+CpoUtil.props.getProperty(Statics.PROP_WLSURL+server)+":"+CpoUtil.props.getProperty(Statics.PROP_WLSCONNPOOL+server));
      }
      else if (name.startsWith(Statics.PROP_JDBC_URL)) {
        String server = name.substring(Statics.PROP_JDBC_URL.length());
        vec.add(server+":*JDBC ONLY*");
      }
    }
    // get local properties
    propsEnum = CpoUtil.localProps.propertyNames();
    while (propsEnum.hasMoreElements()) {
      String name = (String)propsEnum.nextElement();
      if (name.startsWith(Statics.PROP_WLSURL)) {
        String server = name.substring(Statics.PROP_WLSURL.length());
        vec.add(server+":"+CpoUtil.localProps.getProperty(Statics.PROP_WLSURL+server)+":"+CpoUtil.localProps.getProperty(Statics.PROP_WLSCONNPOOL+server));
      }
      else if (name.startsWith(Statics.PROP_JDBC_URL)) {
        String server = name.substring(Statics.PROP_JDBC_URL.length());
        vec.add(server+":*JDBC ONLY*");
      }
    }
    // sort the list
    Collections.sort(vec);
    String[] choices = new String[vec.size()];
    vec.toArray(choices);
    String selection = (String)JOptionPane.showInputDialog(frame,"Select server to connect to:","Server Selection",JOptionPane.PLAIN_MESSAGE,null,choices,null);
    return (selection==null)?null:selection.substring(0,selection.indexOf(":"));
  }
  
  public static void setCustomClassPath(String message) {
    JOptionPane.showMessageDialog(frame,new CpoUtilClassPathPanel(files),message,JOptionPane.PLAIN_MESSAGE);
    StringBuffer sbClasspath = new StringBuffer();
    for (File f : files) {
      sbClasspath.append(f);
      sbClasspath.append(File.pathSeparator);
    }
    localProps.setProperty(Statics.LPROP_CLASSPATH,sbClasspath.toString());
    saveLocalProps();
  }
  
  public static void saveLocalProps() {
    try {
      File propsFile = new File(System.getProperties().getProperty("user.home") + File.separator + PROPERTIES_FILE);
      FileOutputStream os = new FileOutputStream(propsFile);
      localProps.store(os,"Cpo Util Local Properties");
    } catch (IOException ioe) {
      showException(ioe);
    }
  }

  protected void saveGloballyProtectedClasses() {

    if (globallyProtectedClasses == null)
      return;

    try {
      File protFile = new File(System.getProperties().getProperty("user.home") + File.separator + PROTECTED_CLASS_FILE);
      PrintWriter pw = new PrintWriter(protFile);
      for (String s : globallyProtectedClasses) {
        pw.println(s);
      }
      pw.flush();
      pw.close();
    } catch (IOException ioe) {
      showException(ioe);
    }
  }

  public static void setNewWLConnection(String editServer) {
    CpoWLPropertyPanel pane = new CpoWLPropertyPanel();
    if (editServer != null) {
      pane.jTextConPool.setText(localProps.getProperty(Statics.PROP_WLSCONNPOOL+editServer));
      pane.jTextCpoJndi.setText(localProps.getProperty(Statics.PROP_CPONAME+editServer));
      pane.jTextCpoUtilName.setText(editServer);
      pane.jTextDefInitCtx.setText(localProps.getProperty(Statics.PROP_WLSINITCTXFCTRY+editServer));
      pane.jTextWLPass.setText(localProps.getProperty(Statics.PROP_WLSPASS+editServer));
      pane.jTextWLURL.setText(localProps.getProperty(Statics.PROP_WLSURL+editServer));
      pane.jTextWLUser.setText(localProps.getProperty(Statics.PROP_WLSUSER+editServer));
    }
    int result = 0;
    boolean complete = false;
    while (result == 0 && !complete) {
      result = JOptionPane.showConfirmDialog(frame,pane,"Create a new WebLogic Connection",JOptionPane.OK_CANCEL_OPTION);
      if (result == 0) {
        String server,wlsconnpool,wlsinitctx,wlspass,wlsurl,wlsuser,cponame;
        if ((server = pane.jTextCpoUtilName.getText()).equals("")) continue;
        if ((wlsconnpool = pane.jTextConPool.getText()).equals("")) continue;
        if ((wlsinitctx = pane.jTextDefInitCtx.getText()).equals("")) continue;
        if ((wlspass = pane.jTextWLPass.getText()).equals("")) continue;
        if ((wlsurl = pane.jTextWLURL.getText()).equals("")) continue;
        if ((wlsuser = pane.jTextWLUser.getText()).equals("")) continue;
        if ((cponame = pane.jTextCpoJndi.getText()).equals("")) continue;
        localProps.setProperty(Statics.PROP_WLSCONNPOOL+server,wlsconnpool);
        localProps.setProperty(Statics.PROP_WLSINITCTXFCTRY+server,wlsinitctx);
        localProps.setProperty(Statics.PROP_WLSPASS+server,wlspass);
        localProps.setProperty(Statics.PROP_WLSURL+server,wlsurl);
        localProps.setProperty(Statics.PROP_WLSUSER+server,wlsuser);
        localProps.setProperty(Statics.PROP_CPONAME+server,cponame);
        complete = true;
        saveLocalProps();
      }
    }
  }
  
  static void setNewJDBCConnection(String editServer) {
    String msg = "Create new JDBC Connection";
    CpoJDBCPropertyPanel pane = new CpoJDBCPropertyPanel();
    if (editServer != null) {
      pane.setCpoUtilName(editServer);
      pane.setJdbcDriver(localProps.getProperty(Statics.PROP_JDBC_DRIVER+editServer));
      pane.setJdbcUrl(localProps.getProperty(Statics.PROP_JDBC_URL+editServer));
      pane.setJDBCParams(localProps.getProperty(Statics.PROP_JDBC_PARAMS+editServer));
      pane.setTablePrefix(localProps.getProperty(Statics.PROP_JDBC_TABLE_PREFIX+editServer));
      pane.setSQLStatementDelimiter(localProps.getProperty(Statics.PROP_JDBC_SQL_STATEMENT_DELIMITER+editServer));
      pane.setSqlDir(localProps.getProperty(Statics.PROP_JDBC_SQL_DIR + editServer));
      msg = "Edit JDBC Connection";
    }
    int result = 0;
    boolean complete = false;
    while (result == 0 && !complete) {
      result = JOptionPane.showConfirmDialog(frame, pane, msg, JOptionPane.OK_CANCEL_OPTION);
      if (result == 0) {
        String server, jdbcdriver, jdbcurl, jdbcparams;
        File sqlDir;
        if ((server = pane.getCpoUtilName()).equals("")) {
          showErrorMessage("You must specify a util name.");
          continue;
        }
        if ((jdbcdriver = pane.getJdbcDriver()).equals("")) {
          showErrorMessage("You must specify a driver.");
          continue;
        }
        if ((jdbcurl = pane.getJdbcUrl()).equals("")) {
          showErrorMessage("You must specify a url.");
          continue;
        }
        if ((sqlDir = pane.getSqlDir()) == null) {
          showErrorMessage("You must specify a sql dir.");
          continue;
        }
        jdbcparams = pane.getJDBCParams();
        localProps.setProperty(Statics.PROP_JDBC_DRIVER + server, jdbcdriver);
        localProps.setProperty(Statics.PROP_JDBC_URL + server, jdbcurl);
        localProps.setProperty(Statics.PROP_JDBC_PARAMS + server, jdbcparams);
        localProps.setProperty(Statics.PROP_JDBC_TABLE_PREFIX + server, pane.getTablePrefix());
        localProps.setProperty(Statics.PROP_JDBC_SQL_STATEMENT_DELIMITER + server, pane.getSQLStatementDelimiter());
        localProps.setProperty(Statics.PROP_JDBC_SQL_DIR + server, sqlDir.getPath());
        complete = true;
        saveLocalProps();
      }
    }
  }
  
  static void editConnection() {
    JOptionPane.showMessageDialog(frame, new CpoEditConnPanel(localProps), "Edit Connections", JOptionPane.QUESTION_MESSAGE);
  }

  static boolean showYesNo(String message) {
    int result = JOptionPane.showConfirmDialog(frame,message,"Yes or No",JOptionPane.YES_NO_OPTION);
    return (result == 0);
  }

  static void showMessage(String message) {
    JOptionPane.showMessageDialog(frame, message);
  }

  static void showErrorMessage(String message) {
    JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  static void showMultipleQueryTextWarning() {
    showMessage("This query text is used by another query group.  Be careful what you do.");
  }

  static void showProtectedWarning() {
    showMessage("This is a protected class.  Be careful what you do.");
  }

  static void removeConnection(String server) {
    localProps.remove(Statics.PROP_CPONAME+server);
    localProps.remove(Statics.PROP_JDBC_DRIVER+server);
    localProps.remove(Statics.PROP_JDBC_URL+server);
    localProps.remove(Statics.PROP_JDBC_SQL_DIR +server);
    localProps.remove(Statics.PROP_THEME_URL+server);
    localProps.remove(Statics.PROP_WLSCONNPOOL+server);
    localProps.remove(Statics.PROP_WLSINITCTXFCTRY+server);
    localProps.remove(Statics.PROP_WLSPASS+server);
    localProps.remove(Statics.PROP_WLSURL+server);
    localProps.remove(Statics.PROP_WLSUSER+server);
    localProps.remove(Statics.PROP_JDBC_TABLE_PREFIX+server);
    saveLocalProps();
  }

  static boolean checkUnsavedData(String message) {
    return checkUnsavedData(message, -1);
  }

  /**
   * @param message Message to display if there is unsaved data.
   * @param tabIdx The index of the tab to consider, if this is -1, all tabs will be checked
   */
  static boolean checkUnsavedData(String message, int tabIdx) {
    boolean unsavedData = false;
    int tabCount = frame.jTabbedPane.getTabCount();
    for (int i = 0 ; i < tabCount ; i++) {
      CpoBrowserPanel panel = (CpoBrowserPanel)frame.jTabbedPane.getComponentAt(i);
      if ((tabIdx == -1 || tabIdx == i) && panel.getProxy().getAllChangedObjects().size() > 0) {
        unsavedData = true;
      }
    }
    if (unsavedData) {
      int result = JOptionPane.showConfirmDialog(frame,message,"Cpo Utility - Unsaved Data",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
      if (result == JOptionPane.YES_OPTION) {
        unsavedData = false;
      }
    }
    return unsavedData;
  }
  
  static void clearMetaClassCacheOnConnectedServers() throws Exception {
    int tabCount = frame.jTabbedPane.getTabCount();
    for (int i = 0 ; i < tabCount ; i++) {
      CpoBrowserPanel panel = (CpoBrowserPanel)frame.jTabbedPane.getComponentAt(i);
      panel.getProxy().clearMetaClassCache();
    }
  }
}
