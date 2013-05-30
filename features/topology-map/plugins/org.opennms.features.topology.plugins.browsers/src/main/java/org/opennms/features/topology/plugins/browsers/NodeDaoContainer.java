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

package org.opennms.features.topology.plugins.browsers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItem;

public class NodeDaoContainer extends OnmsDaoContainer<OnmsNode,Integer> {

	private static final long serialVersionUID = -5697472655705494537L;

	private Map<Object,Class<?>> m_properties;

	public NodeDaoContainer(NodeDao dao) {
		super(dao);
	}

	@Override
	public Class<OnmsNode> getItemClass() {
		return OnmsNode.class;
	}

	private synchronized void loadPropertiesIfNull() {
		if (m_properties == null) {
			m_properties = new TreeMap<Object,Class<?>>();
			BeanItem<OnmsNode> item = new BeanItem<OnmsNode>(new OnmsNode());
			for (Object key : item.getItemPropertyIds()) {
				m_properties.put(key, item.getItemProperty(key).getType());
			}
		}
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		loadPropertiesIfNull();

		return Collections.unmodifiableCollection(m_properties.keySet());
	}

	@Override
	protected Integer getId(OnmsNode bean){
		return bean == null ? null : bean.getId();
	}

	@Override
	public Class<?> getType(Object propertyId) {
		return m_properties.get(propertyId);
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		loadPropertiesIfNull();

		Collection<Object> propertyIds = new HashSet<Object>();
		propertyIds.addAll(m_properties.keySet());

		// We have to have special handling inside the NodeTable for this field to be sortable
		// since it is not a database field. We must sort using Comparators instead of Criteria 
		// ordering.
		//propertyIds.remove("primaryInterface");

		return Collections.unmodifiableCollection(propertyIds);
	}

	@Override
	public void selectionChanged(SelectionContext selectionContext) {
		Collection<Order> oldOrders = m_criteria.getOrders();
		Set<Restriction> restrictions = new HashSet<Restriction>();
		for (VertexRef ref : selectionContext.getSelectedVertexRefs()) {
			if ("nodes".equals(ref.getNamespace())) {
				try {
					restrictions.add(new EqRestriction("id", Integer.valueOf(ref.getId())));
				} catch (NumberFormatException e) {
					LoggerFactory.getLogger(this.getClass()).warn("Cannot filter nodes with ID: {}", ref.getId());
				}
			}
		}

		m_criteria = new Criteria(getItemClass());
		if (restrictions.size() > 0) {
			AnyRestriction any = new AnyRestriction(restrictions.toArray(new Restriction[0]));
			m_criteria.addRestriction(any);
		}
		m_criteria.setOrders(oldOrders);
		fireItemSetChangedEvent();
	}
}
