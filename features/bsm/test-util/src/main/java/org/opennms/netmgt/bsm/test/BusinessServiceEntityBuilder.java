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

package org.opennms.netmgt.bsm.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.AbstractReductionFunctionEntity;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class BusinessServiceEntityBuilder {

    private String name;
    private final Map<String, String> attributes = new HashMap<>();
    private Set<BusinessServiceEntity> children = new HashSet<>();
    private Set<OnmsMonitoredService> ipServices = new HashSet<>();
    private Long id;
    private AbstractReductionFunctionEntity reduceFunction;

    public BusinessServiceEntityBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BusinessServiceEntityBuilder addAttribute(String key, String value) {
        attributes.put(key, value);
        return this;
    }

    public BusinessServiceEntityBuilder addChildren(BusinessServiceEntity children) {
       this.children.add(children);
        return this;
    }

    public BusinessServiceEntityBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public BusinessServiceEntity toEntity() {
        BusinessServiceEntity entity = new BusinessServiceEntity();
        entity.setName(name);
        entity.setId(id);
        entity.setIpServices(ipServices);
        entity.setAttributes(attributes);
        if (reduceFunction != null) {
            entity.setReductionFunction(reduceFunction);
        }

        // TODO MVR we have to deal with the map function stuff
        ipServices.forEach(e -> entity.addIpServiceEdge(e, new IdentityEntity()));
        children.forEach(e -> entity.addChildServiceEdge(e, new IdentityEntity()));
        reductionKeys.forEach(e -> entity.addReductionKeyEdge(e, new IdentityEntity()));
        return entity;
    }

    public BusinessServiceEntityBuilder addIpService(OnmsMonitoredService ipService) {
        ipServices.add(ipService);
        return this;
    }

    public BusinessServiceEntityBuilder addReductionKey(String reductionKey) {
        reductionKeys.add(reductionKey);
        return this;
    }

    public BusinessServiceEntityBuilder reduceFunction(AbstractReductionFunctionEntity reductionFunction) {
        this.reduceFunction = reductionFunction;
        return this;
    }
}
