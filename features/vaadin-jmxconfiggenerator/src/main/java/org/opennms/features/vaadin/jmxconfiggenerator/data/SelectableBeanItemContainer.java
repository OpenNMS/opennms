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
package org.opennms.features.vaadin.jmxconfiggenerator.data;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.AbstractInMemoryContainer;
import com.vaadin.v7.data.util.VaadinPropertyDescriptor;

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
	 * We do not allow adding additional properties to the container.
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

	public boolean isSelected(T itemId) {
		return itemIdToItem.get(itemId).isSelected();
	}

	public Collection<T> getSelectedAttributes() {
		return Collections2.filter(itemIdToItem.keySet(), new Predicate<T>() {
			@Override
			public boolean apply(T input) {
				return isSelected(input);
			}
		});
	}

	public void selectAllItems(boolean select) {
		for (Object eachItemId : getItemIds()) {
			itemIdToItem.get(eachItemId).setSelected(select);
		}
		fireItemSetChange();
	}
}
