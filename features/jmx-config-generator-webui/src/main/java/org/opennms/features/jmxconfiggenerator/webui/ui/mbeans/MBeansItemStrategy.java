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

import com.vaadin.data.Item;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaMBeanItem;
import org.opennms.features.jmxconfiggenerator.webui.data.StringRenderer;
import org.opennms.features.jmxconfiggenerator.webui.ui.IconProvider;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

/**
 *
 * @author Markus von Rüden
 */
//TODO mvonrued -> comment
class MBeansItemStrategyHandler {

	final private Map<Class<?>, ItemStrategy> propertyStrategy = new HashMap<Class<?>, ItemStrategy>();
	private final Map<Class<?>, StringRenderer<?>> extractors = new HashMap<Class<?>, StringRenderer<?>>();

	public MBeansItemStrategyHandler() {
		propertyStrategy.put(Map.Entry.class, new EntryItemStrategy());
		propertyStrategy.put(Mbean.class, new MBeanItemStrategy());
		propertyStrategy.put(String.class, new StringItemStrategy());

		//add extractors, is needed for comparsion (so tree is sorted alphabetically)
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
	}

	protected ItemStrategy getStrategy(Class<?> clazz) {
		return MBeansHelper.getValueForClass(propertyStrategy, clazz);
	}

	protected StringRenderer getStringRenderer(Class<?> clazz) {
		return MBeansHelper.getValueForClass(extractors, clazz);
	}

	protected void setItemProperties(Item item, Object itemId) {
		if (itemId == null || item == null) return;
		getStrategy(itemId.getClass()).setItemProperties(item, itemId);
	}

	protected static interface ItemStrategy {

		void setItemProperties(Item item, Object itemId);

		Object[] getVisibleColumns();

		void handleSelectDeselect(Item item, Object itemId, boolean select);

		void updateIcon(Item item);

		void updateModel(Item item, Object itemId);
	}

	private static class StringItemStrategy implements ItemStrategy {

		@Override
		public void setItemProperties(Item item, Object itemId) {
			item.getItemProperty(MetaMBeanItem.ICON).setValue(IconProvider.getIcon(IconProvider.PACKAGE_ICON));
			item.getItemProperty(MetaMBeanItem.CAPTION).setValue(itemId);
			item.getItemProperty(MetaMBeanItem.TOOLTIP).setValue(itemId);
		}

		@Override
		public Object[] getVisibleColumns() {
			return new Object[]{MetaMBeanItem.CAPTION};
		}

		@Override
		public void handleSelectDeselect(Item item, Object itemId, boolean select) {
			; //do nothing
		}

		@Override
		public void updateIcon(Item item) {
			; //do nothing
		}

		@Override
		public void updateModel(Item item, Object itemId) {
			; //read only
		}
	}

	private static class EntryItemStrategy implements ItemStrategy {

		@Override
		public void setItemProperties(Item item, Object itemId) {
			item.getItemProperty(MetaMBeanItem.ICON).setValue(IconProvider.getIcon(IconProvider.PACKAGE_ICON));
			item.getItemProperty(MetaMBeanItem.CAPTION).setValue(((Map.Entry) itemId).getValue());
			item.getItemProperty(MetaMBeanItem.TOOLTIP).setValue(((Map.Entry) itemId).getValue());
		}

		@Override
		public Object[] getVisibleColumns() {
			return new Object[]{MetaMBeanItem.CAPTION};
		}

		@Override
		public void handleSelectDeselect(Item item, Object itemId, boolean select) {
			; //do nothing
		}

		@Override
		public void updateIcon(Item item) {
			; //do nothing
		}

		@Override
		public void updateModel(Item item, Object itemId) {
			; //read only
		}
	}

	private static class MBeanItemStrategy implements ItemStrategy {

		@Override
		public void setItemProperties(Item item, Object itemId) {
			if (!(itemId instanceof Mbean)) return;
			Mbean bean = (Mbean) itemId;
			item.getItemProperty(MetaMBeanItem.ICON).setValue(IconProvider.getIcon(IconProvider.MBEANS_ICON));
			item.getItemProperty(MetaMBeanItem.OBJECTNAME).setValue(bean.getObjectname());
			item.getItemProperty(MetaMBeanItem.NAME).setValue(bean.getName());
			item.getItemProperty(MetaMBeanItem.TOOLTIP).setValue(bean.getObjectname());
			item.getItemProperty(MetaMBeanItem.CAPTION).setValue(MBeansHelper.getLeafLabel(bean));
		}

		@Override
		public Object[] getVisibleColumns() {
			return new Object[]{MetaMBeanItem.SELECTED, MetaMBeanItem.OBJECTNAME, MetaMBeanItem.NAME};
		}

		@Override
		public void handleSelectDeselect(Item item, Object itemId, boolean selected) {
			item.getItemProperty(MetaMBeanItem.SELECTED).setValue(selected);
			updateIcon(item, selected);
		}

		private void updateIcon(Item item, boolean selected) {
			item.getItemProperty(MetaMBeanItem.ICON).setValue(IconProvider.getMBeansIcon(selected));
		}

		@Override
		public void updateIcon(Item item) {
			updateIcon(item, (Boolean) item.getItemProperty(MetaMBeanItem.SELECTED).getValue());
		}

		@Override
		public void updateModel(Item item, Object itemId) {
			if (itemId == null || !(itemId instanceof Mbean)) return;
			Mbean bean = (Mbean)itemId;
			bean.setName((String)item.getItemProperty(MetaMBeanItem.NAME).getValue());
		}
	}
}
