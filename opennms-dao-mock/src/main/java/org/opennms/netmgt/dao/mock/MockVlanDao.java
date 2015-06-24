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

import org.opennms.netmgt.dao.api.VlanDao;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsVlan;

public class MockVlanDao extends AbstractMockDao<OnmsVlan,Integer> implements VlanDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected Integer getId(final OnmsVlan entity) {
        return entity.getId();
    }

    @Override
    protected void generateId(final OnmsVlan entity) {
        entity.setId(m_id.getAndIncrement());
    }

    @Override
    public void markDeletedIfNodeDeleted() {
        for (final OnmsVlan vlan : findAll()) {
            if (vlan.getNode() != null && "D".equals(vlan.getNode().getType())) {
                vlan.setStatus(StatusType.DELETED);
            }
        }
    }

    @Override
    public void deactivateForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        for (final OnmsVlan vlan : getVlansForNodeIdIfOlderThan(nodeId, scanTime)) {
            if (StatusType.ACTIVE.equals(vlan.getStatus())) {
                vlan.setStatus(StatusType.INACTIVE);
            }
        }
    }

    @Override
    public void deleteForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsVlan> vlans = getVlansForNodeIdIfOlderThan(nodeId, scanTime);
        for (final OnmsVlan vlan : vlans) {
            if (!StatusType.ACTIVE.equals(vlan.getStatus())) {
                delete(vlan);
            }
        }
    }

    @Override
    public void setStatusForNode(final Integer nodeId, final StatusType action) {
        for (final OnmsVlan vlan : findAll()) {
            if (vlan.getNode() != null && vlan.getNode().getId() == nodeId) {
                vlan.setStatus(action);
            }
        }
    }

    private List<OnmsVlan> getVlansForNodeIdIfOlderThan(final int nodeId, final Date scanTime) {
        final List<OnmsVlan> vlans = new ArrayList<OnmsVlan>();
        for (final OnmsVlan vlan : findAll()) {
            if (vlan.getNode() != null && vlan.getNode().getId() != nodeId) continue;
            if (vlan.getLastPollTime() != null && vlan.getLastPollTime().before(scanTime)) {
                vlans.add(vlan);
            }
        }
        return vlans;
    }

    @Override
    public OnmsVlan findByNodeAndVlan(final Integer nodeId, final Integer vlanId) {
        for (final OnmsVlan vlan : findAll()) {
            if (vlan.getNode() != null && vlan.getNode().getId() != nodeId) continue;
            if (vlan.getVlanId() == vlanId) {
                return vlan;
            }
        }
        return null;
    }

}
