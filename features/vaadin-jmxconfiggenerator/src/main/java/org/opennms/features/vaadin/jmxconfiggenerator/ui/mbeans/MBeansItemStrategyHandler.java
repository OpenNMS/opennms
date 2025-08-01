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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.features.vaadin.jmxconfiggenerator.Config;
import org.opennms.features.vaadin.jmxconfiggenerator.data.Reflections;
import org.opennms.features.vaadin.jmxconfiggenerator.data.StringRenderer;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

import com.vaadin.v7.data.Item;
import com.vaadin.server.FontAwesome;

/**
 *
 * @author Markus von Rüden
 */
class MBeansItemStrategyHandler {

	private final Map<Class<?>, ItemStrategy> propertyStrategy = new HashMap<Class<?>, ItemStrategy>();
	private final Map<Class<?>, StringRenderer<?>> extractors = new HashMap<Class<?>, StringRenderer<?>>();

	public MBeansItemStrategyHandler() {
		propertyStrategy.put(Map.Entry.class, new EntryItemStrategy());
		propertyStrategy.put(Mbean.class, new MBeanItemStrategy());
		propertyStrategy.put(String.class, new StringItemStrategy());
		propertyStrategy.put(CompAttrib.class, new CompAttribItemStrategy());

		//add extractors, is needed for comparison (so tree is sorted alphabetically)
		extractors.put(String.class, new StringRenderer<String>() {
			@Override
			public String render(String input) {
				return input;
			}
		});
		extractors.put(Mbean.class, new StringRenderer<Mbean>() {
			@Override
			public String render(Mbean input) {
				return MBeansHelper.getLeafLabel(input);
			}
		});
		extractors.put(Entry.class, new StringRenderer<Entry>() {
			@Override
			public String render(Entry entry) {
				return (String) entry.getValue();
			}
		});
		extractors.put(CompAttrib.class, new StringRenderer<CompAttrib>() {

			@Override
			public String render(CompAttrib input) {
				return input.getName();
			}
		});
	}

	protected ItemStrategy getStrategy(Class<?> clazz) {
		return getValueForClass(propertyStrategy, clazz);
	}

	protected StringRenderer getStringRenderer(Class<?> clazz) {
		return getValueForClass(extractors, clazz);
	}

	protected void setItemProperties(Item item, Object itemId) {
		if (itemId == null || item == null) return;
		final ItemStrategy strategy = getStrategy(itemId.getClass());
		strategy.setItemProperties(item, itemId);
	}

	protected interface ItemStrategy {

		void setItemProperties(Item item, Object itemId);

	}

	private static class StringItemStrategy implements ItemStrategy {
		@Override
		public void setItemProperties(Item item, Object itemId) {
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.ICON).setValue(Config.Icons.DUMMY);
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.CAPTION).setValue(itemId);
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.TOOLTIP).setValue(itemId);
		}
	}

	private static class EntryItemStrategy implements ItemStrategy {
		@Override
		public void setItemProperties(Item item, Object itemId) {
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.ICON).setValue(Config.Icons.DUMMY);
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.CAPTION).setValue(((Map.Entry) itemId).getValue());
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.TOOLTIP).setValue(((Map.Entry) itemId).getValue());
		}
	}

	private static class MBeanItemStrategy implements ItemStrategy {
		@Override
		public void setItemProperties(Item item, Object itemId) {
			if (!(itemId instanceof Mbean)) return;
			Mbean bean = (Mbean) itemId;
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.ICON).setValue(FontAwesome.SITEMAP);
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.TOOLTIP).setValue(bean.getObjectname());
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.CAPTION).setValue(MBeansHelper.getLeafLabel(bean));
		}
	}

	private static class CompAttribItemStrategy implements ItemStrategy  {
		@Override
		public void setItemProperties(Item item, Object itemId) {
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.ICON).setValue(Config.Icons.DUMMY);
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.CAPTION).setValue(((CompAttrib) itemId).getName());
			item.getItemProperty(MBeansTree.MetaMBeansTreeItem.TOOLTIP).setValue(((CompAttrib) itemId).getName());
		}
	}

	/**
	 * Builds the class hierarchy of the given <code>clazz</code> and returns
	 * the value of the given map if any class in the hierarchy of
	 * <code>clazz</code> is registered as a key to the map.
	 *
	 * @param <T>
	 *            type of the value in the map
	 * @param map
	 *            a map to lookup for any class in <code>clazz</code> hierarchy.
	 * @param clazz
	 *            the class to look up any value in <code>map</code>
	 * @return T if a key is found in <code>map</code>, otherwise null.
	 */
	protected static <T> T getValueForClass(Map<Class<?>, T> map, Class<?> clazz) {
		List<Class<?>> classes = Reflections.buildClassHierarchy(clazz);
		for (int i = classes.size() - 1; i >= 0; i--) {
			if (map.get(classes.get(i)) != null) return map.get(classes.get(i));
		}
		return null;
	}
}
