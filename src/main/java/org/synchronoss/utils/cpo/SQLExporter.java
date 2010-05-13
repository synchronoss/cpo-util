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
import gnu.regexp.*;
import org.apache.log4j.Logger;

import javax.swing.tree.TreeNode;
import java.util.*;

public class SQLExporter  {

  private static Logger OUT = Logger.getLogger(SQLExporter.class);

  private String tablePrefix = "";
  private final static String DELIMITER_SLASH = "\n/\n";
  private final static String DELIMITER_SEMI = ";\n";
  private String sqlDelimiter = DELIMITER_SLASH;
	
  @SuppressWarnings("unused")
  private SQLExporter() {
    // hide the default constructor
  }
	
  public SQLExporter(String tablePrefix, String sqlDelimiter) {
    this.tablePrefix = tablePrefix == null ? "" : tablePrefix;

    if (OUT.isDebugEnabled())
      OUT.debug("Creating new SQLExporter with tablePrefix=<" + tablePrefix + ">");

    if (sqlDelimiter != null && sqlDelimiter.trim().length() >0) {
      if (sqlDelimiter.equals("/")) {
        this.sqlDelimiter = DELIMITER_SLASH;
      } else if (sqlDelimiter.equals(";")) {
        this.sqlDelimiter = DELIMITER_SEMI;
      } else {
        this.sqlDelimiter = sqlDelimiter;
      }
    }
 }

  public SQLClassExport exportSQL(TreeNode parent) {
    return exportSQL(parent, false);
  }

