/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
