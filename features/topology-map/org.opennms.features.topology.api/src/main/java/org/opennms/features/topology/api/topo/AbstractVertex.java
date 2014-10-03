/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.topo;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class AbstractVertex extends DefaultVertexRef implements Vertex {

	private String m_tooltipText;
	private String m_iconKey;
	private String m_styleName;
	private VertexRef m_parent;
	private Integer m_x;
	private Integer m_y;
	private boolean m_selected;
	private boolean m_locked = false;
	private String m_ipAddr ="127.0.0.1";
	private Integer m_nodeID;

	@Deprecated
	public AbstractVertex(String namespace, String id) {
		super(namespace, id);
	}

    public AbstractVertex(String namespace, String id, String label){
        super(namespace, id, label);
    }
	/**
	 * @deprecated Use namespace/id tuple
	 */
	@Override
	public final String getKey() {
		return getNamespace() + ":" + getId();
	}

	@Override
	public Item getItem() {
		return new BeanItem<AbstractVertex>(this);
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
	public final Integer getX() {
		return m_x;
	}

	public final void setX(Integer x) {
		m_x = x;
	}

	@Override
	public final Integer getY() {
		return m_y;
	}

	public final void setY(Integer y) {
		m_y = y;
	}

        @Override
	public final VertexRef getParent() {
		return m_parent;
	}

	/**
	 * @param parent
	 */
	@Override
	public final void setParent(VertexRef parent) {
		if (this.equals(parent)) return;
		m_parent = parent;
	}

	@Override
	public final boolean isLocked() {
		return m_locked;
	}

	public final void setLocked(boolean locked) {
		m_locked = locked;
	}

	@Override
	public boolean isGroup() {
		return false;
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
	public final Integer getNodeID() {
		return m_nodeID;
	}

	public final void setNodeID(Integer nodeID) {
		m_nodeID = nodeID;
	}

	 @Override
	 public String toString() { return "Vertex:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 
}
