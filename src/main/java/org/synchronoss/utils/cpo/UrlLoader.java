package org.synchronoss.utils.cpo;

import java.io.*;
import java.net.*;

/**
 * User: michael
 * Date: Nov 7, 2009
 * Time: 9:14:41 PM
 */
public class UrlLoader implements Runnable {

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
      conn.setConnectTimeout(3000);
      conn.connect();

      connInputStream = conn.getInputStream();
    } catch (Exception ex) {
      // ignore
    }
  }
}
