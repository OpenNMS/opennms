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

import org.opennms.features.topology.api.topo.Vertex;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class TopoVertex implements Vertex {
    
    public static final String LABEL_PROPERTY = "label";
    public static final String TOOLTIP_TEXT = "tooltipText";
    public static final String SEMANTIC_ZOOM_LEVEL = "semanticZoomLevel";
    public static final String ICON_KEY = "iconKey";
	
    private final String m_key;
	private final Object m_itemId;
	private final Item m_item;
	private final SimpleGraphContainer m_graphContainer;
	
	public TopoVertex(SimpleGraphContainer graphContainer, String key, Object itemId, Item item) {
		m_graphContainer = graphContainer;
		m_key = key;
		m_itemId = itemId;
		m_item = item;
	}
	
	public Object getItemId() {
		return m_itemId;
	}
	
	public boolean isLeaf() {
		return !getGraphContainer().hasChildren(getItemId());
	}

	public String toString() {
    	return "v" + getItemId();
    }

	public Item getItem() {
		return m_item;
	}
	
	public String getKey() {
		return m_key;
	}
	
	public int getSemanticZoomLevel() {
		return getGraphContainer().getSemanticZoomLevel(getItemId());
	}

	public String getLabel() {
		Property labelProperty = getItem().getItemProperty(LABEL_PROPERTY);
		return labelProperty == null ? "no such label" : (String)(labelProperty.getValue());
	}

	public String getIconKey() {
		Property iconKeyProperty = getItem().getItemProperty(ICON_KEY);
		return iconKeyProperty == null ? null : (String)(iconKeyProperty.getValue());
    }

	public String getTooltipText() {
		Property tooltipTextProperty = getItem().getItemProperty(TOOLTIP_TEXT);
		return tooltipTextProperty == null 
				? getLabel() 
				: tooltipTextProperty.getValue() == null
				? getLabel()
				: (String)(tooltipTextProperty.getValue());
	}

	public SimpleGraphContainer getGraphContainer() {
		return m_graphContainer;
	}

	@Override
	public String getNamespace() {
		return "nodes";
	}

	@Override
	public String getId() {
		return getKey();
	}

	@Override
	public String getStyleName() {
		return "vertex";
	}

}