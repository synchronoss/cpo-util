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

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;

public class CpoUtilClassLoader extends ClassLoader {

  private static CpoUtilClassLoader loader;
  private static final Object LOCK = new Object();
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private CpoUtilClassLoader(ClassLoader parent) {
    super(parent);
  }

  public static CpoUtilClassLoader getInstance(ClassLoader parent) {
    synchronized (LOCK) {
      if (loader == null) {
        loader = new CpoUtilClassLoader(parent);
      }
    }
    return loader;
  }

  public static void unloadLoader() {
    synchronized (LOCK) {
      loader = null;
    }
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    Class<?> c;
    // Convert class name argument to filename
    // Convert package names into subdirectories
    try {
      byte[] data = loadClassData(name);
      c = defineClass(name, data, 0, data.length);
      if (c == null) {
        throw new ClassNotFoundException(name);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("loaded class: " + name);
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw new ClassNotFoundException("Error reading class: " + name, e);
    }
    return c;
  }

  private byte[] loadClassData(String name) throws IOException {
    for (File file : CpoUtil.getInstance().getCustomClasspathEntries()) {
      if (file.isFile()) {
        if (file.getName().toLowerCase().endsWith(".jar")) {
          String filename = name.replace('.', '/') + ".class";
          JarFile jf = new JarFile(file);
          Enumeration<JarEntry> e = jf.entries();
          while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            if (filename.equals(entry.getName())) {
              InputStream is = jf.getInputStream(entry);
              int l = (int)entry.getSize();
              byte[] buff = new byte[l];
              int read = 0;
              while (read < l) {
                int incr = is.read(buff, read, l - read);
                read += incr;
              }
              return buff;
            }
          }
        }
      } else if (file.isDirectory()) {
        String filename = name.replace('.', File.separatorChar) + ".class";
        File f = new File(file, filename);

        // Get size of class file
        int size = (int)f.length();

        // Reserve space to read
        byte[] buff = new byte[size];

        // Get stream to read from
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);

        // Read in data
        dis.readFully(buff);

        // close stream
        dis.close();

        // return data
        return buff;
      }
    }
    throw new IOException("Could not find class");
  }
}
