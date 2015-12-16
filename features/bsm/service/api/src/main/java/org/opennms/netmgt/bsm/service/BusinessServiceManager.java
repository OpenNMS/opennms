/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service;

import java.util.List;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;
import org.opennms.netmgt.model.OnmsSeverity;

public interface BusinessServiceManager {

    List<BusinessServiceDTO> findAll();

    Long save(BusinessServiceDTO newObject);

    void update(BusinessServiceDTO objectToUpdate);

    BusinessServiceDTO getById(Long id);

    void delete(Long id);

    boolean assignIpInterface(Long serviceId, Integer ipServiceId);

    boolean removeIpInterface(Long serviceId, Integer ipServiceId);

    boolean assignChildService(Long serviceId, Long childServiceId);

    boolean removeChildService(Long serviceId, Long childServiceId);

    Set<BusinessServiceDTO> getFeasibleChildServices(BusinessServiceDTO service);

    Set<BusinessServiceDTO> getParentServices(BusinessServiceDTO service);

    OnmsSeverity getOperationalStatusForBusinessService(Long serviceId);

    OnmsSeverity getOperationalStatusForIPService(Integer ipServiceId);
}
