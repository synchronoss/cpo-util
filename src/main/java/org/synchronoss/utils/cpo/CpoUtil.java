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
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.List;
//import com.l2fprod.gui.plaf.skin.*;
//import com.l2fprod.gui.*;
//import com.l2fprod.util.*;

public class CpoUtil {
  static Properties props = new Properties();
  static Properties localProps = new Properties();
  static MainFrame frame;
  static List<File> files = new ArrayList<File>();
  static String username;
  
  public CpoUtil(String propsLocation) {
    loadProps(propsLocation);
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

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {
      e.printStackTrace();
    }
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
    JOptionPane.showMessageDialog(frame, new ExceptionPanel(e), "Exception Caught!", JOptionPane.PLAIN_MESSAGE);
  }
  static void updateStatus(String status) {
    frame.statusBar.setText(status);
  }
  private void loadProps(String propsLocation) {
    try {
      InputStream is;
      if (propsLocation == null)
        is = getClass().getResourceAsStream("/Default.properties");
      else
        is = new URL(propsLocation).openStream();
      if (is == null) {
        throw new IOException("Could not find properties file!  propsLocation passed to me: "+propsLocation+"\nCPU Util will exit now.");
      }
      props.load(is);
      File propsFile = new File(System.getProperties().getProperty("user.home")+File.separator+".cpoutil.properties");
      if (propsFile.exists() && propsFile.canRead()) {
        is = new FileInputStream(propsFile);
        localProps.load(is);
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
      File propsFile = new File(System.getProperties().getProperty("user.home")+File.separator+".cpoutil.properties");
      FileOutputStream os = new FileOutputStream(propsFile);
      localProps.store(os,"Cpo Util Local Properties");
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
          JOptionPane.showMessageDialog(frame, "You must specify a util name.", "Error", JOptionPane.ERROR_MESSAGE);
          continue;
        }
        if ((jdbcdriver = pane.getJdbcDriver()).equals("")) {
          JOptionPane.showMessageDialog(frame, "You must specify a driver.", "Error", JOptionPane.ERROR_MESSAGE);
          continue;
        }
        if ((jdbcurl = pane.getJdbcUrl()).equals("")) {
          JOptionPane.showMessageDialog(frame, "You must specify a url.", "Error", JOptionPane.ERROR_MESSAGE);
          continue;
        }
        if ((sqlDir = pane.getSqlDir()) == null) {
          JOptionPane.showMessageDialog(frame, "You must specify a sql dir.", "Error", JOptionPane.ERROR_MESSAGE);
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
    JOptionPane.showMessageDialog(frame,new CpoEditConnPanel(localProps),"Edit Connections",
        JOptionPane.OK_OPTION);
  }
  static boolean showYesNo(String message) {
    int result = JOptionPane.showConfirmDialog(frame,message,"Yes or No",JOptionPane.YES_NO_OPTION);
    return (result == 0);
  }

  static void showMessage(String message) {
    JOptionPane.showMessageDialog(frame, message);
  }

  static File getDefaultDir() {
    File file = null;
    if (localProps.getProperty(Statics.LPROP_DEFDIR) != null) {
      try {
        file = new File(localProps.getProperty(Statics.LPROP_DEFDIR));
      } catch (Exception e) {
        showException(e);
      }
    }
    return file;
  }
  static void setDefaultDir(File file) {
    localProps.setProperty(Statics.LPROP_DEFDIR,file.toString());
    saveLocalProps();
  }
  static String getDefaultPackageName() {
    return localProps.getProperty(Statics.LPROP_DEFPACK);
  }
  static void setDefaultPackageName(String pack) {
    localProps.setProperty(Statics.LPROP_DEFPACK,pack);
    saveLocalProps();
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
    boolean unsavedData = false;
    int tabCount = frame.jTabbedPane.getTabCount();
    for (int i = 0 ; i < tabCount ; i++) {
      CpoBrowserPanel panel = (CpoBrowserPanel)frame.jTabbedPane.getComponentAt(i);
      if (panel.getProxy().getAllChangedObjects().size() > 0) unsavedData = true;
    }
    if (unsavedData) {
      int result = JOptionPane.showConfirmDialog(frame,message,"Cpo Utility - Unsaved Data",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
      if (result == JOptionPane.YES_OPTION) {
        unsavedData = false;
      }
    }
//    if (!unsavedData) {
//      this.setVisible(false);
//      System.exit(0);      
//    }
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