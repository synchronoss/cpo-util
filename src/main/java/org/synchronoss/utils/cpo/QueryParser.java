/*
 *  Copyright (C) 2008 Michael Bellomo
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

import java.io.StringReader;
import java.text.ParseException;
import java.util.*;

/**
 * User: michael.bellomo
 * Date: Nov 20, 2008
 * Time: 11:55:42 AM
 */
public class QueryParser {

  private static Logger OUT = Logger.getLogger(QueryParser.class);

  private final static String COMPARE_CHARS = " =<>!()";
  private final static String SEPARATOR_CHARS = " .,()\n";

  public QueryParser() {
    // do nothing
  }

  public static int countBindMarkers(String query) {
    return getBindMarkerIndexes(query).size();
  }

  private static Collection<Integer> getBindMarkerIndexes(String query) {
    Collection<Integer> indexes = new Vector<Integer>();

    if (query != null) {
      StringReader reader = new StringReader(query);

      try {
        
        int idx = 0;
        int rc = -1;
        boolean inDoubleQuotes = false;
        boolean inSingleQuotes = false;

        do {
          rc = reader.read();
          if (((char)rc) == '\'') {
            inSingleQuotes = !inSingleQuotes;
          } else if (((char)rc) == '"') {
            inDoubleQuotes = !inDoubleQuotes;
          } else if (!inSingleQuotes && !inDoubleQuotes && ((char)rc) == '?') {
            indexes.add(idx);
          }
          idx++;
        } while (rc != -1);
      } catch (Exception e) {
        OUT.error("error counting bind markers");
      }
    }
    return indexes;
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

      // use the last close paren, this will make weird inner select stuff work,
      // but it won't be able to guess the inner select values
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
        if (val.trim().equals("?")) {
          // just a ?, use the col
          colList.add(cols[i].trim());
        } else if (val.contains("?")) {
          // more than just a ?, parse the val
          QueryParser qp = new QueryParser();
          colList.addAll(qp.parse(val));
        }
      }
    } else {
      // query is in the format of:  ...col1 = ? , col2 = ?...
      // so we'll have to move left to right from the ? looking for the field name

      int startIdx = 0;
      Collection<Integer> indexes = getBindMarkerIndexes(query);
      for (int qIdx : indexes) {
        String chunk = query.substring(startIdx, qIdx);

        if (OUT.isDebugEnabled())
          OUT.debug("Chunk [" + chunk + "]");

        int idx = chunk.length() - 1;
        int fieldStartIdx = -1;
        int fieldEndIdx = -1;

        boolean found = false;
        boolean inFunction = false;
        while (!found && (idx >= 0)) {
          char c = chunk.charAt(idx);

          if (fieldEndIdx == -1) {
            // if the character is a ( this might be a function like UPPER(), try to parse that out
            if (!inFunction && c == '(') {
              inFunction = true;
            } else if (inFunction && COMPARE_CHARS.indexOf(c) != -1) {
              inFunction = false;
            }

            // till we find the first char of the end of the field name, ignore compare chars
            if (!inFunction && COMPARE_CHARS.indexOf(c) == -1) {
              // found a char, must be the end of the field name
              fieldEndIdx = idx;
            }
          } else {
            // if we find a separator, we've reached the beginning of the field name
            if (SEPARATOR_CHARS.indexOf(c) >= 0) {
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

        // move the starting index to where the ? was
        startIdx = qIdx + 1;
      }
    }

    return colList;
  }

  public static void main(String[] args) throws Exception {
    String query = "select * from IMPL_EMAIL_CONFIG where REGION = ? and UPPER(STATE) = ? and REASON_CODE = ? and IS_DELETED = '0'";
    //String query = "insert into lnp_order_curr_disp(transaction_id, category, revision, state, timestamp,disp_transaction_id, trx_seq, user_id, tn, trans_comment) (select transaction_id, category, revision, state, timestamp,disp_transaction_id, trx_seq, user_id, tn, trans_comment from lnp_order_curr_disp_v where transaction_id = (select transaction_id from lnp_order_disp_head where disp_transaction_id = ?) and category = ? and (tn = ? or ? is null))";
    QueryParser parser = new QueryParser();
    List<String> colList = parser.parse(query);
    int count = 1;
    for (String col : colList) {
      System.out.println("Column[" + count + "] = " + col);
      count++;
    }
  }
}
