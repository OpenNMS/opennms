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

package org.opennms.features.topology.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.features.topology.api.topo.AbstractVertex;

public abstract class SimpleVertex extends AbstractVertex {

	int m_x;
	int m_y;
	boolean m_selected;
	boolean m_locked = false;
	String m_icon;
	SimpleGroup m_parent = null;
	List<SimpleEdge> m_edges = new ArrayList<SimpleEdge>();
	private String m_ipAddr ="127.0.0.1";
	private int m_nodeID = -1;
	private int m_semanticZoomLevel = -1;

	private String m_label;
	private String m_tooltipText;
	private String m_iconKey;

	public SimpleVertex(String id) {
		super("simple", id);
	}

	public SimpleVertex(String id, int x, int y) {
		this(id);
		m_x = x;
		m_y = y;
	}

	@XmlIDREF
	public SimpleGroup getParent() {
		return m_parent;
	}

	@Override
	public void setParent(SimpleGroup parent) {
		if (m_parent != null) {
			m_parent.removeMember(this);
		}
		m_parent = parent;
		if (m_parent != null) {
			m_parent.addMember(this);
		}
	}

	@Override
	public boolean isLocked() {
		return m_locked;
	}

	@Override
	public void setLocked(boolean locked) {
		m_locked = locked;
	}

	@Override
	public abstract boolean isLeaf();

	@Override
	public boolean isRoot() {
		return m_parent == null;
	}

	@Override
	public int getX() {
		return m_x;
	}

	@Override
	public void setX(int x) {
		m_x = x;
	}

	@Override
	public int getY() {
		return m_y;
	}

	@Override
	public void setY(int y) {
		m_y = y;
	}

	@Override
	public boolean isSelected() {
		return m_selected;
	}

	@Override
	public void setSelected(boolean selected) {
		m_selected = selected;
	}

	@Override
	public String getLabel() {
		return m_label;
	}

	@Override
	public void setLabel(String label) {
		m_label = label;
	}

	@Override
	public String getIpAddr() {
		return m_ipAddr;
	}

	@Override
	public void setIpAddr(String ipAddr){
		m_ipAddr = ipAddr;
	}

	@Override
	public int getNodeID() {
		return m_nodeID;
	}

	@Override
	public void setNodeID(int nodeID) {
		m_nodeID = nodeID;
	}

	@XmlTransient
	@Override
	public List<SimpleEdge> getEdges() {
		return m_edges;
	}

	@Override
	void addEdge(SimpleEdge edge) {
		m_edges.add(edge);
	}

	@Override
	void removeEdge(SimpleEdge edge) {
		m_edges.remove(edge);
	}

	public int getSemanticZoomLevel() {
		return m_semanticZoomLevel >= 0
		? m_semanticZoomLevel
				: m_parent == null 
				? 0 
						: m_parent.getSemanticZoomLevel() + 1;
	}

	//	public SimpleVertex getDisplayVertex(int semanticZoomLevel) {
		//		if(getParent() == null || getSemanticZoomLevel() <= semanticZoomLevel) {
	//			return this;
	//		}else {
	//			return getParent().getDisplayVertex(semanticZoomLevel);
	//		}
	//
	//	}
}
