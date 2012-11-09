package org.opennms.features.topology.plugins.topo.adapter;

import com.vaadin.data.Item;

interface ItemFinder {
	Item getItem(Object itemId);
}