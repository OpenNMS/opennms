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

import org.opennms.netmgt.dao.api.StpInterfaceDao;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsStpInterface;

public class MockStpInterfaceDao extends AbstractMockDao<OnmsStpInterface,Integer> implements StpInterfaceDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected Integer getId(final OnmsStpInterface entity) {
        return entity.getId();
    }

    @Override
    protected void generateId(final OnmsStpInterface entity) {
        entity.setId(m_id.getAndIncrement());
    }

    @Override
    public void markDeletedIfNodeDeleted() {
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && "D".equals(iface.getNode().getType())) {
                iface.setStatus(StatusType.DELETED);
            }
        }
    }

    @Override
    public void deactivateForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        for (final OnmsStpInterface iface : getStpInterfacesForNodeIdIfOlderThan(nodeId, scanTime)) {
            if (StatusType.ACTIVE.equals(iface.getStatus())) {
                iface.setStatus(StatusType.INACTIVE);
            }
        }
    }

    @Override
    public void deleteForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsStpInterface> ifaces = getStpInterfacesForNodeIdIfOlderThan(nodeId, scanTime);
        for (final OnmsStpInterface iface : ifaces) {
            if (!StatusType.ACTIVE.equals(iface.getStatus())) {
                delete(iface);
            }
        }
    }

    @Override
    public void setStatusForNode(final Integer nodeId, final StatusType action) {
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() == nodeId) {
                iface.setStatus(action);
            }
        }
    }

    @Override
    public void setStatusForNodeAndIfIndex(final Integer nodeId, final Integer ifIndex, final StatusType action) {
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() == nodeId) {
                if (iface.getIfIndex() == ifIndex) {
                    iface.setStatus(action);
                }
            }
        }
    }

    @Override
    public OnmsStpInterface findByNodeAndVlan(final Integer nodeId, final Integer bridgePort, final Integer vlan) {
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() == nodeId) {
                if (iface.getBridgePort() == bridgePort && iface.getVlan() == vlan) {
                    return iface;
                }
            }
        }
        return null;
    }


    private List<OnmsStpInterface> getStpInterfacesForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsStpInterface> ifaces = new ArrayList<OnmsStpInterface>();
        for (final OnmsStpInterface iface : findAll()) {
            if (iface.getNode() != null && iface.getNode().getId() != nodeId) continue;
            if (iface.getLastPollTime() != null && iface.getLastPollTime().before(scanTime)) {
                ifaces.add(iface);
            }
        }
        return ifaces;
    }

}
