/*
 * Copyright (C) 2003-2012 David E. Berry, Michael A. Bellomo
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * A copy of the GNU Lesser General Public License may also be found at
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.synchronoss.cpo.util;

import org.slf4j.*;
import org.synchronoss.cpo.*;
import org.synchronoss.cpo.core.cpoCoreConfig.CtDataSourceConfig;
import org.synchronoss.cpo.core.cpoCoreMeta.StFunctionGroupType;
import org.synchronoss.cpo.exporter.*;
import org.synchronoss.cpo.meta.CpoMetaDescriptor;
import org.synchronoss.cpo.meta.domain.*;
import org.synchronoss.cpo.parser.ExpressionParser;

import javax.swing.tree.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy object that handles most of the GUI to domain logic
 */
public abstract class Proxy {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  // the cpo meta file for this proxy
  protected File cpoMetaXml;

  // last directory the user selected
  protected File lastDir;

  // The CpoMetaDescriptor
  protected CpoMetaDescriptor metaDescriptor = null;

  // cache for the adapters, to prevent reloading
  protected Map<String, CpoAdapter> adapterCache = new ConcurrentHashMap<String, CpoAdapter>();

  // the tree model
  protected DefaultTreeModel treeModel = null;

  // short or long class names
  private boolean displayShortClassName = false;

  protected Proxy() {
    super();
  }

  /**
   * Returns the file that was loaded to create this proxy
   */
  protected final File getCpoMetaXml() {
    return cpoMetaXml;
  }

  /**
   * Sets the meta xml file
   */
  protected final void setCpoMetaXml(File cpoMetaXml) {
    this.cpoMetaXml = cpoMetaXml;

    // set the last dir
    lastDir = cpoMetaXml.getParentFile();
  }

  /**
   * Sets the CpoMetaDescriptor
   */
  protected final void setMetaDescriptor(CpoMetaDescriptor metaDescriptor) {
    this.metaDescriptor = metaDescriptor;
  }

  /**
   * Creates a new tree model that represents this proxy
   */
  public final TreeModel createTreeModel() throws CpoException {
    CpoRootNode rootNode = new CpoRootNode(this);

    JTreeMetaVisitor visitor = new JTreeMetaVisitor(rootNode);
    for (CpoClass cpoClass : metaDescriptor.getCpoClasses()) {
      cpoClass.acceptMetaDFVisitor(visitor);
    }

    treeModel = new DefaultTreeModel(rootNode);
    return treeModel;
  }

  /**
   * @return The root of the tree
   */
  protected CpoRootNode getRoot() {
    return (CpoRootNode)treeModel.getRoot();
  }

  /**
   * Returns the default package name
   */
  public String getDefaultPackageName() {
    return metaDescriptor.getDefaultPackageName();
  }

  /**
   * Sets the default package name
   */
  public void setDefaultPackageName(String packageName) {
    metaDescriptor.setDefaultPackageName(packageName);
  }

  /**
   * Returns the last directory used
   */
  public File getLastDir() {
    return lastDir;
  }

  /**
   * Sets the last directory used
   */
  public void setLastDir(File file) {
    if (file != null) {
      lastDir = file;
    }
  }

  @Override
  public String toString() {
    return cpoMetaXml.getName();
  }

  /**
   * @return The absolute path of the meta xml file
   */
  public String getMetaXmlFullName() {
    return cpoMetaXml.getAbsolutePath();
  }

  /**
   * Returns a list of connections that will work with this proxy
   */
  public Vector<String> getConnectionList() throws CpoException {
    Vector<String> result = new Vector<String>();

    SupportedType type = SupportedType.getTypeForMetaDescriptor(metaDescriptor);

    List<CtDataSourceConfig> dataSourceConfigs = CpoUtil.getInstance().getDataSourceConfigs();
    for (CtDataSourceConfig dataSourceConfig : dataSourceConfigs) {
      if (type.getDataSourceConfigClass().isAssignableFrom(dataSourceConfig.getClass())) {
        result.add(dataSourceConfig.getName());
      }
    }

    Collections.sort(result, new CaseInsensitiveStringComparator());
    return result;
  }

