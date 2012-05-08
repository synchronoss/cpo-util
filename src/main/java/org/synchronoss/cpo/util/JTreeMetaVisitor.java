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

import org.synchronoss.cpo.MetaVisitor;
import org.synchronoss.cpo.meta.domain.*;

/**
 * JTree builder for meta objects
 *
 * @author Michael Bellomo
 * @since 4/24/12
 */
public class JTreeMetaVisitor implements MetaVisitor {

  protected CpoRootNode rootNode;

  protected CpoClassNode currentClassNode;
  protected CpoFunctionGroupNode currentFunctionGroupNode;
  protected CpoFunctionNode currentFunctionNode;

  public JTreeMetaVisitor(CpoRootNode rootNode) {
    this.rootNode = rootNode;
  }

  @Override
  public void visit(CpoClass cpoClass) {
    CpoClassNode classNode = rootNode.getProxy().createClassNode(cpoClass);
    classNode.setProtected(CpoUtil.getInstance().isClassProtected(cpoClass));

    // add the node to the tree
    rootNode.add(classNode);

    // save the reference
    currentClassNode = classNode;
  }

  @Override
  public void visit(CpoAttribute cpoAttribute) {
    if (currentClassNode != null) {
      CpoAttributeNode attributeNode = rootNode.getProxy().createAttributeNode(cpoAttribute);
      currentClassNode.getAttributeLabelNode().add(attributeNode);
    }
  }

  @Override
  public void visit(CpoFunctionGroup cpoFunctionGroup) {
    if (currentClassNode != null) {
      CpoFunctionGroupNode functionGroupNode = rootNode.getProxy().createFunctionGroupNode(cpoFunctionGroup);
      currentClassNode.getFunctionGroupLabelNode().add(functionGroupNode);

      // save the reference
      currentFunctionGroupNode = functionGroupNode;
    }
  }

  @Override
  public void visit(CpoFunction cpoFunction) {
    if (currentFunctionGroupNode != null) {
      CpoFunctionNode functionNode = rootNode.getProxy().createFunctionNode(cpoFunction);
      currentFunctionGroupNode.add(functionNode);

      // save the reference
      currentFunctionNode = functionNode;
    }
  }

  @Override
  public void visit(CpoArgument cpoArgument) {
    if (currentFunctionNode != null) {
      CpoArgumentNode argumentNode = rootNode.getProxy().createArgumentNode(cpoArgument);
      currentFunctionNode.add(argumentNode);
    }
  }
}

