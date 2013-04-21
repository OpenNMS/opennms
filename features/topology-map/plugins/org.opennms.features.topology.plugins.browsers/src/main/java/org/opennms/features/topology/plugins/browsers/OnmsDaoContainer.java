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

package org.opennms.features.topology.plugins.browsers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Order;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionNotifier;
import org.opennms.netmgt.dao.OnmsDao;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

public abstract class OnmsDaoContainer<T,K extends Serializable> implements SelectionNotifier, SelectionListener, Container, Container.Sortable, Container.Ordered, Container.ItemSetChangeNotifier {

	private static final long serialVersionUID = -9131723065433979979L;

	private final OnmsDao<T,K> m_dao;

	/**
	 * TODO: Fix concurrent access to this field
	 */
	protected Criteria m_criteria = new Criteria(getItemClass());

	/**
	 * TODO: Fix concurrent access to this field
	 */
	private final Collection<ItemSetChangeListener> m_itemSetChangeListeners = new HashSet<ItemSetChangeListener>();

	/**
	 * TODO: Fix concurrent access to this field
	 */
	private Collection<SelectionListener> m_selectionListeners = new HashSet<SelectionListener>();

	public OnmsDaoContainer(OnmsDao<T,K> dao) {
		m_dao = dao;
	}

	@Override
	public final boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot add new properties to objects in this container");
	}

	/**
	 * Can be overridden if you want to support adding items.
	 */
	@Override
	public Object addItem() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot add new items to this container");
	}

	/**
	 * Can be overridden if you want to support adding items.
	 */
	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot add new items to this container");
	}

	@Override
	public boolean containsId(Object itemId) {
		if (itemId == null) {
			return false;
		}
		return m_dao.get((K)itemId) != null;
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		Item item = getItem(itemId);
		return item.getItemProperty(propertyId);
	}

	@Override
	public abstract Collection<?> getContainerPropertyIds();

	@Override
	public Item getItem(Object itemId) {
		if (itemId == null) {
			return null;
		}
		T bean = m_dao.get((K)itemId);
		return new BeanItem<T>(bean);
	}

	@Override
	public Collection<?> getItemIds() {
		List<T> beans = m_dao.findMatching(m_criteria);
		List<K> retval = new ArrayList<K>();
		for (T bean : beans) {
			retval.add(getId(bean));
		}
		return retval;
	}

	protected abstract K getId(T bean);

	public abstract Class<T> getItemClass();

	@Override
	public abstract Class<?> getType(Object propertyId);

	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {
		m_dao.clear();
		return true;
	}

	@Override
	public final boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot remove properties from objects in this container");
	}

	@Override
	public boolean removeItem(Object itemId) throws UnsupportedOperationException {
		m_dao.delete((K)itemId);
		return true;
	}

	@Override
	public int size() {
		return m_dao.countMatching(m_criteria);
	}

	@Override
	public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot add new items to this container");
	}

	@Override
	public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot add new items to this container");
	}

	/**
	 * Very inefficient default behavior, override in subclasses.
	 */
	@Override
	public Object firstItemId() {
		Iterator<T> itr = m_dao.findMatching(m_criteria).iterator();
		if (itr.hasNext()) {
			return getId(itr.next());
		} else {
			return null;
		}
	}

	/**
	 * Very inefficient default behavior, override in subclasses.
	 */
	@Override
	public boolean isFirstId(Object itemId) {
		Object firstItemId = firstItemId();
		if (firstItemId == null) {
			return false;
		} else {
			return firstItemId.equals(itemId);
		}
	}

	/**
	 * Very inefficient default behavior, override in subclasses.
	 */
	@Override
	public boolean isLastId(Object itemId) {
		Object lastItemId = lastItemId();
		if (lastItemId == null) {
			if (itemId == null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (itemId == null) {
				return false;
			} else {
				return lastItemId.equals(itemId);
			}
		}
		
	}

	/**
	 * Very inefficient default behavior, override in subclasses.
	 */
	@Override
	public Object lastItemId() {
		List<T> all = m_dao.findMatching(m_criteria);
		if (all.size() > 0) {
			return getId(all.get(all.size() - 1));
		} else {
			return null;
		}
	}

	/**
	 * Very inefficient default behavior, override in subclasses.
	 */
	@Override
	public Object nextItemId(Object itemId) {
		if (itemId == null) {
			return null;
		}

		Iterator<T> itr = m_dao.findMatching(m_criteria).iterator();
		do {
			if (itemId.equals(getId(itr.next()))) {
				if (itr.hasNext()) {
					return getId(itr.next());
				} else {
					return null;
				}
			}
		} while (itr.hasNext());
		return null;
	}

	/**
	 * Very inefficient default behavior, override in subclasses.
	 */
	@Override
	public Object prevItemId(Object itemId) {
		if (itemId == null) {
			return null;
		}

		Iterator<T> itr = m_dao.findMatching(m_criteria).iterator();
		T previous = null;
		do {
			T current = (T)itr.next();
			if (itemId.equals(getId(current))) {
				return previous;
			}
			previous = current;
		} while (itr.hasNext());
		return null;
	}

	/**
	 * This function returns {@link #getContainerPropertyIds()}.
	 */
	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		return this.getContainerPropertyIds();
	}

	@Override
	public void sort(Object[] propertyId, boolean[] ascending) {
		if (propertyId.length > ascending.length) {
			throw new IllegalArgumentException("Property list and ascending list are different sizes");
		}

		List<Order> orders = new ArrayList<Order>();
		for(int i = 0; i < propertyId.length; i++) {
			if (ascending[i]) {
				orders.add(Order.asc((String)propertyId[i]));
			} else {
				orders.add(Order.desc((String)propertyId[i]));
			}
		}
		m_criteria.setOrders(orders);
	}

	@Override
	public abstract void selectionChanged(SelectionContext selectionContext);

	@Override
	public void addListener(ItemSetChangeListener listener) {
		m_itemSetChangeListeners.add(listener);
	}

	@Override
	public void removeListener(ItemSetChangeListener listener) {
		m_itemSetChangeListeners.remove(listener);
	}

	protected void fireItemSetChangedEvent() {
		ItemSetChangeEvent event = new ItemSetChangeEvent() {
			private static final long serialVersionUID = -2796401359570611938L;
			@Override
			public Container getContainer() {
				return OnmsDaoContainer.this;
			}
		};
		for (ItemSetChangeListener listener : m_itemSetChangeListeners) {
			listener.containerItemSetChange(event);
		}
	}

	@Override
	public void addSelectionListener(SelectionListener listener) {
		if (listener != null) {
			m_selectionListeners.add(listener);
		}
	}
	
	@Override
	public void setSelectionListeners(Set<SelectionListener> listeners) {
		m_selectionListeners = listeners;
	}
	
	@Override
	public void removeSelectionListener(SelectionListener listener) {
		m_selectionListeners.remove(listener);
	}
}
