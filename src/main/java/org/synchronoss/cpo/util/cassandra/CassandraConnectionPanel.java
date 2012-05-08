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

import org.synchronoss.cpo.CpoException;
import org.synchronoss.cpo.core.cpoCoreConfig.CtDataSourceConfig;
import org.synchronoss.cpo.util.AbstractConnectionPanel;

/**
 * JPanel for creating and editing Cassandra connections
 *
 * @author Michael Bellomo
 * @since 5/5/12
 */
public class CassandraConnectionPanel extends AbstractConnectionPanel {

  public CassandraConnectionPanel() {
    super();
  }

  @Override
  protected String getConfigProcessor() {
    // TODO - Implement org.synchronoss.cpo.util.cassandra.CassandraConnectionPanel.getConfigProcessor
    throw new UnsupportedOperationException("org.synchronoss.cpo.util.cassandra.CassandraConnectionPanel.getConfigProcessor has not been implemented.");
  }

  @Override
  public String getTitle() {
    // TODO - Implement org.synchronoss.cpo.util.cassandra.CassandraConnectionPanel.getTitle
    throw new UnsupportedOperationException("org.synchronoss.cpo.util.cassandra.CassandraConnectionPanel.getTitle has not been implemented.");
  }

  @Override
  public CtDataSourceConfig newDataSourceConfig() {
    // TODO - Implement org.synchronoss.cpo.util.cassandra.CassandraConnectionPanel.newDataSourceConfig
    throw new UnsupportedOperationException("org.synchronoss.cpo.util.cassandra.CassandraConnectionPanel.newDataSourceConfig has not been implemented.");
  }

  @Override
  public CtDataSourceConfig createDataSourceConfig() throws CpoException {
    // TODO - Implement org.synchronoss.cpo.util.cassandra.CassandraConnectionPanel.createDataSourceConfig
    throw new UnsupportedOperationException("org.synchronoss.cpo.util.cassandra.CassandraConnectionPanel.createDataSourceConfig has not been implemented.");
  }
}
