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

package org.opennms.features.topology.plugins.topo.adapter;

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Vertex;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

class ItemVertex extends AbstractVertex {

    private static final String ICON_KEY = "iconKey";
    private static final String LABEL = "label";
    private static final String TOOLTIP_TEXT = "tooltipText";
    private static final String STYLE_NAME = "styleName";

    private final Object m_itemId;
    private final ItemFinder m_itemFinder;
    
    // These are used by reflection
    public class Defaults implements Vertex {
    	// delegate to containing vertex for local properties
		@Override
    	public String getKey() { return ItemVertex.this.getKey(); }
    	public Object getItemId() {	return ItemVertex.this.getItemId(); }
		@Override
    	public String getId() { return ItemVertex.this.getId(); };
		@Override
    	public String getNamespace() { return ItemVertex.this.getNamespace(); }
		@Override
		public Item getItem() {	return ItemVertex.this.getItem(); }
		// defaults values for things the delegated item may not provide
		@Override
		public String getLabel() { return "no label provided";	}
		@Override
		public String getTooltipText() { return ""; }
		@Override
		public String getIconKey() { return null; }
		@Override
		public boolean isLeaf() { return true; }
		@Override
		public String getStyleName() { return getNamespace()+" vertex"; }
    }
    
    public ItemVertex(String namespace, String id, Object itemId, ItemFinder itemFinder) {
    	super(namespace, id);
        m_itemId = itemId;
        m_itemFinder = itemFinder;

    }

    public Object getItemId() {
        return m_itemId;
    }

    @Override
    public boolean isLeaf() {
        return (Boolean)m_itemFinder.getItem(m_itemId).getItemProperty("leaf").getValue();
    }

    @Override
    public String getKey() {
        return getNamespace()+":"+getId();
    }
    @Override
    public String getLabel() {
        Property labelProperty = getItem().getItemProperty(LABEL);
        String label = labelProperty == null ? "labels unsupported " : (String) labelProperty.getValue();
        return label;
    }

    public void setLabel(String label) {
        Property labelProperty = getItem().getItemProperty(LABEL);
        if (labelProperty != null && !labelProperty.isReadOnly()) {
            labelProperty.setValue(label);
        }

    }
    
    @Override
	public Item getItem() {
		return new ChainedItem(m_itemFinder.getItem(m_itemId), new BeanItem<Defaults>(new Defaults()));
	}

    @Override
    public String getTooltipText() {
        if(getItem().getItemProperty(TOOLTIP_TEXT) != null && getItem().getItemProperty(TOOLTIP_TEXT).getValue() != null) {
            return (String) getItem().getItemProperty(TOOLTIP_TEXT).getValue();
        }else {
            return null;
        }
    }

    @Override
    public String getIconKey() {
        return (String) getItem().getItemProperty(ICON_KEY).getValue();
    }

    @Override
    public String getStyleName() {
        Property styleProperty = getItem().getItemProperty(STYLE_NAME);
        return styleProperty == null ? null : (String)styleProperty.getValue();
    }

}
