/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import java.util.List;

/**
 * This interface defines a tree node to provide a simple tree implementation. A node can have none or multiple child. A
 * node can be a root or a leaf node. A node can also have user data to save (getData()).
 *
 * @author Markus von Rüden
 */
public interface TreeNode {

	/**
	 * Defines if the
	 * <code>TreeNode</code> is a root node
	 */
	boolean isRoot();

	/**
	 * Indicates weather this
	 * <code>TreeNode</code> has children or not. *
	 */
	boolean hasChildren();

	/**
	 * Adds a child (
	 * <code>TreeNode</codE>) to the current
	 * <code>TreeNode</code>
	 */
	void addChild(TreeNode child);

	/**
	 * Sets the new parent of the current
	 * <code>TreeNode</code>
	 */
	void setParent(TreeNode parent);

	/**
	 * Gets the parent of this
	 * <code>TreeNode</code>. Returns <b>null</b> if it is a root node
	 *
	 * @return the parent node or null if root
	 */
	TreeNode getParent();

	/**
	 * Assigns user data to the
	 * <code>TreeNode</code>
	 *
	 *
	 * @param data any user object which represents the node (e.g. a String, or any other JAVA object)
	 */
	void setData(Object data);

	/**
	 * @return the stored user data or null if no user data is saved
	 */
	Object getData();

	/**
	 * @return all children of this node, or an empty list if there are no children. Should NEVER return null.
	 */
	List<TreeNode> getChildren();
}
