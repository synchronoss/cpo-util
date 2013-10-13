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
import org.synchronoss.cpo.core.cpoCoreMeta.*;
import org.synchronoss.cpo.meta.CpoMetaDescriptor;

import java.io.File;

/**
 * Factory class for obtaining Proxy objects
 *
 * @author Michael Bellomo
 * @since 5/2/12
 */
public class ProxyFactory {

  protected static Logger logger = LoggerFactory.getLogger(ProxyFactory.class);

  private static ProxyFactory proxyFactory = new ProxyFactory();

  private ProxyFactory() {
    super();
  }

  /**
   * Returns the singleton factory instance
   */
  public static ProxyFactory getInstance() {
    return proxyFactory;
  }

  /**
   * Returns a proxy object for the specified file.
   */
  public Proxy getProxy(File cpoMetaXml) throws CpoException {
    try {
      // create meta descriptor
      CpoMetaDescriptor metaDescriptor = createMetaDescriptor(cpoMetaXml);

      // find the supported type that maps to the meta descriptor
      SupportedType type = SupportedType.getTypeForMetaDescriptor(metaDescriptor);

      // create a proxy
      Proxy proxy = type.newProxyInstance();
      proxy.setCpoMetaXml(cpoMetaXml);
      proxy.setMetaDescriptor(metaDescriptor);
      return proxy;
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new CpoException(ex);
    }
  }

  public Proxy newProxy(File cpoMetaXml, SupportedType type) throws CpoException {
    try {
      // create a new document
      CpoMetaDataDocument cpoMetaDataDocument = CpoMetaDataDocument.Factory.newInstance();
      CtCpoMetaData ctCpoMetaData = cpoMetaDataDocument.addNewCpoMetaData();
      ctCpoMetaData.setMetaDescriptor(type.getMetaDescriptorClass().getName());

      // save the document so we can use it
      cpoMetaDataDocument.save(cpoMetaXml);

      return getProxy(cpoMetaXml);
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new CpoException(ex);
    }
  }

  public void refreshMetaDescriptor(Proxy proxy, File file) throws CpoException {
    if (file == null) {
      // use the file in the proxy if non was specified
      file = proxy.getCpoMetaXml();
    }
    CpoMetaDescriptor metaDescriptor = createMetaDescriptor(file);
    proxy.setCpoMetaXml(file);
    proxy.setMetaDescriptor(metaDescriptor);
  }

  /**
   * Creates the meta adapter
   *
   * @param cpoMetaXml The meta xml file to create the adapter from
   * @return A CpoMetaDescriptor for the specified file.
   *
   * @throws CpoException if anything goes wrong
   */
  private CpoMetaDescriptor createMetaDescriptor(File cpoMetaXml) throws CpoException {
    String metaDescriptorName = "CpoUtil:" + System.currentTimeMillis() + ":" + cpoMetaXml.getAbsolutePath();
    return CpoMetaDescriptor.getInstance(metaDescriptorName, cpoMetaXml.getAbsolutePath(), false);
  }
}
