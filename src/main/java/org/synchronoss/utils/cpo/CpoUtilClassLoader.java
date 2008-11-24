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
package org.synchronoss.utils.cpo;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class CpoUtilClassLoader extends ClassLoader {

  private static List<File> files;
  private static CpoUtilClassLoader loader;
  private Logger OUT = Logger.getLogger(this.getClass());

  private CpoUtilClassLoader (ClassLoader parent) {
    super(parent);
  }
  
  public static CpoUtilClassLoader getInstance(List<File> files, ClassLoader parent) {
    if (files == null)
      throw new IllegalArgumentException ("Null classpath passed!");
    if (loader == null)
      loader = new CpoUtilClassLoader(parent); 
    CpoUtilClassLoader.files = files;
    return loader;
  }

  public static void unloadLoader() {
    loader = null;
  }

  protected Class findClass (String name) 
  throws ClassNotFoundException {
    Class c;
      // Convert class name argument to filename
      // Convert package names into subdirectories
    try {
      byte data[] = loadClassData(name);
      c = defineClass (name, data, 0, data.length);
      if (c == null)
        throw new ClassNotFoundException (name);
      OUT.debug("loaded class: "+name);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ClassNotFoundException ("Error reading class: " + name);
    }
    return c;
  }
  
  private byte[] loadClassData (String name)  
  throws IOException {
    for (File file : files) {
      if (file.isFile()) {
        if (file.getName().toLowerCase().endsWith(".jar")) {
          String filename = name.replace ('.', '/') + ".class";
          JarFile jf = new JarFile (file);            
          for (Enumeration e = jf.entries(); e.hasMoreElements() ;) {
            ZipEntry entry = (ZipEntry) e.nextElement ();
//            OUT.debug (entry.getName());
            if (filename.equals (entry.getName ())) {
              InputStream is = jf.getInputStream (entry);
              int l = (int) entry.getSize ();
              byte [] buff = new byte [l];
              int read = 0;
              while (read < l) {
                int incr = is.read (buff, read, l - read);
                read += incr;
              }
              return buff;
            }
          }
        }        
      } else if (file.isDirectory()) {
        String filename = name.replace ('.', File.separatorChar) + ".class";
        File f = new File (file, filename);

        // Get size of class file
        int size = (int)f.length();

        // Reserve space to read
        byte buff[] = new byte[size];

        // Get stream to read from
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream (fis);

        // Read in data
        dis.readFully (buff);

        // close stream
        dis.close();

        // return data
        return buff;
      }
    }
    throw new IOException("Could not find class");
  }
}
