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

import org.synchronoss.cpo.jdbc.*;

public class Statics  {
  
  public static final String PROP_WLSURL="cpoutil.wls.url.";
  public static final String PROP_WLSUSER="cpoutil.wls.user.";
  public static final String PROP_WLSPASS="cpoutil.wls.pass.";
  public static final String PROP_WLSCONNPOOL="cpoutil.wls.connpool.";
  public static final String PROP_WLSINITCTXFCTRY="cpoutil.wls.initialcontextfactory.";
  public static final String PROP_CPONAME="cpoutil.wls.cponame.";
  public static final String PROP_THEME_URL="cpoutil.app.theme_url";

  public static final String PROP_JDBC_URL="cpoutil.jdbc.url.";
  public static final String PROP_JDBC_DRIVER="cpoutil.jdbc.driver.";
  public static final String PROP_JDBC_PARAMS="cpoutil.jdbc.params.";
  public static final String PROP_JDBC_TABLE_PREFIX="cpoutil.jdbc.tablePrefix.";
  public static final String PROP_JDBC_SQL_STATEMENT_DELIMITER="cpoutil.jdbc.sqlStatementDelimiter.";
  public static final String PROP_JDBC_SQL_DIR ="cpoutil.jdbc.sqlDir.";
  public static final String PROP_JDBC_DEFDIR="cpoutil.jdbc.defaultdir.";
  public static final String PROP_JDBC_DEFPACK="cpoutil.jdbc.defaultpackage.";
  public static final String PROP_JDBC_IGNORE_PROTECTED ="cpoutil.jdbc.ignoreProtected.";

  public static final String LOAD_JDBC_URL="cpoutil.jdbc.url";
  public static final String LOAD_JDBC_DRIVER="cpoutil.jdbc.driver";
  public static final String LOAD_JDBC_PARAMS="cpoutil.jdbc.params";
  public static final String LOAD_JDBC_TABLE_PREFIX="cpoutil.jdbc.tablePrefix";
  public static final String LOAD_JDBC_SQL_STATEMENT_DELIMITER="cpoutil.jdbc.sqlStatementDelimiter";

  public static final String LPROP_CLASSPATH="cpoutil.classpath";
  public static final String LPROP_DEFDIR="cpoutil.defaultdir";
  public static final String LPROP_DEFPACK="cpoutil.defaultpackage";

  public static final String CPO_TYPE_CREATE="CREATE";
  public static final String CPO_TYPE_DELETE="DELETE";
  public static final String CPO_TYPE_LIST="LIST";
  public static final String CPO_TYPE_RETRIEVE="RETRIEVE";
  public static final String CPO_TYPE_UPDATE="UPDATE";
  public static final String CPO_TYPE_EXIST="EXIST";
  public static final String CPO_TYPE_EXECUTE="EXECUTE";

  public static final String CREATE_ALL_FILE_NAME = "CPO_CREATE_ALL.sql";

  public static String getJavaSqlType(int sqlTypeNum) {
	  JavaSqlType<?> jdbcType = JavaSqlTypes.getJavaSqlType(sqlTypeNum);
	  return jdbcType.getJavaSqlTypeName();
  }
}