  /**
   * @return The data types that this instance of cpo supports
   */
  public Vector<String> getAllowableDataTypes() {
    Vector<String> dataTypes = new Vector<String>();

    try {
      dataTypes.addAll(metaDescriptor.getAllowableDataTypes());
    } catch (CpoException ex) {
      CpoUtil.showException(ex);
    }

    return dataTypes;
  }

  /**
   * Saves all the meta data to the specified file.
   *
   * If the file is null, uses the cpoMetaXml file.
   */
  public void save(File file) throws CpoException {
    File dest = file;
    if (dest == null) {
      dest = cpoMetaXml;
    }

    // if we're removing the node, we need to take it out of the meta now before we export
    List<AbstractCpoNode> changedNodes = getRoot().getChangedNodes();
    for (AbstractCpoNode node : changedNodes) {
      if (node.isRemove()) {
        node.performRemove();
      }
    }

    try {
      metaDescriptor.export(dest);
      getRoot().clearChangedNodes();
    } catch (CpoException ex) {
      CpoUtil.showException(ex);
    }
  }

  public void toggleClassNames() {
    this.displayShortClassName = !displayShortClassName;
  }

  public boolean isDisplayShortClassName() {
    return this.displayShortClassName;
  }

  /**
   * Creates a new cpo class instance with the given class name
   */
  protected CpoClass createCpoClass(String className) throws CpoException {
    CpoClass cpoClass = metaDescriptor.createCpoClass();
    cpoClass.setName(className);

    // save the package name
    if (cpoClass.getName().lastIndexOf(".") != -1) {
      setDefaultPackageName(cpoClass.getName().substring(0, cpoClass.getName().lastIndexOf(".")));
    }

    return cpoClass;
  }

  /**
   * Creates a new cpo attribute instance
   */
  protected CpoAttribute createCpoAttribute() throws CpoException {
    return metaDescriptor.createCpoAttribute();
  }

  /**
   * Creates a new cpo function group with the supplied name and type
   */
  protected CpoFunctionGroup createCpoFunctionGroup(String name, String type) throws CpoException {
    CpoFunctionGroup functionGroup = metaDescriptor.createCpoFunctionGroup();
    functionGroup.setName(name);
    functionGroup.setType(type);
    return functionGroup;
  }

  /**
   * Creates a new cpo function
   */
  protected CpoFunction createCpoFunction() throws CpoException {
    CpoFunction function = metaDescriptor.createCpoFunction();
    // make sure we set an expression
    function.setExpression("");
    return function;
  }

  /**
   * Creates a new cpo argument
   */
  protected CpoArgument createCpoArgument() throws CpoException {
    return metaDescriptor.createCpoArgument();
  }

  // node creation functions

  /**
   * Creates a new cpo class node for the supplied cpo class
   */
  protected CpoClassNode createClassNode(CpoClass cpoClass) {
    return new CpoClassNode(cpoClass);
  }

  /**
   * Creates a new cpo attribute node for the supplied cpo attribute
   */
  protected CpoAttributeNode createAttributeNode(CpoAttribute cpoAttribute) {
    return new CpoAttributeNode(cpoAttribute);
  }

  /**
   * Creates a new cpo function group node for the supplied cpo function group
   */
  protected CpoFunctionGroupNode createFunctionGroupNode(CpoFunctionGroup cpoFunctionGroup) {
    return new CpoFunctionGroupNode(cpoFunctionGroup);
  }

  /**
   * Creates a new cpo function node for the supplied cpo function
   */
  protected CpoFunctionNode createFunctionNode(CpoFunction cpoFunction) {
    return new CpoFunctionNode(cpoFunction);
  }

  /**
   * Creates a new cpo argument node for the supplied cpo argument
   */
  protected CpoArgumentNode createArgumentNode(CpoArgument cpoArgument) {
    return new CpoArgumentNode(cpoArgument);
  }
  // node creation functions

  // add methods

