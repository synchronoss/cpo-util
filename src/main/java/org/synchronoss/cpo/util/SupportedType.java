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
import org.synchronoss.cpo.cassandra.cpoCassandraConfig.CtCassandraConfig;
import org.synchronoss.cpo.cassandra.meta.CassandraCpoMetaDescriptor;
import org.synchronoss.cpo.core.cpoCoreConfig.CtDataSourceConfig;
import org.synchronoss.cpo.jdbc.cpoJdbcConfig.CtJdbcConfig;
import org.synchronoss.cpo.jdbc.meta.JdbcCpoMetaDescriptor;
import org.synchronoss.cpo.meta.CpoMetaDescriptor;
import org.synchronoss.cpo.util.cassandra.*;
import org.synchronoss.cpo.util.jdbc.*;

/**
 * Enumeration of supported meta types
 */
public enum SupportedType {

  Jdbc(JdbcCpoMetaDescriptor.class, JdbcProxy.class, CtJdbcConfig.class, JdbcConnectionPanel.class),
  Cassandra(CassandraCpoMetaDescriptor.class, CassandraProxy.class, CtCassandraConfig.class, CassandraConnectionPanel.class),
  ;

  protected static Logger logger = LoggerFactory.getLogger(SupportedType.class);

  private Class<? extends CpoMetaDescriptor> metaDescriptorClass = null;
  private Class<? extends Proxy> proxyClass = null;
  private Class<? extends CtDataSourceConfig> dataSourceConfigClass = null;
  private Class<? extends AbstractConnectionPanel> connectionPanelClass = null;

  SupportedType(Class<? extends CpoMetaDescriptor> metaDescriptorClass, Class<? extends Proxy> proxyClass, Class<? extends CtDataSourceConfig> dataSourceConfigClass, Class<? extends AbstractConnectionPanel> connectionPanelClass) {
    this.metaDescriptorClass = metaDescriptorClass;
    this.proxyClass = proxyClass;
    this.dataSourceConfigClass = dataSourceConfigClass;
    this.connectionPanelClass = connectionPanelClass;
  }

  public Class<? extends CpoMetaDescriptor> getMetaDescriptorClass() {
    return metaDescriptorClass;
  }

  public Class<? extends CtDataSourceConfig> getDataSourceConfigClass() {
    return dataSourceConfigClass;
  }

  public Class<? extends AbstractConnectionPanel> getConnectionPanelClass() {
    return connectionPanelClass;
  }

  public Proxy newProxyInstance() throws Exception {
    return proxyClass.newInstance();
  }

  public static SupportedType getTypeForMetaDescriptor(CpoMetaDescriptor metaDescriptor) throws CpoException {
    for (SupportedType type : values()) {
      if (type.metaDescriptorClass.isAssignableFrom(metaDescriptor.getClass())) {
        return type;
      }
    }
    throw new CpoException("Unknown meta descriptor");
  }

  public static SupportedType getTypeForConnection(CtDataSourceConfig dataSourceConfig) throws CpoException {
    for (SupportedType type : values()) {
      if (type.dataSourceConfigClass.isAssignableFrom(dataSourceConfig.getClass())) {
        return type;
      }
    }
    throw new CpoException("Unknown data source config");
  }
}