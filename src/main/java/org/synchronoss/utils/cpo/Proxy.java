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
import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.synchronoss.cpo.CpoAdapter;
import org.synchronoss.cpo.jdbc.JdbcCpoAdapter;
import org.synchronoss.cpo.jdbc.JdbcDataSourceInfo;
import org.synchronoss.cpo.jdbc.JavaSqlTypes;


import org.apache.log4j.Category;

public class Proxy implements Observer {
  boolean revsEnabled = false;
  private Context ctx;
  private Properties defProps;
  private String server;
  private Connection conn;
  private Hashtable classCache = new Hashtable();
  private Hashtable classCacheById = new Hashtable();
  private Hashtable attMapCache = new Hashtable();
  private Hashtable queryParamCache = new Hashtable();
  private Hashtable queryCache = new Hashtable();
  private Hashtable queryGroupCache = new Hashtable();
  private Hashtable queryGroupByTextCache = new Hashtable();
  private ArrayList queryTextCache = new ArrayList();
  private ArrayList allChangedObjects = new ArrayList();
//  private Object cpoMan; //CpoManager
  private CpoAdapter cpoMan;
  private String[] sqlTypes;
//  private Method sqlTypeClassMeth = null;
//  private Method sqlTypeClassMethInt = null;
//  private HashMap cpoTypeMap = new HashMap();
  private CpoBrowserTree cpoTree;
  private String databaseName;
  private String tablePrefix;
  private Properties connProps;
  private boolean classNameToggle = false;
  private Category OUT = Category.getInstance(this.getClass());
  private String connectionClassName;
  //org.synchronoss.cpo.JdbcCpoAdapter
  public Proxy(Properties props, String server, CpoBrowserTree cpoTree) throws Exception {
    if (server == null || props == null) throw new Exception("No Server Selected!");
    this.defProps = props;
    this.server = server;
    this.cpoTree = cpoTree;
    getConnection();
    checkForRevsEnabled();
  }  
  public String getTablePrefix() {
	  return this.tablePrefix;
  }
  
