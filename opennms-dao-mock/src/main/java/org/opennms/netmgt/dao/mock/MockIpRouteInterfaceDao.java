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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.IpRouteInterfaceDao;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsNode;

public class MockIpRouteInterfaceDao extends AbstractMockDao<OnmsIpRouteInterface,Integer> implements IpRouteInterfaceDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected Integer getId(final OnmsIpRouteInterface entity) {
        return entity.getId();
    }

    @Override
    protected void generateId(final OnmsIpRouteInterface entity) {
        entity.setId(m_id.getAndIncrement());
    }

    @Override
    public void markDeletedIfNodeDeleted() {
        for (final OnmsIpRouteInterface iface : findAll()) {
            final OnmsNode node = iface.getNode();
            if (node != null && "D".equals(node.getType())) {
                iface.setStatus(StatusType.DELETED);
            }
        }
    }

    @Override
    public void deactivateForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        for (final OnmsIpRouteInterface iface : getInterfacesForNodeIdIfOlderThan(nodeId, scanTime)) {
            if (!iface.getStatus().equals(StatusType.ACTIVE)) continue;
            iface.setStatus(StatusType.INACTIVE);
        }
    }

    @Override
    public void deleteForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsIpRouteInterface> ifaces = getInterfacesForNodeIdIfOlderThan(nodeId, scanTime);
        for (final OnmsIpRouteInterface iface : ifaces) {
            if (!iface.getStatus().equals(StatusType.ACTIVE)) {
                delete(iface);
            }
        }
    }

    @Override
    public void setStatusForNode(final Integer nodeId, final StatusType action) {
        for (final OnmsIpRouteInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() == nodeId) {
                iface.setStatus(action);
            }
        }
    }

    @Override
    public void setStatusForNodeAndIfIndex(final Integer nodeId, final Integer ifIndex, final StatusType action) {
        for (final OnmsIpRouteInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() == nodeId
                    && iface.getRouteIfIndex() == ifIndex) {
                iface.setStatus(action);
            }
        }
    }

    @Override
    public OnmsIpRouteInterface findByNodeAndDest(final Integer nodeId, final String routeDest) {
        for (final OnmsIpRouteInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() == nodeId && routeDest.equals(iface.getRouteDest())) {
                return iface;
            }
        }
        return null;
    }

    private List<OnmsIpRouteInterface> getInterfacesForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsIpRouteInterface> ifaces = new ArrayList<OnmsIpRouteInterface>();
        for (final OnmsIpRouteInterface iface : findAll()) {
            final OnmsNode node = iface.getNode();
            if (node == null || nodeId != node.getId()) continue;
            if (iface.getLastPollTime() == null || iface.getLastPollTime().before(scanTime)) {
                ifaces.add(iface);
            }
        }
        return ifaces;
    }

}
