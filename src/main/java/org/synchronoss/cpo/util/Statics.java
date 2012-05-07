/**
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

import org.synchronoss.cpo.core.cpoCoreMeta.StFunctionGroupType;

import java.io.File;

public class Statics {

  // CpoUtil config files
  public static final String CPOUTIL_CONFIG_DIRNAME = ".cpoutil";
  public static final File CPOUTIL_CONFIG_DIR = new File(System.getProperties().getProperty("user.home"), CPOUTIL_CONFIG_DIRNAME);
  public static final String CPOUTIL_CONFIG_FILE = "CpoUtilConfig.xml";
  public static final String CPOUTIL_PROPERTIES_FILE = "cpoutil.properties";

  public static final String BOOTSTRAP_URL_PROP = "cpoutil.bootstrapUrl";
  public static final String PROTECTED_CLASS_PROP = "cpoutil.protectedClasses";

  public static final String CPOUTIL_TITLE = "cpoutil.title";
  public static final String CPOUTIL_VERSION = "cpoutil.version";
  public static final String CPOUTIL_MINIMUM_VERSION = "cpoutil.minimumVersion";
  public static final String CPOUTIL_AUTHOR = "cpoutil.author";
  public static final String CPOUTIL_COPYRIGHT = "cpoutil.copyright";
  public static final String CPOUTIL_COMPANY = "cpoutil.company";

  public static final String CPO_TYPE_CREATE = StFunctionGroupType.CREATE.toString();
  public static final String CPO_TYPE_DELETE = StFunctionGroupType.DELETE.toString();
  public static final String CPO_TYPE_EXECUTE = StFunctionGroupType.EXECUTE.toString();
  public static final String CPO_TYPE_EXIST = StFunctionGroupType.EXIST.toString();
  public static final String CPO_TYPE_LIST = StFunctionGroupType.LIST.toString();
  public static final String CPO_TYPE_RETRIEVE = StFunctionGroupType.RETRIEVE.toString();
  public static final String CPO_TYPE_UPDATE = StFunctionGroupType.UPDATE.toString();
}