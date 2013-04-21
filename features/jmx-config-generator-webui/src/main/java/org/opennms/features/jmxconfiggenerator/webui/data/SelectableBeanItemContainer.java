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

package org.opennms.features.jmxconfiggenerator.webui.data;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractInMemoryContainer;
import com.vaadin.data.util.VaadinPropertyDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a vaadin container (data source). Therefore it extends
 * <code>AbstractInMemoryContainer</code>. Usually
 * <code>BeanItemContainer</code> or
 * <code>AbstractBeanContainer</code> would be the best container to use. But we need to add a property to the
 * container items which indicates if any item is selected. Due to some limitations we cannot inherit BeanItemContainer
 * or AbstractBeanContainer to fulfill this requirement. Therefore this class is mainly a rough copy of the
 * <code>BeanItemContainer</code> but does not support any kind of filtering. This may be included in future releases.
 *
 *
 * @param <T> the type of the bean we want to store in SelectableItem
 * @author Markus von Rüden
 * @see SelectableItem
 */
public class SelectableBeanItemContainer<T> extends AbstractInMemoryContainer<T, String, SelectableItem<T>> {

	/**
	 * Mapping of an itemId to a
	 * <code>SelectableItem</code>
	 */
	private final Map<T, SelectableItem<T>> itemIdToItem = new HashMap<T, SelectableItem<T>>();
	/**
	 * We build a model to make a mapping between bean objects and the selected property. In this case we do not have to
	 * deal with reading and writing the data to the underlying been.
	 */
	private final Map<String, VaadinPropertyDescriptor> model;
	/**
	 * The type of our bean
	 */
	private final Class<? super T> type;

	public SelectableBeanItemContainer(Class<? super T> type) {
		model = SelectableItem.getPropertyDescriptors(type);
		this.type = type;
	}

	@Override
	protected SelectableItem<T> getUnfilteredItem(Object itemId) {
		return itemIdToItem.get(itemId);
	}

	@Override
	public Collection<String> getContainerPropertyIds() {
		return model.keySet();
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		Item item = getItem(itemId);
		if (item == null) return null;
		return item.getItemProperty(propertyId);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		return model.get(propertyId).getPropertyType();
	}

	private boolean isValid(Object itemId) {
		return itemId != null && type.isAssignableFrom(itemId.getClass());
	}

	@Override
	public Item addItemAt(int index, Object newItemId) {
		if (!isValid(newItemId)) return null;
		return internalAddItemAt(index, (T) newItemId, createItem((T) newItemId), true);
	}

	private SelectableItem<T> createItem(T itemId) {
		if (itemId == null) return null;
		return new SelectableItem<T>(itemId, model);
	}

	@Override
	public Item addItemAfter(Object previousItemId, Object newItemId) {
		if (!isValid(previousItemId) || !isValid(newItemId)) return null;
		return internalAddItemAfter((T) previousItemId, (T) newItemId, createItem((T) newItemId), true);
	}

	@Override
	public Item addItem(Object itemId) {
		if (!isValid(itemId)) return null;
		return internalAddItemAtEnd((T) itemId, createItem((T) itemId), true);
	}

	@Override
	public boolean removeItem(Object itemId) {
		int position = indexOfId(itemId);
		if (internalRemoveItem(itemId)) {
			itemIdToItem.remove(itemId);
			fireItemRemoved(position, itemId);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAllItems() {
		internalRemoveAllItems();
		itemIdToItem.clear();
		fireItemSetChange();
		return true;
	}

	@Override
	protected void registerNewItem(int position, T itemId, SelectableItem<T> item) {
		itemIdToItem.put(itemId, item);
	}

	/**
	 * we do not allow adding additional properties to the container.
	 */
	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Adding container properties not supported. Override the addContainerProperty() method if required.");
	}

	/**
	 * We do not allow removing properties from the container. This may change in future releases.
	 */
	@Override
	public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Removing container properties not supported. Override the addContainerProperty() method if required.");
	}
}
