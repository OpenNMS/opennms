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

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;

import com.google.common.collect.Sets;

public abstract class AbstractMockEdge implements Edge {

    private int weight = Edge.DEFAULT_WEIGHT;

    private Status status;

    private BusinessService source;

    private MapFunction mapFunction;

    private Set<String> reductionKeys = Sets.newHashSet();

    private long id;

    public AbstractMockEdge(Long id, MapFunction mapFunction) {
        this.id = id;
        this.mapFunction = mapFunction;
    }

    @Override
    public BusinessService getSource() {
        return source;
    }

    @Override
    public Status getOperationalStatus() {
        return status;
    }

    @Override
    public void setMapFunction(MapFunction mapFunction) {
        this.mapFunction = mapFunction;
    }

    @Override
    public void setSource(BusinessService source) {
        this.source = source;
    }

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Set<String> getReductionKeys() {
        return reductionKeys;
    }

    @Override
    public MapFunction getMapFunction() {
        return mapFunction;
    }

    @Override
    public int getWeight() {
        return weight;
    }
}
