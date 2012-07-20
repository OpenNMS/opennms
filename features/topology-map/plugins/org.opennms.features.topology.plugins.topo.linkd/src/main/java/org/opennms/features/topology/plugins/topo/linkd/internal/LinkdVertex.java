/**
 * 
 */
package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

abstract public class LinkdVertex {
	String m_id;
	int m_x;
	int m_y;
	boolean m_selected;
	boolean m_locked = false;
	String m_icon;
	String m_label;
	LinkdGroup m_parent = null;
	List<LinkdEdge> m_edges = new ArrayList<LinkdEdge>();
	private int m_semanticZoomLevel = -1;
	
	public LinkdVertex() {}

	public LinkdVertex(String id, String icon, String label) {
	    m_id=id;
	    m_icon=icon;
	    m_label = label;
	}
	
	public LinkdVertex(String id, int x, int y, String icon, String label) {
		m_id=id;
		m_x=x;
		m_y=y;
		m_icon=icon;
		m_label=label;
	}
	
	@XmlIDREF
	public LinkdGroup getParent() {
		return m_parent;
	}
	
	public void setParent(LinkdGroup parent) {
		if (m_parent != null) {
			m_parent.removeMember(this);
		}
		m_parent = parent;
		if (m_parent != null) {
			m_parent.addMember(this);
		}
	}
	
	public boolean isLocked() {
		return m_locked;
	}

	public void setLocked(boolean locked) {
		m_locked = locked;
	}

	public abstract boolean isLeaf();
	
	public boolean isRoot() {
		return m_parent == null;
	}
	
	@XmlID
	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	public int getX() {
		return m_x;
	}

	public void setX(int x) {
		m_x = x;
	}

	public int getY() {
		return m_y;
	}

	public void setY(int y) {
		m_y = y;
	}

	public boolean isSelected() {
		return m_selected;
	}

	public void setSelected(boolean selected) {
		m_selected = selected;
	}

	public String getIcon() {
		return m_icon;
	}

	public void setIcon(String icon) {
		m_icon = icon;
	}

	public String getLabel() {
		return m_label;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	@XmlTransient
	public List<LinkdEdge> getEdges() {
		return m_edges;
	}
	
	void addEdge(LinkdEdge edge) {
		m_edges.add(edge);
	}
	
	void removeEdge(LinkdEdge edge) {
		m_edges.remove(edge);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
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
		LinkdVertex other = (LinkdVertex) obj;
		if (m_id == null) {
			if (other.m_id != null)
				return false;
		} else if (!m_id.equals(other.m_id))
			return false;
		return true;
	}
	
	public int getSemanticZoomLevel() {
		return m_semanticZoomLevel >= 0
				? m_semanticZoomLevel
				: m_parent == null 
				? 0 
				: m_parent.getSemanticZoomLevel() + 1;
	}
	
	public LinkdVertex getDisplayVertex(int semanticZoomLevel) {
		if(getParent() == null || getSemanticZoomLevel() <= semanticZoomLevel) {
			return this;
		}else {
			return getParent().getDisplayVertex(semanticZoomLevel);
		}

	}
	
	
}