  /**
   * Adds a new CpoClass to the meta descriptor
   *
   * @param className The name of the class
   * @return The newly create class node
   * @throws CpoException if any error occurs
   */
  public CpoClassNode addClass(String className) throws CpoException {
    CpoClass cpoClass = createCpoClass(className);
    CpoClassNode cpoClassNode = createClassNode(cpoClass);
    cpoClassNode.setProtected(CpoUtil.getInstance().isClassProtected(cpoClass));

    // add to the meta data
    metaDescriptor.addCpoClass(cpoClass);

    // add to tree
    getRoot().add(cpoClassNode);
    cpoClassNode.setNew(true);

    return cpoClassNode;
  }

  /**
   * Adds the attribute to the cpoClassNode specified if it doesn't already exist.
   *
   * @param cpoClassNode The CpoClassNode
   * @param attribute The CpoAttribute
   * @return true if the attribute was added, false if it already existed
   */
  public boolean addAttribute(CpoClassNode cpoClassNode, CpoAttribute attribute) {
    boolean added = false;

    if (!cpoClassNode.attributeExists(attribute.getJavaName())) {
      // add to the meta data
      cpoClassNode.getUserObject().addAttribute(attribute);

      // create the tree node
      CpoAttributeNode cpoAttributeNode = createAttributeNode(attribute);
      cpoClassNode.getAttributeLabelNode().add(cpoAttributeNode);
      cpoAttributeNode.setNew(true);
      added = true;
    }

    return added;
  }

  /**
   * Adds a new function group using the supplied name and type to the cpoClassNode
   *
   * @param cpoClassNode The class node
   * @param name The name of the function group
   * @param type The type of the function group
   * @return The newly created function group node
   * @throws CpoException if any error occurs
   */
  public CpoFunctionGroupNode addFunctionGroup(CpoClassNode cpoClassNode, String name, String type) throws CpoException {
    CpoFunctionGroup cpoFunctionGroup = createCpoFunctionGroup(name, type);
    CpoFunctionGroupNode cpoFunctionGroupNode = createFunctionGroupNode(cpoFunctionGroup);
    cpoFunctionGroupNode.setProtected(cpoClassNode.isProtected());

    // add to the meta data
    cpoClassNode.getUserObject().addFunctionGroup(cpoFunctionGroup);

    // add to the tree
    cpoClassNode.getFunctionGroupLabelNode().add(cpoFunctionGroupNode);
    cpoFunctionGroupNode.setNew(true);

    return cpoFunctionGroupNode;
  }

  /**
   * Adds a new function to the supplied function group node
   *
   * @param cpoFunctionGroupNode The function group node
   * @return The newly created function node
   * @throws CpoException if any error occurs
   */
  public CpoFunctionNode addFunction(CpoFunctionGroupNode cpoFunctionGroupNode) throws CpoException {
    CpoFunction cpoFunction = createCpoFunction();
    CpoFunctionNode cpoFunctionNode = createFunctionNode(cpoFunction);
    cpoFunctionNode.setProtected(cpoFunctionGroupNode.isProtected());

    // add to the meta data
    cpoFunctionGroupNode.getUserObject().addFunction(cpoFunction);

    // add to the tree
    cpoFunctionGroupNode.add(cpoFunctionNode);
    cpoFunctionNode.setNew(true);

    return cpoFunctionNode;
  }

  /**
   * Adds a new argument to the supplied function for the supplied cpoAttribute
   *
   * @param cpoFunctionNode The function to add the argument to
   * @param cpoAttribute The cpo attribute
   * @return The newly created argument node
   * @throws CpoException if any error occurs
   */
  public CpoArgumentNode addArgument(CpoFunctionNode cpoFunctionNode, CpoAttribute cpoAttribute) throws CpoException {
    CpoArgument cpoArgument = createCpoArgument();
    if (cpoAttribute != null) {
      cpoArgument.setAttribute(cpoAttribute);
    }
    CpoArgumentNode cpoArgumentNode = createArgumentNode(cpoArgument);
    cpoArgumentNode.setProtected(cpoFunctionNode.isProtected());

    // add to the meta data
    cpoFunctionNode.getUserObject().addArgument(cpoArgument);

    // add to the tree
    cpoFunctionNode.add(cpoArgumentNode);
    cpoArgumentNode.setNew(true);

    return cpoArgumentNode;
  }
  // add methods

