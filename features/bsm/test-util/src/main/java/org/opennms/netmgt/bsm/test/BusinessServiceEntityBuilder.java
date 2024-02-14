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
package org.opennms.netmgt.bsm.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.AbstractMapFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.AbstractReductionFunctionEntity;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class BusinessServiceEntityBuilder {

    private class EdgeDefinition<V> {
        private final V value;
        private final String friendlyName;
        private final AbstractMapFunctionEntity mapFunction;
        private final int weight;

        public EdgeDefinition(V value, AbstractMapFunctionEntity mapFunction, int weight, String friendlyName) {
            this.value = value;
            this.mapFunction = mapFunction;
            this.weight = weight;
            this.friendlyName = friendlyName;
        }
    }

    private String name;
    private final Map<String, String> attributes = new HashMap<>();
    private List<EdgeDefinition<BusinessServiceEntity>> children = new ArrayList<>();
    private List<EdgeDefinition<OnmsMonitoredService>> ipServices = new ArrayList<>();
    private List<EdgeDefinition<String>> reductionKeys = new ArrayList<>();
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

    public BusinessServiceEntityBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public BusinessServiceEntity toEntity() {
        BusinessServiceEntity entity = new BusinessServiceEntity();
        entity.setName(name);
        entity.setId(id);
        entity.setAttributes(attributes);
        if (reduceFunction != null) {
            entity.setReductionFunction(reduceFunction);
        }
        ipServices.forEach(e -> entity.addIpServiceEdge(e.value, e.mapFunction, e.weight, e.friendlyName));
        children.forEach(e -> entity.addChildServiceEdge(e.value, e.mapFunction, e.weight));
        reductionKeys.forEach(e -> entity.addReductionKeyEdge(e.value, e.mapFunction, e.weight, e.friendlyName));
        return entity;
    }

    public BusinessServiceEntityBuilder addIpService(OnmsMonitoredService ipService, AbstractMapFunctionEntity mapFunctionEntity) {
        return addIpService(ipService, mapFunctionEntity, 1);
    }

    public BusinessServiceEntityBuilder addIpService(OnmsMonitoredService ipService, AbstractMapFunctionEntity mapFunctionEntity, int weight) {
        ipServices.add(new EdgeDefinition<>(ipService, mapFunctionEntity, weight, null));
        return this;
    }

    public BusinessServiceEntityBuilder addReductionKey(String reductionKey, AbstractMapFunctionEntity mapFunctionEntity, String friendlyName) {
        return addReductionKey(reductionKey, mapFunctionEntity, 1, friendlyName);
    }

    public BusinessServiceEntityBuilder addReductionKey(String reductionKey, AbstractMapFunctionEntity mapFunctionEntity) {
        return addReductionKey(reductionKey, mapFunctionEntity, 1, null);
    }

    public BusinessServiceEntityBuilder addReductionKey(String reductionKey, AbstractMapFunctionEntity mapFunctionEntity, int weight) {
        return addReductionKey(reductionKey, mapFunctionEntity, weight, null);
    }

    public BusinessServiceEntityBuilder addReductionKey(String reductionKey, AbstractMapFunctionEntity mapFunctionEntity, int weight, String friendlyName) {
        reductionKeys.add(new EdgeDefinition<>(reductionKey, mapFunctionEntity, weight, friendlyName));
        return this;
    }

    public BusinessServiceEntityBuilder addChildren(BusinessServiceEntity child, AbstractMapFunctionEntity mapFunctionEntity) {
        return addChildren(child, mapFunctionEntity, 1);
    }

    public BusinessServiceEntityBuilder addChildren(BusinessServiceEntity child, AbstractMapFunctionEntity mapFunctionEntity, int weight) {
        children.add(new EdgeDefinition<>(child, mapFunctionEntity, weight, null));
        return this;
    }

    public BusinessServiceEntityBuilder reduceFunction(AbstractReductionFunctionEntity reductionFunction) {
        this.reduceFunction = reductionFunction;
        return this;
    }
}
