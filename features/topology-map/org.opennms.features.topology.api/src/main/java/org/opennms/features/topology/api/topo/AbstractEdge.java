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

package org.opennms.features.topology.api.topo;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class AbstractEdge extends AbstractEdgeRef implements Edge {

	private final SimpleConnector m_source;
	private final SimpleConnector m_target;

	private String m_tooltipText;
	private String m_styleName;

	public AbstractEdge(String namespace, String id, Vertex source, Vertex target) {
		super(namespace, id);
		if (source == null) {
			throw new IllegalArgumentException("Source is null");
		} else if (target == null) {
			throw new IllegalArgumentException("Target is null");
		}
		m_source = new SimpleConnector(namespace, id + "::" + source.getId(), source.getLabel() + " Connector", source, this);
		m_target = new SimpleConnector(namespace, id + "::" + target.getId(), target.getLabel() + " Connector", target, this);
		m_styleName = "edge";
	}

	public AbstractEdge(String namespace, String id, SimpleConnector source, SimpleConnector target) {
		super(namespace, id);
		m_source = source;
		m_target = target;
		m_styleName = "edge";
	}

	/**
	 * @deprecated Use namespace/id tuple
	 */
	@Override
	public String getKey() {
		return getNamespace() + ":" + getId();
	}

	@Override
	public String getTooltipText() {
		return m_tooltipText;
	}

	@Override
	public final String getStyleName() {
		return m_styleName;
	}

	@Override
	public final void setTooltipText(String tooltipText) {
		m_tooltipText = tooltipText;
	}

	@Override
	public final void setStyleName(String styleName) {
		m_styleName = styleName;
	}

	@Override
	public Item getItem() {
		return new BeanItem<AbstractEdge>(this);
	}

	@Override
	public final SimpleConnector getSource() {
		return m_source;
	}

	@Override
	public final SimpleConnector getTarget() {
		return m_target;
	}

	@Override
	public String toString() { return "Edge:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; } 

}