  public SQLClassExport exportSQL(TreeNode parent, boolean skipDeletes) {
    HashSet<String> createdQueryTexts = new HashSet<String>();
    StringBuffer sqlDeleteClassBuffer = new StringBuffer();
    StringBuffer sqlInsertBuffer = new StringBuffer();
    StringBuffer sqlInsertQueryText = new StringBuffer();

    /**
     * export Class rows
     */
    if (parent instanceof CpoClassNode) {
      CpoClassNode classNode = (CpoClassNode)parent;
      /**
       * remove cpo_class rows first
       */
//      String tempTableName = "sncr_temp_cpo";
      if (!skipDeletes) {
        sqlDeleteClassBuffer.append("delete from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_QUERY_PARAMETER  where query_id in (select distinct query_id from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_QUERY where group_id in (select distinct group_id from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_QUERY_GROUP where class_id=(select class_id from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_CLASS where name='");
        sqlDeleteClassBuffer.append(classNode.getClassName());
        sqlDeleteClassBuffer.append("')))");
        sqlDeleteClassBuffer.append(sqlDelimiter);

        sqlDeleteClassBuffer.append("delete from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_QUERY where group_id in (select distinct group_id from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_QUERY_GROUP where class_id=(select class_id from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_CLASS where name='");
        sqlDeleteClassBuffer.append(classNode.getClassName());
        sqlDeleteClassBuffer.append("'))");
        sqlDeleteClassBuffer.append(sqlDelimiter);
      }

      /**
       * remove cpo_query_group rows first
       */
      sqlInsertBuffer.append("insert into ");
      sqlInsertBuffer.append(tablePrefix);
      sqlInsertBuffer.append("cpo_class (class_id, name) values ('");
      sqlInsertBuffer.append(classNode.getClassId());
      sqlInsertBuffer.append("','");
      sqlInsertBuffer.append(classNode.getClassName());
      sqlInsertBuffer.append("')");
      sqlInsertBuffer.append(sqlDelimiter);
      Enumeration<AbstractCpoNode> enumLabels = classNode.children();

      /**
       * must do CpoAttributeMaps FIRST
       */
      while (enumLabels.hasMoreElements()) {
        Object classLabel = enumLabels.nextElement();
        /**
         * find Labels
         */
        if (classLabel instanceof CpoAttributeLabelNode) {
          Enumeration<CpoAttributeMapNode> enumAtts = ((CpoAttributeLabelNode)classLabel).children();
          /**
           * remove attribute map before insert
           */
          while (enumAtts.hasMoreElements()) {
            CpoAttributeMapNode attMapNode = enumAtts.nextElement();
            /**
             * export cpo_attribute_map rows
             */
            sqlInsertBuffer.append("insert into ");
            sqlInsertBuffer.append(tablePrefix);
            sqlInsertBuffer.append("cpo_attribute_map (attribute_id, class_id, column_name, attribute, ");
            sqlInsertBuffer.append("column_type, db_column, db_table,transform_class) values ('");
            sqlInsertBuffer.append(attMapNode.getAttributeId());
            sqlInsertBuffer.append("','");
            sqlInsertBuffer.append(attMapNode.getClassId());
            sqlInsertBuffer.append("','");
            sqlInsertBuffer.append(attMapNode.getColumnName());
            sqlInsertBuffer.append("','");
            sqlInsertBuffer.append(attMapNode.getAttribute());
            sqlInsertBuffer.append("',");
            sqlInsertBuffer.append("'");
            sqlInsertBuffer.append(attMapNode.getColumnType());
            sqlInsertBuffer.append("'");
            sqlInsertBuffer.append(",");
            sqlInsertBuffer.append(attMapNode.getDbColumn() == null ? null : "'" + attMapNode.getDbColumn() + "'");
            sqlInsertBuffer.append(",");
            sqlInsertBuffer.append(attMapNode.getDbTable() == null ? null : "'" + attMapNode.getDbTable() + "'");
            sqlInsertBuffer.append(",");
            sqlInsertBuffer.append(attMapNode.getTransformClass() == null ? null : "'" + attMapNode.getTransformClass() + "'");
            sqlInsertBuffer.append(")");
            sqlInsertBuffer.append(sqlDelimiter);
          }
        }
      }
      enumLabels = classNode.children();

      /**
       * must do query groups second
       */
      while (enumLabels.hasMoreElements()) {
        Object classLabel = enumLabels.nextElement();

        /**
         * find Labels
         */
        if (classLabel instanceof CpoQueryGroupLabelNode) {
          Enumeration<CpoQueryGroupNode> queryGroupEnum = ((CpoQueryGroupLabelNode)classLabel).children();
          while (queryGroupEnum.hasMoreElements()) {
            CpoQueryGroupNode queryGroupNode = queryGroupEnum.nextElement();
            /**
             * export Query Group rows
             */
            /**
             * remove cpo_query_group before inserting
             */
            /**
             * remove cpo_query before inserting
             */
            sqlInsertBuffer.append("insert into ");
            sqlInsertBuffer.append(tablePrefix);
            sqlInsertBuffer.append("cpo_query_group (group_id, class_id, group_type, name) values ('");
            sqlInsertBuffer.append(queryGroupNode.getGroupId());
            sqlInsertBuffer.append("','");
            sqlInsertBuffer.append(queryGroupNode.getClassId());
            sqlInsertBuffer.append("','");
            sqlInsertBuffer.append(queryGroupNode.getType());
            sqlInsertBuffer.append("',");
            sqlInsertBuffer.append(queryGroupNode.getGroupName() == null ? null : "'" + queryGroupNode.getGroupName() + "'");
            sqlInsertBuffer.append(")");
            sqlInsertBuffer.append(sqlDelimiter);
              
            Enumeration<CpoQueryNode> enumQuery = queryGroupNode.children();
            while (enumQuery.hasMoreElements()) {
              CpoQueryNode queryNode = enumQuery.nextElement();
              /**
               * export Query rows
               */
              /**
               * remove cpo_query by query_id before insert
               */
              /**
               * remove any query parameters before inserting new ones
               */
              sqlInsertBuffer.append("insert into ");
              sqlInsertBuffer.append(tablePrefix);
              sqlInsertBuffer.append("cpo_query (query_id, group_id, text_id, seq_no) values ('");
              sqlInsertBuffer.append(queryNode.getQueryId());
              sqlInsertBuffer.append("','");
              sqlInsertBuffer.append(queryNode.getGroupId());
              sqlInsertBuffer.append("','");
              sqlInsertBuffer.append(queryNode.getTextId());
              sqlInsertBuffer.append("','");
              sqlInsertBuffer.append(queryNode.getSeqNo());
              sqlInsertBuffer.append("')");
              sqlInsertBuffer.append(sqlDelimiter);

              CpoQueryTextNode queryTextNode = queryNode.getQueryText();

              /**
               * remove query_text before trying insert
               */
              if (!skipDeletes) {
                sqlDeleteClassBuffer.append("delete from ");
                sqlDeleteClassBuffer.append(tablePrefix);
                sqlDeleteClassBuffer.append("cpo_query_text where text_id = '");
                sqlDeleteClassBuffer.append(queryTextNode.getTextId());
                sqlDeleteClassBuffer.append("'");
                sqlDeleteClassBuffer.append(sqlDelimiter);
              }

              /**
               * export query_text row
               */
              String queryTextSql = queryTextNode.getSQL();
              if (queryTextSql != null) {
                 queryTextSql = queryTextSql.trim();
              }
                  
              String queryTextDesc = queryTextNode.getDesc();
              try {
                RE reQuote = new RE("'");
                if (queryTextSql != null)
                  queryTextSql = reQuote.substituteAll(queryTextSql,"''");
                if (queryTextDesc != null)
                  queryTextDesc = reQuote.substituteAll(queryTextDesc,"''");
                    
              } catch (REException ree) {
                CpoUtil.showException(ree);
                return null;
              }
              if (!createdQueryTexts.contains(queryTextNode.getTextId())) {
                sqlInsertQueryText.append("insert into ");
                sqlInsertQueryText.append(tablePrefix);
                sqlInsertQueryText.append("cpo_query_text (text_id, sql_text, description) values ('");
                sqlInsertQueryText.append(queryTextNode.getTextId());
                sqlInsertQueryText.append("','");
                sqlInsertQueryText.append(queryTextSql);
                sqlInsertQueryText.append("',");
                sqlInsertQueryText.append(queryTextDesc == null ? null : "'" + queryTextDesc + "'");
                sqlInsertQueryText.append(")");
                sqlInsertQueryText.append(sqlDelimiter);

                /**
                 * This seems completely useless, commenting it out for now
                sqlInsertQueryText.append("update ");
                sqlInsertQueryText.append(tablePrefix);
                sqlInsertQueryText.append("cpo_query_text set sql_text = '");
                sqlInsertQueryText.append(queryTextSql);
                sqlInsertQueryText.append("', description = ");
                sqlInsertQueryText.append(queryTextDesc == null ? null : "'" + queryTextDesc + "'");
                sqlInsertQueryText.append(" where text_id = '");
                sqlInsertQueryText.append(queryTextNode.getTextId());
                sqlInsertQueryText.append("'");
                sqlInsertQueryText.append(sqlDelimiter);
                **/

                createdQueryTexts.add(queryTextNode.getTextId());
              }

              Enumeration<CpoQueryParameterNode> enumQueryParam = queryNode.children();
              while (enumQueryParam.hasMoreElements()) {
                CpoQueryParameterNode queryParamNode = enumQueryParam.nextElement();
                /**
                 * export query parameter rows
                 */
                sqlInsertBuffer.append("insert into ");
                sqlInsertBuffer.append(tablePrefix);
                sqlInsertBuffer.append("cpo_query_parameter (attribute_id, query_id, seq_no,param_type) values ('");
                sqlInsertBuffer.append(queryParamNode.getAttributeId());
                sqlInsertBuffer.append("','");
                sqlInsertBuffer.append(queryParamNode.getQueryId());
                sqlInsertBuffer.append("','");
                sqlInsertBuffer.append(queryParamNode.getSeqNo());
                sqlInsertBuffer.append("','");
                sqlInsertBuffer.append(queryParamNode.getType());
                sqlInsertBuffer.append("')");
                sqlInsertBuffer.append(sqlDelimiter);
              }
            }
          }
        }
      }

      if (!skipDeletes) {
        sqlDeleteClassBuffer.append("delete from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_QUERY_GROUP where class_id=(select class_id from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_CLASS where name='");
        sqlDeleteClassBuffer.append(classNode.getClassName());
        sqlDeleteClassBuffer.append("')");
        sqlDeleteClassBuffer.append(sqlDelimiter);

        sqlDeleteClassBuffer.append("delete from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_ATTRIBUTE_MAP where class_id=(select class_id from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_CLASS where name='");
        sqlDeleteClassBuffer.append(classNode.getClassName());
        sqlDeleteClassBuffer.append("')");
        sqlDeleteClassBuffer.append(sqlDelimiter);

        sqlDeleteClassBuffer.append("delete from ");
        sqlDeleteClassBuffer.append(tablePrefix);
        sqlDeleteClassBuffer.append("CPO_CLASS where name='");
        sqlDeleteClassBuffer.append(classNode.getClassName());
        sqlDeleteClassBuffer.append("'");
        sqlDeleteClassBuffer.append(sqlDelimiter);
      }
    }

    SQLClassExport ce = new SQLClassExport();
    ce.setDeleteSql(sqlDeleteClassBuffer.toString());
    ce.setInsertQueryTextSql(sqlInsertQueryText.toString());
    ce.setInsertSql(sqlInsertBuffer.toString());
    return ce;
  }

