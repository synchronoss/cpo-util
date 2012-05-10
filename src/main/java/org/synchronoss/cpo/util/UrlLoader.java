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

import java.io.InputStream;
import java.net.*;

/**
 * @author  Michael Bellomo
 * @since 11/7/2009
 */
public class UrlLoader implements Runnable {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public static final int TIMEOUT = 3000;

  private URL url;
  private InputStream connInputStream = null;

  public UrlLoader(String url) throws MalformedURLException {
    this(new URL(url));
  }

  public UrlLoader(URL url) {
    this.url = url;
  }

  public InputStream getInputStream() {
    return connInputStream;
  }

  public void run() {
    try {
      URLConnection conn = url.openConnection();
      conn.setConnectTimeout(TIMEOUT);
      conn.connect();

      connInputStream = conn.getInputStream();
    } catch (Exception ex) {
      logger.error(ex.getMessage());
    }
  }
}
