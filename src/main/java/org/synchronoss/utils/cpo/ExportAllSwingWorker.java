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
import java.util.Enumeration;

/**
 * SwingWorker used to launch a progress box up while the sql is being exported.
 */
public class ExportAllSwingWorker extends SwingWorker {

    private static final Logger logger = Logger.getLogger(ExportAllSwingWorker.class);

    ProgressFrame pf = null;
    AbstractCpoNode menuNode = null;
    File file = null;
    boolean deleteAll = false;

    Exception error = null;

    public ExportAllSwingWorker(AbstractCpoNode menuNode, File file, boolean deleteAll) {
        setLocalName("ExportWorker");
        this.menuNode = menuNode;
        this.file = file;
        this.deleteAll = deleteAll;
    }

    @Override
    public Object construct() {
        logger.debug("Exporting...");
        pf = new ProgressFrame("Exporting...", -1);
        pf.start();

        pf.progressMade(new ProgressMaxEvent(this, menuNode.getChildCount() - 1));

        FileWriter fw = null;
        try {

            StringBuffer sbSql = new StringBuffer();
            Enumeration menuEnum = menuNode.children();
            while (menuEnum.hasMoreElements()) {
                SQLExporter sqlEx = new SQLExporter(menuNode.getProxy().getTablePrefix(), menuNode.getProxy().getSqlDelimiter());
                sbSql.append(sqlEx.exportSQL((AbstractCpoNode)menuEnum.nextElement(), deleteAll));
                // after the first deleteAll - don't need it again
                deleteAll = false;
                pf.progressMade(new ProgressValueEvent(this, 1));
            }

            fw = new FileWriter(file);
            fw.write(sbSql.toString());
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
            CpoUtil.updateStatus("Exported SQL for server: " + menuNode.toString());
        }
    }
}