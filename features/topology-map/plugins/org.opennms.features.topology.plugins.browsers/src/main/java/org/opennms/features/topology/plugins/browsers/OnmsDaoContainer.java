/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionNotifier;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public abstract class OnmsDaoContainer<T,K extends Serializable> implements Container, Container.Sortable, Container.Ordered, Container.Indexed, Container.ItemSetChangeNotifier, SelectionNotifier, SelectionListener {

    private static final long serialVersionUID = -9131723065433979979L;

    protected static final int DEFAULT_PAGE_SIZE = 200; // items per page/cache

    protected static final int DEFAULT_SIZE_RELOAD_TIME = 10000; // ms

    protected static class Page {
        protected int length;
        protected int offset;
        protected final Size size;

        public Page (int length, Size size) {
            this.length = length;
            this.size = size;
        }

        /**
         * Updates the offset of the current page and returns weather the offset was changed or not.
         * @param index
         * @return true when the offset has changed, false otherwise.
         */
        public boolean updateOffset(int index) {
            int oldOffset = offset;
            if (index < 0) index = 0;
            offset = index / length * length;
            return oldOffset != offset; // an update has been made
        }

        public int getStart() {
            return offset;
        }
    }

    protected static class Size {
        private Date lastUpdate = null;
        private int value;
        private int reloadTimer; // in ms
        private final SizeReloadStrategy reloadStrategy;

        protected Size(int reloadTimer, SizeReloadStrategy reloadStrategy) {
            this.reloadTimer = reloadTimer == 0 ? DEFAULT_SIZE_RELOAD_TIME : reloadTimer;
            this.reloadStrategy = reloadStrategy;
        }

        synchronized public int getValue() {
            if (isOutdated()) reloadSize();
            return value;
        }

        private boolean isOutdated() {
            return lastUpdate == null || lastUpdate.getTime() + reloadTimer < new Date().getTime();
        }

        synchronized private void reloadSize() {
            value = reloadStrategy.reload();
            lastUpdate = new Date();
        }

        private void setOutdated() {
            lastUpdate = null;
        }
    }

    protected static interface SizeReloadStrategy {
        int reload();
    }

    protected static class SortEntry implements Comparable<SortEntry> {
        private final String propertyId;
        private final boolean ascending;

        private SortEntry(String propertyId, boolean ascending) {
            this.propertyId = propertyId;
            this.ascending = ascending;
        }

        @Override
        public int compareTo(SortEntry o) {
            return propertyId.compareTo(o.propertyId);
        }
    }

	protected class Cache {
        // Maps each itemId to a item.
        private Map<K, BeanItem<T>> cacheContent = new HashMap<K, BeanItem<T>>();

        // Maps each row to an itemId
        private Map<Integer, K> rowMap = new HashMap<Integer, K>();

        private Cache() {
        }

        public boolean containsItemId(K itemId) {
            if (itemId == null) return false;
            return cacheContent.containsKey(itemId);
        }

        public boolean containsIndex(int index) {
            return rowMap.containsKey(Integer.valueOf(index));
        }

        public BeanItem<T> getItem(K itemId) {
            if (containsItemId(itemId)) return cacheContent.get(itemId);
            return null;
        }

        public void addItem(int rowNumber, K itemId, T bean) {
            if (containsItemId(itemId)) return; //already added
            cacheContent.put(itemId, new BeanItem<T>(bean));
            rowMap.put(rowNumber, itemId);
        }

        public int getIndex(K itemId) {
            for (Map.Entry<Integer, K> eachRow : rowMap.entrySet()) {
                if (eachRow.getValue().equals(itemId))
                    return eachRow.getKey();
            }
            return -1; // not found
        }

        public void reset() {
            cacheContent.clear();
            rowMap.clear();
        }

        public K getItemId(int rowIndex) {
            return rowMap.get(Integer.valueOf(rowIndex));
        }

        public void reload(Page page) {
            size.setOutdated(); // force to be outdated
            reset();
            List<T> beans = getItemsForCache(m_dao, page);
            if (beans == null) return;
            int rowNumber = page.getStart();
            for (T eachBean : beans) {
                addItem(rowNumber, getId(eachBean), eachBean);
                doItemAddedCallBack(rowNumber, getId(eachBean), eachBean);
                rowNumber++;
            }
        }
    }

	private final OnmsDao<T,K> m_dao;

    // ORDER/SORTING
    private final List<Order> m_orders = new ArrayList<Order>();

    // FILTERING
    private final List<Restriction> m_restrictions = new ArrayList<Restriction>();

    private final List< SortEntry> m_sortEntries = new ArrayList<SortEntry>();

	/**
	 * TODO: Fix concurrent access to this field
	 */
	private final Collection<ItemSetChangeListener> m_itemSetChangeListeners = new HashSet<ItemSetChangeListener>();

	/**
	 * TODO: Fix concurrent access to this field
	 */
	private Collection<SelectionListener> m_selectionListeners = new HashSet<SelectionListener>();

    private final Map<String,String> m_beanToHibernatePropertyMapping = new HashMap<String,String>();

    private final Size size;

    private final Page page;

    private final Cache cache;

    private final Class<T> m_itemClass;

    private Map<Object,Class<?>> m_properties;

	public OnmsDaoContainer(Class<T> itemClass, OnmsDao<T,K> dao) {
        m_itemClass = itemClass;
		m_dao = dao;
        size = new Size(DEFAULT_SIZE_RELOAD_TIME, new SizeReloadStrategy() {
            @Override
            public int reload() {
                return m_dao.countMatching(getCriteria(null, false));  // no paging!!!!
            }
        });
        page = new Page(DEFAULT_PAGE_SIZE, size);
        cache = new Cache();
	}

	@Override
	public boolean containsId(Object itemId) {
		if (itemId == null) return false;
        if (cache.containsItemId((K) itemId)) return true;
        int index = indexOfId(itemId);
        return index >= 0;
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		Item item = getItem(itemId);
		if (item == null) {
			return null;
		} else {
			return item.getItemProperty(propertyId);
		}
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
        loadPropertiesIfNull();
        updateContainerPropertyIds(m_properties);
        return Collections.unmodifiableCollection(m_properties.keySet());
    }

	@Override
	public Item getItem(Object itemId) {
		if (itemId == null) return null;
        if (cache.containsItemId((K)itemId)) return cache.getItem((K)itemId);

        // not in cache, get the right page
        final int index = indexOfId(itemId);
        if (index == -1) return null; // not in container

        // page has the item now in container
        return cache.getItem((K)itemId);
	}

    public Class<T> getItemClass() {
        return m_itemClass;
    }

    protected abstract K getId(T bean);

	@Override
	public Class<?> getType(Object propertyId) {
        return m_properties.get(propertyId);
    }

	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {
		m_dao.clear();
		return true;
	}

	@Override
	public boolean removeItem(Object itemId) throws UnsupportedOperationException {
		m_dao.delete((K)itemId);
		return true;
	}

	@Override
	public int size() {
        return size.getValue();
	}

	@Override
	public Object firstItemId() {
        if (!cache.containsIndex(0)) {
            updatePage(0);
        }
        return cache.getItemId(0);
	}

    @Override
    public Object lastItemId() {
        int lastIndex = size() - 1;
        if (!cache.containsIndex(lastIndex)) {
            updatePage(lastIndex);
        }
        return cache.getItemId(lastIndex);
    }

	@Override
	public boolean isFirstId(Object itemId) {
		return firstItemId().equals(itemId);
	}

    private void updatePage(final int index) {
        boolean changed = page.updateOffset(index);
        if (changed) {
            cache.reload(page);
        }
    }

	@Override
	public boolean isLastId(Object itemId) {
		return lastItemId().equals(itemId);
	}

	@Override
	public Object nextItemId(Object itemId) {
		if (itemId == null) return null;
        int nextIdIndex = indexOfId(itemId) + 1;
        if(cache.getItemId(nextIdIndex) == null) {
            updatePage(page.offset + page.length);
        }
        return cache.getItemId(nextIdIndex);
	}

	@Override
	public Object prevItemId(Object itemId) {
		if (itemId == null) return null;
        int prevIdIndex = indexOfId(itemId) - 1;
        if (cache.getItemId(prevIdIndex) == null) {
            updatePage(prevIdIndex);
        }
        return cache.getItemId(prevIdIndex);
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
        List<SortEntry> newSortEntries = createSortEntries(propertyId, ascending);
        if (!m_sortEntries.equals(newSortEntries)) {
            m_sortEntries.clear();
            m_sortEntries.addAll(newSortEntries);
            m_orders.clear();
            for(int i = 0; i < propertyId.length; i++) {
                final String beanProperty = (String)propertyId[i];
                String hibernateProperty = m_beanToHibernatePropertyMapping.get(beanProperty);
                if (hibernateProperty == null) hibernateProperty = beanProperty;
                if (ascending[i]) {
                    m_orders.add(Order.asc(hibernateProperty));
                } else {
                    m_orders.add(Order.desc(hibernateProperty));
                }
            }
            cache.reload(page);
        }
	}

    protected List<SortEntry> createSortEntries(Object[] propertyId, boolean[] ascending) {
        List<SortEntry> sortEntries = new ArrayList<SortEntry>();
        for (int i=0; i<propertyId.length; i++) {
            sortEntries.add(new SortEntry((String)propertyId[i], ascending[i]));
        }
        return sortEntries;
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

    public void setRestrictions(List<Restriction> newRestrictions) {
        m_restrictions.clear();
        if (newRestrictions == null) return;
        m_restrictions.addAll(newRestrictions);
    }

    public List<Restriction> getRestrictions() {
        return Collections.unmodifiableList(m_restrictions);
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

    public void addBeanToHibernatePropertyMapping(final String key, final String value) {
        m_beanToHibernatePropertyMapping.put(key, value);
    }

    @Override
    public Collection<?> getItemIds() {
        int overallSize = size();
        Page page = new Page(1000, size);  // only get 10000 items at once
        List<K> itemIds = new ArrayList<K>();
        for (int i=0; i<overallSize; i+=page.length) {
            List<T> tmpItems = m_dao.findMatching(getCriteria(page, false));
            for (T eachItem : tmpItems) {
                itemIds.add(getId(eachItem));
            }
            page.updateOffset(page.offset + page.length);
        }
        return itemIds;
    }

    @Override
    public int indexOfId(Object itemId) {
        if (cache.containsItemId(((K) itemId)))
            return cache.getIndex((K) itemId); // cache hit *yay*

        //itemId is not in the cache so we try to find the right page
        boolean circled = false; // we have run through the whole cache
        int startOffset = page.offset;
        do {
            int indexOfId = page.offset + page.length; // we have to start somewhere
            // check if we are not at the end, if so, start from beginning
            if (indexOfId > size()) {
                indexOfId = 0;
            }
            // reload next page
            updatePage(indexOfId);
            // check if element now is in cache
            if (cache.containsItemId((K) itemId)) {
                return cache.getIndex((K)itemId);
            }
            // ensure that we have not circled yet
            if (startOffset == indexOfId) {
                circled = true;
            }
        } while (!circled); // continue as far as we have not found the element yet
        return -1; // not found
    }

    @Override
    public Object getIdByIndex(int index) {
        if (cache.containsIndex(index)) return cache.getItemId(index);
        updatePage(index);
        return cache.getItemId(index); // it is now in the cache or it does not exist
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot add new items to this container");
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot add new items to this container");
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot add new items to this container");
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot add new items to this container");
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
    public final boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot remove properties from objects in this container");
    }

    protected void updateContainerPropertyIds(Map<Object,Class<?>> properties) {
        // by default we do nothing with the properties;
    }

    /**
     * Creates a {@link Criteria} object to get data from database.
     * If considerPaging is set Limit and offset values are added as restriction.
     * @param page
     * @param doOrder
     * @return
     */
    protected Criteria getCriteria(Page page, boolean doOrder) {
        Criteria tmpCriteria = new Criteria(getItemClass());
        for (Restriction eachRestriction : m_restrictions) {
            tmpCriteria.addRestriction(eachRestriction);
        }
        if (doOrder) {
            tmpCriteria.setOrders(m_orders);
        }
        if (page != null) {
            tmpCriteria.setOffset(page.offset);
            tmpCriteria.setLimit(page.length);
        }
        addAdditionalCriteriaOptions(tmpCriteria, page, doOrder);
        return tmpCriteria;
    }

    // must be overwritten by subclass if you want to add some alias and so on
    protected void addAdditionalCriteriaOptions(Criteria criteria, Page page, boolean doOrder) {

    }

    protected void doItemAddedCallBack(int rowNumber, K id, T eachBean) {

    }

    protected List<T> getItemsForCache(final OnmsDao<T, K> dao, final Page page) {
        return dao.findMatching(getCriteria(page, true));
    }

    protected Cache getCache() {
        return cache;
    }

    protected Page getPage() {
        return page;
    }

    private synchronized void loadPropertiesIfNull() {
        if (m_properties == null) {
            m_properties = new TreeMap<Object,Class<?>>();
            BeanItem<T> item = null;
            try {
                item = new BeanItem<T>(m_itemClass.newInstance());
            } catch (InstantiationException e) {
                LoggerFactory.getLogger(getClass()).error("Class {} does not have a default constructor. Cannot create an instance.", getItemClass());
            } catch (IllegalAccessException e) {
                LoggerFactory.getLogger(getClass()).error("Class {} does not have a public default constructor. Cannot create an instance.", getItemClass());
            }
            for (Object key : item.getItemPropertyIds()) {
                m_properties.put(key, item.getItemProperty(key).getType());
            }
        }
    }
}
