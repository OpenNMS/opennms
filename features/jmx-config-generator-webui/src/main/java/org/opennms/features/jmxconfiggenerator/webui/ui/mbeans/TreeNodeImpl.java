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

import java.util.ArrayList;
import java.util.List;

/**
 * Simple straight forward implementation of {@link TreeNode}.
 *
 * @author Markus von Rüden
 */
public class TreeNodeImpl implements TreeNode {

	/**
	 * Stores the children of this node.
	 */
	private final List<TreeNode> children = new ArrayList<TreeNode>();
	/**
	 * Stores the parent of this node. If is null, current node is a root node.
	 */
	private TreeNode parent = null;
	/**
	 * Stores the user data of this node.
	 */
	private Object data;

	public TreeNodeImpl() {
		this(null, null);
	}

	public TreeNodeImpl(TreeNode parent, Object data) {
		this.parent = parent;
		this.data = data;
	}

	public TreeNodeImpl(Object data) {
		this(null, data);
	}

	@Override
	public boolean isRoot() {
		return parent == null;
	}

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	public void addChild(TreeNode child) {
		if (children.contains(child)) return;
		children.add(child);
	}

	@Override
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public Object getData() {
		return this.data;
	}

	@Override
	public List<TreeNode> getChildren() {
		return this.children;
	}
}
