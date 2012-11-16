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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.features.topology.api.HierarchicalBeanContainer;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

@SuppressWarnings("serial")
public class VertexProviderFilterableHierarchicalContainer extends HierarchicalContainer implements ItemSetChangeListener {
    
    HierarchicalBeanContainer m_container;
    List<Object> m_filteredItems;
    private LinkedList<Object> m_filteredRoots = null;
    private HashMap<Object, LinkedList<Object>> m_filteredChildren = null;
    private HashMap<Object, Object> m_filteredParent = null;
    private boolean m_includeParentsWhenFiltering = true;
    private Set<Object> m_filterOverride = null;
    private final HashMap<Object, Object> m_parent = new HashMap<Object, Object>();
    private VertexProvider m_vertexProvider;
    private Map<Object, Item> m_itemList;
    
   public VertexProviderFilterableHierarchicalContainer(VertexProvider vertexProvider) {
        super();
        
        m_vertexProvider = vertexProvider;
        m_itemList = createItemList(m_vertexProvider);
    }

     private Map<Object, Item> createItemList(VertexProvider vertexProvider) {
         Map<Object, Item> items = new HashMap<Object, Item>();
         for(Vertex v : vertexProvider.getVertices()) {
             Item item = new PropertysetItem();
             item.addItemProperty("id", new ObjectProperty<Object>(v.getItemId()));
             item.addItemProperty("label", new ObjectProperty<String>(v.getLabel()));
             items.put(v.getItemId(), item);
         }
         
         return items;
    }

     @Override
     public Item getItem(Object itemId) {
        return m_itemList.get(itemId);
     }

    @Override
    public Collection<String> getContainerPropertyIds() {
        return Arrays.asList("id", "label");
    }

    @Override
    public Collection<?> getItemIds() {
        if(isFiltered()) {
            return m_filteredItems;
        }else {
            return m_itemList.keySet();
        }
        
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        Item item = m_itemList.get(itemId);
        return item.getItemProperty(propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        return m_itemList.entrySet().iterator().next().getValue().getItemProperty(propertyId).getType();
    }

    @Override
    public int size() {
        return m_itemList.size();
    }

    @Override
    public boolean containsId(Object itemId) {
        return m_itemList.containsKey(itemId);
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
            Collection<Object> children = new ArrayList<Object>();
            Vertex vertex = m_vertexProvider.getVertex( (String) itemId );
            
            for( Vertex v : m_vertexProvider.getChildren(vertex)) {
                children.add(m_itemList.get(v.getItemId()));
            }
            return children; //m_container.getChildren(itemId);
        }
        
    }

    @Override
    public Object getParent(Object itemId) {
        if (m_filteredParent != null) {
            return m_filteredParent.get(itemId);
        }
        Vertex vertex = m_vertexProvider.getVertex( (String) itemId );
        return getItemForVertex( m_vertexProvider.getParent(vertex) );
    }
    
    private Item getItemForVertex(Vertex v) {
        return m_itemList.get(v.getItemId());
    }

    @Override
    public Collection<?> rootItemIds() {
        if(m_filteredRoots != null) {
            return Collections.unmodifiableCollection(m_filteredRoots);
        }else {
            return getRootItemIds();
        }
        
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The setParent method is unsupported in VertexProviderFilterableHierarchicalContainer");
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        throw new UnsupportedOperationException("The operation areChildrenAllowed is not supported");
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("setChildrenAllowed not supported");
    }

    @Override
    public boolean isRoot(Object itemId) {
        if(m_filteredRoots != null) {
            
        }
        Vertex v = m_vertexProvider.getVertex( (String)itemId );
        return m_vertexProvider.getParent(v) == null;
    }

    @Override
    public boolean hasChildren(Object itemId) {
        if(m_filteredChildren != null) {
            return m_filteredChildren.containsKey(itemId);
        }else {
            Vertex vertex = m_vertexProvider.getVertex( (String) itemId );
            return m_vertexProvider.hasChildren(vertex);
            //return m_container.hasChildren(itemId);
        }
        
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        return m_itemList.remove(itemId) != null; //m_container.removeItem(itemId);
    }
    
    @Override
    protected boolean doFilterContainer(boolean hasFilters) {
        if (!hasFilters) {
            // All filters removed
            m_filteredRoots = null;
            m_filteredChildren = null;
            m_filteredParent = null;
            
            if(getFilteredItemIds() != null) {
                boolean changed = m_itemList.keySet().size() != getFilteredItemIds().size();
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
            for (Object rootId : getRootItemIds()) {
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

    private Collection<?> getRootItemIds() {
        List<? extends Vertex> rootVertices = m_vertexProvider.getRootGroup();
        
        Collection<Object> rootIds = new ArrayList<Object>();
        for(Vertex v : rootVertices) {
            rootIds.add(v.getItemId());
        }
        
        return rootIds;
        //return m_container.rootItemIds();
    }
    
    private void addFilteredChildrenRecursively(Object parentItemId, HashSet<Object> includedItems) {
        Collection<?> childList = getChildren(parentItemId);
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

        Collection<?> childList = getChildren(itemId);
        if (childList != null) {
            for (Object childItemId : getChildren(itemId)) {
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
    protected Item getUnfilteredItem(Object itemId) {
        return m_itemList.get(itemId);
    }

    @Override
    public void containerItemSetChange(Container.ItemSetChangeEvent event) {
        fireItemSetChange();
    }
    

}
