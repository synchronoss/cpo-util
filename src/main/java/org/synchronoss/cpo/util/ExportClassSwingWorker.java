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

import org.slf4j.*;

import java.io.*;

/**
 * SwingWorker used to launch a progress box up while the sql is being exported.
 */
public class ExportClassSwingWorker extends SwingWorker {

    private static final Logger logger = LoggerFactory.getLogger(ExportClassSwingWorker.class);

    ProgressFrame pf = null;
    AbstractCpoNode menuNode = null;
    File dir = null;

    Exception error = null;

    public ExportClassSwingWorker(AbstractCpoNode menuNode, File dir) {
        setLocalName("ExportWorker");
        this.menuNode = menuNode;
        this.dir = dir;
    }

    @Override
    public Object construct() {
        logger.debug("Exporting...");
        pf = new ProgressFrame("Exporting...", -1);
        pf.start();

        FileWriter fw = null;
        try {
            SQLExporter sqlEx = new SQLExporter(menuNode.getProxy().getTablePrefix(), menuNode.getProxy().getSqlDelimiter());
            SQLClassExport classExport = sqlEx.exportSQL(menuNode);

            StringBuilder sql = new StringBuilder();
            sql.append(classExport.getDeleteSql());
            sql.append(classExport.getInsertQueryTextSql());
            sql.append(classExport.getInsertSql());

            CpoClassNode classNode = (CpoClassNode)menuNode;
            String fileName = classNode.getCpoClass().getName() + ".sql";

            File file = new File(dir, fileName);
            fw = new FileWriter(file);
            fw.write(sql.toString());
            fw.flush();
            fw.close();
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
        } else {
            CpoUtil.updateStatus("Exported SQL for class: " + menuNode.toString());
        }
    }
}