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

import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;

public class NCSSelectionTree extends SelectionTree {

	private static final long serialVersionUID = 8778577903128733601L;

	public static class NCSSelectionTreeCriteria extends ArrayList<Long> implements Criteria {

		private static final long serialVersionUID = 5833460704861282509L;

		@Override
		public String getNamespace() {
			return "ncs";
		}

		@Override
		public ElementType getType() {
			return ElementType.EDGE;
		}
	}

	private NCSComponentRepository m_dao;
	private NodeDao m_nodeDao;
	private final NCSSelectionTreeCriteria m_currentCriteria = new NCSSelectionTreeCriteria();
	//private EdgeProviderMapImpl m_edges = new EdgeProviderMapImpl();

	public NCSSelectionTree(FilterableHierarchicalContainer container) {
		super(container);
	}

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
	public void select(Object itemId) {
		m_currentCriteria.add((Long)itemId);
		m_graphContainer.setCriteria(m_currentCriteria);

		/*
		// TODO: Create edge references that correspond to the selected items
		NCSComponent comp = m_dao.get((Long)itemId);
		Long parentId = (Long)getParent(itemId);
		for (Object peerKey : getChildren(parentId)) {
			if (peerKey.equals(itemId)) {
				continue;
			} else if (isSelected(parentId)) {
				// create edge between node and peer
			} else if (isSelected(peerKey)) {
				// create edge between node and peer
			}
		}
		*/
		super.select(itemId);
	}

	@Override
	public void unselect(Object itemId) {
		m_currentCriteria.remove((Long)itemId);
		m_graphContainer.setCriteria(m_currentCriteria);

		// TODO: Remove edge references that correspond to the unselected items

		super.unselect(itemId);
	}

}