  void getConnection() throws Exception {
    if (CpoUtil.localProps.containsKey(Statics.PROP_JDBC_DRIVER+server) ||
        CpoUtil.localProps.containsKey(Statics.PROP_WLSURL+server))
      connProps = CpoUtil.localProps;
    else
      connProps = defProps;
    
    this.tablePrefix = connProps.getProperty(Statics.PROP_JDBC_TABLE_PREFIX+server);

//    Class cpoSqlTypesClass =  CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass("org.synchronoss.cpo.jdbc.JavaSqlTypes");
    //javaSqlTypes = cpoSqlTypesClass.newInstance();
//    Method cpoManGetSqlTypesMeth = cpoSqlTypesClass.getMethod("getSqlTypes",null);
//    sqlTypeClassMeth = cpoSqlTypesClass.getMethod("getSqlTypeClass",new Class[]{String.class});
//    sqlTypeClassMethInt = cpoSqlTypesClass.getMethod("getSqlTypeClass",new Class[]{Integer.class});
//    Collection coll = (Collection)cpoManGetSqlTypesMeth.invoke(null,null);
    Collection coll = JavaSqlTypes.getSqlTypes();
    sqlTypes = new String[coll.size()];
    coll.toArray(sqlTypes);

    if (CpoUtil.props.getProperty(Statics.PROP_JDBC_URL+server) == null
        && CpoUtil.localProps.getProperty(Statics.PROP_JDBC_URL+server) == null) {
     getInitialContext();
//      try {
        conn = ((DataSource)ctx.lookup(connProps.getProperty(Statics.PROP_WLSCONNPOOL+server))).getConnection();
        conn.setAutoCommit(false);
        databaseName = conn.getMetaData().getURL();


        // Replace the dynamic link with a hard link.
        cpoMan = new JdbcCpoAdapter(new JdbcDataSourceInfo(connProps.getProperty(Statics.PROP_WLSCONNPOOL+server),getTablePrefix()));
//        this.getClass().getClassLoader().loadClass("javax.ejb.EJBHome");
//        connectionClassName = "org.synchronoss.cpo.CpoManagerHome";
//  /      Class cpoManHomeClass = CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass(connectionClassName);

//        Object cpoManHome = ctx.lookup(connProps.getProperty(Statics.PROP_CPONAME+server));
//        Method cpoManHomeCreateMeth = cpoManHome.getClass().getMethod("create",null);
//        cpoMan = cpoManHomeCreateMeth.invoke(cpoManHome,null);
//        Method cpoManGetSqlTypesMeth = cpoMan.getClass().getMethod("getSqlTypes",null);
//        Collection coll = (Collection)cpoManGetSqlTypesMeth.invoke(cpoMan,null);
//        sqlTypes = new String[coll.size()];
//        coll.toArray(sqlTypes);
         
//      } catch (NamingException ne) {
//        throw new ProxyException("Could not find context lookup at weblogic instance",ne);
//      } catch (SQLException se) {
//        throw new ProxyException("Caught a sql exception while creating Proxy",se);
//      } catch (NoClassDefFoundError cdfe) {
//        throw new ClassNotFoundException (cdfe.getMessage());
//      } catch (ClassNotFoundException cnfe) {
//        throw cnfe;
//      } catch (Exception e) {
//        e.printStackTrace();
//        throw new ProxyException("Caught unknown exception while creating Proxy",e);
//      }
    }
    else {
      Class driverClass;
      try {
        driverClass = Class.forName(connProps.getProperty(Statics.PROP_JDBC_DRIVER+server));
      } catch (Exception e) {
        driverClass = CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass(connProps.getProperty(Statics.PROP_JDBC_DRIVER+server));
      }
      OUT.debug("Class: "+driverClass);
        Properties connectionProperties = new Properties();
        if (connProps.getProperty(Statics.PROP_JDBC_PARAMS+server) != null && !connProps.getProperty(Statics.PROP_JDBC_PARAMS+server).equals("")) {
          StringTokenizer st = new StringTokenizer(connProps.getProperty(Statics.PROP_JDBC_PARAMS+server),";");
          while (st.hasMoreTokens()) {
            String token = st.nextToken();
            StringTokenizer stNameValue = new StringTokenizer(token,"=");
            String name = null, value = null;
            if (stNameValue.hasMoreTokens())
              name = stNameValue.nextToken();
            if (stNameValue.hasMoreTokens())
              value = stNameValue.nextToken();
            connectionProperties.setProperty(name,value);
          }
        }
        conn = DriverManager.getConnection(connProps.getProperty(Statics.PROP_JDBC_URL+server),connectionProperties);
        conn.setAutoCommit(false);
        databaseName = conn.getMetaData().getURL();
        // Replace the dynamic link with a hard link.
        cpoMan = new JdbcCpoAdapter(new JdbcDataSourceInfo(connProps.getProperty(Statics.PROP_JDBC_DRIVER+server),connProps.getProperty(Statics.PROP_JDBC_URL+server),connectionProperties,1,1,false,getTablePrefix()));
/*  Removed DB060223
         Class jdbcCpoAdapter;
        try {
          connectionClassName = "org.synchronoss.cpo.jdbc.JdbcCpoAdapter";
          jdbcCpoAdapter = CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass(connectionClassName);
        } catch (Exception e) {
          connectionClassName = "org.synchronoss.cpo.JdbcCpoAdapter";        
          jdbcCpoAdapter = CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass(connectionClassName);
        }
//        Method createMeth = jdbcCpoAdapter.getMethod("createAdapter",new Class[]{Connection.class});
//        cpoMan = createMeth.invoke(null,new Object[]{conn});
        try {
          Constructor classCtor = jdbcCpoAdapter.getConstructor(new Class[]{Connection.class});
          cpoMan = classCtor.newInstance(new Object[]{conn});          
        } catch (Exception e) {
          OUT.error("Caught exception instantiating JdbcCpoAdapter ...  will try something different: "+e.getMessage(),e);
          Constructor classCtor = jdbcCpoAdapter.getConstructor(new Class[]{String.class,String.class,Properties.class});
          cpoMan = classCtor.newInstance(new Object[]{connProps.getProperty(Statics.PROP_JDBC_DRIVER+server),connProps.getProperty(Statics.PROP_JDBC_URL+server),connectionProperties});
        }
*/
        //Class cpoSqlTypesClass =  CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass("org.synchronoss.cpo.jdbc.JavaSqlTypes");
        //Method cpoManGetSqlTypesMeth = cpoSqlTypesClass.getMethod("getSqlTypes",null);
        //Method cpoManGetSqlTypesMeth = cpoMan.getClass().getMethod("getSqlTypes",null);
        //Collection coll = (Collection)cpoManGetSqlTypesMeth.invoke(cpoMan,null);
        //sqlTypes = new String[coll.size()];
        //coll.toArray(sqlTypes);
    }
//      } catch (SQLException se) {
//        throw new ProxyException("SQL Exception thrown",se);
//      } catch (NoSuchMethodException nsme) {
//        nsme.printStackTrace();
//        throw new ProxyException("Couldn't find method name",nsme);
//      } catch (IllegalAccessException iae) {
//        throw new ProxyException("Illegal Access: is the method private?  package?",iae);
//      } catch (InvocationTargetException ite) {
//        throw new ProxyException("InvocationTargetException thrown",ite);
//      }
  }
  private void getInitialContext() throws Exception {
//    try {
      Properties h = new Properties();
      h.put(Context.INITIAL_CONTEXT_FACTORY, connProps.getProperty(Statics.PROP_WLSINITCTXFCTRY+server));
      h.put(Context.PROVIDER_URL, connProps.getProperty(Statics.PROP_WLSURL+server));
      if (connProps.getProperty(Statics.PROP_WLSUSER+server) != null) {
        h.put(Context.SECURITY_PRINCIPAL, connProps.getProperty(Statics.PROP_WLSUSER+server));
        if (connProps.getProperty(Statics.PROP_WLSPASS+server) == null)
          h.put(Context.SECURITY_CREDENTIALS, "");
        else
          h.put(Context.SECURITY_CREDENTIALS, connProps.getProperty(Statics.PROP_WLSPASS+server));
      }
      ctx = new InitialContext(h);
      OUT.debug("About to return initial context");
//    } catch (NamingException ne) {
//      ne.printStackTrace();
//      throw new ProxyException("Could not connect to weblogic instance",ne);
//    } catch (Exception e) {
//      e.printStackTrace();
//      throw new ProxyException("Could not connect to weblogic for some reason",e);
//    }
  }
  private void checkForRevsEnabled() {
    StringBuffer sql = new StringBuffer("select distinct userid from {$table.prefix}cpo_class_rev");
    PreparedStatement pstmt = null;
    try {
      pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
      pstmt.executeQuery();
      revsEnabled = true;
    } catch (Exception e) {
    }
    finally {
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
  }
  public String getDatabaseName() {
    return this.databaseName;
  }
  public String toString() {
    return server;
  }
  /**
   * returns sql types that this instance of cpo supports
   */
  public String[] getCpoSqlTypes() {
    return this.sqlTypes;
  }
  /**
   * returns CpoClassNode(s)
   */
  public CpoClassNode getClassNode(String classId) throws Exception {
    return (CpoClassNode)this.classCacheById.get(classId);
  }
  public Hashtable getClassesById() {
    return this.classCacheById;
  }
  public ArrayList getClasses(AbstractCpoNode parent) throws Exception {
    if (this.classCache.containsKey(parent)) {
      return (ArrayList)this.classCache.get(parent);
    }
    ArrayList al = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      StringBuffer sql=new StringBuffer();
      if (revsEnabled)
        sql.append("select class_id, name, userid, createdate from {$table.prefix}cpo_class order by upper(name)");
      else
    	  sql.append("select class_id, name from {$table.prefix}cpo_class order by upper(name)");
      pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
      rs = pstmt.executeQuery();
      while (rs.next()) {
        CpoClassNode cpoClassNode = new CpoClassNode(rs.getString("name"),rs.getString("class_id"),parent);
        if (revsEnabled) {
          cpoClassNode.setUserName(rs.getString("userid"));
          cpoClassNode.setCreateDate(rs.getTimestamp("createdate"));
        }
        al.add(cpoClassNode);
        classCacheById.put(cpoClassNode.getClassId(),cpoClassNode);
      }
//    } catch (SQLException se) {
//      se.printStackTrace();
//      throw new ProxyException("getClasses()",se);
    }
    finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    this.classCache.put(parent,al);
    return al;
  }
  public CpoQueryTextLabelNode getCpoQueryTextLabelNode(CpoServerNode serverNode) {
    Enumeration serverNodeEnum = serverNode.children();
    while (serverNodeEnum.hasMoreElements()) {
      AbstractCpoNode node = (AbstractCpoNode)serverNodeEnum.nextElement();
      if (node instanceof CpoQueryTextLabelNode) return (CpoQueryTextLabelNode)node;
    }
    return null;
  }
  public ArrayList getQueryText(CpoServerNode cpoServer) throws Exception {
    if (this.queryTextCache.size() != 0)
      return this.queryTextCache;
    CpoQueryTextLabelNode parent = this.getCpoQueryTextLabelNode(cpoServer);
    ArrayList al = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      StringBuffer sql=new StringBuffer();
      if (revsEnabled)
        sql.append("select text.text_id, text.sql_text, text.description, (select count(*) from {$table.prefix}cpo_query where text_id = text.text_id) usagecount, text.userid, text.createdate from {$table.prefix}cpo_query_text text order by text.description");
      else
        sql.append("select text.text_id, text.sql_text, text.description, count(query.text_id) usagecount from {$table.prefix}cpo_query_text text, {$table.prefix}cpo_query query WHERE text.text_id = query.text_id GROUP BY text.text_id, text.sql_text, text.description order by text.description");
      pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
      rs = pstmt.executeQuery();
      while (rs.next()) {
        CpoQueryTextNode cpoQTNode = new CpoQueryTextNode(rs.getString("text_id"),
            rs.getString("sql_text"),rs.getString("description"),parent);
        cpoQTNode.setUsageCount(rs.getInt("usagecount"));
        if (revsEnabled) {
          cpoQTNode.setUserName(rs.getString("userid"));
          cpoQTNode.setCreateDate(rs.getTimestamp("createdate"));
        }
        al.add(cpoQTNode);
      }
//    } catch (SQLException se) {
//      throw new ProxyException("getQueryText()",se);
    }
    finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    queryTextCache = al;
    return al;
  }
  public ArrayList getQueryGroups(CpoQueryTextNode textNode) throws Exception {
    if (this.queryGroupByTextCache.containsKey(textNode)) {
      return (ArrayList)this.queryGroupByTextCache.get(textNode);
    }
    ArrayList al = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuffer sql=new StringBuffer();
      if (revsEnabled)
    	  sql.append("select group_id, class_id, group_type, name, userid, createdate from {$table.prefix}cpo_query_group where group_id in "+
            "(select group_id from {$table.prefix}cpo_query where text_id = ?) order by group_id");
      else
    	  sql.append("select group_id, class_id, group_type, name from {$table.prefix}cpo_query_group where group_id in "+
            "(select group_id from {$table.prefix}cpo_query where text_id = ?) order by group_id");
      pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
      pstmt.setString(1,textNode.getTextId());
      rs = pstmt.executeQuery();
      while (rs.next()) {
        CpoQueryGroupNode cpoQueryGroupNode = new CpoQueryGroupNode(rs.getString("name"),rs.getString("class_id"), rs.getString("group_id"), rs.getString("group_type"),null);
        if (revsEnabled) {
          cpoQueryGroupNode.setUserName(rs.getString("userid"));
          cpoQueryGroupNode.setCreateDate(rs.getTimestamp("createdate"));
        }
        al.add(cpoQueryGroupNode);
      }
    } finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    this.queryGroupByTextCache.put(textNode,al);
    return al;
  }
  public ArrayList getQueryGroups(CpoQueryGroupLabelNode parent) throws Exception {
    if (this.queryGroupCache.containsKey(parent)) {
      return (ArrayList)this.queryGroupCache.get(parent);
    }
    ArrayList al = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuffer sql=new StringBuffer();
      if (revsEnabled)
    	  sql.append("select group_id, class_id, group_type, name, userid, createdate from {$table.prefix}cpo_query_group where class_id = ? order by group_id");
      else
    	  sql.append("select group_id, class_id, group_type, name from {$table.prefix}cpo_query_group where class_id = ? order by group_id");
      pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
      pstmt.setString(1,((CpoClassNode)parent.getParent()).getClassId());
      rs = pstmt.executeQuery();
      while (rs.next()) {
        CpoQueryGroupNode cpoQueryGroupNode = new CpoQueryGroupNode(rs.getString("name"),rs.getString("class_id"), rs.getString("group_id"), rs.getString("group_type"),parent);
        if (revsEnabled) {
          cpoQueryGroupNode.setUserName(rs.getString("userid"));
          cpoQueryGroupNode.setCreateDate(rs.getTimestamp("createdate"));
        }
        al.add(cpoQueryGroupNode);
      }
//    } catch (SQLException se) {
//      throw new ProxyException("getQueryGroups()",se);
    }
    finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    this.queryGroupCache.put(parent,al);
    return al;
  }
  public CpoQueryGroupLabelNode getQueryGroupLabelNode(CpoClassNode classNode) {
    Enumeration classEnum = classNode.children();
    while (classEnum.hasMoreElements()) {
      Object obj = classEnum.nextElement();
      if (obj instanceof CpoQueryGroupLabelNode) return (CpoQueryGroupLabelNode)obj;
    }
    return null;
  }
  public CpoQueryGroupNode getQueryGroupNode(CpoClassNode classNode, String name, String type) throws Exception {
    CpoQueryGroupLabelNode cqgl = getQueryGroupLabelNode(classNode);
    ArrayList al = getQueryGroups(cqgl);
    Iterator iter = al.iterator();
    while (iter.hasNext()) {
      CpoQueryGroupNode node = (CpoQueryGroupNode)iter.next();
      if (((name == null && node.getGroupName() == null) ||
          node.getGroupName() != null && name != null &&
          node.getGroupName().equals(name)) 
          && node.getType().equals(type)) return node;
    }
    return null;
  }
  /**
   * returns CpoQueryNode(s)
   */
  public ArrayList getQueries(CpoQueryGroupNode parent) throws Exception {
    if (this.queryCache.containsKey(parent)) {
      return (ArrayList)this.queryCache.get(parent);
    }
    ArrayList al = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuffer sql=new StringBuffer();
      if (revsEnabled)
    	  sql.append("select q.query_id, q.group_id, q.text_id, q.seq_no, q.userid, q.createdate from "
          + "{$table.prefix}cpo_query q where q.group_id = ? order by q.seq_no");
      else
    	  sql.append("select q.query_id, q.group_id, q.text_id, q.seq_no from "
          + "{$table.prefix}cpo_query q where q.group_id = ? order by q.seq_no");
      pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
      pstmt.setString(1,parent.getGroupId());
      rs = pstmt.executeQuery();
      while (rs.next()) {
        CpoQueryTextNode cQTnode = this.getQueryText(
            (CpoServerNode)parent.getParent().getParent().getParent(),rs.getString("text_id"));
        CpoQueryNode cpoQueryNode = new CpoQueryNode(rs.getString("query_id"),
            rs.getString("group_id"), rs.getInt("seq_no"),
            cQTnode,parent);
        if (revsEnabled) {
          cpoQueryNode.setUserName(rs.getString("userid"));
          cpoQueryNode.setCreateDate(rs.getTimestamp("createdate"));
        }
        al.add(cpoQueryNode);
      }
//    } catch (SQLException se) {
//      throw new ProxyException("getQueries",se);
    }
    finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    this.queryCache.put(parent,al);
    return al;
  }
  /**
   * returns CpoQueryParameterNode(s)
   */
  public ArrayList getQueryParameters(CpoQueryNode qNode) throws Exception {
    if (this.queryParamCache.containsKey(qNode)) {
      return (ArrayList)this.queryParamCache.get(qNode);
    }    
    ArrayList al = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuffer sql=new StringBuffer();
      if (revsEnabled)
    	  sql.append("select qp.attribute_id, qp.query_id, qp.seq_no, qp.param_type, qp.userid, qp.createdate "
          + "from {$table.prefix}cpo_query_parameter qp "
          + "where query_id = ? order by qp.seq_no");
      else
    	  sql.append("select qp.attribute_id, qp.query_id, qp.seq_no, qp.param_type "
          + "from {$table.prefix}cpo_query_parameter qp "
          + "where query_id = ? order by qp.seq_no");
      pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
      pstmt.setString(1,qNode.getQueryId());
      rs = pstmt.executeQuery();
//      OUT.debug ("Parent for "+qNode+": "+qNode.getParent());
      while (rs.next()) {
        CpoQueryParameterNode cpoQPB = new CpoQueryParameterNode(qNode, rs.getInt("seq_no"), 
            getAttributeMap((CpoServerNode)qNode.getParent().getParent().getParent().getParent(),rs.getString("attribute_id")),
            rs.getString("param_type"));
        
        if (revsEnabled) {
          cpoQPB.setUserName(rs.getString("userid"));
          cpoQPB.setCreateDate(rs.getTimestamp("createdate"));
        }
        al.add(cpoQPB);
      }
//    } catch (SQLException se) {
//      throw new ProxyException("getQueryParameters",se);
    }
    finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    this.queryParamCache.put(qNode,al);
    return al;
  }
  public ArrayList getAttributeMap(CpoClassNode cpoClassNode) throws Exception {
    Enumeration classEnum = cpoClassNode.children();
    while (classEnum.hasMoreElements()) {
      AbstractCpoNode node = (AbstractCpoNode)classEnum.nextElement();
      if (node instanceof CpoAttributeLabelNode)
        return getAttributeMap((CpoAttributeLabelNode)node);
    }
    return null;
  }
  public CpoAttributeMapNode getAttributeMap(CpoClassNode cpoClassNode, String attributeId) throws Exception {
    Enumeration classEnum = cpoClassNode.children();
    while (classEnum.hasMoreElements()) {
      AbstractCpoNode node = (AbstractCpoNode)classEnum.nextElement();
      if (node instanceof CpoAttributeLabelNode)
        return getAttributeMap((CpoAttributeLabelNode)node,attributeId);
    }
    return null;
  }
  public CpoAttributeMapNode getAttributeMap(CpoServerNode cpoServerNode, String attributeId) throws Exception {
    Iterator classIt = this.getClasses(cpoServerNode).iterator();
    while (classIt.hasNext()) {
      CpoClassNode cpoClassNode = (CpoClassNode)classIt.next();
      CpoAttributeMapNode attMapNode = getAttributeMap(cpoClassNode,attributeId);
      if (attMapNode != null)
        return attMapNode;
    }
    return null;
  }
  public CpoAttributeMapNode getAttributeMap(CpoAttributeLabelNode cpoAttLabNode, String attributeId) throws Exception {
    Iterator iter = getAttributeMap(cpoAttLabNode).iterator();
    while (iter.hasNext()) {
      CpoAttributeMapNode camnTmp = (CpoAttributeMapNode)iter.next();
      if (camnTmp.getAttributeId().equals(attributeId)) {
        return camnTmp;
      }
    }
    return null;
  }
  /**
   * returns CpoAttributeMapNode(s)
   */
  public ArrayList getAttributeMap(CpoAttributeLabelNode cpoAttLabNode) throws Exception {
    if (this.attMapCache.containsKey(cpoAttLabNode)) {
      return (ArrayList)this.attMapCache.get(cpoAttLabNode);
    }
    ArrayList al = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuffer sql=new StringBuffer();
      if (revsEnabled)
    	  sql.append("select am.attribute_id, am.class_id, am.column_name, am.attribute, am.column_type, "
          + "am.transform_class, am.db_table, am.db_column, am.userid, am.createdate "
          + "from {$table.prefix}cpo_attribute_map am "
          + "where am.class_id = ? order by am.attribute");
      else
    	  sql.append("select am.attribute_id, am.class_id, am.column_name, am.attribute, am.column_type, "
          + "am.transform_class, am.db_table, am.db_column "
          + "from {$table.prefix}cpo_attribute_map am "
          + "where am.class_id = ? order by am.attribute");
      pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
      pstmt.setString(1,((CpoClassNode)cpoAttLabNode.getParent()).getClassId());
      rs = pstmt.executeQuery();
      while (rs.next()) {
        CpoAttributeMapNode cpoAB = new CpoAttributeMapNode(cpoAttLabNode,rs.getString("attribute_id"),
            rs.getString("class_id"), rs.getString("column_name"), 
            rs.getString("attribute"), rs.getString("column_type"), rs.getString("transform_class"),
            rs.getString("db_table"), rs.getString("db_column"),"IN");
        if (revsEnabled) {
          cpoAB.setUserName(rs.getString("userid"));
          cpoAB.setCreateDate(rs.getTimestamp("createdate"));
        }
        al.add(cpoAB);
      }
//    } catch (SQLException se) {
//      throw new ProxyException("getAttributeMap",se);
    }
    finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    this.attMapCache.put(cpoAttLabNode,al);
    return al;
  }
  /**
   * return the number of times a query text is being used
   */
  public int getQueryTextUsageCount(String textId) throws Exception {
    int result;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuffer sql=new StringBuffer("select count(*) from {$table.prefix}cpo_query where text_id = ?");
      pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
      pstmt.setString(1,textId);
      rs = pstmt.executeQuery();
      rs.next();
      result = rs.getInt(1);
//    } catch (SQLException se) {
//      throw new ProxyException("getQueryTextUsageCount",se);
    } finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    return result;
  }
  /**
   * lets try to close this oracle connection b4 we take a hike
   */
  protected void finalize() throws Throwable {
    super.finalize();
    try {
      conn.close();
    } catch (Exception e) {}
  }
  /**
   * observer method
   */
  public void update(Observable obs, Object obj) {
    if (obj instanceof AbstractCpoNode) {
      AbstractCpoNode can = (AbstractCpoNode)obj;
      OUT.debug ("Proxy got notification of changed object (new) "+can.isNew()+" (remove) "+can.isRemove());
      if ((can.isNew() && can.isRemove()) || (can.isNew() && can.isLabel())) {
        OUT.debug ("Removing object from all cache");
        this.removeAllChildrenFromAllCache(can);
        this.removeObjectFromAllCache(can);
      }
      else if (can.isDirty() && !allChangedObjects.contains(can)) {
        allChangedObjects.add(can);
      }
      else if (can.isNew() && !allChangedObjects.contains(can)) {
        allChangedObjects.add(can);
      }
      else if (can.isRemove() && !allChangedObjects.contains(can)) {
        allChangedObjects.add(can);
      }
      else if (!can.isDirty() && !can.isNew() && !can.isRemove() && allChangedObjects.contains(can)) {
        allChangedObjects.remove(can);
      }
      AbstractCpoNode parent = ((AbstractCpoNode)((AbstractCpoNode)obs).getParent());
      if (parent != null) {
        parent.setChildDirty((AbstractCpoNode)obj);
        parent.setChildNew((AbstractCpoNode)obj);
        parent.setChildRemove((AbstractCpoNode)obj);
        int index = 0;
        index = parent.getIndex((TreeNode)obs);
        OUT.debug ("Set parent flags - index is: "+index);
        if (index >= 0 && obs == obj) {
          OUT.debug ("Index: "+index+" size: "+((AbstractCpoNode)obs).getParent().getChildCount());
          if (((AbstractCpoNode)obs).isRemove()) {
            if (((AbstractCpoNode)obs).isNew()
                && !parent.isLeaf()) {        
              //OUT.debug ("Notifying non-leaf parent of node removal");
              ((DefaultTreeModel)this.cpoTree.getModel()).nodesWereRemoved(parent,new int[]{index},new Object[]{obs});
            }
            else if (!parent.isLeaf()) {
              //OUT.debug ("Notifying non-leaf parent of node (marked deleted) change");
              ((DefaultTreeModel)this.cpoTree.getModel()).nodesChanged(parent,new int[]{index});
            }
            else {
              //OUT.debug ("Notifying a leaf parent of node (marked deleted) change");
              ((DefaultTreeModel)this.cpoTree.getModel()).nodesChanged(parent,new int[]{index});
            }
          }
          else if (((AbstractCpoNode)obs).isDirty()) {
            //OUT.debug ("Notifying parent of node change (dirty and all else)");
            ((DefaultTreeModel)this.cpoTree.getModel()).nodesChanged(parent,new int[]{index});
          }
          else if (((AbstractCpoNode)obs).isNew()) {
            //OUT.debug ("Notifying parent ("+parent+") of node insertion ("+obj+")");
            ((DefaultTreeModel)this.cpoTree.getModel()).nodesWereInserted(parent,new int[]{index});
          }
        } 
        else if (obs == obj) {
          //OUT.debug ("Object equal to notifier: "+obs+" : parent: "+parent);
          ((DefaultTreeModel)this.cpoTree.getModel()).nodeStructureChanged(parent);
        } else
          OUT.debug ("object not equal to notifier: "+obs+" : object: "+obj);  
      } else
        OUT.debug ("Parent of "+obs.getClass()+" was null");
    }
  }
  /**
   * removes all children of an object from all cache
   */
  public void removeAllChildrenFromAllCache(AbstractCpoNode can) {
    OUT.debug ("Removing: "+can.getClass().getName());
    Enumeration childEnum = can.children();
    while (childEnum != null && childEnum.hasMoreElements()) {
      AbstractCpoNode canChild = (AbstractCpoNode)childEnum.nextElement();
//      this.removeAllChildrenFromAllCache(canChild);
      canChild.setRemove(true);
//      this.removeObjectFromAllCache(canChild);
    }
  }
  /**
   * get a list of all changed objects
   */
  public ArrayList getAllChangedObjects() {
    return this.allChangedObjects;
  }
  /**
   * retreives a new guid to use for newly created objects
   */
  public String getNewGuid() throws Exception {
    return GUID.getGUID();
  }
  /**
   * saves all nodes passed to it to the db
   */
  public void saveNodes(ArrayList nodes) throws Exception {
    PreparedStatement pstmt = null;
    try {
      Iterator iter = nodes.iterator();
      /**
       * do class nodes first
       */
      ArrayList toBeCleaned = new ArrayList();
      while (iter.hasNext()) {
        Object node = iter.next();
        if (node instanceof CpoClassNode) {
          CpoClassNode ccn = (CpoClassNode)node;
          if (ccn.isNew()) {
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled)
            	sql.append("insert into {$table.prefix}cpo_class (class_id, name, userid) "
                + "values (?,?,'"+CpoUtil.username+"')");
            else
            	sql.append("insert into {$table.prefix}cpo_class (class_id, name) "
                + "values (?,?)");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.setString(2,ccn.getClassName());
            int result = pstmt.executeUpdate();
//            OUT.debug("Inserted Class Node: "+ccn.getClassId()+" "+result);
          }
          else if (ccn.isRemove()) {
        	  StringBuffer sqlDelQueryParam=new StringBuffer("delete from {$table.prefix}cpo_query_parameter where query_id in "
                + "(select query_id from {$table.prefix}cpo_query where group_id in "
                + "(select group_id from {$table.prefix}cpo_query_group where class_id = ?))");
        	  StringBuffer sqlDelAttMap=new StringBuffer("delete from {$table.prefix}cpo_attribute_map where class_id = ?");
        	  StringBuffer sqlDelQuery=new StringBuffer("delete from {$table.prefix}cpo_query where group_id in "
                + "(select group_id from {$table.prefix}cpo_query_group where class_id = ?)");
        	  StringBuffer sqlDelQueryGroup=new StringBuffer("delete from {$table.prefix}cpo_query_group where class_id = ?");
        	  StringBuffer sqlDelClass=new StringBuffer("delete from {$table.prefix}cpo_class where class_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelQueryParam, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
            pstmt.close();
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelAttMap, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
            pstmt.close();
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelQuery, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
            pstmt.close();
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelQueryGroup, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
            pstmt.close();
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelClass, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
          }
          else if (ccn.isDirty()) {
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled)
              sql.append("update {$table.prefix}cpo_class set name = ?, userid = '"+CpoUtil.username+"' where class_id = ?");
            else
              sql.append("update {$table.prefix}cpo_class set name = ? where class_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,ccn.getClassName());
            pstmt.setString(2,ccn.getClassId());
            pstmt.executeUpdate();
          }
        }        
        else
          continue;
        toBeCleaned.add(node);
        if (pstmt != null)
          pstmt.close();
      }
      iter = nodes.iterator();
      /**
       * do attribute maps second
       */
      while (iter.hasNext()) {
        Object node = iter.next();
        if (node instanceof CpoAttributeMapNode) {
          CpoAttributeMapNode camn = (CpoAttributeMapNode)node;
          if (camn.isNew()) {
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled)
              sql.append("insert into {$table.prefix}cpo_attribute_map (attribute_id, class_id, "
                + "column_name, attribute, column_type, db_table, db_column, userid, transform_class) "
                + "values (?,?,?,?,?,?,?,'"+CpoUtil.username+"',?)");
            else
              sql.append("insert into {$table.prefix}cpo_attribute_map (attribute_id, class_id, "
                + "column_name, attribute, column_type, db_table, db_column,transform_class) "
                + "values (?,?,?,?,?,?,?,?)");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,camn.getAttributeId());
            pstmt.setString(2,camn.getClassId());
            pstmt.setString(3,camn.getColumnName());
            pstmt.setString(4,camn.getAttribute());
            pstmt.setString(5,camn.getColumnType());
            pstmt.setString(6,camn.getDbTable());
            pstmt.setString(7,camn.getDbColumn());
            pstmt.setString(8,camn.getTransformClass());
            int result = pstmt.executeUpdate();
//            OUT.debug("Inserted attribute map node: "+camn.getAttributeId()+" "+result);
          }
          else if (camn.isRemove()) {
        	  StringBuffer sql=new StringBuffer("delete from {$table.prefix}cpo_attribute_map where attribute_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,camn.getAttributeId());
            pstmt.execute();
          }
          else if (camn.isDirty()) {
            //  update everything except class_id - might want to change this later...
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled)
              sql.append("update {$table.prefix}cpo_attribute_map set "
                + "column_name = ?, attribute = ?, column_type = ?, db_table = ?, "
                + "db_column = ?, userid = '"+CpoUtil.username+"', transform_class=? where attribute_id = ?");
            else
              sql.append("update {$table.prefix}cpo_attribute_map set "
                + "column_name = ?, attribute = ?, column_type = ?, db_table = ?, "
                + "db_column = ?, transform_class=? where attribute_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,camn.getColumnName());
            pstmt.setString(2,camn.getAttribute());
            pstmt.setString(3,camn.getColumnType());
            pstmt.setString(4,camn.getDbTable());
            pstmt.setString(5,camn.getDbColumn());
            pstmt.setString(6,camn.getTransformClass());
            pstmt.setString(7,camn.getAttributeId());
            pstmt.executeUpdate();
          }
        }        
        else
          continue;
        toBeCleaned.add(node);
        if (pstmt != null)
          pstmt.close();
      }
      iter = nodes.iterator();
      /**
       * do query groups
       */
      while (iter.hasNext()) {
        Object node = iter.next();
        if (node instanceof CpoQueryGroupNode) {
          CpoQueryGroupNode cqgn = (CpoQueryGroupNode)node;
          if (cqgn.isNew()) {
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled)
              sql.append("insert into {$table.prefix}cpo_query_group (group_id, class_id, group_type, "
                + "name,userid) values (?,?,?,?,'"+CpoUtil.username+"')");
            else
              sql.append("insert into {$table.prefix}cpo_query_group (group_id, class_id, group_type, "
                + "name) values (?,?,?,?)");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqgn.getGroupId());
            pstmt.setString(2,cqgn.getClassId());
            pstmt.setString(3,cqgn.getType());
            pstmt.setString(4,cqgn.getGroupName());
            int result = pstmt.executeUpdate();
//            OUT.debug("Inserted query group: "+cqgn.getGroupId()+" "+result);
          }
          else if (cqgn.isRemove()) {
        	  StringBuffer sqlDelQueryParam=new StringBuffer("delete from {$table.prefix}cpo_query_parameter where query_id in "
                + "(select query_id from {$table.prefix}cpo_query where group_id = ?)");
        	  StringBuffer sqlDelQuery=new StringBuffer("delete from {$table.prefix}cpo_query where group_id = ?");
        	  StringBuffer sqlDelQueryGroup=new StringBuffer("delete from {$table.prefix}cpo_query_group where group_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelQueryParam, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqgn.getGroupId());
            pstmt.execute();
            pstmt.close();
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelQuery, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqgn.getGroupId());
            pstmt.execute();
            pstmt.close();
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelQueryGroup, "{$table.prefix}", this.tablePrefix)).toString());
           pstmt.setString(1,cqgn.getGroupId());
            pstmt.execute();
          }
          else if (cqgn.isDirty()) {
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled)
              sql.append("update {$table.prefix}cpo_query_group set group_type = ?, name = ?, userid = '"+CpoUtil.username+"' where "
                + "group_id = ?");
            else
              sql.append("update {$table.prefix}cpo_query_group set group_type = ?, name = ? where "
                + "group_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqgn.getType());
            pstmt.setString(2,cqgn.getGroupName());
            pstmt.setString(3,cqgn.getGroupId());
            pstmt.executeUpdate();
          }
        }
        else
          continue;
        toBeCleaned.add(node);
        if (pstmt != null)
          pstmt.close();
      }
      iter = nodes.iterator();
      /**
       * do query text
       */
      while (iter.hasNext()) {
        Object node = iter.next();
        if (node instanceof CpoQueryTextNode) {
          CpoQueryTextNode cQTnode = (CpoQueryTextNode)node;
          // strip sql of CRs
          String sqlText = cQTnode.getSQL();
          if (sqlText == null) sqlText = "";
          else sqlText = sqlText.trim();      
          try {
            RE reStripBlankLines = new RE("\\r\\n\\s*\\r\\n");
            sqlText = reStripBlankLines.substituteAll(sqlText,"\r\n");
            RE reStripBlankLines2 = new RE("\\n\\s*\\n");
            sqlText = reStripBlankLines2.substituteAll(sqlText,"\n");
          } catch (REException ree) {
            CpoUtil.showException(ree);
          }
          if (cQTnode.isNew()) {
        	  StringBuffer insertTextId=new StringBuffer();
            if (revsEnabled)
              insertTextId.append("insert into {$table.prefix}cpo_query_text (text_id, sql_text, description,userid) "
                + "values (?,?,?,'"+CpoUtil.username+"')");
            else
              insertTextId.append("insert into {$table.prefix}cpo_query_text (text_id, sql_text, description) "
                + "values (?,?,?)");
            pstmt = conn.prepareStatement((Statics.replaceMarker(insertTextId, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cQTnode.getTextId());
            pstmt.setString(2,sqlText);
            pstmt.setString(3,cQTnode.getDesc());
            int result = pstmt.executeUpdate();
//            OUT.debug("Inserted query TEXT node: "+cQTnode.getTextId()+" "+result);
          }
          else if (cQTnode.isRemove()) {
        	  StringBuffer removeTextId=new StringBuffer("delete from {$table.prefix}cpo_query_text where text_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(removeTextId, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cQTnode.getTextId());
            pstmt.execute();
          }
          else if (cQTnode.isDirty()) {
        	  StringBuffer updateTextId=new StringBuffer();
            if (revsEnabled)
              updateTextId.append("update {$table.prefix}cpo_query_text set sql_text = ?, description = ?, userid = '"+CpoUtil.username+"' where "
                + "text_id = ?");
            else
              updateTextId.append("update {$table.prefix}cpo_query_text set sql_text = ?, description = ? where "
                + "text_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(updateTextId, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,sqlText);
            pstmt.setString(2,cQTnode.getDesc());
            pstmt.setString(3,cQTnode.getTextId());
            pstmt.executeUpdate();
          }
        }
        else
          continue;
        toBeCleaned.add(node);
        if (pstmt != null)
          pstmt.close();
      }
      iter = nodes.iterator();
      /**
       * do query node
       */
      while (iter.hasNext()) {
        Object node = iter.next();
        if (node instanceof CpoQueryNode) {
          CpoQueryNode cqn = (CpoQueryNode)node;
          if (cqn.isNew()) {
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled) 
              sql.append("insert into {$table.prefix}cpo_query (query_id, group_id, text_id, seq_no, userid) "
                + "values (?,?,?,?,'"+CpoUtil.username+"')");
            else
              sql.append("insert into {$table.prefix}cpo_query (query_id, group_id, text_id, seq_no) "
                + "values (?,?,?,?)");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqn.getQueryId());
            pstmt.setString(2,cqn.getGroupId());
            pstmt.setString(3,cqn.getTextId());
            pstmt.setInt(4,cqn.getSeqNo());
            int result = pstmt.executeUpdate();
//            OUT.debug("Inserted query node: "+cqn.getQueryId()+" "+result);
          }
          else if (cqn.isRemove()) {
        	  StringBuffer sqlDelQueryParam=new StringBuffer("delete from {$table.prefix}cpo_query_parameter where query_id = ?");
        	  StringBuffer sqlDelQuery=new StringBuffer("delete from {$table.prefix}cpo_query where query_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelQueryParam, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqn.getQueryId());
            pstmt.execute();
            pstmt.close();
            pstmt = conn.prepareStatement((Statics.replaceMarker(sqlDelQuery, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqn.getQueryId());
            pstmt.execute();
          }
          else if (cqn.isDirty()) {
            // not updating group_id ... could change later
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled)
              sql.append("update {$table.prefix}cpo_query set text_id = ?, seq_no = ?, userid = '"+CpoUtil.username+"' where query_id = ?");
            else
              sql.append("update {$table.prefix}cpo_query set text_id = ?, seq_no = ? where query_id = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqn.getTextId());
            pstmt.setInt(2,cqn.getSeqNo());
            pstmt.setString(3,cqn.getQueryId());
            pstmt.executeUpdate();
          }
        }
        else
          continue;
        toBeCleaned.add(node);
        if (pstmt != null)
          pstmt.close();
      }
      iter = nodes.iterator();
      /**
       * do query parameters
       */
      while (iter.hasNext()) {
        Object node = iter.next();
        if (node instanceof CpoQueryParameterNode) {
          CpoQueryParameterNode cqpn = (CpoQueryParameterNode)node;
          if (cqpn.isNew()) {
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled)
              sql.append("insert into {$table.prefix}cpo_query_parameter (attribute_id, query_id, seq_no, userid, param_type) "
                + "values (?,?,?,'"+CpoUtil.username+"',?)");
            else
              sql.append("insert into {$table.prefix}cpo_query_parameter (attribute_id, query_id, seq_no, param_type) "
                + "values (?,?,?,?)");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqpn.getAttributeId());
            pstmt.setString(2,cqpn.getQueryId());
            pstmt.setInt(3,cqpn.getSeqNo());
            pstmt.setString(4,cqpn.getType());
//            OUT.debug("Inserting query attribute parameter: "+cqpn.getSeqNo()+" for query node: "+cqpn.getQueryId()+" and attribute map node: "+cqpn.getAttributeId());
            int result = pstmt.executeUpdate();
//            OUT.debug("Inserted query attribute parameter: "+cqpn.getSeqNo()+" for query node: "+cqpn.getQueryId()+" and attribute map node: "+cqpn.getAttributeId()+" "+result);
          }
          else if (cqpn.isRemove()) {
        	  StringBuffer sql=new StringBuffer("delete from {$table.prefix}cpo_query_parameter where query_id = ? and seq_no = ?");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqpn.getQueryId());
            pstmt.setInt(2,cqpn.getSeqNo());
            pstmt.execute();
          }
          else if (cqpn.isDirty()) {
        	  StringBuffer sql=new StringBuffer();
            if (revsEnabled)
              sql.append("update {$table.prefix}cpo_query_parameter set attribute_id = ?, param_type = ?, userid = '"+CpoUtil.username+"' where query_id = ? and seq_no = ? ");
            else
              sql.append("update {$table.prefix}cpo_query_parameter set attribute_id = ?, param_type = ? where query_id = ? and seq_no = ? ");
            pstmt = conn.prepareStatement((Statics.replaceMarker(sql, "{$table.prefix}", this.tablePrefix)).toString());
            pstmt.setString(1,cqpn.getAttributeId());
            pstmt.setString(2,cqpn.getType());
            pstmt.setString(3,cqpn.getQueryId());
            pstmt.setInt(4,cqpn.getSeqNo());
            pstmt.executeUpdate();
            //OUT.debug("updated parameter query_id="+cqpn.getQueryId()+" seq="+cqpn.getSeqNo()+ " group_type="+cqpn.getType());
          }
        }
        else
          continue;
        toBeCleaned.add(node);
        if (pstmt != null)
          pstmt.close();
      }
      conn.commit();
      // commit completed ok - clean up objects
      Iterator toCleanIt = toBeCleaned.iterator();
      while (toCleanIt.hasNext()) {
        AbstractCpoNode node = (AbstractCpoNode)toCleanIt.next();
        node.setNew(false);
        node.setDirty(false);
        if (node.isRemove()) {
          this.removeObjectFromAllCache(node);
          node.setRemove(false);
        }
      }
    } catch (SQLException se) {
      try {
        conn.rollback();
      } catch (Exception e) {}
      throw se;
//      CpoUtil.showException(se);
//      return;
    }
    finally {
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
  }
  /**
   * cleans ALL cache elements completely out
   */
  public void removeObjectsFromAllCache() {
    classCache.clear();
    queryTextCache.clear();
    allChangedObjects.clear();
    attMapCache.clear();
    queryCache.clear();
    queryGroupCache.clear();
    queryParamCache.clear();
    queryGroupByTextCache.clear();
    classCacheById.clear();
  }
  /**
   * removes a particular object from all caches, including changed object cache
   */
  public void removeObjectFromAllCache(Object obj) {
    if (queryTextCache.contains(obj))
      queryTextCache.remove(obj);
    if (allChangedObjects.contains(obj))
      allChangedObjects.remove(obj);
    this.allChangedObjects.remove(obj);

    Enumeration classEnum = this.classCache.elements();
    while (classEnum.hasMoreElements()) {
      ArrayList classes = (ArrayList)classEnum.nextElement();
      if (classes.contains(obj))
        classes.remove(obj);
    }   
    if (this.classCache.contains(obj)) this.classCache.remove(obj);
    
    classEnum = this.attMapCache.elements();
    while (classEnum.hasMoreElements()) {
      ArrayList attMap = (ArrayList)classEnum.nextElement();
      if (attMap.contains(obj))
        attMap.remove(obj);
    }
    if (this.attMapCache.contains(obj)) this.attMapCache.remove(obj);
    
    classEnum = this.queryCache.elements();
    while (classEnum.hasMoreElements()) {
      ArrayList attMap = (ArrayList)classEnum.nextElement();
      if (attMap.contains(obj))
        attMap.remove(obj);
    }
    if (this.queryCache.contains(obj)) this.queryCache.remove(obj);
    
    classEnum = this.queryGroupCache.elements();
    while (classEnum.hasMoreElements()) {
      ArrayList attMap = (ArrayList)classEnum.nextElement();
      if (attMap.contains(obj))
        attMap.remove(obj);
    }
    if (this.queryGroupCache.contains(obj)) this.queryGroupCache.remove(obj);
    
    classEnum = this.queryParamCache.elements();
    while (classEnum.hasMoreElements()) {
      ArrayList attMap = (ArrayList)classEnum.nextElement();
      if (attMap.contains(obj))
        attMap.remove(obj);
    }
    if (this.queryParamCache.contains(obj)) this.queryParamCache.remove(obj);
    
    classEnum = this.queryGroupByTextCache.elements();
    while (classEnum.hasMoreElements()) {
      ArrayList qGroups = (ArrayList)classEnum.nextElement();
      if (qGroups.contains(obj))
        qGroups.remove(obj);
    }
    if (this.queryGroupByTextCache.contains(obj)) this.queryGroupByTextCache.remove(obj);
  }
  /**
   * get a particular querytextnode
   */
  public CpoQueryTextNode getQueryText(CpoServerNode serverNode, String textId) throws Exception {
    Iterator iter = getQueryText(serverNode).iterator();
    while (iter.hasNext()) {
      CpoQueryTextNode cQTnode = (CpoQueryTextNode)iter.next();
      if (cQTnode.getTextId().equals(textId))
        return cQTnode;
    }
    return null;
  }
  /**
   * get an arraylist of querytextnodes that match a certain string
   */
  public ArrayList getQueryTextMatches(CpoServerNode serverNode, String match) throws Exception {
    if (match.equals("")) return this.getQueryText(serverNode);
    ArrayList al = new ArrayList();
    Iterator iter = getQueryText(serverNode).iterator();
    while (iter.hasNext()) {
      CpoQueryTextNode cQTnode = (CpoQueryTextNode)iter.next();
      int index = cQTnode.toString().toLowerCase().indexOf(match.toLowerCase());
      if (index != -1)
        al.add(cQTnode);
    }
    return al;
  }
  public void clearMetaClassCache() throws Exception {
//    try {
      //Method cpoManClearMetaClassMeth = this.cpoMan.getClass().getMethod("clearMetaClass",null);
      //cpoManClearMetaClassMeth.invoke(cpoMan,null);
      this.cpoMan.clearMetaClass();
//    } catch (Exception re) {
//      throw new ProxyException("clearMetaClassCache()",re);
//    }
  }
  public void clearMetaClassCache(String className) throws Exception {
//    try {
//      Method cpoManClearMetaClassMeth = this.cpoMan.getClass().getMethod("clearMetaClass",new Class[]{String.class});
//      cpoManClearMetaClassMeth.invoke(cpoMan,new Object[]{className});
      this.cpoMan.clearMetaClass(className);
//    } catch (Exception re) {
//      throw new ProxyException("clearMetaClassCache(String)",re);
//    }
  }

  public Class getSqlTypeClass(String typeName) throws Exception {
      //Method cpoGetSqlTypeClassMeth = this.cpoMan.getClass().getMethod("getSqlTypeClass",new Class[]{String.class});
      //return (Class) cpoGetSqlTypeClassMeth.invoke(cpoMan,new Object[]{typeName});
      //return (Class) this.sqlTypeClassMeth.invoke(cpoMan,new Object[]{typeName});
      return JavaSqlTypes.getSqlTypeClass(typeName);
  }
  
  public Class getSqlTypeClass(int typeInt) throws Exception {
    //Method cpoGetSqlTypeClassMeth = this.cpoMan.getClass().getMethod("getSqlTypeClass",new Class[]{String.class});
    //return (Class) cpoGetSqlTypeClassMeth.invoke(cpoMan,new Object[]{typeName});
    //return (Class) this.sqlTypeClassMethInt.invoke(cpoMan,new Object[]{new Integer(typeInt)});
    return JavaSqlTypes.getSqlTypeClass(typeInt);
  }

  public String makeClassOuttaSql(String className, String sql) throws Exception {
    StringBuffer sbTopClass = new StringBuffer();
    StringBuffer sbClass = new StringBuffer();
    String packageName;
    if (className.lastIndexOf(".") != -1) {
      packageName = className.substring(0,className.lastIndexOf("."));
      className = className.substring(className.lastIndexOf(".")+1);
      sbTopClass.append("package "+packageName+";\n");
    }
    sbTopClass.append("import java.sql.*;\n");
    sbTopClass.append("import java.io.Serializable;\n");
    sbTopClass.append("public class "+className+" implements Serializable {\n");
    sbClass.append("  public "+className+"() {\n  }\n");
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
        pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      int columns = rsmd.getColumnCount();
      for (int i = 1 ; i <= columns ; i++) {
        String attName = makeAttFromColName(rsmd.getColumnName(i));
        //String attClassName = rsmd.getColumnClassName(i);
        String attClassName = getSqlTypeClass(rsmd.getColumnType(i)).getName();
        if ("[B".equals(attClassName)) {
            attClassName = "byte[]";
        }
        if (attName.length() > 1)
          sbClass.append("  public void set"+attName.substring(0,1).toUpperCase()+attName.substring(1)+"("+attClassName+" "+attName+") {\n");
        else
          sbClass.append("  public void set"+attName.toUpperCase()+"("+attClassName+" "+attName+") {\n");
        sbClass.append("    this."+attName+" = "+attName+";\n");
        sbClass.append("  }\n");
        if (attName.length() > 1)
          sbClass.append("  public "+attClassName+" get"+attName.substring(0,1).toUpperCase()+attName.substring(1)+"() {\n");
        else
          sbClass.append("  public "+attClassName+" get"+attName.toUpperCase()+"() {\n");
        sbClass.append("    return this."+attName+";\n");
        sbClass.append("  }\n");
        sbTopClass.append("  private "+attClassName+" "+attName+";\n");
      }
//    } catch (SQLException se) {
//      throw new ProxyException("makeClassOuttaSql",se);
    } finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    sbClass.append("}\n");
    return sbTopClass.toString()+sbClass.toString();
  }
  public String makeClassOuttaNode(CpoClassNode node) throws Exception {
    //Method meth = cpoMan.getClass().getMethod("getSqlTypeClass",new Class[]{String.class});
    String className = node.getClassName();
    StringBuffer sbTopClass = new StringBuffer();
    StringBuffer sbClass = new StringBuffer();
    String packageName;
    sbTopClass.append("/** This class auto-generated by "+this.getClass().getName()+" **/\n\n");
    if (className.lastIndexOf(".") != -1) {
      packageName = className.substring(0,className.lastIndexOf("."));
      className = className.substring(className.lastIndexOf(".")+1);
      sbTopClass.append("package "+packageName+";\n");
    }
    sbTopClass.append("import java.sql.*;\n");
    sbTopClass.append("import java.io.Serializable;\n");
    sbTopClass.append("public class "+className+" implements Serializable {\n");
    sbClass.append("  public "+className+"() {\n  }\n");
    ArrayList alAttMap = this.getAttributeMap(node);
    Iterator attMapIt = alAttMap.iterator();
    while (attMapIt.hasNext()) {
      CpoAttributeMapNode atMapNode = (CpoAttributeMapNode)attMapIt.next();
      String attName = makeAttFromColName(atMapNode.getColumnName());
      //Class attClass = (Class) this.sqlTypeClassMeth.invoke(cpoMan,new Object[]{atMapNode.getColumnType()});
      Class attClass = getSqlTypeClass(atMapNode.getColumnType());
      String attClassName = attClass.getName();
      if (attName.length() > 1)
        sbClass.append("  public void set"+attName.substring(0,1).toUpperCase()+attName.substring(1)+"("+attClassName+" "+attName+") {\n");
      else
        sbClass.append("  public void set"+attName.toUpperCase()+"("+attClassName+" "+attName+") {\n");
      sbClass.append("    this."+attName+" = "+attName+";\n");
      sbClass.append("  }\n");
      if (attName.length() > 1)
        sbClass.append("  public "+attClassName+" get"+attName.substring(0,1).toUpperCase()+attName.substring(1)+"() {\n");
      else
        sbClass.append("  public "+attClassName+" get"+attName.toUpperCase()+"() {\n");
      sbClass.append("    return this."+attName+";\n");
      sbClass.append("  }\n");
      sbTopClass.append("  private "+attClassName+" "+attName+";\n");      
    }
    sbClass.append("}\n");
    return sbTopClass.toString()+sbClass.toString();
  }
  String makeAttFromColName(String columnName) throws REException {
    columnName = columnName.toLowerCase();
    RE reFixCol = new RE("_.");
    REMatch match = null;
    while ((match = reFixCol.getMatch(columnName)) != null ) {
      columnName = columnName.substring(0,match.getStartIndex())+reFixCol.substitute(columnName,match.toString().substring(1).toUpperCase(),match.getStartIndex());
    }
    return columnName;
  }
  void generateNewAttributeMap(CpoClassNode ccn, Method[] methods) throws Exception {
    CpoAttributeLabelNode attMapParent = null;
    Enumeration enumChildren = ccn.children();
    while (enumChildren.hasMoreElements()) {
      AbstractCpoNode child = (AbstractCpoNode)enumChildren.nextElement();
      if (child instanceof CpoAttributeLabelNode)
        attMapParent = (CpoAttributeLabelNode)child;
    }
    if (attMapParent == null) throw new Exception ("Can't find the Attribute Label for this node: "+ccn);
    for (int i = 0 ; i < methods.length ; i++) {
      if (methods[i].getName().startsWith("get")) {
        Class returnClass = methods[i].getReturnType();
        String columnTypeName = "VARCHAR"; // this needs to be fixed!!!!
        String attributeName = methods[i].getName().substring(3);
        String columnName = attributeName;
        CpoAttributeMapNode camn = new CpoAttributeMapNode(attMapParent,this.getNewGuid(),
            ccn.getClassId(),columnName,attributeName, 
            columnTypeName,null, null,null,"IN");
        this.getAttributeMap(ccn).add(camn);
        camn.setNew(true);
      }
    }
  }
  void generateNewAttributeMap(CpoClassNode ccn, String sql) throws Exception {
    CpoAttributeLabelNode attMapParent = null;
    Enumeration enumChildren = ccn.children();
    while (enumChildren.hasMoreElements()) {
      AbstractCpoNode child = (AbstractCpoNode)enumChildren.nextElement();
      if (child instanceof CpoAttributeLabelNode)
        attMapParent = (CpoAttributeLabelNode)child;
    }
    if (attMapParent == null) throw new Exception ("Can't find the Attribute Label for this node: "+ccn);
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
        pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      int columns = rsmd.getColumnCount();
      for (int i = 1 ; i <= columns ; i++) {
        CpoAttributeMapNode camn = new CpoAttributeMapNode(attMapParent,this.getNewGuid(),
            ccn.getClassId(),rsmd.getColumnName(i),this.makeAttFromColName(rsmd.getColumnName(i)),
            Statics.getJavaSqlType(rsmd.getColumnType(i)),null, null,null, "IN");
        this.getAttributeMap(ccn).add(camn);
        camn.setNew(true);
      }
    } finally {
      try {
        rs.close();
      } catch (Exception e) {}
      try {
        pstmt.close();
      } catch (Exception e) {}
    }
    
  }
  public Collection executeQueryGroup(Object obj, Object objReturnType, CpoQueryGroupNode cpoQGnode, boolean persist) throws Exception {
//    try {
      if (cpoQGnode.getType().equals(Statics.CPO_TYPE_CREATE)) {
//insert
        Method meth = cpoMan.getClass().getMethod("insertObject",new Class[]{String.class,Object.class});
        meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
// retrieve from cpo, so we can verify insertion
        meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        ArrayList al = new ArrayList();
        if (resultObj != null)
          al.add(resultObj);
        return al;      
      }
      else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_DELETE)) {
        Method meth = cpoMan.getClass().getMethod("deleteObject",new Class[]{String.class,Object.class});
        meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
// retrieve from cpo, so we can verify deletion
        meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        ArrayList al = new ArrayList();
        if (resultObj != null)
          al.add(resultObj);
        return al;      
      }
      else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_LIST)) {
        Collection coll = null;
        Class c = CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass("org.synchronoss.cpo.CpoWhere");
        Method[] meth = cpoMan.getClass().getMethods();
        for (int i = 0 ; i < meth.length ; i++) {
          if (meth[i].getName().equals("retrieveObjects")) {
//            OUT.debug(meth[i]);
            Class[] paramClasses = meth[i].getParameterTypes();
//            for (int j = 0 ; j < paramClasses.length ; j++) {
//             OUT.debug("reflect loader: "+paramClasses[j].getClassLoader());
//              OUT.debug("my loader:"+c.getClassLoader());
//            }
            if (paramClasses.length == 5) {
              coll = (Collection)meth[i].invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj,objReturnType,null,null});
            }
          }
        }
