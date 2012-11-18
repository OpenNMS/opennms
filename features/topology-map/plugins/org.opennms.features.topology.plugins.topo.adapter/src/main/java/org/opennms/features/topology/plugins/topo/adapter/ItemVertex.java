package org.opennms.features.topology.plugins.topo.adapter;


import org.opennms.features.topology.api.topo.Vertex;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

class ItemVertex implements Vertex {

    private static final String ICON_KEY = "iconKey";
    private static final String LABEL = "label";
    private static final String TOOLTIP_TEXT = "tooltipText";
    private static final String STYLE_NAME = "styleName";
    //private static final String SEMANTIC_ZOOM_LEVEL = "semanticZoomLevel";

    final String m_namespace;
    final String m_id;
    private final Object m_itemId;
    private final ItemFinder m_itemFinder;
    //private int m_semanticZoomLevel = -1;
    
    // These are used by reflection
    public class Defaults implements Vertex {
    	// delegate to containing vertex for local properties
    	public String getKey() { return ItemVertex.this.getKey(); }
    	public Object getItemId() {	return ItemVertex.this.getItemId(); }
    	public String getId() { return ItemVertex.this.getId(); };
    	public String getNamespace() { return ItemVertex.this.getNamespace(); }
		public Item getItem() {	return ItemVertex.this.getItem(); }
		// defaults values for things the delegated item may not provide
		public String getLabel() { return "no label provided";	}
		public String getTooltipText() { return ""; }
		public String getIconKey() { return null; }
		public String getStyleName() { return getNamespace()+" vertex"; }
    }
    
    public ItemVertex(String namespace, String id, Object itemId, ItemFinder itemFinder) {
        m_namespace = namespace;
        m_id = id;
        m_itemId = itemId;
        m_itemFinder = itemFinder;

    }

    @Override
    public String getNamespace() {
        return m_namespace;
    }

    @Override
    public String getId() {
        return m_id;
    }

    public Object getItemId() {
        return m_itemId;
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


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_id == null) ? 0 : m_id.hashCode());
        result = prime * result
                + ((m_namespace == null) ? 0 : m_namespace.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemVertex other = (ItemVertex) obj;
        if (m_id == null) {
            if (other.m_id != null)
                return false;
        } else if (!m_id.equals(other.m_id))
            return false;
        if (m_namespace == null) {
            if (other.m_namespace != null)
                return false;
        } else if (!m_namespace.equals(other.m_namespace))
            return false;
        return true;
    }
}