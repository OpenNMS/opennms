/**
 * 
 */
package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;


abstract public class TestVertex {
	String m_id;
	int m_x;
	int m_y;
	String m_icon;
	TestGroup m_parent = null;
	List<TestEdge> m_edges = new ArrayList<TestEdge>();
	
	public TestVertex() {}
	
	public TestVertex(String id) {
		m_id = id;
	}

	public TestVertex(String id, int x, int y) {
		m_id = id;
		m_x = x;
		m_y = y;
	}
	
	@XmlIDREF
	public TestGroup getParent() {
		return m_parent;
	}
	
	public void setParent(TestGroup parent) {
		if (m_parent != null) {
			m_parent.removeMember(this);
		}
		m_parent = parent;
		if (m_parent != null) {
			m_parent.addMember(this);
		}
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

	public String getIcon() {
		return m_icon;
	}

	public void setIcon(String icon) {
		m_icon = icon;
	}

	@XmlTransient
    public List<TestEdge> getEdges() {
		return m_edges;
	}
	
	void addEdge(TestEdge edge) {
		m_edges.add(edge);
	}
	
	void removeEdge(TestEdge edge) {
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
		TestVertex other = (TestVertex) obj;
		if (m_id == null) {
			if (other.m_id != null)
				return false;
		} else if (!m_id.equals(other.m_id))
			return false;
		return true;
	}
	
	
}