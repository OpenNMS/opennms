package org.opennms.features.topology.api.topo;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

import com.vaadin.data.Item;

public abstract class AbstractVertex implements Vertex {

	private final String m_namespace;
	private final String m_id;
	private String m_label;
	private String m_tooltipText;
	private String m_iconKey;
	private String m_styleName;
	private Vertex m_parent;
	protected Item m_item;
	int m_x;
	int m_y;
	private boolean m_selected;
	private boolean m_locked = false;
	private String m_ipAddr ="127.0.0.1";
	private int m_nodeID = -1;
	private int m_semanticZoomLevel = -1;

	public AbstractVertex(String namespace, String id) {
		m_namespace = namespace;
		m_id = id;
	}

	@Override
	@XmlID
	public final String getId() {
		return m_id;
	}

	public final void setId(String id) {
		throw new UnsupportedOperationException("Property id is not writable");
	}

	@Override
	public final String getNamespace() {
		return m_namespace;
	}

	@Override
	public final String getKey() {
		return m_namespace+":"+m_id;
	}

	@Override
	public Item getItem() {
		return m_item;
	}

	@Override
	public final String getLabel() {
		return m_label;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	@Override
	public String getTooltipText() {
		return m_tooltipText != null ? m_tooltipText : getLabel();
	}

	public final void setTooltipText(String tooltpText) {
		m_tooltipText = tooltpText;
	}

	@Override
	public final String getIconKey() {
		return m_iconKey;
	}

	public final void setIconKey(String iconKey) {
		m_iconKey = iconKey;
	}

	@Override
	public String getStyleName() {
		return m_styleName;
	}

	public final void setStyleName(String styleName) {
		m_styleName = styleName;
	}

	@Override
	public final int getX() {
		return m_x;
	}

	public final void setX(int x) {
		m_x = x;
	}

	@Override
	public final int getY() {
		return m_y;
	}

	public final void setY(int y) {
		m_y = y;
	}

	@XmlIDREF
	public final Vertex getParent() {
		return m_parent;
	}

	/**
	 * @param parent
	 */
	@Override
	public final void setParent(Vertex parent) {
		this.m_parent = parent;
	}

	@Override
	public final boolean isLocked() {
		return m_locked;
	}

	public final void setLocked(boolean locked) {
		m_locked = locked;
	}

	@Override
	public abstract boolean isLeaf();

	@Override
	public boolean isRoot() {
		return getParent() == null;
	}

	@Override
	public final boolean isSelected() {
		return m_selected;
	}

	public final void setSelected(boolean selected) {
		m_selected = selected;
	}

	@Override
	public final String getIpAddress() {
		return m_ipAddr;
	}

	public final void setIpAddress(String ipAddr){
		m_ipAddr = ipAddr;
	}

	@Override
	public final int getNodeID() {
		return m_nodeID;
	}

	public final void setNodeID(int nodeID) {
		m_nodeID = nodeID;
	}

	@Override
	public final int getSemanticZoomLevel() {
		return m_semanticZoomLevel >= 0
		? m_semanticZoomLevel
				: getParent() == null 
				? 0 
						: getParent().getSemanticZoomLevel() + 1;
	}

	@Override
	public Vertex getDisplayVertex(int semanticZoomLevel) {
		if(getParent() == null || getSemanticZoomLevel() <= semanticZoomLevel) {
			return this;
		}else {
			return getParent().getDisplayVertex(semanticZoomLevel);
		}

	}

	@Override
	public int hashCode() {
		 final int prime = 31;
		 int result = 1;
		 result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
		 result = prime * result
		 + ((m_namespace == null) ? 0 : m_namespace.hashCode());
		 return result;
	 }

	 @Override
	 public boolean equals(Object obj) {
		 if (this == obj) return true;
		 if (obj == null) return false;
		 if (!(obj instanceof VertexRef))	return false;

		 VertexRef e = (VertexRef)obj;
		 return getNamespace().equals(e.getNamespace()) && getId().equals(e.getId());

	 }

	 @Override
	 public String toString() { return "Vertex:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 
}
