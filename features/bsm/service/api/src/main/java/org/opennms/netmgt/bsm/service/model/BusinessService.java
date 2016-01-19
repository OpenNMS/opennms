/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.model;

import org.opennms.web.rest.api.ResourceLocation;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsSeverity;

public interface BusinessService {
    Long getId();

    String getName();

    void setName(String name);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    void addAttribute(String key, String value);

    String removeAttribute(String key);

    Set<IpService> getIpServices();

    void setIpServices(Set<IpService> ipServices);

    void addIpService(IpService ipService);

    void removeIpService(IpService ipService);

    Set<BusinessService> getChildServices();

    void setChildServices(Set<BusinessService> childServices);

    void addChildService(BusinessService childService);

    void removeChildService(BusinessService childService);

    Set<BusinessService> getParentServices();

    void save();

    void delete();

    void setReductionKeys(Set<String> reductionKeySet);

    Set<String> getReductionKeys();

    OnmsSeverity getOperationalStatus();
}
