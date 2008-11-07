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
import javax.swing.tree.*;

import org.apache.log4j.Category;

import java.util.Enumeration;
import java.util.Properties;
import java.util.HashSet;

import gnu.regexp.*;

public class SQLExporter  {
	  private Category OUT = Category.getInstance(this.getClass());
	private String tablePrefix="";
  private final static String DELIMITER_SLASH = "\n/\n";
  private final static String DELIMITER_SEMI = ";\n";
  private String sqlDelimiter = DELIMITER_SLASH;
	
  private SQLExporter(){}
	
  public SQLExporter(String tablePrefix, String sqlDelimiter) {
	  this.tablePrefix=tablePrefix==null?"":tablePrefix;
	  OUT.debug("Creating new SQLExporter with tablePrefix=<"+this.tablePrefix+">");
    if (sqlDelimiter != null && sqlDelimiter.trim().length() >0) {
      if (sqlDelimiter.equals("/")) {
        this.sqlDelimiter = DELIMITER_SLASH;
      } else if (sqlDelimiter.equals(";")) {
        this.sqlDelimiter = DELIMITER_SEMI;
      } else {
        this.sqlDelimiter = sqlDelimiter;
      }
    } else {
      // use default
    }
 }
  
  public String exportSQL(TreeNode parent, boolean deleteAll) {
    HashSet createdQueryTexts = new HashSet();
    StringBuffer sqlDeleteAll = new StringBuffer();
    StringBuffer sqlDeleteClassBuffer = new StringBuffer();
    StringBuffer sqlInsertBuffer = new StringBuffer();
    StringBuffer sqlInsertQueryText = new StringBuffer();
    sqlDeleteAll.append("delete from {$table.prefix}cpo_query_parameter").append(this.sqlDelimiter);
    sqlDeleteAll.append("delete from {$table.prefix}cpo_attribute_map").append(this.sqlDelimiter);
    sqlDeleteAll.append("delete from {$table.prefix}cpo_query").append(this.sqlDelimiter);
    sqlDeleteAll.append("delete from {$table.prefix}cpo_query_text").append(this.sqlDelimiter);
    sqlDeleteAll.append("delete from {$table.prefix}cpo_query_group").append(this.sqlDelimiter);
    sqlDeleteAll.append("delete from {$table.prefix}cpo_class").append(this.sqlDelimiter);
    /**
     * export Class rows
     */
    if (parent instanceof CpoClassNode) {
      CpoClassNode classNode = (CpoClassNode)parent;
      /**
       * remove cpo_class rows first
       */
//      String tempTableName = "sncr_temp_cpo";
      sqlDeleteClassBuffer.append("delete from {$table.prefix}CPO_QUERY_PARAMETER  where query_id in (select distinct query_id from {$table.prefix}CPO_QUERY where group_id in (select distinct group_id from {$table.prefix}CPO_QUERY_GROUP where class_id=(select class_id from {$table.prefix}CPO_CLASS where name='" + classNode.getClassName() + "')))").append(this.sqlDelimiter);
//      sqlDeleteClassBuffer.append("create table " + tempTableName + " as (select distinct text_id from {$table.prefix}CPO_QUERY where group_id in (select distinct group_id from {$table.prefix}CPO_QUERY_GROUP where class_id=(select class_id from {$table.prefix}CPO_CLASS where name='" + classNode.getClassName() + "')))").append(this.sqlDelimiter);
      sqlDeleteClassBuffer.append("delete from {$table.prefix}CPO_QUERY where group_id in (select distinct group_id from {$table.prefix}CPO_QUERY_GROUP where class_id=(select class_id from {$table.prefix}CPO_CLASS where name='" + classNode.getClassName() + "'))").append(this.sqlDelimiter);
//      sqlDeleteClassBuffer.append("delete from {$table.prefix}CPO_QUERY_TEXT where text_id in (select text_id from " + tempTableName +")").append(this.sqlDelimiter);
//      sqlDeleteClassBuffer.append("drop table " + tempTableName + "").append(this.sqlDelimiter);

      /**
       * remove cpo_query_group rows first
       */
      sqlInsertBuffer.append("insert into {$table.prefix}cpo_class (class_id, name) values ('"+classNode.getClassId() +"','"+classNode.getClassName()+"')").append(this.sqlDelimiter);
      Enumeration enumLabels = classNode.children();
      /**
       * must do CpoAttributeMaps FIRST
       */
      while (enumLabels.hasMoreElements()) {
        Object classLabel = enumLabels.nextElement();
        /**
         * find Labels
         */
        if (classLabel instanceof CpoAttributeLabelNode) {
          Enumeration enumAtts = ((CpoAttributeLabelNode)classLabel).children();
          /**
           * remove attribute map before insert
           */
          while (enumAtts.hasMoreElements()) {
            Object attMap = enumAtts.nextElement();
            /**
             * export cpo_attribute_map rows
             */
            if (attMap instanceof CpoAttributeMapNode) {
              CpoAttributeMapNode attMapNode = (CpoAttributeMapNode)attMap;
              sqlInsertBuffer.append("insert into {$table.prefix}cpo_attribute_map (attribute_id, class_id, column_name, attribute, "
                  + "column_type, db_column, db_table,transform_class) values ('"+attMapNode.getAttributeId()+"','"
                  + attMapNode.getClassId() +"','"+attMapNode.getColumnName()+"','"
                  + attMapNode.getAttribute() +"',"
                  +"'"+attMapNode.getColumnType()+"'"+","
                  + (attMapNode.getDbColumn() == null ? null : "'"+attMapNode.getDbColumn()+"'")  +","
                  + (attMapNode.getDbTable() == null ? null : "'"+attMapNode.getDbTable()+"'") +","
                  + (attMapNode.getTransformClass() == null ? null : "'"+attMapNode.getTransformClass()+"'")
                  +")").append(this.sqlDelimiter);
            }
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
          Enumeration queryGroupEnum = ((CpoQueryGroupLabelNode)classLabel).children();
          while (queryGroupEnum.hasMoreElements()) {
            Object queryGroup = queryGroupEnum.nextElement();
            /**
             * export Query Group rows
             */
            if (queryGroup instanceof CpoQueryGroupNode) {
              CpoQueryGroupNode queryGroupNode = (CpoQueryGroupNode)queryGroup;
              /**
               * remove cpo_query_group before inserting
               */
              /**
               * remove cpo_query before inserting
               */
              sqlInsertBuffer.append("insert into {$table.prefix}cpo_query_group (group_id, class_id, group_type, name) values ('"
                  + queryGroupNode.getGroupId() + "','" + queryGroupNode.getClassId() + "','"
                  + queryGroupNode.getType() + "',"
                  + (queryGroupNode.getGroupName() == null ? null : "'" + queryGroupNode.getGroupName() + "'")
                  +")").append(this.sqlDelimiter);
              Enumeration enumQuery = queryGroupNode.children();
              while (enumQuery.hasMoreElements()) {
                Object query = enumQuery.nextElement();
                /**
                 * export Query rows
                 */
                if (query instanceof CpoQueryNode) {
                  CpoQueryNode queryNode = (CpoQueryNode)query;
                  /**
                   * remove cpo_query by query_id before insert
                   */
                  /**
                   * remove any query parameters before inserting new ones
                   */

                  sqlInsertBuffer.append("insert into {$table.prefix}cpo_query (query_id, group_id, text_id, seq_no) values ('"
                      +queryNode.getQueryId()+"','"+queryNode.getGroupId()+"','"+queryNode.getTextId()
                      +"','"+queryNode.getSeqNo()+"')").append(this.sqlDelimiter);
                  CpoQueryTextNode queryTextNode = queryNode.getQueryText();
                  /**
                   * remove query_text before trying insert
                   */
                  sqlDeleteClassBuffer.append("delete from {$table.prefix}cpo_query_text where text_id = '"+queryTextNode.getTextId()+"'").append(this.sqlDelimiter);
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
                    sqlInsertQueryText.append("insert into {$table.prefix}cpo_query_text (text_id, sql_text, description) values ('"
                        +queryTextNode.getTextId()+"','"+queryTextSql+"',"
                        +(queryTextDesc == null ? null : "'"+queryTextDesc+"'")
                        +")").append(this.sqlDelimiter);
                    sqlInsertQueryText.append("update {$table.prefix}cpo_query_text set sql_text = '"+queryTextSql
                        +"', description = "
                        +(queryTextDesc == null ? null : "'"+queryTextDesc+"'")
                        +" where text_id = '"+queryTextNode.getTextId()+"'").append(this.sqlDelimiter);
                    createdQueryTexts.add(queryTextNode.getTextId());
                  }

                  Enumeration enumQueryParam = queryNode.children();
                  while (enumQueryParam.hasMoreElements()) {
                    Object queryParam = enumQueryParam.nextElement();
                    /**
                     * export query parameter rows
                     */
                    if (queryParam instanceof CpoQueryParameterNode) {
                      CpoQueryParameterNode queryParamNode = (CpoQueryParameterNode)queryParam;
                      sqlInsertBuffer.append("insert into {$table.prefix}cpo_query_parameter (attribute_id, query_id, seq_no,param_type) values ('"
                          +queryParamNode.getAttributeId()+"','"+queryParamNode.getQueryId()+"','"
                          +queryParamNode.getSeqNo()+"','"
                          +queryParamNode.getType()+"')").append(this.sqlDelimiter);
                    }
                  }
                }
              }
            }
          }
        }
      }
      sqlDeleteClassBuffer.append("delete from {$table.prefix}CPO_QUERY_GROUP where class_id=(select class_id from {$table.prefix}CPO_CLASS where name='" + classNode.getClassName() + "')").append(this.sqlDelimiter);
      sqlDeleteClassBuffer.append("delete from {$table.prefix}CPO_ATTRIBUTE_MAP where class_id=(select class_id from {$table.prefix}CPO_CLASS where name='" + classNode.getClassName() + "')").append(this.sqlDelimiter);
      sqlDeleteClassBuffer.append("delete from {$table.prefix}CPO_CLASS where name='" + classNode.getClassName() + "'").append(this.sqlDelimiter);
    }
    // replace the table prefix marker with the actual prefix

    sqlDeleteAll = Statics.replaceMarker(sqlDeleteAll, "{$table.prefix}", this.tablePrefix);
    sqlDeleteClassBuffer = Statics.replaceMarker(sqlDeleteClassBuffer, "{$table.prefix}", this.tablePrefix);
    sqlInsertQueryText = Statics.replaceMarker(sqlInsertQueryText, "{$table.prefix}", this.tablePrefix);
    sqlInsertBuffer = Statics.replaceMarker(sqlInsertBuffer, "{$table.prefix}", this.tablePrefix);
    
    if (deleteAll)
      return sqlDeleteAll.toString()+sqlDeleteClassBuffer.toString()+sqlInsertQueryText.toString()+sqlInsertBuffer.toString();
    else
      return sqlDeleteClassBuffer.toString()+sqlInsertQueryText.toString()+sqlInsertBuffer.toString();
  }
}