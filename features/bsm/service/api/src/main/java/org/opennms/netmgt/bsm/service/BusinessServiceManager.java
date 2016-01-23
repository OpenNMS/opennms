/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service;

import java.util.List;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.mapreduce.MapFunction;
import org.opennms.netmgt.bsm.service.model.mapreduce.ReductionFunction;
import org.opennms.netmgt.core.service.NodeManager;

public interface BusinessServiceManager extends NodeManager {

    List<BusinessService> getAllBusinessServices();

    List<BusinessServiceDTO> search(BusinessServiceSearchCriteria businessServiceSearchCriteria);

    Long save(BusinessServiceDTO newObject);

    <T extends Edge> T createEdge(Class<T> type, BusinessService child, MapFunction mapFunction);

    void saveBusinessService(BusinessService newObject);

    void deleteBusinessService(BusinessService service);

    BusinessService getBusinessServiceById(Long id);

    boolean assignIpService(BusinessService service, IpService ipService);

    boolean removeIpService(BusinessService service, IpService ipService);

    boolean assignChildService(BusinessService service, BusinessService childService);

    boolean removeChildService(BusinessService service, BusinessService childService);

    Set<BusinessService> getFeasibleChildServices(BusinessService service);

    Status getOperationalStatusForBusinessService(BusinessService service);

    Status getOperationalStatusForIPService(IpService ipService);

    List<IpService> getAllIpServices();

    IpService getIpServiceById(Integer id);

    void setIpServices(BusinessService service, Set<IpService> ipServices);

    void setChildServices(BusinessService service, Set<BusinessService> childServices);

    /**
     * Triggers a reload of the Business Service Daemon.
     */
    void triggerDaemonReload();

    Set<BusinessService> getParentServices(Long id);

    Status getOperationalStatusForReductionKey(String reductionKey);

    List<MapFunction> listMapFunctions();

    List<ReductionFunction> listReduceFunctions();
}
