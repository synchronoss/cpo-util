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
package org.synchronoss.utils.cpo;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;

/**
 * SwingWorker used to launch a progress box up while the sql is being exported.
 */
public class ExportChangedSwingWorker extends SwingWorker {

    private static final Logger logger = Logger.getLogger(ExportChangedSwingWorker.class);

    ProgressFrame pf = null;
    List<AbstractCpoNode> nodeList = null;

    Exception error = null;

    public ExportChangedSwingWorker(List<AbstractCpoNode> nodeList) {
        setLocalName("ExportWorker");
        this.nodeList = nodeList;
    }

    @Override
    public Object construct() {
        logger.debug("Exporting...");

        if (nodeList == null || nodeList.isEmpty())
            return null;
      
        pf = new ProgressFrame("Exporting...", -1);
        pf.start();

        AbstractCpoNode first = nodeList.get(0);
        String dir = first.getProxy().getSqlDir();

        FileWriter fw = null;
        try {
            // First let's make sure that the sql dir exists
            File sqlDir = new File(dir);
            if (!sqlDir.exists()) {
                if (!sqlDir.mkdirs()) {
                    throw new IOException("Unable to create directory: " + sqlDir.getPath());
                }
            }

            if (!sqlDir.isDirectory()) {
                throw new IOException("The sql dir is not a directory: " + sqlDir.getPath());
            }

            if (!sqlDir.canWrite()) {
                throw new IOException("Unable to write to directory: " + sqlDir.getPath());
            }

            SQLExporter sqlEx = new SQLExporter(first.getProxy().getTablePrefix(), first.getProxy().getSqlDelimiter());

            for (AbstractCpoNode acn : nodeList) {
              AbstractCpoNode current = acn;
              while (current != null) {
                if (current instanceof CpoClassNode) {
                  CpoClassNode node = (CpoClassNode)current;

                  SQLClassExport classExport = sqlEx.exportSQL(node);

                  StringBuilder sql = new StringBuilder();
                  sql.append(classExport.getDeleteSql());
                  sql.append(classExport.getInsertQueryTextSql());
                  sql.append(classExport.getInsertSql());

                  String fileName = node.getCpoClass().getName() + ".sql";

                  File file = new File(dir, fileName);
                  fw = new FileWriter(file);
                  fw.write(sql.toString());
                  fw.flush();
                  fw.close();
                }
                current = (AbstractCpoNode)current.getParent();
              }
            }

        } catch (Exception ex) {
            error = ex;
            logger.error("Exception caught", ex);
        } finally {
            try {
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
              // ignore
            }
            pf.stop();
        }
        return null;
    }

    @Override
    public void finished() {
        if (error != null) {
            CpoUtil.showException(error);
        }
    }
}