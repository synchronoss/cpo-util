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

            // use the last close paren, this will make weird inner select stuff work, but it won't be able to guess
            // the inner select values
            int valParenEnd = query.lastIndexOf(")");
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
                if (val.contains("?")) {
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

    public static void main(String[] args) throws Exception {
        String query = "insert into lnp_order_prtblks(TRANSACTION_ID,REVISION,FROM_TN,TO_TN,LT,LT_FIRST_NAME,LT_MI,LT_LAST_NAME,LT_ALT_NAME,LT_SUFFIX,LT_ADD_STREETNUM,LT_ADD_STREETPFX,LT_ADD_STREETNAME,LT_ADD_STREETTYPE,LT_ADD_STREETSUFF,LT_ADD_CITY,LT_ADD_STATE,LT_ADD_ZIP,LT_ADD_COUNTRY,LT_ADD_EVNG_TN,LT_ADD_DYTM_TN,LT_ADD_EMAIL1,LT_ADD_EMAIL2,LT_ADD_STNUM_PFX,LT_ADD_STNUM_SFX,LT_ADD_UNITINF,LT_ADD_SECLOCDES,LT_ADD_ROOM,LT_ADD_FLOOR,LT_ADD_BLDG,LT_LISTED,LT_LISTADD,DLNM,LSO,LT_ADD_STRUCT_TYPE,BYPASS_PIC,BYPASS_LPIC,YPHC,YP_VERBIAGE,CARE_BLOCKING_BM,CARE_TYPE,PICCIC,PICJURIS,OLD_PICCIC,OLD_PICJURIS,LPICCIC,LPICJURIS,OLD_LPICCIC,OLD_LPICJURIS,PIC_RES_IND,LT_OLD_FIRST_NAME,LT_OLD_MI,LT_OLD_LAST_NAME,LT_OLD_ALT_NAME,LT_OLD_SUFFIX,LT_OLD_ADD_STREETNUM,LT_OLD_ADD_STREETPFX,LT_OLD_ADD_STREETNAME,LT_OLD_ADD_STREETTYPE,LT_OLD_ADD_STREETSUFF,LT_OLD_ADD_CITY,LT_OLD_ADD_STATE,LT_OLD_ADD_ZIP,LT_OLD_ADD_COUNTRY,LT_OLD_ADD_EVNG_TN,LT_OLD_ADD_DYTM_TN,\n" + "LT_OLD_ADD_EMAIL1,LT_OLD_ADD_EMAIL2,LT_OLD_ADD_STNUM_PFX,LT_OLD_ADD_STNUM_SFX,LT_OLD_ADD_UNITINF,LT_OLD_ADD_SECLOCDES,LT_OLD_ADD_ROOM,LT_OLD_ADD_FLOOR,LT_OLD_ADD_BLDG,LT_OLD_ADD_STRUCT_TYPE,RTY,OLD_RTY,STYC,OLD_STYC,HDRTN,OLD_HDRTN,DOI,OLD_DOI,LVL,OLD_LVL,PLS,OLD_PLS,PLINFO,OLD_PLINFO,LTXTY,OLD_LTXTY,LPHRASE,OLD_LPHRASE,TL,OLD_TL,TITLE,OLD_TITLE,TITLE2,OLD_TITLE2,TLD,OLD_TLD,TITLE1D,OLD_TITLE1D,TITLE2D,OLD_TITLE2D,BRO,OLD_BRO,PLA,OLD_PLA,DIRTYP,OLD_DIRTYP,DIRQTYA,OLD_DIRQTYA,DNA,OLD_DNA,OLD_YPHC,OLD_YP_VERBIAGE,DIRIDL,OLD_DIRIDL,HS,OLD_HS,PLTN,OLD_PLTN,LTXTY2,OLD_LTXTY2,LPHRASE2,OLD_LPHRASE2,DIRQTYA2,DIRTYP2,OLD_DIRQTYA2,OLD_DIRTYP2,DIRQTYA3,DIRTYP3,OLD_DIRQTYA3,OLD_DIRTYP3,DIRQTYNC1,OLD_DIRQTYNC1,DIRQTYNC2,OLD_DIRQTYNC2,DIRQTYNC3,OLD_DIRQTYNC3,LVL2,OLD_LVL2,PLS2,OLD_PLS2,PLINFO2,OLD_PLINFO2,PLTN2,OLD_PLTN2,LVL3,OLD_LVL3,PLS3,OLD_PLS3,PLINFO3,OLD_PLINFO3,PLTN3,OLD_PLTN3,LVL4,OLD_LVL4,PLS4,OLD_PLS4,PLINFO4,OLD_PLINFO4,PLTN4,OLD_PLTN4,LVL5,OLD_LVL5,PLS5,OLD_PLS5,PLINFO5,OLD_PLINFO5,PLTN5,OLD_PLTN5,LVL6,OLD_LVL6,PLS6,OLD_PLS6,PLINFO6,OLD_PLINFO6,PLTN6,OLD_PLTN6, LT_OLD_LISTED,LT_OLD_LISTADD,OLD_DLNM,LT_ADD_UNIT_TYPE,LT_ADD_STRUCT_INFO,LT_OLD_ADD_UNIT_TYPE, LT_OLD_ADD_STRUCT_INFO,ALI,OLD_ALI,TC_OPT,TC_TO_PRI,TC_NAME,TCID,TC_PER,TC_MESS1,TCMI, DNO, ACA, SHTN, LEX, LNPL, LTNE, OMTN, NSTN, SIC, ADV_CONT_TN, ADV_CONT, EA, EOS, WPP, MTN, PPTN, DML, NOSL, TMKT, ADV, OCD, STR, PROF, DIRNAME, DIRSUB, LID1, LID2, OMSD, TOA) values (?,(select max(revision) from lnp_order_header where transaction_id = ?),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        QueryParser parser = new QueryParser();
        parser.parse(query);
    }
}
