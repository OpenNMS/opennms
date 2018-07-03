/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockSnmpInterfaceDao extends AbstractMockDao<OnmsSnmpInterface, Integer> implements SnmpInterfaceDao {
    private static final Logger LOG = LoggerFactory.getLogger(MockSnmpInterfaceDao.class);
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public Integer save(final OnmsSnmpInterface iface) {
        updateParent(iface);
        return super.save(iface);
    }

    @Override
    public void update(final OnmsSnmpInterface iface) {
        updateParent(iface);
        super.update(iface);
    }

    @Override
    protected void generateId(final OnmsSnmpInterface iface) {
        iface.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsSnmpInterface iface) {
        return iface.getId();
    }

    private void updateParent(final OnmsSnmpInterface iface) {
        OnmsNode node = null;
        if (iface.getNodeId() != null) {
            node = getNodeDao().get(iface.getNodeId());
        } else if (iface.getNode() != null) {
            node = getNodeDao().findByForeignId(iface.getNode().getForeignSource(), iface.getNode().getForeignId());
        }
        if (node != null && node != iface.getNode()) {
            LOG.debug("merging node {} into node {}", iface.getNode(), node);
            node.mergeNode(iface.getNode(), new NullEventForwarder(), false);
            iface.setNode(node);
        }
        if (!iface.getNode().getSnmpInterfaces().contains(iface)) {
            LOG.debug("adding SNMP interface to node {}: {}", iface.getNode().getId(), iface);
            iface.getNode().addSnmpInterface(iface);
        }
    }


    @Override
    public OnmsSnmpInterface findByNodeIdAndIfIndex(final Integer nodeId, final Integer ifIndex) {
        for (final OnmsSnmpInterface iface : findAll()) {
            if (nodeId.equals(iface.getNode().getId()) && ifIndex.equals(iface.getIfIndex())) {
                return iface;
            }
        }
        return null;
    }

    @Override
    public OnmsSnmpInterface findByForeignKeyAndIfIndex(final String foreignSource, final String foreignId, final Integer ifIndex) {
        for (final OnmsSnmpInterface iface : findAll()) {
            final OnmsNode node = iface.getNode();
            if (foreignSource.equals(node.getForeignSource()) && foreignId.equals(node.getForeignId()) && ifIndex.equals(iface.getIfIndex())) {
                return iface;
            }
        }
        return null;
    }

    @Override
    public OnmsSnmpInterface findByNodeIdAndDescription(Integer nodeId, String description) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void markHavingFlows(final Integer nodeId, final Collection<Integer> snmpIfIndexes) {
    }

    @Override
    public List<OnmsSnmpInterface> findAllHavingFlows(Integer nodeId) {
        return Collections.emptyList();
    }
}
