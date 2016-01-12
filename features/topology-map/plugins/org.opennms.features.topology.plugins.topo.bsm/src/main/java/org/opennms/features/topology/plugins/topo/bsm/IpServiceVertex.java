/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.bsm;

import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;
import org.opennms.netmgt.bsm.service.model.IpServiceDTO;

public class IpServiceVertex extends AbstractBusinessServiceVertex {

    private final Long businessServiceId;

    private final Integer ipServiceId;

    public IpServiceVertex(BusinessServiceDTO businessService, IpServiceDTO ipServiceDTO) {
        this(businessService.getId() + ":" + ipServiceDTO.getId(),
                ipServiceDTO.getServiceName(),
                businessService.getId(),
                Integer.valueOf(ipServiceDTO.getId()),
                ipServiceDTO.getIpAddress());
    }

    private IpServiceVertex(String id, String ipServiceName, Long businessServiceId, Integer ipServiceId, String ipAddress) {
        super(id, ipServiceName);
        this.businessServiceId = businessServiceId;
        this.ipServiceId = ipServiceId;
        setIpAddress(ipAddress);
        setLabel(ipServiceName);
        setTooltipText(String.format("Service '%s', IP: %s", ipServiceName, ipAddress));
    }

    public Long getBusinessServiceId() {
        return businessServiceId;
    }

    public Integer getIpServiceId() {
        return ipServiceId;
    }
}
