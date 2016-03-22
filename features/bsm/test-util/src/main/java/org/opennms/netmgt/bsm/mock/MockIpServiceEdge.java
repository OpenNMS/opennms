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
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;

public class MockIpServiceEdge extends AbstractMockEdge implements IpServiceEdge {

    private IpService m_ipService;

    private String m_friendlyName;

    public MockIpServiceEdge(long id, IpService ipService, String friendlyName) {
        super(id, new Identity());
        m_friendlyName = friendlyName;
        m_ipService = ipService;
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
    public String getFriendlyName() {
        return m_friendlyName;
    }

    @Override
    public void setIpService(IpService ipService) {
        m_ipService = ipService;
    }

    @Override
    public void setFriendlyName(String friendlyName) {
        m_friendlyName = friendlyName;
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