  // remove methods

  /**
   * Removes the cpo class node from the meta descriptor
   *
   * @param cpoClassNode The class node to remove
   * @throws CpoException if any error occurs
   */
  public void removeClass(CpoClassNode cpoClassNode) throws CpoException {
    // remove from meta
    metaDescriptor.removeCpoClass(cpoClassNode.getUserObject());

    // remove from tree
    cpoClassNode.removeFromParent();
  }

  /**
   * Removes the cpo attribute node from the meta descriptor
   *
   * @param cpoAttributeNode The attribute node to remove
   * @throws CpoException if any error occurs
   */
  public void removeAttribute(CpoAttributeNode cpoAttributeNode) throws CpoException {
    CpoClassNode cpoClassNode = cpoAttributeNode.getParent().getParent();

    // remove from meta
    cpoClassNode.getUserObject().removeAttribute(cpoAttributeNode.getUserObject());

    // remove from tree
    cpoAttributeNode.removeFromParent();
  }

  /**
   * Removes the cpo function group node from the meta descriptor
   *
   * @param cpoFunctionGroupNode The function group node to remove
   * @throws CpoException if any error occurs
   */
  public void removeFunctionGroup(CpoFunctionGroupNode cpoFunctionGroupNode) throws CpoException {
    CpoClassNode cpoClassNode = cpoFunctionGroupNode.getParent().getParent();

    // remove from meta
    cpoClassNode.getUserObject().removeFunctionGroup(cpoFunctionGroupNode.getUserObject());

    // remove from tree
    cpoFunctionGroupNode.removeFromParent();
  }

  /**
   * Removes the cpo function node from the meta descriptor
   *
   * @param cpoFunctionNode The function node to remove
   * @throws CpoException if any error occurs
   */
  public void removeFunction(CpoFunctionNode cpoFunctionNode) throws CpoException {
    CpoFunctionGroupNode cpoFunctionGroupNode = cpoFunctionNode.getParent();

    // remove from meta
    cpoFunctionGroupNode.getUserObject().removeFunction(cpoFunctionNode.getUserObject());

    // remove from tree
    cpoFunctionNode.removeFromParent();
  }

  /**
   * Removes the cpo argument node from the meta descriptor
   *
   * @param cpoArgumentNode The argument node to remove
   * @throws CpoException if any error occurs
   */
  public void removeArgument(CpoArgumentNode cpoArgumentNode) throws CpoException {
    CpoFunctionNode cpoFunctionNode = cpoArgumentNode.getParent();

    // remove from meta
    cpoFunctionNode.getUserObject().removeArgument(cpoArgumentNode.getSeqNo());

    // remove from tree
    cpoArgumentNode.removeFromParent();
  }
  // remove methods

  /**
   * Returns a set of all function groups that have functions having arguments using the supplied attribute
   */
  public Set<CpoFunctionGroup> getFunctionGroupsUsingAttribute(CpoAttributeNode cpoAttributeNode) {
    SortedSet<CpoFunctionGroup> functionGroups = new TreeSet<CpoFunctionGroup>();

    Enumeration functionGroupNodes = cpoAttributeNode.getParent().getParent().getFunctionGroupLabelNode().children();
    while (functionGroupNodes.hasMoreElements()) {
      CpoFunctionGroupNode functionGroupNode = (CpoFunctionGroupNode)functionGroupNodes.nextElement();

      // if we're deleting the function group, then we don't care
      if (!functionGroupNode.isRemove()) {
        CpoFunctionGroup functionGroup = functionGroupNode.getUserObject();
        Enumeration functionNodes = functionGroupNode.children();
        while (functionNodes.hasMoreElements()) {
          CpoFunctionNode functionNode = (CpoFunctionNode)functionNodes.nextElement();
          Enumeration argumentNodes = functionNode.children();
          while (argumentNodes.hasMoreElements()) {
            CpoArgumentNode argumentNode = (CpoArgumentNode)argumentNodes.nextElement();
            if (argumentNode.getUserObject().getAttributeName().equals(cpoAttributeNode.getUserObject().getJavaName())) {
              functionGroups.add(functionGroup);
            }
          }
        }
      }
    }

    return functionGroups;
  }

