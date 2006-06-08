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
import java.util.Enumeration;
import gnu.regexp.*;

public class SQLExporter  {
  public SQLExporter() {
  }
  public static String exportSQL(TreeNode parent, boolean deleteAll) {
    StringBuffer sqlDeleteAll = new StringBuffer();
    StringBuffer sqlDeleteBuffer = new StringBuffer();
    StringBuffer sqlInsertBuffer = new StringBuffer();
    StringBuffer sqlInsertQueryText = new StringBuffer();
    sqlDeleteAll.append("delete from CPO_QUERY_PARAMETER;\n");
    sqlDeleteAll.append("delete from cpo_attribute_map;\n");
    sqlDeleteAll.append("delete from cpo_query;\n");
    sqlDeleteAll.append("delete from cpo_query_text;\n");
    sqlDeleteAll.append("delete from cpo_query_group;\n");
    sqlDeleteAll.append("delete from cpo_class;\n");
    /**
     * export Class rows
     */
    if (parent instanceof CpoClassNode) {
      CpoClassNode classNode = (CpoClassNode)parent;
      /**
       * remove cpo_class rows first
       */
      sqlDeleteBuffer.insert(0,"delete from cpo_class where class_id = '"+classNode.getClassId()
          +"' or name = '"+classNode.getClassName()+"';\n");
      /**
       * remove cpo_query_group rows first
       */
      sqlDeleteBuffer.insert(0,"delete from cpo_query_group where class_id = '"+classNode.getClassId()+"';\n");
      sqlInsertBuffer.append("insert into cpo_class (class_id, name) values ('"+classNode.getClassId()
          +"','"+classNode.getClassName()+"');\n");
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
          sqlDeleteBuffer.insert(0,"delete from cpo_attribute_map where class_id = '"+classNode.getClassId()+"';\n");
          while (enumAtts.hasMoreElements()) {
            Object attMap = enumAtts.nextElement();
            /**
             * export cpo_attribute_map rows
             */
            if (attMap instanceof CpoAttributeMapNode) {
              CpoAttributeMapNode attMapNode = (CpoAttributeMapNode)attMap;
              sqlInsertBuffer.append("insert into cpo_attribute_map (attribute_id, class_id, column_name, attribute, "
                  + "column_type, db_column, db_table,transform_class) values ('"+attMapNode.getAttributeId()+"','"
                  + attMapNode.getClassId() +"','"+attMapNode.getColumnName()+"','"
                  + attMapNode.getAttribute() +"',"
                  +"'"+attMapNode.getColumnType()+"'"+","
                  + (attMapNode.getDbColumn() == null ? null : "'"+attMapNode.getDbColumn()+"'")  +","
                  + (attMapNode.getDbTable() == null ? null : "'"+attMapNode.getDbTable()+"'") +","
                  + (attMapNode.getTransformClass() == null ? null : "'"+attMapNode.getTransformClass()+"'")
                  +");\n");
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
              sqlDeleteBuffer.insert(0,"delete from cpo_query_group where group_id = '"+queryGroupNode.getGroupId()
                  +"';\n");
              /**
               * remove cpo_query before inserting
               */
              sqlDeleteBuffer.insert(0,"delete from cpo_query where group_id = '"+queryGroupNode.getGroupId()+"';\n");
              sqlInsertBuffer.append("insert into cpo_query_group (group_id, class_id, group_type, name) values ('"
                  + queryGroupNode.getGroupId() + "','" + queryGroupNode.getClassId() + "','"
                  + queryGroupNode.getType() + "',"
                  + (queryGroupNode.getGroupName() == null ? null : "'" + queryGroupNode.getGroupName() + "'")
                  +");\n");                
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
                  sqlDeleteBuffer.insert(0,"delete from cpo_query where query_id = '"+queryNode.getQueryId()+"';\n");
                  /**
                   * remove any query parameters before inserting new ones
                   */
                  sqlDeleteBuffer.insert(0,"delete from cpo_query_parameter where query_id = '"+queryNode.getQueryId()
                      + "';\n");

                  sqlInsertBuffer.append("insert into cpo_query (query_id, group_id, text_id, seq_no) values ('"
                      +queryNode.getQueryId()+"','"+queryNode.getGroupId()+"','"+queryNode.getTextId()
                      +"','"+queryNode.getSeqNo()+"');\n");
                  CpoQueryTextNode queryTextNode = queryNode.getQueryText();
                  /**
                   * remove query_text before trying insert
                   */
                  sqlDeleteBuffer.append("delete from cpo_query_text where text_id = '"+queryTextNode.getTextId()+"';\n");
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
//                    RE reComma = new RE(",");
//                    RE reQuestion = new RE("\\?");
//                    RE reLparen = new RE("\\(");
//                    RE reRparen = new RE("\\)");
                    if (queryTextSql != null)
                      queryTextSql = reQuote.substituteAll(queryTextSql,"''");
                    if (queryTextDesc != null)
                      queryTextDesc = reQuote.substituteAll(queryTextDesc,"''");
//                    queryTextSql = reComma.substituteAll(queryTextSql,"^,");
//                    queryTextDesc = reComma.substituteAll(queryTextDesc,"^,");
//                    queryTextSql = reQuestion.substituteAll(queryTextSql,"^?");
//                    queryTextDesc = reQuestion.substituteAll(queryTextDesc,"^?");
//                    queryTextSql = reLparen.substituteAll(queryTextSql,"^(");
//                    queryTextDesc = reLparen.substituteAll(queryTextDesc,"^(");
//                    queryTextSql = reRparen.substituteAll(queryTextSql,"^)");
//                    queryTextDesc = reRparen.substituteAll(queryTextDesc,"^)");
                    
                  } catch (REException ree) {
                    CpoUtil.showException(ree);
                    return null;
                  }                  
                  sqlInsertQueryText.append("insert into cpo_query_text (text_id, sql_text, description) values ('"
                      +queryTextNode.getTextId()+"','"+queryTextSql+"',"
                      +(queryTextDesc == null ? null : "'"+queryTextDesc+"'")
                      +");\n");
                  sqlInsertQueryText.append("update cpo_query_text set sql_text = '"+queryTextSql
                      +"', description = "
                      +(queryTextDesc == null ? null : "'"+queryTextDesc+"'")
                      +" where text_id = '"+queryTextNode.getTextId()+"';\n");
                  Enumeration enumQueryParam = queryNode.children();
                  while (enumQueryParam.hasMoreElements()) {
                    Object queryParam = enumQueryParam.nextElement();
                    /**
                     * export query parameter rows
                     */
                    if (queryParam instanceof CpoQueryParameterNode) {
                      CpoQueryParameterNode queryParamNode = (CpoQueryParameterNode)queryParam;
                      sqlInsertBuffer.append("insert into cpo_query_parameter (attribute_id, query_id, seq_no,param_type) values ('"
                          +queryParamNode.getAttributeId()+"','"+queryParamNode.getQueryId()+"','"
                          +queryParamNode.getSeqNo()+"','"
                          +queryParamNode.getType()+"');\n");
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    if (deleteAll)
      return sqlDeleteAll.toString()+sqlDeleteBuffer.toString()+sqlInsertQueryText.toString()+sqlInsertBuffer.toString();
    else
      return sqlDeleteBuffer.toString()+sqlInsertQueryText.toString()+sqlInsertBuffer.toString();
  }
}