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

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opennms.features.jmxconfiggenerator.webui.data.JmxCollectionCloner;
import org.opennms.features.jmxconfiggenerator.webui.data.SelectableBeanItemContainer;
import org.opennms.features.jmxconfiggenerator.webui.data.UiModel;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaMBeanItem;
import org.opennms.features.jmxconfiggenerator.webui.data.StringRenderer;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.MBeansController.AttributesContainerCache;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

/**
 *
 * @author Markus von Rüden
 */
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
		addContainerProperty(MetaMBeanItem.ICON, Resource.class, null);
		addContainerProperty(MetaMBeanItem.NAME, String.class, "");
		addContainerProperty(MetaMBeanItem.TOOLTIP, String.class, "");
		addContainerProperty(MetaMBeanItem.SELECTED, Boolean.class, true);
		addContainerProperty(MetaMBeanItem.OBJECTNAME, String.class, "");
		addContainerProperty(MetaMBeanItem.CAPTION, String.class, "");
	}

	public void updateDataSource(UiModel model) {
		mbeans.clear();
		buildInternalTree(model);
		updateContainer();
	}

	private void buildInternalTree(UiModel model) {
		root = new TreeNodeImpl();
		for (Mbean bean : getMBeans(model))
			add(bean);
	}

	public Collection<Mbean> getMBeans() {
		return this.mbeans;
	}

	private List<Mbean> getMBeans(UiModel model) {
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

	private void addNodes(Mbean bean) {
		TreeNode root = this.root;
		for (Object node : MBeansHelper.getMBeansTreeElements(bean))
			root = addChild(root, node);
		addChild(root, bean);
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