//        Method methRO = cpoMan.getClass().getMethod("retrieveObjects",new Class[]{String.class,Object.class,Object.class,c,Collection.class});
        if (coll == null)
          return new ArrayList();
        return coll;
//        return (Object[])methRO.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj,obj,null,null});      
//        return null;
      }
      else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_RETRIEVE)) {
        Method meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        ArrayList al = new ArrayList();
        if (resultObj != null)
          al.add(resultObj);
        return al;
      }
      else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_UPDATE)) {
        Method meth = cpoMan.getClass().getMethod("updateObject",new Class[]{String.class,Object.class});
        meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
// retrieve from cpo, so we can verify update
        meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        ArrayList al = new ArrayList();
        if (resultObj != null)
          al.add(resultObj);
        return al;            
      }
      else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_EXIST) && persist) {
        Method meth = cpoMan.getClass().getMethod("persistObject",new Class[]{String.class,Object.class});
        meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
// retrieve from cpo, so we can verify update
        meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        ArrayList al = new ArrayList();
        if (resultObj != null)
          al.add(resultObj);
        return al;        
      }
      else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_EXIST)) {
        Method meth = cpoMan.getClass().getMethod("existsObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        ArrayList al = new ArrayList();
        if (resultObj != null)
          al.add(resultObj);
        return al;
      }
//    } catch (Exception e) {
//      e.printStackTrace();
//      throw new ProxyException("executeQueryGroup()",e);
//    }
    return new ArrayList();
  }
  public void addToAttributeMapFromSQL(CpoAttMapTableModel model, String sql) throws Exception {
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      String ignoredAttr = "";
      try {
          pstmt = conn.prepareStatement(sql);
          rs = pstmt.executeQuery();
          ResultSetMetaData rsmd = rs.getMetaData();
          int columns = rsmd.getColumnCount();
          for (int i = 1; i <= columns; i++) {
              String columnName = this.makeAttFromColName(rsmd.getColumnName(i));
              if (model.attributeExists(columnName)) ignoredAttr += columnName+" ";
              else model.addNewAttribute(rsmd.getColumnName(i), columnName, Statics.getJavaSqlType(rsmd.getColumnType(i)),null,null,null);
          }
      } finally {
          try {
              rs.close();
          } catch (Exception e) {}
          try {
              pstmt.close();
          } catch (Exception e) {}
      }
      if (ignoredAttr.length()>0) {
          String message = " Did not add attributes "+ignoredAttr+"because they already exist.";
          CpoUtil.updateStatus(message);
      }
  }
  public void toggleClassNames() {
    this.classNameToggle = !classNameToggle;
  }
  public boolean getClassNameToggle() {
    return this.classNameToggle;
  }
  public String getConnectionClassName() {
    return this.connectionClassName;
  }
}