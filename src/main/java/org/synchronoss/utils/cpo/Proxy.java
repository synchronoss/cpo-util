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
import org.synchronoss.cpo.*;
import org.synchronoss.cpo.jdbc.*;

import javax.naming.*;
import javax.sql.DataSource;
import javax.swing.tree.*;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class Proxy implements Observer {
  boolean revsEnabled = false;
  private Context ctx;
  private Properties defProps;
  private String server;
  private Connection conn;
  private Hashtable<AbstractCpoNode, List<CpoClassNode>> classCache = new Hashtable<AbstractCpoNode, List<CpoClassNode>>();
  private Hashtable<String, CpoClassNode> classCacheById = new Hashtable<String, CpoClassNode>();
  private Hashtable<CpoAttributeLabelNode, List<CpoAttributeMapNode>> attMapCache = new Hashtable<CpoAttributeLabelNode, List<CpoAttributeMapNode>>();
  private Hashtable<CpoQueryNode, List<CpoQueryParameterNode>> queryParamCache = new Hashtable<CpoQueryNode, List<CpoQueryParameterNode>>();
  private Hashtable<CpoQueryGroupNode, List<CpoQueryNode>> queryCache = new Hashtable<CpoQueryGroupNode, List<CpoQueryNode>>();
  private Hashtable<CpoQueryGroupLabelNode, List<CpoQueryGroupNode>> queryGroupCache = new Hashtable<CpoQueryGroupLabelNode, List<CpoQueryGroupNode>>();
  private Hashtable<CpoQueryTextNode, List<CpoQueryGroupNode>> queryGroupByTextCache = new Hashtable<CpoQueryTextNode, List<CpoQueryGroupNode>>();
  private List<CpoQueryTextNode> queryTextCache = new ArrayList<CpoQueryTextNode>();
  private List<AbstractCpoNode> allChangedObjects = new ArrayList<AbstractCpoNode>();
  private CpoAdapter cpoMan;
  private String[] sqlTypes;
  private CpoBrowserTree cpoTree;
  private String databaseName;
  private String tablePrefix;
  private String sqlDelimiter;
  private Properties connProps;
  private boolean classNameToggle = false;
  private Logger OUT = Logger.getLogger(this.getClass());
  private String connectionClassName = null;

  // connection based protected classes
  private HashSet<String> protectedClasses = new HashSet<String>();

  public Proxy(Properties props, String server, CpoBrowserTree cpoTree) throws Exception {
    if (server == null || props == null) throw new Exception("No Server Selected!");
    this.defProps = props;
    this.server = server;
    this.cpoTree = cpoTree;
    getConnection();
    checkForRevsEnabled();
    loadProtectedClasses();
  }
  
  public String getTablePrefix() {
	  return this.tablePrefix;
  }

  public String getSqlDelimiter() {
	  return this.sqlDelimiter;
  }

  public CpoServerNode getServerNode() {
    CpoServerNode serverNode = null;
    if (cpoTree != null && cpoTree.getModel() != null) {
      serverNode = (CpoServerNode)cpoTree.getModel().getRoot();
    }
    return serverNode;
  }

  public String getSqlDir() {
    return connProps.getProperty(Statics.PROP_JDBC_SQL_DIR + server);
  }

  public boolean isIgnoreProtected() {
    // ability to bypass protected classes
    if (CpoUtil.localProps.containsKey(Statics.PROP_JDBC_IGNORE_PROTECTED + server)) {
      if (Boolean.valueOf(CpoUtil.localProps.getProperty(Statics.PROP_JDBC_IGNORE_PROTECTED + server))) {
        return true;
      }
    }
    return false;
  }

  public String getDefaultPackageName() {
    // default to the package for the connection...if there isn't one, use the old style
    String defPack = connProps.getProperty(Statics.PROP_JDBC_DEFPACK + server);
    if (defPack == null)
      defPack = CpoUtil.localProps.getProperty(Statics.LPROP_DEFPACK);
    return defPack;
  }

  public void setDefaultPackageName(String pack) {
    connProps.setProperty(Statics.PROP_JDBC_DEFPACK + server, pack);
    CpoUtil.saveLocalProps();
  }

  public File getDefaultDir() {
    File file = null;
    try {
      // default to the dir for the connection...if there isn't one, use the old style
      if (connProps.getProperty(Statics.PROP_JDBC_DEFDIR + server) != null) {
        file = new File(connProps.getProperty(Statics.PROP_JDBC_DEFDIR + server));
      } else if (CpoUtil.localProps.getProperty(Statics.LPROP_DEFDIR) != null) {
        file = new File(CpoUtil.localProps.getProperty(Statics.LPROP_DEFDIR));
      }
    } catch (Exception e) {
      CpoUtil.showException(e);
    }
    return file;
  }

  public void setDefaultDir(File file) {
    connProps.setProperty(Statics.PROP_JDBC_DEFDIR + server, file.toString());
    CpoUtil.saveLocalProps();
  }

  void getConnection() throws Exception {
    if (CpoUtil.localProps.containsKey(Statics.PROP_JDBC_DRIVER+server) ||
        CpoUtil.localProps.containsKey(Statics.PROP_WLSURL+server))
      connProps = CpoUtil.localProps;
    else
      connProps = defProps;
    
    this.tablePrefix = connProps.getProperty(Statics.PROP_JDBC_TABLE_PREFIX+server);
    this.sqlDelimiter = connProps.getProperty(Statics.PROP_JDBC_SQL_STATEMENT_DELIMITER+server);

    Collection<String> coll = JavaSqlTypes.getSqlTypes();
    sqlTypes = new String[coll.size()];
    coll.toArray(sqlTypes);

    if (CpoUtil.props.getProperty(Statics.PROP_JDBC_URL+server) == null
        && CpoUtil.localProps.getProperty(Statics.PROP_JDBC_URL+server) == null) {
      getInitialContext();
      conn = ((DataSource)ctx.lookup(connProps.getProperty(Statics.PROP_WLSCONNPOOL+server))).getConnection();
      conn.setAutoCommit(false);
      databaseName = conn.getMetaData().getURL();

      // Replace the dynamic link with a hard link.
      cpoMan = new JdbcCpoAdapter(new JdbcDataSourceInfo(connProps.getProperty(Statics.PROP_WLSCONNPOOL+server),getTablePrefix()));
    } else {
      Class<?> driverClass;
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
    }
  }
  
  private void getInitialContext() throws Exception {
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
  }

  private void checkForRevsEnabled() {
    StringBuilder sql = new StringBuilder("select distinct userid from ");
    sql.append(tablePrefix);
    sql.append("cpo_class_rev");
    
    PreparedStatement pstmt = null;
    try {
      pstmt = conn.prepareStatement(sql.toString());
      pstmt.executeQuery();
      revsEnabled = true;
    } catch (Exception e) {
    } finally {
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {}
    }
  }

  private void loadProtectedClasses() {
    String url = connProps.getProperty("cpoutil.protectedClasses." + server);
    if (OUT.isDebugEnabled()) OUT.debug("Connection Protected Classes Url: " + url);

    if (url != null) {
      Set<String> protClasses = CpoUtil.loadProtectedClassesFromUrl(url);
      if (protClasses != null) {
        // updated from url, use them and save them locally
        OUT.debug("Connected to url, loading classes");
        protectedClasses.addAll(protClasses);
        saveProtectedClasses();
      } else {
        // if the set was null, it couldn't be read, so use the local copy
        OUT.debug("Couldn't connect to url, so loading local copy");
        try {
          File protFile = new File(System.getProperties().getProperty("user.home") + File.separator + CpoUtil.PROTECTED_CLASS_FILE + "." + server);
          BufferedReader br = new BufferedReader(new FileReader(protFile));
          String line;
          while ((line = br.readLine()) != null) {
            protectedClasses.add(line);
          }
          br.close();
        } catch (IOException ioe) {
          CpoUtil.showException(ioe);
        }
      }
    }

    // add globally protected classes
    protectedClasses.addAll(CpoUtil.globallyProtectedClasses);
  }

  protected void saveProtectedClasses() {

    if (protectedClasses == null)
      return;

    try {
      File protFile = new File(System.getProperties().getProperty("user.home") + File.separator + CpoUtil.PROTECTED_CLASS_FILE + "." + server);
      PrintWriter pw = new PrintWriter(protFile);
      for (String s : protectedClasses) {
        pw.println(s);
      }
      pw.flush();
      pw.close();
    } catch (IOException ioe) {
      CpoUtil.showException(ioe);
    }
  }

  public String getDatabaseName() {
    return this.databaseName;
  }

  // just in case someone decides to change toString()
  public String getServer() {
    return server;
  }

  @Override
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
    return classCacheById.get(classId);
  }

  public Hashtable<String, CpoClassNode> getClassesById() {
    return this.classCacheById;
  }

  public List<CpoClassNode> getClasses(AbstractCpoNode parent) throws Exception {
    if (this.classCache.containsKey(parent)) {
      List<CpoClassNode> al = this.classCache.get(parent);
      Collections.sort(al, new CpoClassNodeComparator());
      return al;
    }
    List<CpoClassNode> al = new ArrayList<CpoClassNode>();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      StringBuilder sql = new StringBuilder();
      if (revsEnabled) {
        sql.append("select class_id, name, userid, createdate from ");
        sql.append(tablePrefix);
        sql.append("cpo_class order by upper(name)");
      } else {
        sql.append("select class_id, name from ");
        sql.append(tablePrefix);
        sql.append("cpo_class order by upper(name)");
      }
      pstmt = conn.prepareStatement(sql.toString());
      rs = pstmt.executeQuery();
      while (rs.next()) {
        CpoClassNode cpoClassNode = new CpoClassNode(rs.getString("name"),rs.getString("class_id"),parent);
        if (revsEnabled) {
          cpoClassNode.setUserName(rs.getString("userid"));
          cpoClassNode.setCreateDate(rs.getTimestamp("createdate"));
        }
        cpoClassNode.setProtected(this.isClassProtected(cpoClassNode.getClassName()));
        al.add(cpoClassNode);
        classCacheById.put(cpoClassNode.getClassId(),cpoClassNode);
      }
      Collections.sort(al, new CpoClassNodeComparator());
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        // ignore
      }
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {
        // ignore
      }
    }
    this.classCache.put(parent,al);
    return al;
  }

  public CpoQueryTextLabelNode getCpoQueryTextLabelNode(CpoServerNode serverNode) {
    Enumeration<AbstractCpoNode> serverNodeEnum = serverNode.children();
    while (serverNodeEnum.hasMoreElements()) {
      AbstractCpoNode node = serverNodeEnum.nextElement();
      if (node instanceof CpoQueryTextLabelNode) 
        return (CpoQueryTextLabelNode)node;
    }
    return null;
  }
  
  public List<CpoQueryTextNode> getQueryText(CpoServerNode cpoServer) throws Exception {
    if (this.queryTextCache.size() != 0)
      return this.queryTextCache;
    CpoQueryTextLabelNode parent = this.getCpoQueryTextLabelNode(cpoServer);
    List<CpoQueryTextNode> al = new ArrayList<CpoQueryTextNode>();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      StringBuilder sql = new StringBuilder();
      if (revsEnabled) {
        sql.append("select text.text_id, text.sql_text, text.description, (select count(*) from ");
        sql.append(tablePrefix);
        sql.append("cpo_query where text_id = text.text_id) as usagecount, text.userid, text.createdate from ");
        sql.append(tablePrefix);
        sql.append("cpo_query_text text order by text.description");
      } else {
        sql.append("select text.text_id, text.sql_text, text.description, count(query.text_id) as usagecount from ");
        sql.append(tablePrefix);
        sql.append("cpo_query_text text, ");
        sql.append(tablePrefix);
        sql.append("cpo_query query WHERE text.text_id = query.text_id GROUP BY text.text_id, text.sql_text, text.description order by text.description");
      }
      pstmt = conn.prepareStatement(sql.toString());
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
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {}
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {}
    }
    queryTextCache = al;
    return al;
  }
  
  public List<CpoQueryGroupNode> getQueryGroups(CpoQueryTextNode textNode) throws Exception {
    if (this.queryGroupByTextCache.containsKey(textNode)) {
      return this.queryGroupByTextCache.get(textNode);
    }
    List<CpoQueryGroupNode> al = new ArrayList<CpoQueryGroupNode>();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuilder sql = new StringBuilder();
      if (revsEnabled) {
        sql.append("select group_id, class_id, group_type, name, userid, createdate from ");
        sql.append(tablePrefix);
        sql.append("cpo_query_group where group_id in ");
        sql.append("(select group_id from ");
        sql.append(tablePrefix);
        sql.append("cpo_query where text_id = ?) order by name, group_type, group_id");
      } else {
        sql.append("select group_id, class_id, group_type, name from ");
        sql.append(tablePrefix);
        sql.append("cpo_query_group where group_id in ");
        sql.append("(select group_id from ");
        sql.append(tablePrefix);
        sql.append("cpo_query where text_id = ?) order by name, group_type, group_id");
      }
      pstmt = conn.prepareStatement(sql.toString());
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
        if (rs != null) rs.close();
      } catch (Exception e) {}
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {}
    }
    this.queryGroupByTextCache.put(textNode,al);
    return al;
  }

  public List<CpoQueryGroupNode> getQueryGroups(CpoQueryGroupLabelNode parent) throws Exception {
    if (this.queryGroupCache.containsKey(parent)) {
      return this.queryGroupCache.get(parent);
    }
    List<CpoQueryGroupNode> al = new ArrayList<CpoQueryGroupNode>();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuilder sql = new StringBuilder();
      if (revsEnabled) {
    	  sql.append("select group_id, class_id, group_type, name, userid, createdate from ");
        sql.append(tablePrefix);
        sql.append("cpo_query_group where class_id = ? order by name, group_type, group_id");
      } else {
    	  sql.append("select group_id, class_id, group_type, name from ");
        sql.append(tablePrefix);
        sql.append("cpo_query_group where class_id = ? order by name, group_type, group_id");
      }
      pstmt = conn.prepareStatement(sql.toString());
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
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {}
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {}
    }
    this.queryGroupCache.put(parent,al);
    return al;
  }

  public CpoQueryGroupLabelNode getQueryGroupLabelNode(CpoClassNode classNode) {
    Enumeration<AbstractCpoNode> classEnum = classNode.children();
    while (classEnum.hasMoreElements()) {
      AbstractCpoNode node = classEnum.nextElement();
      if (node instanceof CpoQueryGroupLabelNode) 
        return (CpoQueryGroupLabelNode)node;
    }
    return null;
  }

  public CpoQueryGroupNode getQueryGroupNode(CpoClassNode classNode, String name, String type) throws Exception {
    CpoQueryGroupLabelNode cqgl = getQueryGroupLabelNode(classNode);
    for (CpoQueryGroupNode node : getQueryGroups(cqgl)) {
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
  public List<CpoQueryNode> getQueries(CpoQueryGroupNode parent) throws Exception {
    if (this.queryCache.containsKey(parent)) {
      return this.queryCache.get(parent);
    }
    List<CpoQueryNode> al = new ArrayList<CpoQueryNode>();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuilder sql = new StringBuilder();
      if (revsEnabled) {
        sql.append("select q.query_id, q.group_id, q.text_id, q.seq_no, q.userid, q.createdate from ");
        sql.append(tablePrefix);
        sql.append("cpo_query q where q.group_id = ? order by q.seq_no");
      } else {
        sql.append("select q.query_id, q.group_id, q.text_id, q.seq_no from ");
        sql.append(tablePrefix);
        sql.append("cpo_query q where q.group_id = ? order by q.seq_no");
      }
      pstmt = conn.prepareStatement(sql.toString());
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
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {}
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {}
    }
    this.queryCache.put(parent,al);
    return al;
  }

  /**
   * returns CpoQueryParameterNode(s)
   */
  public List<CpoQueryParameterNode> getQueryParameters(CpoQueryNode qNode) throws Exception {
    if (this.queryParamCache.containsKey(qNode)) {
      return this.queryParamCache.get(qNode);
    }    
    List<CpoQueryParameterNode> al = new ArrayList<CpoQueryParameterNode>();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuilder sql = new StringBuilder();
      if (revsEnabled) {
        sql.append("select qp.attribute_id, qp.query_id, qp.seq_no, qp.param_type, qp.userid, qp.createdate from ");
        sql.append(tablePrefix);
        sql.append("cpo_query_parameter qp where query_id = ? order by qp.seq_no");
      } else {
        sql.append("select qp.attribute_id, qp.query_id, qp.seq_no, qp.param_type from ");
        sql.append(tablePrefix);
        sql.append("cpo_query_parameter qp where query_id = ? order by qp.seq_no");
      }
      pstmt = conn.prepareStatement(sql.toString());
      pstmt.setString(1,qNode.getQueryId());
      rs = pstmt.executeQuery();
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
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {}
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {}
    }
    this.queryParamCache.put(qNode,al);
    return al;
  }
  
  public List<CpoAttributeMapNode> getAttributeMap(CpoClassNode cpoClassNode) throws Exception {
    Enumeration<AbstractCpoNode> classEnum = cpoClassNode.children();
    while (classEnum.hasMoreElements()) {
      AbstractCpoNode node = classEnum.nextElement();
      if (node instanceof CpoAttributeLabelNode)
        return getAttributeMap((CpoAttributeLabelNode)node);
    }
    return null;
  }
  
  public CpoAttributeMapNode getAttributeMap(CpoClassNode cpoClassNode, String attributeId) throws Exception {
    Enumeration<AbstractCpoNode> classEnum = cpoClassNode.children();
    while (classEnum.hasMoreElements()) {
      AbstractCpoNode node = classEnum.nextElement();
      if (node instanceof CpoAttributeLabelNode)
        return getAttributeMap((CpoAttributeLabelNode)node,attributeId);
    }
    return null;
  }
  
  public CpoAttributeMapNode getAttributeMap(CpoServerNode cpoServerNode, String attributeId) throws Exception {
    for (CpoClassNode cpoClassNode : getClasses(cpoServerNode)) {
      CpoAttributeMapNode attMapNode = getAttributeMap(cpoClassNode,attributeId);
      if (attMapNode != null)
        return attMapNode;
    }
    return null;
  }
  
  public CpoAttributeMapNode getAttributeMap(CpoAttributeLabelNode cpoAttLabNode, String attributeId) throws Exception {
    for (CpoAttributeMapNode camnTmp : getAttributeMap(cpoAttLabNode)) {
      if (camnTmp.getAttributeId().equals(attributeId)) {
        return camnTmp;
      }
    }
    return null;
  }
  
  /**
   * returns CpoAttributeMapNode(s)
   */
  public List<CpoAttributeMapNode> getAttributeMap(CpoAttributeLabelNode cpoAttLabNode) throws Exception {
    if (this.attMapCache.containsKey(cpoAttLabNode)) {
      return this.attMapCache.get(cpoAttLabNode);
    }
    List<CpoAttributeMapNode> al = new ArrayList<CpoAttributeMapNode>();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
    	StringBuilder sql = new StringBuilder();
      if (revsEnabled) {
        sql.append("select am.attribute_id, am.class_id, am.column_name, am.attribute, am.column_type, ");
        sql.append("am.transform_class, am.db_table, am.db_column, am.userid, am.createdate from ");
        sql.append(tablePrefix);
        sql.append("cpo_attribute_map am where am.class_id = ? order by am.attribute");
      } else {
        sql.append("select am.attribute_id, am.class_id, am.column_name, am.attribute, am.column_type, ");
        sql.append("am.transform_class, am.db_table, am.db_column from ");
        sql.append(tablePrefix);
        sql.append("cpo_attribute_map am where am.class_id = ? order by am.attribute");
      }
      pstmt = conn.prepareStatement(sql.toString());
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
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {}
      try {
        if (pstmt != null) pstmt.close();
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
    	StringBuilder sql = new StringBuilder("select count(*) from ");
      sql.append(tablePrefix);
      sql.append("cpo_query where text_id = ?");
      pstmt = conn.prepareStatement(sql.toString());
      pstmt.setString(1,textId);
      rs = pstmt.executeQuery();
      rs.next();
      result = rs.getInt(1);
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {}
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {}
    }
    return result;
  }

  /**
   * lets try to close this oracle connection b4 we take a hike
   */
  @Override
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
      } else if (can.isDirty() && !allChangedObjects.contains(can)) {
        allChangedObjects.add(can);
      } else if (can.isNew() && !allChangedObjects.contains(can)) {
        allChangedObjects.add(can);
      } else if (can.isRemove() && !allChangedObjects.contains(can)) {
        allChangedObjects.add(can);
      } else if (!can.isDirty() && !can.isNew() && !can.isRemove() && allChangedObjects.contains(can)) {
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
            } else if (!parent.isLeaf()) {
              //OUT.debug ("Notifying non-leaf parent of node (marked deleted) change");
              ((DefaultTreeModel)this.cpoTree.getModel()).nodesChanged(parent,new int[]{index});
            } else {
              //OUT.debug ("Notifying a leaf parent of node (marked deleted) change");
              ((DefaultTreeModel)this.cpoTree.getModel()).nodesChanged(parent,new int[]{index});
            }
          } else if (((AbstractCpoNode)obs).isDirty()) {
            //OUT.debug ("Notifying parent of node change (dirty and all else)");
            ((DefaultTreeModel)this.cpoTree.getModel()).nodesChanged(parent,new int[]{index});
          } else if (((AbstractCpoNode)obs).isNew()) {
            //OUT.debug ("Notifying parent ("+parent+") of node insertion ("+obj+")");
            ((DefaultTreeModel)this.cpoTree.getModel()).nodesWereInserted(parent,new int[]{index});
          }
        }  else if (obs == obj) {
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
    Enumeration<? extends AbstractCpoNode> childEnum = can.children();
    while (childEnum != null && childEnum.hasMoreElements()) {
      AbstractCpoNode canChild = childEnum.nextElement();
      canChild.setRemove(true);
    }
  }

  /**
   * get a list of all changed objects
   */
  public List<AbstractCpoNode> getAllChangedObjects() {
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
  public void saveNodes(List<AbstractCpoNode> nodes) throws Exception {
    PreparedStatement pstmt = null;
    try {
      /**
       * do class nodes first
       */
      List<AbstractCpoNode> toBeCleaned = new ArrayList<AbstractCpoNode>();
      for (AbstractCpoNode node : nodes) {
        if (node instanceof CpoClassNode) {
          CpoClassNode ccn = (CpoClassNode)node;
          if (ccn.isNew()) {
        	  StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_class (class_id, name, userid) ");
              sql.append("values (?,?,'");
              sql.append(CpoUtil.username);
              sql.append("')");
            } else {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_class (class_id, name) values (?,?)");
            }
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.setString(2,ccn.getClassName());
            pstmt.executeUpdate();
          } else if (ccn.isRemove()) {
        	  StringBuilder sqlDelQueryParam=new StringBuilder("delete from ");
            sqlDelQueryParam.append(tablePrefix);
            sqlDelQueryParam.append("cpo_query_parameter where query_id in ");
            sqlDelQueryParam.append("(select query_id from ");
            sqlDelQueryParam.append(tablePrefix);
            sqlDelQueryParam.append("cpo_query where group_id in ");
            sqlDelQueryParam.append("(select group_id from ");
            sqlDelQueryParam.append(tablePrefix);
            sqlDelQueryParam.append("cpo_query_group where class_id = ?))");
            
        	  StringBuilder sqlDelAttMap=new StringBuilder("delete from ");
        	  sqlDelAttMap.append(tablePrefix);
        	  sqlDelAttMap.append("cpo_attribute_map where class_id = ?");
            
        	  StringBuilder sqlDelQuery=new StringBuilder("delete from ");
        	  sqlDelQuery.append(tablePrefix);
        	  sqlDelQuery.append("cpo_query where group_id in ");
            sqlDelQuery.append("(select group_id from ");
            sqlDelQuery.append(tablePrefix);
            sqlDelQuery.append("cpo_query_group where class_id = ?)");

        	  StringBuilder sqlDelQueryGroup=new StringBuilder("delete from ");
            sqlDelQueryGroup.append(tablePrefix);
            sqlDelQueryGroup.append("cpo_query_group where class_id = ?");

        	  StringBuilder sqlDelClass=new StringBuilder("delete from ");
            sqlDelClass.append(tablePrefix);
            sqlDelClass.append("cpo_class where class_id = ?");
		
            pstmt = conn.prepareStatement(sqlDelQueryParam.toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
            pstmt.close();
            
            pstmt = conn.prepareStatement(sqlDelAttMap.toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
            pstmt.close();
            
            pstmt = conn.prepareStatement(sqlDelQuery.toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
            pstmt.close();
            
            pstmt = conn.prepareStatement(sqlDelQueryGroup.toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
            pstmt.close();
            
            pstmt = conn.prepareStatement(sqlDelClass.toString());
            pstmt.setString(1,ccn.getClassId());
            pstmt.execute();
          } else if (ccn.isDirty()) {
        	  StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_class set name = ?, userid = '");
              sql.append(CpoUtil.username);
              sql.append("' where class_id = ?");
            } else {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_class set name = ? where class_id = ?");
            }
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1,ccn.getClassName());
            pstmt.setString(2,ccn.getClassId());
            pstmt.executeUpdate();
          }
        } else
          continue;
        toBeCleaned.add(node);
        if (pstmt != null)
          pstmt.close();
      }

      /**
       * do attribute maps second
       */
      for (AbstractCpoNode node : nodes) {
        if (node instanceof CpoAttributeMapNode) {
          CpoAttributeMapNode camn = (CpoAttributeMapNode)node;
          if (camn.isNew()) {
        	  StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_attribute_map (attribute_id, class_id, ");
              sql.append("column_name, attribute, column_type, db_table, db_column, userid, transform_class) ");
              sql.append("values (?,?,?,?,?,?,?,'");
              sql.append(CpoUtil.username);
              sql.append("',?)");
            } else {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_attribute_map (attribute_id, class_id, ");
              sql.append("column_name, attribute, column_type, db_table, db_column,transform_class) ");
              sql.append("values (?,?,?,?,?,?,?,?)");
            }
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1,camn.getAttributeId());
            pstmt.setString(2,camn.getClassId());
            pstmt.setString(3,camn.getColumnName());
            pstmt.setString(4,camn.getAttribute());
            pstmt.setString(5,camn.getColumnType());
            pstmt.setString(6,camn.getDbTable());
            pstmt.setString(7,camn.getDbColumn());
            pstmt.setString(8,camn.getTransformClass());
            pstmt.executeUpdate();
          } else if (camn.isRemove()) {
            StringBuilder sql = new StringBuilder("delete from ");
            sql.append(tablePrefix);
            sql.append("cpo_attribute_map where attribute_id = ?");
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1,camn.getAttributeId());
            pstmt.execute();
          } else if (camn.isDirty()) {
            //  update everything except class_id - might want to change this later...
            StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_attribute_map set ");
              sql.append("column_name = ?, attribute = ?, column_type = ?, db_table = ?, ");
              sql.append("db_column = ?, userid = '");
              sql.append(CpoUtil.username);
              sql.append("', transform_class=? where attribute_id = ?");
            } else {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_attribute_map set ");
              sql.append("column_name = ?, attribute = ?, column_type = ?, db_table = ?, ");
              sql.append("db_column = ?, transform_class=? where attribute_id = ?");
            }
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1,camn.getColumnName());
            pstmt.setString(2,camn.getAttribute());
            pstmt.setString(3,camn.getColumnType());
            pstmt.setString(4,camn.getDbTable());
            pstmt.setString(5,camn.getDbColumn());
            pstmt.setString(6,camn.getTransformClass());
            pstmt.setString(7,camn.getAttributeId());
            pstmt.executeUpdate();
          }
        } else
          continue;
        toBeCleaned.add(node);
        if (pstmt != null)
          pstmt.close();
      }
      /**
       * do query groups
       */
      for (AbstractCpoNode node : nodes) {
        if (node instanceof CpoQueryGroupNode) {
          CpoQueryGroupNode cqgn = (CpoQueryGroupNode)node;
          if (cqgn.isNew()) {
        	  StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_query_group (group_id, class_id, group_type, ");
              sql.append("name,userid) values (?,?,?,?,'");
              sql.append(CpoUtil.username);
              sql.append("')");
            } else {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_query_group (group_id, class_id, group_type,name) values (?,?,?,?)");
            }
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1,cqgn.getGroupId());
            pstmt.setString(2,cqgn.getClassId());
            pstmt.setString(3,cqgn.getType());
            pstmt.setString(4,cqgn.getGroupName());
            pstmt.executeUpdate();
          } else if (cqgn.isRemove()) {
            StringBuilder sqlDelQueryParam = new StringBuilder("delete from ");
            sqlDelQueryParam.append(tablePrefix);
            sqlDelQueryParam.append("cpo_query_parameter where query_id in ");
            sqlDelQueryParam.append("(select query_id from ");
            sqlDelQueryParam.append(tablePrefix);
            sqlDelQueryParam.append("cpo_query where group_id = ?)");
                        
            StringBuilder sqlDelQuery = new StringBuilder("delete from ");
            sqlDelQuery.append(tablePrefix);
            sqlDelQuery.append("cpo_query where group_id = ?");
                        
            StringBuilder sqlDelQueryGroup = new StringBuilder("delete from ");
            sqlDelQueryGroup.append(tablePrefix);
            sqlDelQueryGroup.append("cpo_query_group where group_id = ?");
                        
            pstmt = conn.prepareStatement(sqlDelQueryParam.toString());
            pstmt.setString(1,cqgn.getGroupId());
            pstmt.execute();
            pstmt.close();
            
            pstmt = conn.prepareStatement(sqlDelQuery.toString());
            pstmt.setString(1,cqgn.getGroupId());
            pstmt.execute();
            pstmt.close();
            
            pstmt = conn.prepareStatement(sqlDelQueryGroup.toString());
            pstmt.setString(1,cqgn.getGroupId());
            pstmt.execute();
          } else if (cqgn.isDirty()) {
            StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_query_group set group_type = ?, name = ?, userid = '");
              sql.append(CpoUtil.username);
              sql.append("' where group_id = ?");
            } else {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_query_group set group_type = ?, name = ? where group_id = ?");
            }
            pstmt = conn.prepareStatement(sql.toString());
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
      /**
       * do query text
       */
      for (AbstractCpoNode node : nodes) {
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
        	  StringBuilder insertTextId=new StringBuilder();
            if (revsEnabled) {
              insertTextId.append("insert into ");
              insertTextId.append(tablePrefix);
              insertTextId.append("cpo_query_text (text_id, sql_text, description,userid) ");
              insertTextId.append("values (?,?,?,'");
              insertTextId.append(CpoUtil.username);
              insertTextId.append("')");
            } else {
              insertTextId.append("insert into ");
              insertTextId.append(tablePrefix);
              insertTextId.append("cpo_query_text (text_id, sql_text, description) values (?,?,?)");
            }
            pstmt = conn.prepareStatement(insertTextId.toString());
            pstmt.setString(1,cQTnode.getTextId());
            pstmt.setString(2,sqlText);
            pstmt.setString(3,cQTnode.getDesc());
            pstmt.executeUpdate();
          } else if (cQTnode.isRemove()) {
            StringBuilder removeTextId=new StringBuilder("delete from ");
            removeTextId.append(tablePrefix);
            removeTextId.append("cpo_query_text where text_id = ?");
            pstmt = conn.prepareStatement(removeTextId.toString());
            pstmt.setString(1,cQTnode.getTextId());
            pstmt.execute();
          } else if (cQTnode.isDirty()) {
        	  StringBuilder updateTextId=new StringBuilder();
            if (revsEnabled) {
              updateTextId.append("update ");
              updateTextId.append(tablePrefix);
              updateTextId.append("cpo_query_text set sql_text = ?, description = ?, userid = '");
              updateTextId.append(CpoUtil.username);
              updateTextId.append("' where text_id = ?");
            } else {
              updateTextId.append("update ");
              updateTextId.append(tablePrefix);
              updateTextId.append("cpo_query_text set sql_text = ?, description = ? where text_id = ?");
            }
            pstmt = conn.prepareStatement(updateTextId.toString());
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
      /**
       * do query node
       */
      for (AbstractCpoNode node : nodes) {
        if (node instanceof CpoQueryNode) {
          CpoQueryNode cqn = (CpoQueryNode)node;
          if (cqn.isNew()) {
        	  StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_query (query_id, group_id, text_id, seq_no, userid) ");
              sql.append("values (?,?,?,?,'");
              sql.append(CpoUtil.username);
              sql.append("')");
            } else {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_query (query_id, group_id, text_id, seq_no) ");
              sql.append("values (?,?,?,?)");
            }
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1,cqn.getQueryId());
            pstmt.setString(2,cqn.getGroupId());
            pstmt.setString(3,cqn.getTextId());
            pstmt.setInt(4,cqn.getSeqNo());
            pstmt.executeUpdate();
          } else if (cqn.isRemove()) {
            StringBuilder sqlDelQueryParam=new StringBuilder("delete from ");
            sqlDelQueryParam.append(tablePrefix);
            sqlDelQueryParam.append("cpo_query_parameter where query_id = ?");
                    	  
            StringBuilder sqlDelQuery=new StringBuilder("delete from ");
            sqlDelQuery.append(tablePrefix);
            sqlDelQuery.append("cpo_query where query_id = ?");
                
            pstmt = conn.prepareStatement(sqlDelQueryParam.toString());
            pstmt.setString(1,cqn.getQueryId());
            pstmt.execute();
            pstmt.close();
                        
            pstmt = conn.prepareStatement(sqlDelQuery.toString());
            pstmt.setString(1,cqn.getQueryId());
            pstmt.execute();
          } else if (cqn.isDirty()) {
            // not updating group_id ... could change later
        	  StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_query set text_id = ?, seq_no = ?, userid = '");
              sql.append(CpoUtil.username);
              sql.append("' where query_id = ?");
            } else {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_query set text_id = ?, seq_no = ? where query_id = ?");
        		}
            pstmt = conn.prepareStatement(sql.toString());
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
      /**
       * do query parameters
       */
      for (AbstractCpoNode node : nodes) {
        if (node instanceof CpoQueryParameterNode) {
          CpoQueryParameterNode cqpn = (CpoQueryParameterNode)node;
          if (cqpn.isNew()) {
        	  StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_query_parameter (attribute_id, query_id, seq_no, userid, param_type) ");
              sql.append("values (?,?,?,'");
              sql.append(CpoUtil.username);
              sql.append("',?)");
            } else {
              sql.append("insert into ");
              sql.append(tablePrefix);
              sql.append("cpo_query_parameter (attribute_id, query_id, seq_no, param_type) ");
              sql.append("values (?,?,?,?)");
            }
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1,cqpn.getAttributeId());
            pstmt.setString(2,cqpn.getQueryId());
            pstmt.setInt(3,cqpn.getSeqNo());
            pstmt.setString(4,cqpn.getType());
//            OUT.debug("Inserting query attribute parameter: "+cqpn.getSeqNo()+" for query node: "+cqpn.getQueryId()+" and attribute map node: "+cqpn.getAttributeId());
            pstmt.executeUpdate();
//            OUT.debug("Inserted query attribute parameter: "+cqpn.getSeqNo()+" for query node: "+cqpn.getQueryId()+" and attribute map node: "+cqpn.getAttributeId()+" "+result);
          } else if (cqpn.isRemove()) {
              StringBuilder sql = new StringBuilder("delete from ");
              sql.append(tablePrefix);
              sql.append("cpo_query_parameter where query_id = ? and seq_no = ?");
              pstmt = conn.prepareStatement(sql.toString());
              pstmt.setString(1,cqpn.getQueryId());
              pstmt.setInt(2,cqpn.getSeqNo());
              pstmt.execute();
          } else if (cqpn.isDirty()) {
        	  StringBuilder sql = new StringBuilder();
            if (revsEnabled) {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_query_parameter set attribute_id = ?, param_type = ?, userid = '");
              sql.append(CpoUtil.username);
              sql.append("' where query_id = ? and seq_no = ? ");
            } else {
              sql.append("update ");
              sql.append(tablePrefix);
              sql.append("cpo_query_parameter set attribute_id = ?, param_type = ? where query_id = ? and seq_no = ? ");
            }
            pstmt = conn.prepareStatement(sql.toString());
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

      for (AbstractCpoNode node : toBeCleaned) {
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
      } catch (Exception e) {
      }
      throw se;
    } finally {
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
  public void removeObjectFromAllCache(AbstractCpoNode obj) {
    if (queryTextCache.contains(obj))
      queryTextCache.remove(obj);
    if (allChangedObjects.contains(obj))
      allChangedObjects.remove(obj);
    this.allChangedObjects.remove(obj);

    Enumeration<List<CpoClassNode>> classEnum = this.classCache.elements();
    while (classEnum.hasMoreElements()) {
      List<CpoClassNode> classes = classEnum.nextElement();
      if (classes.contains(obj))
        classes.remove(obj);
    }   
    if (this.classCache.contains(obj)) this.classCache.remove(obj);
    
    Enumeration<List<CpoAttributeMapNode>>attMapEnum = this.attMapCache.elements();
    while (attMapEnum.hasMoreElements()) {
      List<CpoAttributeMapNode> attMap = attMapEnum.nextElement();
      if (attMap.contains(obj))
        attMap.remove(obj);
    }
    if (this.attMapCache.contains(obj)) this.attMapCache.remove(obj);
    
    Enumeration<List<CpoQueryNode>> queryEnum = this.queryCache.elements();
    while (queryEnum.hasMoreElements()) {
      List<CpoQueryNode> attMap = queryEnum.nextElement();
      if (attMap.contains(obj))
        attMap.remove(obj);
    }
    if (this.queryCache.contains(obj)) this.queryCache.remove(obj);
    
    Enumeration<List<CpoQueryGroupNode>> groupEnum = this.queryGroupCache.elements();
    while (groupEnum.hasMoreElements()) {
      List<CpoQueryGroupNode> group = groupEnum.nextElement();
      if (group.contains(obj))
        group.remove(obj);
    }
    if (this.queryGroupCache.contains(obj)) this.queryGroupCache.remove(obj);
    
    Enumeration<List<CpoQueryParameterNode>> paramEnum = this.queryParamCache.elements();
    while (paramEnum.hasMoreElements()) {
      List<CpoQueryParameterNode> params = paramEnum.nextElement();
      if (params.contains(obj))
        params.remove(obj);
    }
    if (this.queryParamCache.contains(obj)) this.queryParamCache.remove(obj);
    
    Enumeration<List<CpoQueryGroupNode>> qgEnum = this.queryGroupByTextCache.elements();
    while (classEnum.hasMoreElements()) {
      List<CpoQueryGroupNode> qGroups = qgEnum.nextElement();
      if (qGroups.contains(obj))
        qGroups.remove(obj);
    }
    if (this.queryGroupByTextCache.contains(obj)) this.queryGroupByTextCache.remove(obj);
  }

  /**
   * get a particular querytextnode
   */
  public CpoQueryTextNode getQueryText(CpoServerNode serverNode, String textId) throws Exception {
    for (CpoQueryTextNode cQTnode : getQueryText(serverNode)) {
      if (cQTnode.getTextId().equals(textId))
        return cQTnode;
    }
    return null;
  }

  /**
   * get an arraylist of querytextnodes that match a certain string
   */
  public List<CpoQueryTextNode> getQueryTextMatches(CpoServerNode serverNode, String match) throws Exception {
    if (match.equals("")) return this.getQueryText(serverNode);

    List<CpoQueryTextNode> al = new ArrayList<CpoQueryTextNode>();
    for (CpoQueryTextNode cQTnode : getQueryText(serverNode)) {
      int index = cQTnode.toString().toLowerCase().indexOf(match.toLowerCase());
      if (index != -1)
        al.add(cQTnode);
    }
    return al;
  }

  public void clearMetaClassCache() throws Exception {
    this.cpoMan.clearMetaClass();
  }

  public void clearMetaClassCache(String className) throws Exception {
    this.cpoMan.clearMetaClass(className);
  }

  public Class<?> getSqlTypeClass(String typeName) throws Exception {
    return JavaSqlTypes.getSqlTypeClass(typeName);
  }
  
  public Class<?> getSqlTypeClass(int typeInt) throws Exception {
    return JavaSqlTypes.getSqlTypeClass(typeInt);
  }

  public String makeClassOuttaSql(String className, String sql) throws Exception {

    TreeMap<String, String> attributes = new TreeMap<String, String>();
    TreeMap<String, Class> attClasses = new TreeMap<String, Class>();

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      int columns = rsmd.getColumnCount();
      for (int i = 1 ; i <= columns ; i++) {
        String attName = makeAttFromColName(rsmd.getColumnName(i));
        OUT.debug("Column Type = "+rsmd.getColumnType(i));
        Class<?> attClass = getSqlTypeClass(rsmd.getColumnType(i));
        String attClassName = getRealClassName(attClass);

        attributes.put(attName, attClassName);
        attClasses.put(attName, attClass);
      }
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {}
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {}
    }

    return generateClass(className, attributes, attClasses);
  }

  public String makeClassOuttaNode(CpoClassNode node) throws Exception {
    String className = node.getClassName();

    TreeMap<String, String> attributes = new TreeMap<String, String>();
    TreeMap<String, Class> attClasses = new TreeMap<String, Class>();

    List<CpoAttributeMapNode> alAttMap = this.getAttributeMap(node);
    for (CpoAttributeMapNode atMapNode : alAttMap) {
      String attName = atMapNode.getAttribute();
      Class<?> attClass = getSqlTypeClass(atMapNode.getColumnType());
      String attClassName = getRealClassName(attClass);

      // if the attribute uses a transform, figure out what class it really is
      if (atMapNode.getTransformClass() != null) {
        try {
          // need to use the CpoUtilClassLoader...and it needs to have the jar loaded for that class
          Class<?> transformClass = CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass(atMapNode.getTransformClass());
          for (Method method : transformClass.getMethods()) {
            if (method.getName().equals("transformIn")) {
              Class<?> returnType = method.getReturnType();
              attClassName = getRealClassName(returnType);
            }
          }
        } catch (Exception e) {
          OUT.debug("Invalid Transform Class specified:<" + atMapNode.getTransformClass() + "> using default");
        }
      }
      attributes.put(attName, attClassName);
      attClasses.put(attName, attClass);
    }

    return generateClass(className, attributes, attClasses);
  }

  private String generateClass(String className, Map<String, String> attributes, Map<String, Class> attClasses) {
    StringBuilder buf = new StringBuilder();

    // generate class header
    buf.append("/** This class auto-generated by " + this.getClass().getName() + " **/\n\n");
    if (className.lastIndexOf(".") != -1) {
      String packageName = className.substring(0, className.lastIndexOf("."));
      className = className.substring(className.lastIndexOf(".") + 1);
      buf.append("package " + packageName + ";\n\n");
    }

    // generate class declaration
    buf.append("public class " + className + " implements java.io.Serializable {\n");
    buf.append("\n");

    // generate property declarations
    buf.append("  /* Properties */\n");
    for (String attName : attributes.keySet()) {
      String attClassName = attributes.get(attName);
      buf.append("  private " + attClassName + " " + attName + ";\n");
    }
    buf.append("\n");

    // generate constructor
    buf.append("  public " + className + "() {\n");
    buf.append("  }\n\n");

    // generate getters and setters
    buf.append("  /* Getters and Setters */\n");
    for (String attName : attributes.keySet()) {
      String attClassName = attributes.get(attName);

      // generate getter
      buf.append(generateClassGetter(attClassName, attName));

      // generate setter
      buf.append(generateClassSetter(attClassName, attName));
    }
    buf.append("\n");

    // generate equals()
    buf.append("  public boolean equals(Object o) {\n");
    buf.append("    if (this == o)\n");
    buf.append("      return true;\n");
    buf.append("    if (o == null || getClass() != o.getClass())\n");
    buf.append("      return false;\n");
    buf.append("\n");
    buf.append("    " + className + " that = (" + className + ")o;\n");
    buf.append("\n");

    for (String attName : attributes.keySet()) {
      // need to look at the classes here
      Class attClass = attClasses.get(attName);
      if (attClass.isPrimitive()) {
        // primitive type, use ==
        buf.append("    if (" + attName + " != that." + attName + ")\n");
      } else {
        // object, use .equals
        buf.append("    if (" + attName + " != null ? !" + attName + ".equals(that." + attName + ") : that." + attName + " != null)\n");
      }
      buf.append("      return false;\n");
    }
    buf.append("\n");
    buf.append("    return true;\n");
    buf.append("  }\n\n");

    // generate hashCode()
    buf.append("  public int hashCode() {\n");
    buf.append("    int result = 0;\n");
    for (String attName : attributes.keySet()) {
      // need to look at the classes here
      Class attClass = attClasses.get(attName);
      if (attClass.isPrimitive()) {
        // primitive type, need some magic
        buf.append("    result = 31 * result + (String.valueOf(" + attName + ").hashCode());\n");
      } else {
        buf.append("    result = 31 * result + (" + attName + " != null ? " + attName + ".hashCode() : 0);\n");
      }
    }
    buf.append("    return result;\n");
    buf.append("  }\n\n");

    // generate toString()
    buf.append("  public String toString() {\n");
    buf.append("    StringBuilder str = new StringBuilder();\n");
    for (String attName : attributes.keySet()) {
      buf.append("    str.append(\"" + attName + " = \" + " + attName + " + \"\\n\");\n");
    }
    buf.append("    return str.toString();\n");
    buf.append("  }\n");

    // end class
    buf.append("}\n");

    return buf.toString();
  }

  private String generateClassGetter(String attClassName, String attName) {
    StringBuilder buf = new StringBuilder();
    if (attName.length() > 1) {
      buf.append("  public " + attClassName + " get" + attName.substring(0, 1).toUpperCase() + attName.substring(1) + "() {\n");
    } else {
      buf.append("  public " + attClassName + " get" + attName.toUpperCase() + "() {\n");
    }

    buf.append("    return this." + attName + ";\n");
    buf.append("  }\n");

    return buf.toString();
  }

  private String generateClassSetter(String attClassName, String attName) {
    StringBuilder buf = new StringBuilder();
    if (attName.length() > 1) {
      buf.append("  public void set" + attName.substring(0, 1).toUpperCase() + attName.substring(1) + "(" + attClassName + " " + attName + ") {\n");
    } else {
      buf.append("  public void set" + attName.toUpperCase() + "(" + attClassName + " " + attName + ") {\n");
    }

    buf.append("    this." + attName + " = " + attName + ";\n");
    buf.append("  }\n");

    return buf.toString();
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
    Enumeration<AbstractCpoNode> enumChildren = ccn.children();
    while (enumChildren.hasMoreElements()) {
      AbstractCpoNode child = enumChildren.nextElement();
      if (child instanceof CpoAttributeLabelNode)
        attMapParent = (CpoAttributeLabelNode)child;
    }
    if (attMapParent == null) throw new Exception ("Can't find the Attribute Label for this node: "+ccn);
    for (int i = 0 ; i < methods.length ; i++) {
      if (methods[i].getName().startsWith("get")) {
        //Class<?> returnClass = methods[i].getReturnType();
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
    Enumeration<AbstractCpoNode> enumChildren = ccn.children();
    while (enumChildren.hasMoreElements()) {
      AbstractCpoNode child = enumChildren.nextElement();
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
        if (rs != null) rs.close();
      } catch (Exception e) {}
      try {
        if (pstmt != null) pstmt.close();
      } catch (Exception e) {}
    }    
  }

  public Collection<?> executeQueryGroup(Object obj, Object objReturnType, CpoQueryGroupNode cpoQGnode, boolean persist) throws Exception {
//    try {
      List<Object> result = new ArrayList<Object>();
      if (cpoQGnode.getType().equals(Statics.CPO_TYPE_CREATE)) {
        //insert
        Method meth = cpoMan.getClass().getMethod("insertObject",new Class[]{String.class,Object.class});
        meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        // retrieve from cpo, so we can verify insertion
        meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        if (resultObj != null)
          result.add(resultObj);
      } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_DELETE)) {
        Method meth = cpoMan.getClass().getMethod("deleteObject",new Class[]{String.class,Object.class});
        meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        // retrieve from cpo, so we can verify deletion
        meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        if (resultObj != null)
          result.add(resultObj);
      } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_LIST)) {
        Collection<?> coll = null;
        CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass("org.synchronoss.cpo.CpoWhere");
        Method[] meth = cpoMan.getClass().getMethods();
        for (int i = 0 ; i < meth.length ; i++) {
          if (meth[i].getName().equals("retrieveObjects")) {
            Class<?>[] paramClasses = meth[i].getParameterTypes();
            if (paramClasses.length == 5) {
              coll = (Collection<?>)meth[i].invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj,objReturnType,null,null});
            }
          }
        }
        if (coll == null)
          return new ArrayList<Object>();
        return coll;
      } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_RETRIEVE)) {
        Method meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        if (resultObj != null)
          result.add(resultObj);
      } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_UPDATE)) {
        Method meth = cpoMan.getClass().getMethod("updateObject",new Class[]{String.class,Object.class});
        meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        // retrieve from cpo, so we can verify update
        meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        if (resultObj != null)
          result.add(resultObj);
      } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_EXIST) && persist) {
        Method meth = cpoMan.getClass().getMethod("persistObject",new Class[]{String.class,Object.class});
        meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        // retrieve from cpo, so we can verify update
        meth = cpoMan.getClass().getMethod("retrieveObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        if (resultObj != null)
          result.add(resultObj);
      } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_EXIST)) {
        Method meth = cpoMan.getClass().getMethod("existsObject",new Class[]{String.class,Object.class});
        Object resultObj = meth.invoke(cpoMan,new Object[]{cpoQGnode.getGroupName(),obj});
        if (resultObj != null)
          result.add(resultObj);
      }
    return result;
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
              if (rs != null) rs.close();
          } catch (Exception e) {}
          try {
              if (pstmt != null) pstmt.close();
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

  private String getRealClassName(Class<?> clazz) {
    String clazzName = clazz.getName();
    byte[] b = new byte[0];
    char[] c = new char[0];

    if (b.getClass().getName().equals(clazzName)) {
      clazzName = "byte[]";
    } else if (c.getClass().getName().equals(clazzName)) {
      clazzName = "char[]";
    }
    return clazzName;
  }

  /**
   * Returns true if a class is protected.  This will match exact class names as well as packages
   * for example:
   * org.synchronoss.utils.cpo.CpoClassNode - would match that exact class
   * org.synchronoss.utils.cpo - would match all classes in the cpo package
   */
  public boolean isClassProtected(String className) {

    // exact match
    if (protectedClasses.contains(className))
      return true;

    // partial match
    for (String s : protectedClasses) {
      if (className.startsWith(s)) {
        return true;
      }
    }

    return false;
  }

  public class CpoClassNodeComparator implements Comparator<CpoClassNode> {
    public int compare(CpoClassNode c1, CpoClassNode c2) {
      return c1.getDisplayClassName().compareTo(c2.getDisplayClassName());
    }
  }

}