  // reorder methods

  /**
   * Reorders a function up one position within the function group
   */
  public void moveFunctionUp(CpoFunctionNode cpoFunctionNode) {
    CpoFunctionGroupNode functionGroupNode = cpoFunctionNode.getParent();
    int idx = functionGroupNode.getIndex(cpoFunctionNode);
    int newIdx = idx - 1;
    reorderFunctionNode(cpoFunctionNode, newIdx);
  }

  /**
   * Reorders a function down one position within the function group
   */
  public void moveFunctionDown(CpoFunctionNode cpoFunctionNode) {
    CpoFunctionGroupNode functionGroupNode = cpoFunctionNode.getParent();
    int idx = functionGroupNode.getIndex(cpoFunctionNode);
    int newIdx = idx + 1;
    reorderFunctionNode(cpoFunctionNode, newIdx);
  }

  /**
   * Reorders the specified cpoFunctionNode to the newIdx
   *
   * @param cpoFunctionNode The function node to move
   * @param newIdx The new index of the function node
   */
  private void reorderFunctionNode(CpoFunctionNode cpoFunctionNode, int newIdx) {

    CpoFunctionGroupNode functionGroupNode = cpoFunctionNode.getParent();

    // adjust the data
    List<CpoFunction> functions = functionGroupNode.getUserObject().getFunctions();
    functions.remove(cpoFunctionNode.getUserObject());
    functions.add(newIdx, cpoFunctionNode.getUserObject());

    // adjust the nodes
    treeModel.removeNodeFromParent(cpoFunctionNode);
    treeModel.insertNodeInto(cpoFunctionNode, functionGroupNode, newIdx);

    cpoFunctionNode.setDirty(true);
  }
  // reorder methods

  /**
   * Attempt to figure out the java type name for the supplied attribute.
   */
  public String getDataTypeName(CpoAttribute cpoAttribute) throws CpoException {
    // figure out the java type
    return metaDescriptor.getDataTypeName(cpoAttribute);
  }

  /**
   * The specified connection and expression are used to create attributes
   *
   * @param connectionName The name of the connection to use
   * @param expression The expression used to create attributes
   * @return List of all the attributes
   * @throws CpoException if there's an error creating the CpoClass
   */
  public List<CpoAttribute> createAttributesFromExpression(String connectionName, String expression) throws CpoException {

    List<CpoAttribute> attributes = new ArrayList<CpoAttribute>();
    if (connectionName != null && !connectionName.isEmpty() && expression != null && !expression.isEmpty()) {
      try {
        CpoAdapter cpoAdapter = getCpoAdapter(connectionName);
        attributes =  cpoAdapter.getCpoAttributes(expression);
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
        throw new CpoException(ex);
      }
    }
    return attributes;
  }

  /**
   * The class is loaded via the classpath and then inspected to create attributes based on all "get" methods.
   *
   * @param className The class name
   *
   * @return List of all the attributes
   * @throws CpoException if there's an error creating the CpoClass
   * @throws ClassNotFoundException if the class isn't on the classpath
   */
  public List<CpoAttribute> createAttributesFromClass(String className) throws CpoException, ClassNotFoundException {

    String getMethodPrefix = "get";

    List<CpoAttribute> attributes = new ArrayList<CpoAttribute>();

    Method[] methods = CpoUtilClassLoader.getInstance(this.getClass().getClassLoader()).loadClass(className).getMethods();
    for (Method method : methods) {
      if (method.getName().startsWith(getMethodPrefix)) {
        String attributeName = method.getName().substring(getMethodPrefix.length());

        CpoAttribute attribute = createCpoAttribute();
        attribute.setJavaName(attributeName);
        attribute.setJavaType(method.getReturnType().getName());
        attribute.setDataName(attributeName);
        // TODO - need a reverse mapping for this off the metaDescriptor?
        attribute.setDataType("VARCHAR");
        attribute.setTransformClassName(null);

        attributes.add(attribute);
      }
    }
    return attributes;
  }

