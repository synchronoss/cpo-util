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

import org.slf4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class CpoJDBCPropertyPanel extends JPanel  {
  /** Version Id for this class. */
  private static final long serialVersionUID=1L;

  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabJdbcUrl = new JLabel();
  private JLabel jLabJdbcDriver = new JLabel();
  private JTextField jTextJdbcUrl = new JTextField();
  private JTextField jTextJdbcDriver = new JTextField();
  private JLabel jLabCpoUtilName = new JLabel();
  private JTextField jTextCpoUtilName = new JTextField();
  private JLabel jLabTablePrefix = new JLabel();
  private JTextField jTextTablePrefix = new JTextField();
  private JLabel jLabSQLStatementDelimiter = new JLabel();
  private JTextField jTextSQLStatementDelimiter = new JTextField();
  private JLabel jLabSqlDir = new JLabel();
  private JTextField jTextSqlDir = new JTextField();
  private JLabel jLabJdbcParams = new JLabel();
  private JScrollPane jScrollParams = new JScrollPane();
  private JTextArea jTextAJDBCParams = new JTextArea();
  private JButton sqlDirBrowseButton = new JButton();
  private JButton loadButton = new JButton();
  private JButton testConnectionButton = new JButton();

  private File sqlDir = null;

  public CpoJDBCPropertyPanel() {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    this.setSize(new Dimension(450, 275));
    jLabJdbcUrl.setText("JDBC URL:");
    jLabJdbcDriver.setText("JDBC Driver:");
    jTextJdbcUrl.setText("jdbc:oracle:thin:[USER]/[PASS]@[HOSTNAME]:1521:[INSTANCE]");
    jTextJdbcDriver.setText("oracle.jdbc.OracleDriver");
    jLabCpoUtilName.setText("Cpo Util Name:");
    jTextCpoUtilName.setText("MyNewServer");
    jLabJdbcParams.setText("JDBC Params:");
    jLabTablePrefix.setText("CPO Table Prefix:");
    jLabSQLStatementDelimiter.setText("SQL Statement Delimiter:");
    jLabSqlDir.setText("SQL Output Directory:");
    jTextSqlDir.setText("Select a directory");
    jTextSqlDir.setEditable(false);

    loadButton.setText("Load");
    loadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadButtonActionPerformed(e);
      }
    });

    sqlDirBrowseButton.setText("Browse");
    sqlDirBrowseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sqlDirBrowseButtonActionPerformed(e);
      }
    });

    testConnectionButton.setText("Test");
    testConnectionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        testConnectionButtonActionPerformed(e);
      }
    });

    jScrollParams.setPreferredSize(new Dimension(300, 100));
    jScrollParams.setMinimumSize(new Dimension(300, 100));
    jScrollParams.getViewport().add(jTextAJDBCParams, null);

    this.add(jLabCpoUtilName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    this.add(jTextCpoUtilName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    this.add(loadButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    this.add(jLabJdbcUrl, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    this.add(jTextJdbcUrl, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    this.add(jLabJdbcDriver, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    this.add(jTextJdbcDriver, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    this.add(testConnectionButton, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    this.add(jLabJdbcParams, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    this.add(jScrollParams, new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    this.add(jLabTablePrefix, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    this.add(jTextTablePrefix, new GridBagConstraints(1, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    this.add(jLabSQLStatementDelimiter, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    this.add(jTextSQLStatementDelimiter, new GridBagConstraints(1, 5, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    this.add(jLabSqlDir, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    this.add(jTextSqlDir, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    this.add(sqlDirBrowseButton, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
  }

  private void loadButtonActionPerformed(ActionEvent e) {
    JFileChooser chooser = new JFileChooser();
    chooser.setApproveButtonText("Select");
    chooser.setDialogTitle("Select configuration to load:");
    /***
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setFileFilter(new FileFilter() {
      public String getDescription() {
        return "Properties files";
      }
      public boolean accept(File f) {
        return (f.getName().toLowerCase().endsWith(".properties"));
      }
    });
    ***/
    int option = chooser.showOpenDialog(this);
    if (option == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (OUT.isDebugEnabled())
        OUT.debug("File: " + file.getPath());

      try {
        Properties configProps = new Properties();
        configProps.load(new FileInputStream(file));
        setJdbcDriver(configProps.getProperty(Statics.LOAD_JDBC_DRIVER));
        setJdbcUrl(configProps.getProperty(Statics.LOAD_JDBC_URL));
        setJDBCParams(configProps.getProperty(Statics.LOAD_JDBC_PARAMS));
        setTablePrefix(configProps.getProperty(Statics.LOAD_JDBC_TABLE_PREFIX));
        setSQLStatementDelimiter(configProps.getProperty(Statics.LOAD_JDBC_SQL_STATEMENT_DELIMITER));
      } catch (Exception ex) {
        CpoUtil.showException(ex);
      }
    }
  }

  private void sqlDirBrowseButtonActionPerformed(ActionEvent e) {
    JFileChooser chooser = new JFileChooser();
    if (sqlDir != null) {
      // try the saved dir first
      chooser.setCurrentDirectory(sqlDir);
    }
    chooser.setApproveButtonText("Select");
    chooser.setDialogTitle("Select directory to save sql:");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int option = chooser.showSaveDialog(this);
    if (option == JFileChooser.APPROVE_OPTION) {
      File dir = chooser.getSelectedFile();
      if (OUT.isDebugEnabled())
        OUT.debug("Directory: " + dir.getPath());
      setSqlDir(dir);
    }
  }

  private void testConnectionButtonActionPerformed(ActionEvent e) {
    try {
      try {
        Class.forName(getJdbcDriver());
      } catch (Exception ex) {
        CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass(getJdbcDriver());
      }
      Properties connectionProperties = new Properties();
      if (getJDBCParams() != null && getJDBCParams().length() > 0) {
        StringTokenizer st = new StringTokenizer(getJDBCParams(),";");
        while (st.hasMoreTokens()) {
          String token = st.nextToken();
          StringTokenizer stNameValue = new StringTokenizer(token,"=");
          String name = null, value = null;
          if (stNameValue.hasMoreTokens())
            name = stNameValue.nextToken();
          if (stNameValue.hasMoreTokens())
            value = stNameValue.nextToken();
          connectionProperties.setProperty(name,value);
        }
      }
      Connection conn = DriverManager.getConnection(getJdbcUrl(), connectionProperties);
      conn.close();
      JOptionPane.showMessageDialog(this, "Connection successful", "Test Connection", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Test Connection", JOptionPane.ERROR_MESSAGE);
    }
  }

  public String getJdbcUrl() {
    return jTextJdbcUrl.getText();
  }

  public void setJdbcUrl(String s) {
    jTextJdbcUrl.setText(s);
  }

  public String getJdbcDriver() {
    return jTextJdbcDriver.getText();
  }

  public void setJdbcDriver(String s) {
    jTextJdbcDriver.setText(s);
  }

  public String getCpoUtilName() {
    return jTextCpoUtilName.getText();
  }

  public void setCpoUtilName(String s) {
    jTextCpoUtilName.setText(s);
  }

  public String getTablePrefix() {
    return jTextTablePrefix.getText();
  }

  public void setTablePrefix(String s) {
    jTextTablePrefix.setText(s);
  }

  public String getSQLStatementDelimiter() {
    return jTextSQLStatementDelimiter.getText();
  }

  public void setSQLStatementDelimiter(String s) {
    jTextSQLStatementDelimiter.setText(s);
  }

  public String getJDBCParams() {
    return jTextAJDBCParams.getText();
  }

  public void setJDBCParams(String s) {
    jTextAJDBCParams.setText(s);
  }

  public File getSqlDir() {
    return sqlDir;
  }

  public void setSqlDir(String s) {
    if (s != null)
      setSqlDir(new File(s));
  }

  public void setSqlDir(File f) {
    if (f != null && f.exists() && f.canWrite()) {
      sqlDir = f;
      jTextSqlDir.setText(sqlDir.getPath());
    }
  }
}