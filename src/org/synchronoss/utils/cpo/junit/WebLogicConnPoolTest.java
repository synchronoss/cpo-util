/*
 * Created on Feb 23, 2005
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
package org.synchronoss.utils.cpo.junit;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.synchronoss.utils.cpo.CpoUtil;
import org.synchronoss.utils.cpo.CpoUtilClassLoader;
import org.synchronoss.utils.cpo.Statics;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author jcolson
 */
public class WebLogicConnPoolTest extends TestCase {
  public static Category OUT = Category.getInstance(WebLogicConnPoolTest.class);
  private Connection conn;
  private Context ctx;
  private final static String _URL = "t3://localhost:7001";
  private final static String _USER = "system";
  private final static String _PASS = "security";
  private final static String _CONNPOOL = "CpoDataSource";
  private final static String _INIT_CTX_FACT = "weblogic.jndi.WLInitialContextFactory";
  private final static String _BAD_SQL = "select to_number(sysdate) from dual";
  public WebLogicConnPoolTest(String test) {
    super(test);
  }
  public static Test suite() {
    OUT.info("Starting WebLogicConnPoolTest TestSuite");
    TestSuite ts = new TestSuite();
    ts.addTest(new WebLogicConnPoolTest("testConn"));
    return ts;
  }
  public void testConn() {
    try {
      getConnection();
      for (int i = 0; i < 2000; i++) {
        PreparedStatement pstmt = null;
        try {
          OUT.debug("About to run bad sql");
          pstmt = conn.prepareStatement(_BAD_SQL);
          pstmt.executeQuery();
	  try {
	    Thread.sleep(100);
	  } catch (InterruptedException ie) {
	  }
        } catch (Exception e) {
          OUT.debug("Got expected exception: "+e.getMessage());
        } finally {
          OUT.debug("gonna close statement");
          try {
            pstmt.close();
          } catch (Exception e) {
            OUT.debug("Caught exception closing statement");
          }
        }
      }
    } catch (Exception e) {
      OUT.error("Caught exception",e);
    }
  }
  
  void getConnection() throws Exception {
    getInitialContext();
    OUT.debug("Getting Datasource");
    DataSource ds = (DataSource)ctx.lookup(_CONNPOOL);
    OUT.debug("Getting Connection");
    conn = ds.getConnection();
    conn.setAutoCommit(false);
  }
  private void getInitialContext() throws Exception {
    Properties h = new Properties();
    h.put(Context.INITIAL_CONTEXT_FACTORY, _INIT_CTX_FACT);
    h.put(Context.PROVIDER_URL, _URL);
    h.put(Context.SECURITY_PRINCIPAL, _USER);
    h.put(Context.SECURITY_CREDENTIALS, _PASS);
    ctx = new InitialContext(h);
    OUT.debug("About to return initial context");
  }
}
