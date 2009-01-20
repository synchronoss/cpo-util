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

import java.text.ParseException;
import java.util.*;

/**
 * User: michael.bellomo
 * Date: Nov 20, 2008
 * Time: 11:55:42 AM
 */
public class QueryParser {

    private Logger OUT = Logger.getLogger(this.getClass());

    public QueryParser() {
        // do nothing
    }

    public List<String> parse(String query) throws ParseException {

        if (OUT.isDebugEnabled())
            OUT.debug("Query: " + query);

        // query is empty, nothing we can do
        if (query.length() < 1)
            return null;

        // no question marks, nothing to do
        if (query.indexOf("?") == -1)
            return null;

        // upper case the query, to make things easier
        query = query.toUpperCase();

        Vector<String> colList = new Vector<String>();

        if (query.startsWith("INSERT")) {
            // query is in the format of:  insert into table(col1, col2...) values(val1, val2...)
            // so we'll use the parens () to parse
            
            int colParenStart = query.indexOf("(");
            if (colParenStart == -1)
                throw new ParseException("Unable to locate starting parenthesis for the column names.", -1);

            int colParenEnd = query.indexOf(")", colParenStart);
            if (colParenEnd == -1)
                throw new ParseException("Unable to locate ending parenthesis for the column names.", -1);

            int valParenStart = query.indexOf("(", colParenEnd);
            if (valParenStart == -1)
                throw new ParseException("Unable to locate starting parenthesis for the column values.", -1);

            int valParenEnd = query.indexOf(")", valParenStart);
            if (valParenEnd == -1)
                throw new ParseException("Unable to locate ending parenthesis for the column values.", -1);

            String[] cols = query.substring(colParenStart + 1, colParenEnd).split(",");
            String[] vals = query.substring(valParenStart + 1, valParenEnd).split(",");

            // if cols or vals is null, it means we couldn't find any
            if (cols == null || vals == null)
                return null;

            if (OUT.isDebugEnabled()) {
                OUT.debug("Found cols: " + cols.length);
                OUT.debug("Found vals: " + vals.length);
            }

            if (cols.length != vals.length)
                throw new ParseException("You seem to have " + cols.length + " columns, and " + vals.length + " values.\n\nThose numbers should be equal.", -1);


            // filter out columns that we're not providing values for
            for (int i = 0; i < vals.length; i++) {
                String val = vals[i];
                if (val.trim().equals("?")) {
                    colList.add(cols[i].trim());
                }
            }
        } else {
            // query is in the format of:  ...col1 = ? , col2 = ?...
            // so we'll have to move left to right from the ? looking for the field name

            String[] chunks = query.split("\\?");
            for (String chunk : chunks) {
                if (OUT.isDebugEnabled())
                    OUT.debug("Chunk: " + chunk);

                int idx = chunk.length() - 1;
                int fieldStartIdx = -1;
                int fieldEndIdx = -1;

                boolean found = false;
                while (!found && (idx >= 0)) {
                    char c = chunk.charAt(idx);

                    if (fieldEndIdx == -1) {
                        // till we find the first char of the end of the field name, ignore spaces and equals
                        if (!(c == ' ' || c == '=')) {
                            // found a char, must be the end of the field name
                            fieldEndIdx = idx;
                        }
                    } else {
                        // if we find a space or a comma, we've reached the beginning of the field name
                        if (c == ' ' || c == ',') {
                            fieldStartIdx = idx + 1;
                            found = true;
                        }
                    }
                    idx--;
                }
                if (found) {
                    String col = chunk.substring(fieldStartIdx, fieldEndIdx + 1);
                    colList.add(col);
                }
            }
        }

        // at this point, the colList will only have columns that correspond to a ?
        if (OUT.isDebugEnabled()) {
            for (String s : colList) {
                OUT.debug("Column [" + s + "]");
            }
        }

        return colList;
    }
}
