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
package org.synchronoss.cpo.util;

import org.slf4j.*;
import org.synchronoss.cpo.*;
import org.synchronoss.cpo.jdbc.*;
import org.synchronoss.cpo.meta.dao.CpoMetaDAO;
import org.synchronoss.cpo.meta.dao.jdbc.JdbcMetaDAO;
import org.synchronoss.cpo.meta.domain.*;
import org.synchronoss.cpo.meta.parser.ExpressionParser;
import org.synchronoss.cpo.util.tree.*;

import javax.naming.*;
import javax.sql.DataSource;
import javax.swing.tree.*;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class Proxy implements Observer {

  private Context ctx;
  private Properties defProps;
  private String server;
  private Connection conn;

  // caches
  private Hashtable<AbstractCpoNode, List<CpoClassNode>> classCache = new Hashtable<AbstractCpoNode, List<CpoClassNode>>();
  private Hashtable<String, CpoClassNode> classCacheById = new Hashtable<String, CpoClassNode>();
  private Hashtable<CpoAttributeLabelNode, List<CpoAttributeMapNode>> attMapCache = new Hashtable<CpoAttributeLabelNode, List<CpoAttributeMapNode>>();
  private Hashtable<CpoQueryNode, List<CpoQueryParameterNode>> queryParamCache = new Hashtable<CpoQueryNode, List<CpoQueryParameterNode>>();
  private Hashtable<CpoQueryGroupNode, List<CpoQueryNode>> queryCache = new Hashtable<CpoQueryGroupNode, List<CpoQueryNode>>();
  private Hashtable<CpoClassNode, List<CpoQueryGroupNode>> queryGroupCache = new Hashtable<CpoClassNode, List<CpoQueryGroupNode>>();
  private Hashtable<CpoQueryTextNode, List<CpoQueryGroup>> queryGroupByTextCache = new Hashtable<CpoQueryTextNode, List<CpoQueryGroup>>();
  private List<CpoQueryTextNode> queryTextCache = new ArrayList<CpoQueryTextNode>();

  // modified items
  private List<AbstractCpoNode> allChangedObjects = new ArrayList<AbstractCpoNode>();

  private CpoAdapter cpoMan;
  private String[] sqlTypes;
  private CpoBrowserTree cpoTree;
  private String databaseName;
  private String tablePrefix;
  private String sqlDelimiter;
  private Properties connProps;
  private boolean classNameToggle = false;
  private Logger OUT = LoggerFactory.getLogger(this.getClass());

  private CpoMetaDAO metaDao = null;

  // connection based protected classes
  private HashSet<String> protectedClasses = new HashSet<String>();

  public Proxy(Properties props, String server, CpoBrowserTree cpoTree) throws Exception {
    if (server == null || props == null) throw new Exception("No Server Selected!");
    this.defProps = props;
    this.server = server;
    this.cpoTree = cpoTree;
    getConnection();
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

    DataSourceInfo dsi = null;
    if (CpoUtil.props.getProperty(Statics.PROP_JDBC_URL + server) == null && CpoUtil.localProps.getProperty(Statics.PROP_JDBC_URL + server) == null) {
      getInitialContext();
      conn = ((DataSource) ctx.lookup(connProps.getProperty(Statics.PROP_WLSCONNPOOL + server))).getConnection();
      conn.setAutoCommit(false);
      databaseName = conn.getMetaData().getURL();

      // Replace the dynamic link with a hard link.
      dsi = new JndiDataSourceInfo(connProps.getProperty(Statics.PROP_WLSCONNPOOL + server), getTablePrefix());
    } else {
      Class<?> driverClass;
      try {
        driverClass = Class.forName(connProps.getProperty(Statics.PROP_JDBC_DRIVER + server));
      } catch (Exception e) {
        driverClass = CpoUtilClassLoader.getInstance(CpoUtil.files, this.getClass().getClassLoader()).loadClass(connProps.getProperty(Statics.PROP_JDBC_DRIVER + server));
      }
      OUT.debug("Class: " + driverClass);
      Properties connectionProperties = new Properties();
      if (connProps.getProperty(Statics.PROP_JDBC_PARAMS + server) != null && !connProps.getProperty(Statics.PROP_JDBC_PARAMS + server).equals("")) {
        StringTokenizer st = new StringTokenizer(connProps.getProperty(Statics.PROP_JDBC_PARAMS + server), ";");
        while (st.hasMoreTokens()) {
          String token = st.nextToken();
          StringTokenizer stNameValue = new StringTokenizer(token, "=");
          String name = null, value = null;
          if (stNameValue.hasMoreTokens())
            name = stNameValue.nextToken();
          if (stNameValue.hasMoreTokens())
            value = stNameValue.nextToken();
          connectionProperties.setProperty(name, value);
        }
      }
      conn = DriverManager.getConnection(connProps.getProperty(Statics.PROP_JDBC_URL + server), connectionProperties);
      conn.setAutoCommit(false);
      databaseName = conn.getMetaData().getURL();
      // Replace the dynamic link with a hard link.
      dsi = new DriverDataSourceInfo(connProps.getProperty(Statics.PROP_JDBC_DRIVER + server), connProps.getProperty(Statics.PROP_JDBC_URL + server), connectionProperties, getTablePrefix());
    }
    cpoMan = new JdbcCpoAdapter(dsi);
    metaDao = new JdbcMetaDAO(dsi);
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

  public List<CpoClassNode> getClasses(CpoServerNode parent) throws Exception {
    if (this.classCache.containsKey(parent)) {
      List<CpoClassNode> al = this.classCache.get(parent);
      // sort by the version of the class name we're displaying (full vs. short)
      Collections.sort(al);
      return al;
    }
    List<CpoClassNode> al = new ArrayList<CpoClassNode>();
    
    for (CpoClass cpoClass : metaDao.getCpoClasses()) {
      CpoClassNode cpoClassNode = new CpoClassNode(cpoClass, parent);
      cpoClassNode.setProtected(this.isClassProtected(cpoClass.getName()));
      al.add(cpoClassNode);
      classCacheById.put(cpoClass.getClassId(), cpoClassNode);
    }

    // sort by the version of the class name we're displaying (full vs. short)
    Collections.sort(al);

    this.classCache.put(parent, al);
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

  public CpoQueryTextNode addQueryText(String desc) {
    
    if (desc == null || desc.equals(""))
      return null;

    CpoQueryText queryText = new CpoQueryText();
    queryText.setTextId(getNewGuid());
    queryText.setSqlText("");
    queryText.setDescription(desc);
    queryText.setUserid(CpoUtil.username);
    queryText.setCreatedate(Calendar.getInstance());
    
    CpoQueryTextNode cQTnode = new CpoQueryTextNode(queryText, getCpoQueryTextLabelNode(getServerNode()));
    cQTnode.setNew(true);

    // add to cache
    try {
      // if cache is empty, try to load it
      if (queryTextCache.isEmpty()) {
        getQueryText(getServerNode());
      }
      queryTextCache.add(cQTnode);
    } catch (Exception ex) {
      // ignore
    }

    return cQTnode;
  }

  public List<CpoQueryTextNode> getQueryText(CpoServerNode cpoServer) throws Exception {
    if (this.queryTextCache.size() != 0) {
      return this.queryTextCache;
    }

    CpoQueryTextLabelNode parent = this.getCpoQueryTextLabelNode(cpoServer);
    List<CpoQueryTextNode> al = new ArrayList<CpoQueryTextNode>();

    List<CpoQueryText> queryTexts = metaDao.getCpoQueryTexts();
    Collections.sort(queryTexts);
    for (CpoQueryText queryText : queryTexts) {
      CpoQueryTextNode cpoQTNode = new CpoQueryTextNode(queryText, parent);
      al.add(cpoQTNode);
    }

    queryTextCache = al;
    return al;
  }
  
  public List<CpoQueryGroup> getQueryGroups(CpoQueryTextNode textNode) throws Exception {
    if (this.queryGroupByTextCache.containsKey(textNode)) {
      return this.queryGroupByTextCache.get(textNode);
    }

    List<CpoQueryGroup> al = new ArrayList<CpoQueryGroup>();
    for (CpoQueryGroup queryGroup : metaDao.getCpoQueryGroups(textNode.getCpoQueryText())) {
      al.add(queryGroup);
    }
    this.queryGroupByTextCache.put(textNode, al);
    return al;
  }

  public List<CpoQueryGroupNode> getQueryGroups(CpoClassNode classNode) throws Exception {
    if (this.queryGroupCache.containsKey(classNode)) {
      return this.queryGroupCache.get(classNode);
    }
    
    List<CpoQueryGroupNode> al = new ArrayList<CpoQueryGroupNode>();

    CpoClass cpoClass = classNode.getCpoClass();

    List<CpoQueryGroup> queryGroups = metaDao.getCpoQueryGroups(cpoClass);
    cpoClass.setQueryGroups(queryGroups);

    for (CpoQueryGroup queryGroup : queryGroups) {
      CpoQueryGroupNode cpoQueryGroupNode = new CpoQueryGroupNode(queryGroup, getQueryGroupLabelNode(classNode));
      al.add(cpoQueryGroupNode);
    }
    this.queryGroupCache.put(classNode, al);
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
    for (CpoQueryGroupNode node : getQueryGroups(classNode)) {
      if (((name == null && node.getGroupName() == null) ||
          node.getGroupName() != null && name != null &&
          node.getGroupName().equals(name)) 
          && node.getType().equals(type)) {
        return node;
      }
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
    
    for (CpoQuery query : metaDao.getCpoQueries(parent.getCpoQueryGroup())) {
      CpoQueryTextNode cQTnode = this.getQueryText((CpoServerNode) parent.getParent().getParent().getParent(), query.getTextId());
      
      query.setQueryText(cQTnode.getCpoQueryText());
      CpoQueryNode cpoQueryNode = new CpoQueryNode(query, parent);
      al.add(cpoQueryNode);
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
    
    for (CpoQueryParameter parameter : metaDao.getCpoQueryParameters(qNode.getCpoQuery())) {
      CpoAttributeMapNode camn = getAttributeMap((CpoServerNode)qNode.getParent().getParent().getParent().getParent(), parameter.getAttributeId());
      CpoQueryParameterNode cpoQPB = new CpoQueryParameterNode(parameter, camn, qNode);
      al.add(cpoQPB);
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

  /**
   * Returns the attribute w/ the specified id
   */
  public CpoAttributeMapNode getAttributeMap(CpoServerNode cpoServerNode, String attributeId) throws Exception {
    for (CpoClassNode cpoClassNode : getClasses(cpoServerNode)) {
      CpoAttributeMapNode attMapNode = getAttributeMap(cpoClassNode, attributeId);
      if (attMapNode != null) {
        return attMapNode;
      }
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
    CpoClass cpoClass = cpoAttLabNode.getParent().getCpoClass();

    List<CpoAttribute> attributes = metaDao.getCpoAttributes(cpoClass);
    cpoClass.setAttributes(attributes);

    for (CpoAttribute attribute : attributes) {
      CpoAttributeMapNode cpoAB = new CpoAttributeMapNode(attribute, cpoAttLabNode);
      al.add(cpoAB);
    }
    this.attMapCache.put(cpoAttLabNode, al);
    return al;
  }
  
  public void close() {
    try {
      conn.close();
    } catch (Exception e) {
      // ignore
    }
    conn = null;
    cpoMan = null;
  }

  /**
   * lets try to close this oracle connection b4 we take a hike
   */
  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
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
        int index = parent.getIndex((TreeNode)obs);
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
  public String getNewGuid() {
    return GUID.getGUID();
  }

  /**
   * saves all nodes passed to it to the db
   */
  public void saveNodes(List<AbstractCpoNode> nodes) throws Exception {
    List<AbstractCpoNode> toBeCleaned = new ArrayList<AbstractCpoNode>();

    /**
     * do class nodes first
     */
    for (AbstractCpoNode node : nodes) {
      if (node instanceof CpoClassNode) {
        CpoClassNode ccn = (CpoClassNode) node;
        CpoClass cpoClass = ccn.getCpoClass();
        if (ccn.isNew()) {
          metaDao.createCpoClass(cpoClass);
        } else if (ccn.isRemove()) {
          metaDao.deleteCpoClass(cpoClass);
        } else if (ccn.isDirty()) {
          metaDao.updateCpoClass(cpoClass);
        }
        toBeCleaned.add(node);
      }
    }

    /**
     * do attribute maps second
     */
    for (AbstractCpoNode node : nodes) {
      if (node instanceof CpoAttributeMapNode) {
        CpoAttributeMapNode camn = (CpoAttributeMapNode) node;
        CpoAttribute attribute = camn.getCpoAttribute();
        if (camn.isNew()) {
          metaDao.createCpoAttribute(attribute);
        } else if (camn.isRemove()) {
          metaDao.deleteCpoAttribute(attribute);
        } else if (camn.isDirty()) {
          metaDao.updateCpoAttribute(attribute);
        }
        toBeCleaned.add(node);
      }
    }

    /**
     * do query groups
     */
    for (AbstractCpoNode node : nodes) {
      if (node instanceof CpoQueryGroupNode) {
        CpoQueryGroupNode cqgn = (CpoQueryGroupNode) node;
        CpoQueryGroup queryGroup = cqgn.getCpoQueryGroup();
        if (cqgn.isNew()) {
          metaDao.createCpoQueryGroup(queryGroup);
        } else if (cqgn.isRemove()) {
          metaDao.deleteCpoQueryGroup(queryGroup);
        } else if (cqgn.isDirty()) {
          metaDao.updateCpoQueryGroup(queryGroup);
        }
        toBeCleaned.add(node);
      }
    }

    /**
     * do query text - only add / updates here
     */
    for (AbstractCpoNode node : nodes) {
      if (node instanceof CpoQueryTextNode) {
        CpoQueryTextNode cQTnode = (CpoQueryTextNode) node;
        CpoQueryText queryText = cQTnode.getCpoQueryText();
        if (cQTnode.isNew()) {
          metaDao.createCpoQueryText(queryText);
          toBeCleaned.add(node);
        } else if (cQTnode.isDirty()) {
          metaDao.updateCpoQueryText(queryText);
          toBeCleaned.add(node);
        }
      }
    }

    /**
     * do query node
     */
    for (AbstractCpoNode node : nodes) {
      if (node instanceof CpoQueryNode) {
        CpoQueryNode cqn = (CpoQueryNode) node;
        CpoQuery query = cqn.getCpoQuery();
        if (cqn.isNew()) {
          metaDao.createCpoQuery(query);
        } else if (cqn.isRemove()) {
          metaDao.deleteCpoQuery(query);
        } else if (cqn.isDirty()) {
          metaDao.updateCpoQuery(query);
        }
        toBeCleaned.add(node);
      }
    }

    /**
     * do query parameters
     */
    for (AbstractCpoNode node : nodes) {
      if (node instanceof CpoQueryParameterNode) {
        CpoQueryParameterNode cqpn = (CpoQueryParameterNode) node;
        CpoQueryParameter queryParameter = cqpn.getCpoQueryParameter();
        if (cqpn.isNew()) {
          metaDao.createCpoQueryParameter(queryParameter);
        } else if (cqpn.isRemove()) {
          metaDao.deleteCpoQueryParameter(queryParameter);
        } else if (cqpn.isDirty()) {
          metaDao.updateCpoQueryParameter(queryParameter);
        }
        toBeCleaned.add(node);
      }
    }

    /**
     * do query text - removes last
     */
    for (AbstractCpoNode node : nodes) {
      if (node instanceof CpoQueryTextNode) {
        CpoQueryTextNode cQTnode = (CpoQueryTextNode) node;
        CpoQueryText queryText = cQTnode.getCpoQueryText();
        if (cQTnode.isRemove()) {
          metaDao.deleteCpoQueryText(queryText);
          toBeCleaned.add(node);
        }
      }
    }

    for (AbstractCpoNode node : toBeCleaned) {
      node.setNew(false);
      node.setDirty(false);
      if (node.isRemove()) {
        this.removeObjectFromAllCache(node);
        node.setRemove(false);
      }
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

    this.allChangedObjects.remove(obj);
    
    if (this.classCache.containsKey(obj))
      classCache.remove(obj);

    // now check the cache values
    if (obj instanceof CpoClassNode) {
      CpoClassNode ccn = (CpoClassNode)obj;
      for (List<CpoClassNode> classes : classCache.values()) {
        if (classes.contains(ccn)) {
          classes.remove(ccn);
        }
      }
      if (queryGroupCache.containsKey(ccn)) {
        queryGroupCache.remove(ccn);
      }
    } else if (obj instanceof CpoAttributeLabelNode) {
      CpoAttributeLabelNode caln = (CpoAttributeLabelNode)obj;
      if (attMapCache.containsKey(caln))
        attMapCache.remove(caln);
    } else if (obj instanceof CpoAttributeMapNode) {
      CpoAttributeMapNode camn = (CpoAttributeMapNode)obj;
      for (List<CpoAttributeMapNode> attMap : attMapCache.values()) {
        if (attMap.contains(camn)) {
          attMap.remove(camn);
        }
      }
    } else if (obj instanceof CpoQueryNode) {
      CpoQueryNode cqn = (CpoQueryNode)obj;
      for (List<CpoQueryNode> attMap : queryCache.values()) {
        if (attMap.contains(cqn)) {
          attMap.remove(cqn);
        }
      }
      if (queryParamCache.containsKey(cqn)) {
        queryParamCache.remove(cqn);
      }
    } else if (obj instanceof CpoQueryGroupNode) {
      CpoQueryGroupNode cqgn = (CpoQueryGroupNode)obj;
      for (List<CpoQueryGroupNode> group : queryGroupCache.values()) {
        if (group.contains(cqgn))
          group.remove(cqgn);
      }

      CpoQueryGroup queryGroup = cqgn.getCpoQueryGroup();
      for (List<CpoQueryGroup> groups : queryGroupByTextCache.values()) {
        if (groups.contains(queryGroup))
          groups.remove(queryGroup);
      }

      if (this.queryCache.containsKey(cqgn)) {
        this.queryCache.remove(cqgn);
      }
    } else if (obj instanceof CpoQueryTextNode) {
      CpoQueryTextNode cqtn = (CpoQueryTextNode)obj;
      if (queryTextCache.contains(cqtn)) {
        queryTextCache.remove(cqtn);
      }

      if (queryGroupByTextCache.containsKey(cqtn)) {
        queryGroupByTextCache.remove(cqtn);
      }
    } else if (obj instanceof CpoQueryParameterNode) {
      CpoQueryParameterNode cqpn = (CpoQueryParameterNode)obj;
      for (List<CpoQueryParameterNode> params : queryParamCache.values()) {
        if (params.contains(cqpn))
          params.remove(cqpn);
      }
    }
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

  public String makeClassOuttaSql(CpoClass cpoClass, String sql) throws Exception {

    TreeMap<String, Class> attributes = new TreeMap<String, Class>();

    if (sql != null && sql.length() > 0) {
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
          Class<?> attClass = JavaSqlTypes.getSqlTypeClass(rsmd.getColumnType(i));

          attributes.put(attName, attClass);
        }
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
    }

    return cpoClass.generateSourceCode(attributes);
  }

  public String makeClassOuttaNode(CpoClassNode node) throws Exception {
    TreeMap<String, Class> attributes = new TreeMap<String, Class>();

    List<CpoAttributeMapNode> alAttMap = this.getAttributeMap(node);
    for (CpoAttributeMapNode atMapNode : alAttMap) {
      String attName = atMapNode.getAttribute();
      Class<?> attClass = JavaSqlTypes.getSqlTypeClass(atMapNode.getColumnType());

      // if the attribute uses a transform, figure out what class it really is
      if (atMapNode.getTransformClass() != null) {
        try {
          // need to use the CpoUtilClassLoader...and it needs to have the jar loaded for that class
          Class<?> transformClass = CpoUtilClassLoader.getInstance(CpoUtil.files,this.getClass().getClassLoader()).loadClass(atMapNode.getTransformClass());
          for (Method method : transformClass.getMethods()) {
            if (method.getName().equals("transformIn") && !method.isSynthetic() && !method.isBridge()) {
              attClass = method.getReturnType();
            }
          }
        } catch (Exception e) {
          OUT.debug("Invalid Transform Class specified:<" + atMapNode.getTransformClass() + "> using default");
        }
      }
      attributes.put(attName, attClass);
    }

    // HACK - ensure that the query groups have been loaded, so the class generates correctly
    getQueryGroups(node);

    CpoClass cpoClass = node.getCpoClass();
    return cpoClass.generateSourceCode(attributes);
  }

  String makeAttFromColName(String columnName) {
    columnName = columnName.toLowerCase();

    int idx;
    while ((idx = columnName.indexOf("_")) > 0) {
      // remove the underscore, upper case the following character
      columnName = columnName.substring(0, idx) + columnName.substring(idx + 1, idx + 2).toUpperCase() + columnName.substring(idx + 2);
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
    for (Method method : methods) {
      if (method.getName().startsWith("get")) {
        String columnTypeName = "VARCHAR"; // FIXME this needs to be fixed!!!!
        String attributeName = method.getName().substring(3);

        CpoAttribute attribute = new CpoAttribute();
        attribute.setAttributeId(getNewGuid());
        attribute.setClassId(ccn.getCpoClass().getClassId());
        attribute.setColumnName(attributeName);
        attribute.setAttribute(attributeName);
        attribute.setColumnType(columnTypeName);
        attribute.setTransformClass(null);
        attribute.setDbTable(null);
        attribute.setDbColumn(null);
        attribute.setUserid(CpoUtil.username);
        attribute.setCreatedate(Calendar.getInstance());

        CpoAttributeMapNode camn = new CpoAttributeMapNode(attribute, attMapParent);
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

    // FIXME - figure out how to do this from the cpoMan object
    if (sql != null && sql.length() > 0) {
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int i = 1 ; i <= columns ; i++) {
          CpoAttribute attribute = new CpoAttribute();
          attribute.setAttributeId(getNewGuid());
          attribute.setClassId(ccn.getCpoClass().getClassId());
          attribute.setColumnName(rsmd.getColumnName(i));
          attribute.setAttribute(this.makeAttFromColName(rsmd.getColumnName(i)));
          attribute.setColumnType(Statics.getJavaSqlType(rsmd.getColumnType(i)));
          attribute.setTransformClass(null);
          attribute.setDbTable(null);
          attribute.setDbColumn(null);
          attribute.setUserid(CpoUtil.username);
          attribute.setCreatedate(Calendar.getInstance());

          CpoAttributeMapNode camn = new CpoAttributeMapNode(attribute, attMapParent);
          this.getAttributeMap(ccn).add(camn);
          camn.setNew(true);
        }
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
    }
  }

  public Collection<?> executeQueryGroup(Object obj, Object objReturnType, CpoQueryGroupNode cpoQGnode, boolean persist) throws Exception {
    List<Object> result = new ArrayList<Object>();
    Object resultObj = null;
    if (cpoQGnode.getType().equals(Statics.CPO_TYPE_CREATE)) {
      //insert
      resultObj = cpoMan.insertObject(cpoQGnode.getGroupName(), obj);
    } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_DELETE)) {
      cpoMan.deleteObject(cpoQGnode.getGroupName(), obj);
      // retrieve from cpo, so we can verify deletion
      resultObj = cpoMan.retrieveBean(cpoQGnode.getGroupName(), obj);
    } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_LIST)) {
      result = cpoMan.retrieveBeans(cpoQGnode.getGroupName(), obj, objReturnType);
    } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_RETRIEVE)) {
      resultObj = cpoMan.retrieveBean(cpoQGnode.getGroupName(), obj);
    } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_UPDATE)) {
      cpoMan.updateObject(cpoQGnode.getGroupName(), obj);
      // retrieve from cpo, so we can verify update
      resultObj = cpoMan.retrieveBean(cpoQGnode.getGroupName(), obj);
    } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_EXIST) && persist) {
      cpoMan.persistObject(cpoQGnode.getGroupName(), obj);
      // retrieve from cpo, so we can verify update
      resultObj = cpoMan.retrieveBean(cpoQGnode.getGroupName(), obj);
    } else if (cpoQGnode.getType().equals(Statics.CPO_TYPE_EXIST)) {
      resultObj = cpoMan.existsObject(cpoQGnode.getGroupName(), obj);
    }

    // always return a list, even if it's empty
    if (result == null)
      return new ArrayList<Object>();

    // if there was a result object, add it to the list
    if (resultObj != null)
      result.add(resultObj);

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
          } catch (Exception e) {
            // ignore
          }
          try {
              if (pstmt != null) pstmt.close();
          } catch (Exception e) {
            // ignore
          }
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

  public ExpressionParser getExpressionParser(String expression) {
    return metaDao.getExpressionParser(expression);
  }
}