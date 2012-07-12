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
package org.synchronoss.cpo.util.jdbc;

import org.slf4j.*;
import org.synchronoss.cpo.*;
import org.synchronoss.cpo.core.cpoCoreConfig.*;
import org.synchronoss.cpo.jdbc.cpoJdbcConfig.*;
import org.synchronoss.cpo.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

/**
 * JPanel for creating and editing Cassandra connections
 *
 * @author Michael Bellomo
 * @since 5/5/12
 */
 public class JdbcConnectionPanel extends AbstractConnectionPanel {

  // Version Id for this class
  private static final long serialVersionUID = 1L;
  private static final String CONFIG_PROCESSOR = "org.synchronoss.cpo.jdbc.config.JdbcCpoConfigProcessor";
  private static final String PARAM_DELIM = ";";
  private static final String PARAM_ASSIGNMENT = "=";

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private JTextField jTextName = new JTextField();
  private JTextField jTextUserName = new JTextField();
  private JPasswordField jTextPassword = new JPasswordField();
  private JTextField jTextUrl = new JTextField();
  private JTextField jTextDriver = new JTextField();
  private JTextArea jTextAreaParams = new JTextArea();

  public JdbcConnectionPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Override
  public CtJdbcConfig newDataSourceConfig() {
    return CtJdbcConfig.Factory.newInstance();
  }

  @Override
  public String getTitle() {
    return "Jdbc Connection";
  }

  @Override
  protected String getConfigProcessor() {
    return CONFIG_PROCESSOR;
  }

  private void jbInit() throws Exception {
    this.setLayout(new GridBagLayout());
    this.setSize(new Dimension(450, 275));

    JLabel jLabCpoUtilName = new JLabel("Connection Name:");
    this.add(jLabCpoUtilName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextName.setText("MyNewConnection");
    this.add(jTextName, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    /**
     * FIXME
    JButton loadButton = new JButton("Load");
    loadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadButtonActionPerformed(e);
      }
    });
    this.add(loadButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    **/

    JLabel jLabelUserName = new JLabel("User Name:");
    this.add(jLabelUserName, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    this.add(jTextUserName, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    JLabel jLabelPassword = new JLabel("Password:");
    this.add(jLabelPassword, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    this.add(jTextPassword, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    JLabel jLabelUrl = new JLabel("URL:");
    this.add(jLabelUrl, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextUrl.setText("jdbc:oracle:thin:@[HOSTNAME]:1521:[INSTANCE]");
    this.add(jTextUrl, new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    JLabel jLabJdbcDriver = new JLabel("Driver:");
    this.add(jLabJdbcDriver, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextDriver.setText("oracle.jdbc.OracleDriver");
    this.add(jTextDriver, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));


    JButton testConnectionButton = new JButton("Test");
    testConnectionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        testConnectionButtonActionPerformed();
      }
    });
    this.add(testConnectionButton, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    JLabel jLabJdbcParams = new JLabel("Jdbc Params:");
    this.add(jLabJdbcParams, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    JScrollPane jScrollParams = new JScrollPane();
    jScrollParams.setPreferredSize(new Dimension(300, 100));
    jScrollParams.setMinimumSize(new Dimension(300, 100));
    jScrollParams.getViewport().add(jTextAreaParams, null);
    this.add(jScrollParams, new GridBagConstraints(1, 5, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
  }

  /**
   * FIXME
  private void loadButtonActionPerformed(ActionEvent e) {
    JFileChooser chooser = new JFileChooser();
    chooser.setApproveButtonText("Select");
    chooser.setDialogTitle("Select configuration to load:");
    int option = chooser.showOpenDialog(this);
    if (option == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (logger.isDebugEnabled()) {
        logger.debug("File: " + file.getPath());
      }

      try {
        Properties configProps = new Properties();
        configProps.load(new FileInputStream(file));
        setJdbcDriver(configProps.getProperty(Statics.LOAD_JDBC_DRIVER));
        setJdbcUrl(configProps.getProperty(Statics.LOAD_JDBC_URL));
        setJDBCParams(configProps.getProperty(Statics.LOAD_JDBC_PARAMS));
      } catch (Exception ex) {
        CpoUtil.showException(ex);
      }
    }
  }
  */

  @Override
  public CtJdbcConfig getDataSourceConfig() {
    return (CtJdbcConfig)super.getDataSourceConfig();
  }

  private void testConnectionButtonActionPerformed() {
    try {
      CtDataSourceConfig dataSourceConfig = createDataSourceConfig();
      CpoAdapter cpoAdapter = CpoAdapterFactory.makeCpoAdapter(dataSourceConfig);
      if (cpoAdapter != null) {
        JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Connection successful", "Test Connection", JOptionPane.INFORMATION_MESSAGE);
      }
    } catch (CpoException ex) {
      CpoUtil.showErrorMessage(ex.getMessage());
    }
  }

  @Override
  public void setDataSourceConfig(CtDataSourceConfig dataSourceConfig) {
    super.setDataSourceConfig(dataSourceConfig);

    CtJdbcConfig jdbcConfig = (CtJdbcConfig)dataSourceConfig;

    jTextName.setText(jdbcConfig.getName());
    if (jdbcConfig.isSetReadWriteConfig()) {
      CtJdbcReadWriteConfig rwc = jdbcConfig.getReadWriteConfig();

      jTextUserName.setText(rwc.getUser());
      jTextPassword.setText(rwc.getPassword());
      jTextUrl.setText(rwc.getUrl());
      if (rwc.isSetDriverClassName()) {
        jTextDriver.setText(rwc.getDriverClassName());
      }

      StringBuilder params = new StringBuilder();
      for (CtProperty prop : rwc.getPropertyArray()) {
        params.append(prop.getName());
        params.append(PARAM_ASSIGNMENT);
        params.append(prop.getValue());
        params.append(PARAM_DELIM);
      }
      jTextAreaParams.setText(params.toString());
    }
  }

  @Override
  public CtDataSourceConfig createDataSourceConfig() throws CpoException {
    String connectionName = jTextName.getText();
    String userName = jTextUserName.getText();
    String password = new String(jTextPassword.getPassword());
    String url = jTextUrl.getText();
    String driver = jTextDriver.getText();
    String params = jTextAreaParams.getText();

    // validation first
    if (connectionName == null || connectionName.isEmpty()) {
      throw new CpoException("Connection name must be provided");
    }

    CtJdbcConfig myConfig = getDataSourceConfig();

    // dupe?
    CtDataSourceConfig dupe = CpoUtil.getInstance().getDataSourceConfig(connectionName);
    if (dupe != null && !dupe.equals(myConfig)) {
      throw new CpoException("Another connection is using the name: " + connectionName + "\n\nConnection names must be unique");
    }

    if (userName == null || userName.isEmpty()) {
      throw new CpoException("A user name must be provided");
    }

    if (password.isEmpty()) {
      throw new CpoException("A password must be provided");
    }

    if (url == null || url.isEmpty()) {
      throw new CpoException("A url must be provided");
    }

    if (driver == null || driver.isEmpty()) {
      throw new CpoException("A driver must be provided");
    }

    // create a new data source config, so we don't muck w/ the live one
    CtJdbcConfig jdbcConfig = newDataSourceConfig();
    jdbcConfig.setName(connectionName);

    if (myConfig != null && myConfig.getMetaDescriptorName() != null) {
      jdbcConfig.setMetaDescriptorName(myConfig.getMetaDescriptorName());
    } else {
      jdbcConfig.setMetaDescriptorName(DEFAULT_META_DESCRIPTOR);
    }

    jdbcConfig.setCpoConfigProcessor(this.getConfigProcessor());

    CtJdbcReadWriteConfig rwc = jdbcConfig.addNewReadWriteConfig();
    rwc.setUser(userName);
    rwc.setPassword(password);
    rwc.setUrl(url);
    rwc.setDriverClassName(driver);

    if (params != null && !params.isEmpty()) {
      StringTokenizer st = new StringTokenizer(params, PARAM_DELIM);
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        StringTokenizer stNameValue = new StringTokenizer(token, PARAM_ASSIGNMENT);
        String name = null, value = null;
        if (stNameValue.hasMoreTokens())
          name = stNameValue.nextToken();
        if (stNameValue.hasMoreTokens())
          value = stNameValue.nextToken();

        CtProperty prop = rwc.addNewProperty();
        prop.setName(name);
        prop.setValue(value);
      }
    }

    return jdbcConfig;

  }
}