  /**
   * Generates a delete all string
   */
  public String exportDeleteAll() {
    StringBuffer sqlDeleteAll = new StringBuffer();
    sqlDeleteAll.append("delete from ");
    sqlDeleteAll.append(tablePrefix);
    sqlDeleteAll.append("cpo_query_parameter");
    sqlDeleteAll.append(sqlDelimiter);

    sqlDeleteAll.append("delete from ");
    sqlDeleteAll.append(tablePrefix);
    sqlDeleteAll.append("cpo_attribute_map");
    sqlDeleteAll.append(sqlDelimiter);

    sqlDeleteAll.append("delete from ");
    sqlDeleteAll.append(tablePrefix);
    sqlDeleteAll.append("cpo_query");
    sqlDeleteAll.append(sqlDelimiter);

    sqlDeleteAll.append("delete from ");
    sqlDeleteAll.append(tablePrefix);
    sqlDeleteAll.append("cpo_query_text");
    sqlDeleteAll.append(sqlDelimiter);

    sqlDeleteAll.append("delete from ");
    sqlDeleteAll.append(tablePrefix);
    sqlDeleteAll.append("cpo_query_group");
    sqlDeleteAll.append(sqlDelimiter);
    
    sqlDeleteAll.append("delete from ");
    sqlDeleteAll.append(tablePrefix);
    sqlDeleteAll.append("cpo_class");
    sqlDeleteAll.append(sqlDelimiter);
    return sqlDeleteAll.toString();
  }

  /**
   * Generates a create all with deletes string
   */
  public String exportCreateAll(CpoServerNode menuNode, ProgressEventListener pel) {
    StringBuffer buf = new StringBuffer();

    // create the delete all statements
    String delSql = exportDeleteAll();
    buf.append(delSql);

    // make the class files
    Enumeration<AbstractCpoNode> menuEnum = menuNode.children();
    while (menuEnum.hasMoreElements()) {
      AbstractCpoNode child = menuEnum.nextElement();
      if (child instanceof CpoClassNode) {
        CpoClassNode classNode = (CpoClassNode)child;
        SQLClassExport classExport = exportSQL(classNode, true);

        buf.append(classExport.getInsertQueryTextSql());
        buf.append(classExport.getInsertSql());

        pel.progressMade(new ProgressValueEvent(this, 1));
      }
    }

    return buf.toString();
  }
}