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

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AtInterfaceDao;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsAtInterface;

public class MockAtInterfaceDao extends AbstractMockDao<OnmsAtInterface, Integer> implements AtInterfaceDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected Integer getId(final OnmsAtInterface entity) {
        return entity.getId();
    }

    @Override
    protected void generateId(final OnmsAtInterface entity) {
        entity.setId(m_id.getAndIncrement());
    }

    @Override
    public void markDeletedIfNodeDeleted() {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsAtInterface.class);
        builder.alias("node", "node").eq("node.type", "D");
        for (final OnmsAtInterface iface :findMatching(builder.toCriteria())) {
            iface.setStatus(StatusType.DELETED);
            saveOrUpdate(iface);
        }
    }

    @Override
    public void deactivateForSourceNodeIdIfOlderThan(final int sourceNodeid, final Date scanTime) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void deleteForNodeSourceIdIfOlderThan(final int sourceNodeid, final Date scanTime) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsAtInterface> findByMacAddress(final String macAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void setStatusForNode(final Integer nodeid, final StatusType action) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void setStatusForNodeAndIp(final Integer nodeid, final String ipAddr, final StatusType action) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void setStatusForNodeAndIfIndex(Integer nodeid, Integer ifIndex, StatusType action) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public OnmsAtInterface findByNodeAndAddress(final Integer nodeId, final InetAddress ipAddress, final String macAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsAtInterface> getAtInterfaceForAddress(final InetAddress address) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
