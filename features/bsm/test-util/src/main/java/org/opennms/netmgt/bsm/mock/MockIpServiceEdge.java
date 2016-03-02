/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.mock;

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyIpServiceEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;

public class MockIpServiceEdge implements ReadOnlyIpServiceEdge {

    private final IpService m_ipService;
    private final Long m_id;
    private final String m_friendlyName;

    public MockIpServiceEdge(long id, IpService ipService, String friendlyName) {
        m_ipService = ipService;
        m_id = id;
        m_friendlyName = friendlyName;
    }

    @Override
    public Long getId() {
        return m_id;
    }

    @Override
    public Type getType() {
        return Type.IP_SERVICE;
    }

    @Override
    public IpService getIpService() {
        return m_ipService;
    }

    @Override
    public Set<String> getReductionKeys() {
        return m_ipService.getReductionKeys();
    }

    @Override
    public MapFunction getMapFunction() {
        return new Identity();
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public String getFriendlyName() {
        return m_friendlyName;
    }
}
