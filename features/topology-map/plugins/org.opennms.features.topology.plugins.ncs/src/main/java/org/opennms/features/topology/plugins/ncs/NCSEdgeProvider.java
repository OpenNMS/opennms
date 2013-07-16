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

package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponent.NodeIdentification;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class NCSEdgeProvider implements EdgeProvider {

	private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
	private static final String HTML_TOOLTIP_TAG_END  = "</p>";

	public static class NCSEdge extends AbstractEdge {
		private final String m_serviceName;
		
		public NCSEdge (String serviceName, NCSVertex source, NCSVertex target) {
			super("ncs", source.getId() + ":::" + target.getId(), source, target);
			m_serviceName = serviceName;
			setStyleName("ncs edge");
		}

		@Override
		public String getTooltipText() {
			StringBuffer toolTip = new StringBuffer();

			toolTip.append(HTML_TOOLTIP_TAG_OPEN);
			toolTip.append("Service: " + m_serviceName);
			toolTip.append(HTML_TOOLTIP_TAG_END);

			toolTip.append(HTML_TOOLTIP_TAG_OPEN);
			toolTip.append("Source: " + getSource().getVertex().getLabel());
			toolTip.append(HTML_TOOLTIP_TAG_END);

			toolTip.append(HTML_TOOLTIP_TAG_OPEN);
			toolTip.append("Target: " + getTarget().getVertex().getLabel());
			toolTip.append(HTML_TOOLTIP_TAG_END);

			return toolTip.toString();
		}

		@Override
		public Item getItem() {
			return new BeanItem<NCSEdge>(this);
		}

	}

	public static class NCSVertex extends AbstractVertex {

		public NCSVertex(String id, String label) {
			super("nodes", id);
			setLabel(label);
		}
	}

	private NCSComponentRepository m_dao;
	private NodeDao m_nodeDao;

	public NodeDao getNodeDao() {
		return m_nodeDao;
	}

	public void setNodeDao(NodeDao dao) {
		m_nodeDao = dao;
	}

	public NCSComponentRepository getNcsComponentRepository() {
		return m_dao;
	}

	public void setNcsComponentRepository(NCSComponentRepository dao) {
		m_dao = dao;
	}

	@Override
	public void addEdgeListener(EdgeListener vertexListener) {
		// TODO: Implement me
	}

	@Override
	public Edge getEdge(String namespace, String id) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Edge getEdge(EdgeRef reference) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * This factory works by using {@link NCSServiceCriteria} to construct edges based on
	 * connecting all of the ServiceElements that make up a Service to each other.
	 * 
	 * @param criteria An {@link NCSServiceCriteria} object
	 */
	@Override
	public List<Edge> getEdges(Criteria criteria) {
		List<Edge> retval = new ArrayList<Edge>();
		NCSServiceCriteria crit = (NCSServiceCriteria)criteria;
		for (Long id : crit) {
			NCSComponent service = m_dao.get(id);
			if (service == null) {
				LoggerFactory.getLogger(this.getClass()).warn("NCSComponent not found for ID {}", id);
			} else {
				NCSComponent[] subs = service.getSubcomponents().toArray(new NCSComponent[0]);
				// Connect all of the ServiceElements to one another
				for (int i = 0; i < subs.length; i++) {
					for (int j = i + 1; j < subs.length; j++) {
						String foreignSource = null, foreignId = null;
						OnmsNode sourceNode = null, targetNode = null;
						NodeIdentification ident = subs[i].getNodeIdentification();
						String sourceLabel = subs[i].getName();
						if (ident != null) {
							foreignSource = ident.getForeignSource();
							foreignId = ident.getForeignId();
							sourceNode = m_nodeDao.findByForeignId(foreignSource, foreignId);
							if (sourceNode == null) {
								continue;
							}
							if (sourceLabel == null) {
								sourceLabel = sourceNode.getLabel();
							}
						}
						ident = subs[j].getNodeIdentification();
						String targetLabel = subs[j].getName();
						if (ident != null) {
							foreignSource = ident.getForeignSource();
							foreignId = ident.getForeignId();
							targetNode = m_nodeDao.findByForeignId(foreignSource, foreignId);
							if (targetNode == null) {
								continue;
							}
							if (targetLabel == null) {
								targetLabel = targetNode.getLabel();
							}
						}
						
						retval.add(new NCSEdge(service.getName(), new NCSVertex(String.valueOf(sourceNode.getId()), sourceLabel), new NCSVertex(String.valueOf(targetNode.getId()), targetLabel)));
					}
				}
			}
		}
		return retval;
	}

	@Override
	public List<Edge> getEdges() {
		throw new UnsupportedOperationException("Not implemented");
		// TODO: Implement me
	}

	@Override
	public List<Edge> getEdges(
			Collection<? extends EdgeRef> references) {
		throw new UnsupportedOperationException("Not implemented");
		// TODO: Implement me
	}

	@Override
	public String getEdgeNamespace() {
		return "ncs";
	}
	
	@Override
	public boolean contributesTo(String namespace) {
		return "nodes".equals(namespace);
	}

	@Override
	public void removeEdgeListener(EdgeListener vertexListener) {
		// TODO: Implement me
	}

	@Override
	public boolean matches(EdgeRef edgeRef, Criteria criteria) {
		throw new UnsupportedOperationException("EdgeProvider.matches is not yet implemented.");
	}

	public static class NCSServiceCriteria extends ArrayList<Long> implements Criteria {
		
		private static final long serialVersionUID = 5833460704861282509L;
		
		public NCSServiceCriteria(Collection<Long> serviceIds) {
			super(serviceIds);
		}

		@Override
		public String getNamespace() {
			return "ncs";
		}

		@Override
		public ElementType getType() {
			return ElementType.EDGE;
		}
	}
	
	public static Criteria createCriteria(Collection<Long> selectedIds) {
		return new NCSServiceCriteria(selectedIds);
	}


	@Override
	public void clearEdges() {
		throw new UnsupportedOperationException("Not implemented");
	}

}
