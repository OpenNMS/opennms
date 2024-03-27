/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.features.vaadin.jmxconfiggenerator.data.StringRenderer;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.server.Resource;

public class MbeansHierarchicalContainer extends HierarchicalContainer {

	private static final Logger LOG = LoggerFactory.getLogger(MbeansHierarchicalContainer.class);

	private final MBeansItemStrategyHandler itemStrategyHandler = new MBeansItemStrategyHandler();
	private final IconUpdater iconUpdater = new IconUpdater();
	private Map<String, Object> itemIdToDataMapping = new HashMap<>();
	private Map<Object, String> dataToItemIdMapping = new HashMap<>();
	private Collection<Mbean> mbeans = new ArrayList<>();

	public MbeansHierarchicalContainer() {
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.VALID, Boolean.class, Boolean.TRUE);
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.ICON, Resource.class, null);
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.TOOLTIP, String.class, "");
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.SELECTED, Boolean.class, Boolean.FALSE);
		addContainerProperty(MBeansTree.MetaMBeansTreeItem.CAPTION, String.class, "");
	}

	private void clearContainer() {
		mbeans.clear();
		itemIdToDataMapping.clear();
		dataToItemIdMapping.clear();
		removeAllItems();
	}

	protected void updateDataSource(List<Mbean> mbeanList) {
		clearContainer();
		for (Mbean eachMbean : mbeanList) {
			add(eachMbean);
		}
		sort(new Object[]{MBeansTree.MetaMBeansTreeItem.CAPTION}, new boolean[]{true});
	}

	@Override
	public Object firstItemId() {
		if (size() > 0) {
			List<Object> rootItemIds = new ArrayList<>(rootItemIds());
			Collections.sort(rootItemIds, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((String)o1).compareTo((String)o2);
				}
			});
			return rootItemIds.get(0);
		}
		return null;
	}

	public Collection<Mbean> getMBeans() {
		return this.mbeans;
	}

	private void add(Mbean bean) {
		String objectName = bean.getObjectname();
		if (Strings.isNullOrEmpty(objectName)) return;
		if (!objectName.contains(":")) return;
		addNodes(bean);
		mbeans.add(bean);
	}


	@Override
	public String toString() {
		final StringBuilder string = new StringBuilder();
		string.append(getClass().getName() + ":\n");
		for (Object eachRootItemId : rootItemIds()) {
			string.append(printInternalTree(eachRootItemId, 0));
			string.append("\n");
		}
		return string.toString().trim();
	}

	public boolean isSelected(Object itemId) {
		return (Boolean) getItem(itemId).getItemProperty(MBeansTree.MetaMBeansTreeItem.SELECTED).getValue();
	}

	public Collection<Mbean> getSelectedMbeans() {
		return Collections2.filter(getMBeans(), new com.google.common.base.Predicate<Mbean>() {
			@Override
			public boolean apply(Mbean input) {
				return isSelected(dataToItemIdMapping.get(input));
			}
		});
	}

	private String addNodes(List nodesToAdd) {
		String rootItemId = null;
		Iterator it = nodesToAdd.iterator();
		while (it.hasNext()) {
			Object eachNode = it.next();
			boolean hasChildren = it.hasNext();
			if (eachNode instanceof Mbean) {
				hasChildren |= ((Mbean)eachNode).getCompAttribList() != null && !((Mbean) eachNode).getCompAttribList().isEmpty();
			}
			rootItemId = addNode(rootItemId, eachNode, hasChildren);
		}
		return rootItemId;
	}

	private String addNode(String rootItemId, Object node, boolean hasChildren) {
		String itemId = buildItemId(node);
		if (rootItemId != null) {
			itemId = rootItemId + "." + itemId;
		}
		addItem(rootItemId, itemId, node, hasChildren);
		return itemId;
	}

	private void addNodes(Mbean mbean) {
		List newNodeList = MBeansHelper.getMBeansTreeElements(mbean);
		newNodeList.add(mbean);
		String rootItemId = addNodes(newNodeList);

		// add optional comp attributes, if there are any
		if (mbean.getCompAttribList() != null && !mbean.getCompAttribList().isEmpty()) {
			for (CompAttrib eachCompAttrib : mbean.getCompAttribList()) {
				addNode(rootItemId, eachCompAttrib, false);
			}
		}
	}

	private String printInternalTree(Object itemId, int depth) {
		String tabs = "";
		String ret = "";
		for (int i = 0; i < depth; i++) {
			tabs += "    ";
		}
		Object data = itemIdToDataMapping.get(itemId);
		String nodeDataAsString = data.toString();
		if (data instanceof Mbean) {
			nodeDataAsString = ((Mbean) data).getObjectname();
		}
		if (data instanceof CompAttrib) {
			nodeDataAsString = ((CompAttrib) data).getName();
		}
		ret += tabs + nodeDataAsString + "\n";
		if (hasChildren(itemId)) {
			for (Object eachChildItemId : getChildren(itemId)) {
				ret += printInternalTree(eachChildItemId, depth + 1);
			}
		}
		return ret;
	}

	private void addItem(String parentItemId, String childItemId, Object childData, boolean hasChildren) {
		if (!containsId(childItemId)) {
			LOG.debug("Adding child {} to parent {}.", childItemId, parentItemId);
			Item item = addItem(childItemId);
			if (item == null) {
				LOG.error("Could not add item with item id {}.", childItemId);
			} else {
				itemIdToDataMapping.put(childItemId, childData); // 1:1 mapping
				dataToItemIdMapping.put(childData, childItemId); // kein 1:1 mapping, n:1 mapping
				setItemProperties(item, childData);
				setParent(childItemId, parentItemId);
				//if we do not set childrenAllowed a "expand/collapse" icon is shown. We do not want that
				setChildrenAllowed(childItemId, hasChildren);
			}
		} else {
			LOG.debug("Child with id {} already added.", childItemId);
		}
	}

	private String buildItemId(Object data) {
		StringRenderer renderer = getStringRenderer(data.getClass());
		String itemId = renderer.render(data);
		return itemId;
	}

	private void setItemProperties(Item item, Object itemData) {
		itemStrategyHandler.setItemProperties(item, itemData);
		iconUpdater.updateIcon(item, (Boolean) item.getItemProperty(MBeansTree.MetaMBeansTreeItem.SELECTED).getValue());
	}

	private StringRenderer getStringRenderer(Class<?> clazz) {
		return itemStrategyHandler.getStringRenderer(clazz);
	}

	public Object getDataFor(String itemId) {
		return itemIdToDataMapping.get(itemId);
	}

	public String getItemIdFor(Object data) {
		return dataToItemIdMapping.get(data);
	}
}
