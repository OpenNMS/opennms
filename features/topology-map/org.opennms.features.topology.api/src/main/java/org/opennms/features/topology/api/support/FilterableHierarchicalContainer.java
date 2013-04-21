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

package org.opennms.features.topology.api.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.HierarchicalContainer;

@SuppressWarnings("serial")
public class FilterableHierarchicalContainer extends HierarchicalContainer implements ItemSetChangeListener {

    HierarchicalBeanContainer<?,?> m_container;
    List<Object> m_filteredItems;
    private LinkedList<Object> m_filteredRoots = null;
    private HashMap<Object, LinkedList<Object>> m_filteredChildren = null;
    private HashMap<Object, Object> m_filteredParent = null;
    private boolean m_includeParentsWhenFiltering = true;
    private Set<Object> m_filterOverride = null;
    private final HashMap<Object, Object> m_parent = new HashMap<Object, Object>();
    
   public FilterableHierarchicalContainer(HierarchicalBeanContainer<?,?> container) {
        super();
        m_container = container;
        m_container.addListener(this);
        m_container.addListener(new PropertySetChangeListener() {

            @Override
            public void containerPropertySetChange(PropertySetChangeEvent event) {
                event.getContainer();
                
            }
        });
    }

 @Override
    public BeanItem<?> getItem(Object itemId) {
        return m_container.getItem(itemId);
    }

    @Override
    public Collection<String> getContainerPropertyIds() {
        return m_container.getContainerPropertyIds();
    }

    @Override
    public Collection<?> getItemIds() {
        if(isFiltered()) {
            return m_filteredItems;
        }else {
            return m_container.getItemIds();
        }
        
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        
        return m_container.getContainerProperty(itemId, propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        return m_container.getType(propertyId);
    }

    @Override
    public int size() {
        return m_container.size();
    }

    @Override
    public boolean containsId(Object itemId) {
        
        return m_container.containsId(itemId);
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        return null;
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        return null;
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        return false;
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        return false;
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        return false;
    }

    @Override
    public Collection<?> getChildren(Object itemId) {
        LinkedList<Object> c;

        if (m_filteredChildren != null) {
            c = m_filteredChildren.get(itemId);
            if(c == null) {
                return Collections.EMPTY_LIST;
            }
            return Collections.unmodifiableCollection(c);
        } else {
            return m_container.getChildren(itemId);
        }
        
    }

    @Override
    public Object getParent(Object itemId) {
        if (m_filteredParent != null) {
            return m_filteredParent.get(itemId);
        }
        return m_container.getParent(itemId);
    }

    @Override
    public Collection<?> rootItemIds() {
        if(m_filteredRoots != null) {
            return Collections.unmodifiableCollection(m_filteredRoots);
        }else {
            return m_container.rootItemIds();
        }
        
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
        return m_container.setParent(itemId, newParentId);
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        return m_container.areChildrenAllowed(itemId);
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        return m_container.setChildrenAllowed(itemId, areChildrenAllowed);
    }

    @Override
    public boolean isRoot(Object itemId) {
        if(m_filteredRoots != null) {
            
        }
        
        return m_container.isRoot(itemId);
    }

    @Override
    public boolean hasChildren(Object itemId) {
        if(m_filteredChildren != null) {
            return m_filteredChildren.containsKey(itemId);
        }else {
            return m_container.hasChildren(itemId);
        }
        
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        return m_container.removeItem(itemId);
    }
    
    @Override
    protected boolean doFilterContainer(boolean hasFilters) {
        if (!hasFilters) {
            // All filters removed
            m_filteredRoots = null;
            m_filteredChildren = null;
            m_filteredParent = null;
            
            if(getFilteredItemIds() != null) {
                boolean changed = m_container.getItemIds().size() != getFilteredItemIds().size();
                setFilteredItemIds(null);
                return changed;
            }else {
                return false;
            }
            
        }

        // Reset data structures
        m_filteredRoots = new LinkedList<Object>();
        m_filteredChildren = new HashMap<Object, LinkedList<Object>>();
        m_filteredParent = new HashMap<Object, Object>();

        if (m_includeParentsWhenFiltering) {
            // Filter so that parents for items that match the filter are also
            // included
            HashSet<Object> includedItems = new HashSet<Object>();
            for (Object rootId : m_container.rootItemIds()) {
                if (filterIncludingParents(rootId, includedItems)) {
                    m_filteredRoots.add(rootId);
                    addFilteredChildrenRecursively(rootId, includedItems);
                }
            }
            // includedItemIds now contains all the item ids that should be
            // included. Filter IndexedContainer based on this
            m_filterOverride = includedItems;
            super.doFilterContainer(hasFilters);
            m_filterOverride = null;

            return true;
        } else {
            // Filter by including all items that pass the filter and make items
            // with no parent new root items

            // Filter IndexedContainer first so getItemIds return the items that
            // match
            super.doFilterContainer(hasFilters);

            LinkedHashSet<Object> filteredItemIds = new LinkedHashSet<Object>(getItemIds());

            for (Object itemId : filteredItemIds) {
                Object itemParent = m_parent.get(itemId);
                if (itemParent == null || !filteredItemIds.contains(itemParent)) {
                    // Parent is not included or this was a root, in both cases
                    // this should be a filtered root
                    m_filteredRoots.add(itemId);
                } else {
                    // Parent is included. Add this to the children list (create
                    // it first if necessary)
                    addFilteredChild(itemParent, itemId);
                }
            }

            return true;
        }
    }
    
    private void addFilteredChildrenRecursively(Object parentItemId, HashSet<Object> includedItems) {
        Collection<?> childList = m_container.getChildren(parentItemId);
        if (childList == null) {
            return;
        }

        for (Object childItemId : childList) {
            if (includedItems.contains(childItemId)) {
                addFilteredChild(parentItemId, childItemId);
                addFilteredChildrenRecursively(childItemId, includedItems);
            }
        }
    }
    
    
    private void addFilteredChild(Object parentItemId, Object childItemId) {
        LinkedList<Object> parentToChildrenList = m_filteredChildren
                .get(parentItemId);
        if (parentToChildrenList == null) {
            parentToChildrenList = new LinkedList<Object>();
            m_filteredChildren.put(parentItemId, parentToChildrenList);
        }
        m_filteredParent.put(childItemId, parentItemId);
        parentToChildrenList.add(childItemId);

    }
    
    private boolean filterIncludingParents(Object itemId, HashSet<Object> includedItems) {
        boolean toBeIncluded = passesFilters(itemId);

        Collection<?> childList = m_container.getChildren(itemId);
        if (childList != null) {
            for (Object childItemId : m_container.getChildren(itemId)) {
                toBeIncluded |= filterIncludingParents(childItemId, includedItems);
            }
        }

        if (toBeIncluded) {
            includedItems.add(itemId);
        }
        return toBeIncluded;
    }
    
    public void setFilteredItemIds(List<Object> itemIds) {
        m_filteredItems = itemIds;
    }
    
    public List<Object> getFilteredItemIds(){
        return m_filteredItems;
    }

    @Override
    protected BeanItem<?> getUnfilteredItem(Object itemId) {
        return m_container.getItem(itemId);
    }

    @Override
    public void containerItemSetChange(Container.ItemSetChangeEvent event) {
        fireItemSetChange();
    }
    

    public void fireItemUpdated() {
        m_container.fireItemSetChange();
    }

}
