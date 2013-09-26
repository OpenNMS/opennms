/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.osgi.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDaoContainer extends OnmsDaoContainer<OnmsNode,Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(NodeDaoContainer.class);
    private static final long serialVersionUID = -5697472655705494537L;

    public NodeDaoContainer(final NodeDao dao) {
        super(OnmsNode.class, dao);
        addBeanToHibernatePropertyMapping("primaryInterface", "ipInterfaces.ipAddress");
    }

    @Override
    protected Integer getId(final OnmsNode bean) {
        return bean == null ? null : bean.getId();
    }

    @Override
    protected void addAdditionalCriteriaOptions(final Criteria criteria, final Page page, final boolean doOrder) {
        if (!doOrder) return;
        criteria.setAliases(Arrays.asList(new Alias[] {
                new Alias("ipInterfaces", "ipInterfaces", Alias.JoinType.LEFT_JOIN, new EqRestriction("ipInterfaces.isSnmpPrimary", PrimaryType.PRIMARY))
        }));
    }

    @Override
    protected void doItemAddedCallBack(final int rowNumber, final Integer id, final OnmsNode eachBean) {
        eachBean.getPrimaryInterface();
    }

    @Override
    @EventConsumer
    public void verticesUpdated(final VerticesUpdateManager.VerticesUpdateEvent event) {
        LOG.debug("verticesUpdated({})", event);
        final Set<VertexRef> vertexRefs = event.getVertexRefs();
        final List<Integer> nodeIds = new ArrayList<Integer>();
        for (final VertexRef vRef : vertexRefs) {
            if(vRef.getNamespace().equals("nodes")) {
                try {
                    nodeIds.add(Integer.parseInt(vRef.getId()));
                } catch (final NumberFormatException e) {
                    //do nothing
                }
            }
        }

        final List<Restriction> newRestrictions = new NodeIdFocusToRestrictionsConverter() {
            @Override
            protected Restriction createRestriction(final Integer nodeId) {
                return new EqRestriction("id", nodeId);
            }
        }.getRestrictions(nodeIds);
        if (!getRestrictions().equals(newRestrictions)) { // selection really changed
            setRestrictions(newRestrictions);
            getCache().reload(getPage());
            fireItemSetChangedEvent();
        } else {
            LOG.warn("Selection has not changed: {}", nodeIds);
        }
    }
}


