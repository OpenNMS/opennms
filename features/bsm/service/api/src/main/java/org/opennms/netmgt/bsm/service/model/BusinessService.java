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

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;

public interface BusinessService {


    Long getId();

    String getName();

    void setName(String name);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    Set<BusinessService> getChildServices();

    Set<BusinessService> getParentServices();

    void save();

    void delete();

    Status getOperationalStatus();

    ReductionFunction getReduceFunction();

    void setReduceFunction(ReductionFunction reductionFunction);

    void setIpServiceEdges(Set<IpServiceEdge> ipServiceEdges);

    void addIpServiceEdge(IpService ipService, MapFunction mapFunction, int weight, String friendlyName);

    void setReductionKeyEdges(Set<ReductionKeyEdge> reductionKeyEdges);

    void addReductionKeyEdge(String reductionKey, MapFunction mapFunction, int weight, String friendlyName);

    void setChildEdges(Set<ChildEdge> childEdges);

    void addChildEdge(BusinessService child, MapFunction mapFunction, int weight);

    void removeEdge(Edge edge);

    Set<ReductionKeyEdge> getReductionKeyEdges();
    
    Set<IpServiceEdge> getIpServiceEdges();

    Set<ChildEdge> getChildEdges();
    
    Set<? extends Edge> getEdges();
}
