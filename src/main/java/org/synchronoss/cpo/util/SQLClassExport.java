/*
 *  Copyright (C) 2008
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

/**
 * User: michael.bellomo
 * Date: Nov 24, 2008
 * Time: 11:26:45 AM
 */
public class SQLClassExport {

  String deleteSql;
  String insertQueryTextSql;
  String insertSql;

  public String getDeleteSql() {
    return deleteSql;
  }

  public void setDeleteSql(String deleteSql) {
    this.deleteSql = deleteSql;
  }

  public String getInsertQueryTextSql() {
    return insertQueryTextSql;
  }

  public void setInsertQueryTextSql(String insertQueryTextSql) {
    this.insertQueryTextSql = insertQueryTextSql;
  }

  public String getInsertSql() {
    return insertSql;
  }

  public void setInsertSql(String insertSql) {
    this.insertSql = insertSql;
  }
}