  /**
   * Adds the attributes to the cpoClassNode specified if they doesn't already exist.
   *
   * @param cpoClassNode The CpoClassNode
   * @param attributes The CpoAttributes
   * @return true if any attribute was added, false if they already existed
   */
  public boolean addAttributes(CpoClassNode cpoClassNode, List<CpoAttribute> attributes) {
    boolean added = false;

    List<CpoAttribute> ignored = new ArrayList<CpoAttribute>();

    for (CpoAttribute att : attributes) {
      if (addAttribute(cpoClassNode, att)) {
        added = true;
      } else {
        // it was ignored
        ignored.add(att);
      }
    }

    if (!ignored.isEmpty()) {
      StringBuilder message = new StringBuilder();
      message.append("Did not add attributes because they already exist:");
      for (CpoAttribute ignoredAtt : ignored) {
        message.append("\n");
        message.append(ignoredAtt.getJavaName());
      }
      CpoUtil.getInstance().setStatusBarText(message.toString());
    }

    return added;
  }

  /**
   * Invoked when nodes are changed.
   */
  protected void nodeChanged(AbstractCpoNode notifier, AbstractCpoNode changedNode) {
    if (logger.isDebugEnabled()) {
      logger.debug("Proxy got notification of changed object (new) " + changedNode.isNew() + " (remove) " + changedNode.isRemove());
    }
    AbstractCpoNode parent = notifier.getParent();
    if (parent != null) {
      // inform parents about the change
      parent.setChildDirty(changedNode);
      parent.setChildNew(changedNode);
      parent.setChildRemove(changedNode);

      int index = parent.getIndex(notifier);
      if (logger.isDebugEnabled()) {
        logger.debug("Set parent flags - index is: " + index);
      }
      if (index >= 0 && notifier.equals(changedNode)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Index: " + index + " size: " + notifier.getParent().getChildCount());
        }
        if (notifier.isRemove()) {
          logger.debug("Notifying a parent of node (marked deleted) change");
          treeModel.nodeChanged(parent);
        } else if (notifier.isDirty()) {
          if (logger.isDebugEnabled()) {
            logger.debug("Notifying parent of node change (dirty and all else)");
          }
          treeModel.nodeChanged(parent);
        } else if (notifier.isNew()) {
          if (logger.isDebugEnabled()) {
            logger.debug("Notifying parent (" + parent + ") of node insertion (" + changedNode + ")");
          }
          treeModel.nodesWereInserted(parent, new int[]{index});
        }
      } else if (notifier.equals(changedNode)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Object equal to notifier: " + notifier + " : parent: " + parent);
        }
        treeModel.nodeStructureChanged(parent);
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("object not equal to notifier: " + notifier + " : object: " + changedNode);
        }
        treeModel.nodeChanged(parent);
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Parent of " + notifier.getClass() + " was null");
      }
    }
  }

  /**
   * Generates the java source code for the specified class.
   *
   * @param node The CpoClassNode to generate source for
   * @return String containing the java source code.
   */
  public CpoInterfaceSourceGenerator generateInterfaceSourceCode(CpoClassNode node) {
    CpoClass cpoClass = node.getUserObject();

    CpoInterfaceSourceGenerator generator = new CpoInterfaceSourceGenerator(metaDescriptor);
    cpoClass.acceptMetaDFVisitor(generator);
    return generator;
  }

  /**
   * Generates the java source code for the specified class.
   *
   * @param node The CpoClassNode to generate source for
   * @return String containing the java source code.
   */
  public CpoClassSourceGenerator generateLegacyClassSourceCode(CpoClassNode node) {
    CpoClass cpoClass = node.getUserObject();

    CpoClassSourceGenerator generator = new CpoLegacyClassSourceGenerator(metaDescriptor);
    cpoClass.acceptMetaDFVisitor(generator);
    return generator;
  }

  /**
   * Generates the java source code for the specified class.
   *
   * @param node The CpoClassNode to generate source for
   * @return String containing the java source code.
   */
  public CpoClassSourceGenerator generateClassSourceCode(CpoClassNode node) {
    CpoClass cpoClass = node.getUserObject();

    CpoClassSourceGenerator generator = new CpoClassSourceGenerator(metaDescriptor);
    cpoClass.acceptMetaDFVisitor(generator);
    return generator;
  }

  /**
   * Returns a CpoAdapter for the specified connection name
   */
  protected CpoAdapter getCpoAdapter(String connectionName) throws CpoException {

    if (adapterCache.containsKey(connectionName)) {
      return adapterCache.get(connectionName);
    }

    CtDataSourceConfig dataSourceConfig = CpoUtil.getInstance().getDataSourceConfig(connectionName);
    if (dataSourceConfig == null) {
      throw new CpoException("Unable to create connection: "+ connectionName);
    }

    // swap the meta descriptor in
    dataSourceConfig.setMetaDescriptorName(metaDescriptor.getName());

    CpoAdapter cpoAdapter = CpoAdapterFactoryManager.makeCpoAdapterFactory(dataSourceConfig).getCpoAdapter();
    adapterCache.put(connectionName, cpoAdapter);
    return cpoAdapter;
  }

  /**
   * Executes the function group.
   */
  public Collection<?> executeFunctionGroup(String connectionName, Object obj, Object objReturnType, CpoFunctionGroup cpoFGnode, boolean persist) throws CpoException {
    CpoAdapter cpoAdapter = getCpoAdapter(connectionName);

    List<Object> result = new ArrayList<Object>();
    Object resultObj = null;
    if (cpoFGnode.getType().equals(StFunctionGroupType.CREATE.toString())) {
      //insert
      resultObj = cpoAdapter.insertObject(cpoFGnode.getName(), obj);
    } else if (cpoFGnode.getType().equals(StFunctionGroupType.DELETE.toString())) {
      cpoAdapter.deleteObject(cpoFGnode.getName(), obj);
      // retrieve from cpo, so we can verify deletion
      resultObj = cpoAdapter.retrieveBean(cpoFGnode.getName(), obj);
    } else if (cpoFGnode.getType().equals(StFunctionGroupType.LIST.toString())) {
      result = cpoAdapter.retrieveBeans(cpoFGnode.getName(), obj, objReturnType);
    } else if (cpoFGnode.getType().equals(StFunctionGroupType.RETRIEVE.toString())) {
      resultObj = cpoAdapter.retrieveBean(cpoFGnode.getName(), obj);
    } else if (cpoFGnode.getType().equals(StFunctionGroupType.UPDATE.toString())) {
      cpoAdapter.updateObject(cpoFGnode.getName(), obj);
      // retrieve from cpo, so we can verify update
      resultObj = cpoAdapter.retrieveBean(cpoFGnode.getName(), obj);
    } else if (cpoFGnode.getType().equals(StFunctionGroupType.EXIST.toString()) && persist) {
      cpoAdapter.persistObject(cpoFGnode.getName(), obj);
      // retrieve from cpo, so we can verify update
      resultObj = cpoAdapter.retrieveBean(cpoFGnode.getName(), obj);
    } else if (cpoFGnode.getType().equals(StFunctionGroupType.EXIST.toString())) {
      resultObj = cpoAdapter.existsObject(cpoFGnode.getName(), obj);
    }

    // always return a list, even if it's empty
    if (result == null) {
      return new ArrayList<Object>();
    }

    // if there was a result object, add it to the list
    if (resultObj != null) {
      result.add(resultObj);
    }

    return result;
  }

  /**
   * Returns the expression parser to use
   */
  public ExpressionParser getExpressionParser(String expression) throws CpoException {
    ExpressionParser parser = metaDescriptor.getExpressionParser();
    parser.setExpression(expression);
    return parser;
  }
}