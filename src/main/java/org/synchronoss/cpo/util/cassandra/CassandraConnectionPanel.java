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
package org.synchronoss.cpo.util.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.synchronoss.cpo.CpoAdapter;
import org.synchronoss.cpo.CpoAdapterFactory;
import org.synchronoss.cpo.CpoException;
import org.synchronoss.cpo.cassandra.cpoCassandraConfig.CtCassandraConfig;
import org.synchronoss.cpo.cassandra.cpoCassandraConfig.CtCassandraReadWriteConfig;
import org.synchronoss.cpo.core.cpoCoreConfig.CtDataSourceConfig;
import org.synchronoss.cpo.util.AbstractConnectionPanel;
import org.synchronoss.cpo.util.CpoUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * JPanel for creating and editing Cassandra connections
 *
 * @author Michael Bellomo
 * @since 5/5/12
 */
public class CassandraConnectionPanel extends AbstractConnectionPanel {

  private static final String CONFIG_PROCESSOR = "org.synchronoss.cpo.cassandra.config.CassandraCpoConfigProcessor";

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private JTextField jTextName = new JTextField();
  //private JTextField jTextUserName = new JTextField();
  //private JPasswordField jTextPassword = new JPasswordField();
  private JTextField jTextHost = new JTextField();
  private JTextField jTextKeyspace = new JTextField();

  public CassandraConnectionPanel() {
    super();
    try {
      jbInit();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Override
  protected String getConfigProcessor() {
    return CONFIG_PROCESSOR;
  }

  @Override
  public String getTitle() {
    return "Cassandra Connection";
  }

  @Override
  public CtCassandraConfig newDataSourceConfig() {
    return CtCassandraConfig.Factory.newInstance();
  }

  private void jbInit() throws Exception {
    this.setLayout(new GridBagLayout());
    this.setSize(new Dimension(450, 275));

    JLabel jLabCpoUtilName = new JLabel("Connection Name:");
    this.add(jLabCpoUtilName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextName.setText("MyNewConnection");
    this.add(jTextName, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    //JLabel jLabelUserName = new JLabel("User Name:");
    //this.add(jLabelUserName, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    //this.add(jTextUserName, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    //JLabel jLabelPassword = new JLabel("Password:");
    //this.add(jLabelPassword, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    //this.add(jTextPassword, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    JLabel jLabelHost = new JLabel("Host:");
    this.add(jLabelHost, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextHost.setText("127.0.0.1");
    this.add(jTextHost, new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    JLabel jLabKeyspace = new JLabel("Keyspace:");
    this.add(jLabKeyspace, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

    jTextKeyspace.setText("MyKeyspace");
    this.add(jTextKeyspace, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

    JButton testConnectionButton = new JButton("Test");
    testConnectionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        testConnectionButtonActionPerformed();
      }
    });
    this.add(testConnectionButton, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
  }

  @Override
  public CtCassandraConfig getDataSourceConfig() {
    return (CtCassandraConfig)super.getDataSourceConfig();
  }


  private void testConnectionButtonActionPerformed() {
    try {
      CtDataSourceConfig dataSourceConfig = createDataSourceConfig();
      CpoAdapter cpoAdapter = CpoAdapterFactoryManager.makeCpoAdapterFactory(dataSourceConfig).getCpoAdapter();
      if (cpoAdapter != null) {
        JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Connection successful", "Test Connection", JOptionPane.INFORMATION_MESSAGE);
      }
    } catch (CpoException ex) {
      CpoUtil.showErrorMessage(ex.getMessage());
    }
  }

  @Override
  public CtDataSourceConfig createDataSourceConfig() throws CpoException {
    String connectionName = jTextName.getText();
    //String userName = jTextUserName.getText();
    //String password = new String(jTextPassword.getPassword());
    String host = jTextHost.getText();
    String keyspace = jTextKeyspace.getText();

    // validation first
    if (connectionName == null || connectionName.isEmpty()) {
      throw new CpoException("Connection name must be provided");
    }

    CtCassandraConfig myConfig = getDataSourceConfig();

    // dupe?
    CtDataSourceConfig dupe = CpoUtil.getInstance().getDataSourceConfig(connectionName);
    if (dupe != null && !dupe.equals(myConfig)) {
      throw new CpoException("Another connection is using the name: " + connectionName + "\n\nConnection names must be unique");
    }

    //if (userName == null || userName.isEmpty()) {
      //throw new CpoException("A user name must be provided");
    //}

    //if (password.isEmpty()) {
      //throw new CpoException("A password must be provided");
    //}

    if (host == null || host.isEmpty()) {
      throw new CpoException("A host must be provided");
    }

    if (keyspace == null || keyspace.isEmpty()) {
      throw new CpoException("A keyspace must be provided");
    }

    // create a new data source config, so we don't muck w/ the live one
    CtCassandraConfig cassandraConfig = newDataSourceConfig();
    cassandraConfig.setName(connectionName);

    if (myConfig != null && myConfig.getMetaDescriptorName() != null) {
      cassandraConfig.setMetaDescriptorName(myConfig.getMetaDescriptorName());
    } else {
      cassandraConfig.setMetaDescriptorName(DEFAULT_META_DESCRIPTOR);
    }

    cassandraConfig.setCpoConfigProcessor(this.getConfigProcessor());

    CtCassandraReadWriteConfig rwc = cassandraConfig.addNewReadWriteConfig();
    // FIXME - need to add username/password support
    //rwc.setUser(userName);
    //rwc.setPassword(password);
    rwc.addContactPoint(host);
    rwc.setKeySpace(keyspace);

    return cassandraConfig;
  }
}
