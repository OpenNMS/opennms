package org.opennms.features.topology.plugins.topo.adapter;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.Edge;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

class ItemEdge extends AbstractEdge {
	
	private static final String LABEL = "label";
	private static final String TOOLTIP_TEXT = "tooltipText";
	private static final String STYLE_NAME = "styleName";

	private final Object m_itemId;
	private final ItemFinder m_itemFinder;
	private ItemConnector m_source;
	private ItemConnector m_target;

	public class Defaults implements Edge {
		public Object getItemId() { return ItemEdge.this.getItemId(); }
		@Override public String getId() { return ItemEdge.this.getId(); }
		@Override public String getNamespace() { return ItemEdge.this.getNamespace(); }
		@Override public String getKey() { return ItemEdge.this.getKey(); }
		@Override public Item getItem() { return ItemEdge.this.getItem(); }
		@Override public Connector getSource() { return ItemEdge.this.getSource(); }
		@Override public Connector getTarget() { return ItemEdge.this.getTarget(); }
		@Override public String getLabel() { return "label not provided"; }
		@Override public String getTooltipText() { return ""; }
		@Override public String getStyleName() { return getNamespace() + " edge"; };
	}

	public ItemEdge(String namespace, String id, Object itemId, ItemFinder itemFinder) {
		super(namespace, id);
		m_itemId = itemId;
		m_itemFinder = itemFinder;
	}
	
	public Object getItemId() {
		return m_itemId;
	}

	@Override
	public ItemConnector getSource() {
		return m_source;
	}
	
	public void setSource(ItemConnector source) {
		m_source = source;
	}

	@Override
	public ItemConnector getTarget() {
		return m_target;
	}
	
	public void setTarget(ItemConnector target) {
		m_target = target;
	}

	@Override
    public String getLabel() {
        Property labelProperty = getItem().getItemProperty(LABEL);
		String label = labelProperty == null ? "labels unsupported " : (String) labelProperty.getValue();
		return label;
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
	public String getStyleName() {
        Property styleProperty = getItem().getItemProperty(STYLE_NAME);
		return styleProperty == null ? null : (String)styleProperty.getValue();
	}

	@Override
	public String getKey() {
		return getNamespace()+":"+getId();
	}

}