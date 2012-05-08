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

import org.synchronoss.cpo.CpoException;
import org.synchronoss.cpo.jdbc.*;
import org.synchronoss.cpo.meta.CpoMetaDescriptor;
import org.synchronoss.cpo.meta.domain.*;
import org.synchronoss.cpo.util.*;

import java.io.File;

/**
 * A JDBC based proxy object.
 *
 * @author Michael Bellomo
 * @since 5/2/12
 */
public class JdbcProxy extends Proxy {

  public JdbcProxy() {
    super();
  }

  public JdbcProxy(File cpoMetaXml, CpoMetaDescriptor metaDescriptor) throws CpoException {
    super();
    this.setCpoMetaXml(cpoMetaXml);
    this.setMetaDescriptor(metaDescriptor);
  }

  // node creation functions
  @Override
  protected JdbcClassNode createClassNode(CpoClass cpoClass) {
    return new JdbcClassNode(cpoClass);
  }

  @Override
  protected CpoAttributeNode createAttributeNode(CpoAttribute cpoAttribute) {
    // shouldn't happen, but if what we got wasn't a JdbcAttribute...
    if (!(cpoAttribute instanceof JdbcCpoAttribute)) {
      return super.createAttributeNode(cpoAttribute);
    }

    return new JdbcAttributeNode((JdbcCpoAttribute)cpoAttribute);
  }

  @Override
  protected CpoFunctionNode createFunctionNode(CpoFunction cpoFunction) {
    return new JdbcFunctionNode(cpoFunction);
  }

  @Override
  protected CpoArgumentNode createArgumentNode(CpoArgument cpoArgument) {
    // shouldn't happen, but if what we got wasn't a JdbcArgument...
    if (!(cpoArgument instanceof JdbcCpoArgument)) {
      return super.createArgumentNode(cpoArgument);
    }

    return new JdbcArgumentNode((JdbcCpoArgument)cpoArgument);
  }
  // node creation functions
}
