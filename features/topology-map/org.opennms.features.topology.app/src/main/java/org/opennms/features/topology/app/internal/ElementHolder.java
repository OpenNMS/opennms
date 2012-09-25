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

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

public abstract class ElementHolder<T> {
	String m_prefix;
	Container m_itemContainer;
	List<T> m_graphElements = Collections.emptyList();
	List<Object> m_itemIds = Collections.emptyList();
	KeyMapper m_elementKey2ItemId;
	Map<String, T> m_keyToElementMap = new HashMap<String, T>();
	
	ElementHolder(String prefix) {
		m_prefix = prefix;
		m_elementKey2ItemId  = new KeyMapper(m_prefix);	
	}
	
	ElementHolder(Container container, String prefix) {
		this(prefix);
		setContainer(container);
	}

	public void setContainer(Container container) {
		m_itemContainer = container;
		
		
		update();
	}
	
	
	
	public void update() {
		List<Object> oldItemIds = m_itemIds;
		List<Object> newItemIds = new ArrayList<Object>(m_itemContainer.getItemIds());
		m_itemIds = newItemIds;
		
		Set<Object> newContainerItems = new LinkedHashSet<Object>(newItemIds);
		newContainerItems.removeAll(oldItemIds);
		
		Set<Object> removedContainerItems = new LinkedHashSet<Object>(oldItemIds);
		removedContainerItems.removeAll(newItemIds);
		
		m_graphElements = new ArrayList<T>(newItemIds.size());
		
		for(Object itemId : removedContainerItems) {
			String key = m_elementKey2ItemId.key(itemId);
			m_elementKey2ItemId.remove(itemId);
			T element = m_keyToElementMap.remove(key);
			remove(element);
		}
		
		for(T element : m_keyToElementMap.values()) {
			m_graphElements.add(update(element));
		}
		
		
		for(Object itemId : newContainerItems) {
		    String key = m_elementKey2ItemId.key(itemId);
		    
		    Item item = m_itemContainer.getItem(itemId);
		    T v = make(key, itemId, item);
            
		    if (v == null) {
		        System.err.println("Warning: unable to make element for key=" + key + ", itemId=" + itemId + ", item=" + item);
		    } else {
		        // System.err.println("make v: " + v);
		        m_graphElements.add(v);
		        // System.err.println("Added v: " + v + " to m_graphElements: " + m_graphElements);
		        m_keyToElementMap.put(key, v);
		    }
		}
	}
	
	List<T> getElements(){
		return m_graphElements;
	}

	protected abstract T make(String key, Object itemId, Item item);
	
	protected T update(T element) { return element; }
	
	protected void remove(T element) { };

	public T getElementByKey(String key) {
		return m_keyToElementMap.get(key);
	}
	
	public String getKeyForItemId(Object itemId) {
		return m_elementKey2ItemId.key(itemId);
	}
	
	public T getElementByItemId(Object itemId) {
		return getElementByKey(m_elementKey2ItemId.key(itemId));
	}
	
	public List<String> getKeysByItemId(Collection<?> itemIds){
	    List<String> elements = new ArrayList<String>(itemIds.size());
        
        for(Object itemId : itemIds) {
            elements.add(getKeyForItemId(itemId));
        }
        
        return elements;
	}
	
	public List<T> getElementsByItemIds(Collection<?> itemIds) {
		List<T> elements = new ArrayList<T>(itemIds.size());
		
		for(Object itemId : itemIds) {
			elements.add(getElementByItemId(itemId));
		}
		
		return elements;
	}

	
	
}