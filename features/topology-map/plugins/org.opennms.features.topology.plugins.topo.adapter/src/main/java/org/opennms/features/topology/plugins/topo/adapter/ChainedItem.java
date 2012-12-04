package org.opennms.features.topology.plugins.topo.adapter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

@SuppressWarnings("serial")
public class ChainedItem implements Item {
	
	private final Item m_item;
	private final Item m_next;
	
	public ChainedItem(Item item, Item next) {
		m_item = item;
		m_next = next;
	}

	@Override
	public Property getItemProperty(Object id) {
		Property property = m_item == null ? null : m_item.getItemProperty(id);
		return property != null ? property : m_next != null ? m_next.getItemProperty(id) : null;
	}

	@Override
	public Collection<?> getItemPropertyIds() {
		Set<Object> propertyIds = new LinkedHashSet<Object>(m_item.getItemPropertyIds());
		if (m_next != null) {
			propertyIds.addAll(m_next.getItemPropertyIds());
		}
		return propertyIds;
	}

	@Override
	public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Item.addItemProperty is not yet implemented.");
	}

	@Override
	public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Item.removeItemProperty is not yet implemented.");
	}
	
}