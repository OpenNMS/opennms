/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.Resource;
import org.opennms.features.vaadin.jmxconfiggenerator.data.StringRenderer;
import org.opennms.features.vaadin.jmxconfiggenerator.data.UiModel;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MbeansHierarchicalContainer extends HierarchicalContainer {

	private class TreeNodeComparator implements Comparator<TreeNode> {

		@Override
		public int compare(TreeNode o1, TreeNode o2) {
			String s1 = o1 == null ? "" : getStringComparable(o1.getData());
			String s2 = o2 == null ? "" : getStringComparable(o2.getData());
			return s1.compareTo(s2);
		}
		
		private String getStringComparable(Object data) {
			if (data == null) return "";
			StringRenderer renderer = controller.getStringRenderer(data.getClass());
			return renderer == null ? data.toString() : renderer.render(data);
		}
	}
	
	private final MBeansController controller;
	private TreeNode root = new TreeNodeImpl();
	private Collection<Mbean> mbeans = new ArrayList<Mbean>();

	public MbeansHierarchicalContainer(MBeansController controller) {
		this.controller = controller;
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.VALID, Boolean.class, Boolean.TRUE);
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.ICON, Resource.class, null);
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.TOOLTIP, String.class, "");
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.SELECTED, Boolean.class, Boolean.TRUE);
		//addContainerProperty(MBeansTree.MetaMBeansTreeItem.PARTIALLY_SELECTED, Boolean.class, Boolean.FALSE);
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.CAPTION, String.class, "");
	}

	public void updateDataSource(UiModel model) {
		mbeans.clear();
		buildInternalTree(model);
		updateContainer();
	}

	private void buildInternalTree(UiModel model) {
		root = new TreeNodeImpl();
		for (Mbean bean : extractMbeans(model)) {
			add(bean);
		}
	}

	public Collection<Mbean> getMBeans() {
		return this.mbeans;
	}

	private static List<Mbean> extractMbeans(UiModel model) {
		return model.getRawModel().getJmxCollection().get(0).getMbeans().getMbean();
	}

	private void add(Mbean bean) {
		String objectName = bean.getObjectname();
		if (Strings.isNullOrEmpty(objectName)) return;
		if (!objectName.contains(":")) return;
		addNodes(bean);
		mbeans.add(bean);
	}

	private void updateContainer() {
		removeAllItems();
		updateChildren(this, root);
	}

	@Override
	public String toString() {
		return getClass().getName() + ":\n" + printInternalTree(root, 0);
	}

	public boolean isSelected(Object itemId) {
		return (Boolean) getItem(itemId).getItemProperty(MBeansTree.MetaMBeansTreeItem.SELECTED).getValue();
	}

	public Collection<Mbean> getSelectedMbeans() {
		return Collections2.filter(getMBeans(), new com.google.common.base.Predicate<Mbean>() {
			@Override
			public boolean apply(Mbean input) {
				return isSelected(input);
			}
		});
	}

	private TreeNode addNodes(TreeNode rootNode, Object... childData)  {
		for (Object node : childData)
			rootNode = addChild(rootNode, node);
		return rootNode;
	}

	private void addNodes(Mbean mbean) {
		List newNodeList = MBeansHelper.getMBeansTreeElements(mbean);
		newNodeList.add(mbean);
		TreeNode mbeanNode = addNodes(this.root, newNodeList.toArray());

		// add optional comp attributes, if there are any
		if (mbean.getCompAttrib() != null && !mbean.getCompAttrib().isEmpty()) {
			for (CompAttrib eachCompAttrib : mbean.getCompAttrib()) {
				addNodes(mbeanNode, eachCompAttrib);
			}
		}
	}

	private String printInternalTree(TreeNode node, int depth) {
		String tabs = "";
		String ret = "";
		for (int i = 0; i < depth; i++)
			tabs += "    ";
		ret += tabs + node.getData() + "\n";
		for (TreeNode n : node.getChildren())
			ret += printInternalTree(n, depth + 1);
		return ret;
	}

	private TreeNode addChild(TreeNode root, Object childData) {
		TreeNode node = findNodeForData(root, childData);
		if (node != null)
			return node; //childData already there
		//childData does not exist, so create it
		node = new TreeNodeImpl(root, childData);
		root.addChild(node);
		return node;
	}

	private TreeNode findNodeForData(TreeNode root, Object data) {
		if (root == null) return null;
		if (root.getData() != null && root.getData().equals(data)) return root;
		for (TreeNode node : root.getChildren()) {
			if (node.getData() != null && node.getData().equals(data))
				return node;
		}
		return null;
	}

	private void addItem(HierarchicalContainer container, TreeNode root, TreeNode child) {
		Item item = container.addItem(child.getData());
		controller.setItemProperties(item, child.getData());
		container.setParent(child.getData(), root.getData());
		//if we do not set childrenAllowed a "expand/collapse" icon is shown. We do not want that
		container.setChildrenAllowed(child.getData(), child.hasChildren());
	}

	private void updateChildren(HierarchicalContainer container, TreeNode root) {
		Collections.sort(root.getChildren(), new TreeNodeComparator());
		for (TreeNode child : root.getChildren()) {
			addItem(container, root, child);
			updateChildren(container, child);
		}
	}
}
