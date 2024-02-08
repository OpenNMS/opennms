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
package org.opennms.netmgt.collection.api;

import java.util.function.Function;

/**
 * Singleton used to lookup {@link ResourceType}s by name.
 *
 * This class is used when resolving {@link org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource}
 * resource types.
 *
 * This class could be removed once all of the {@link org.opennms.netmgt.collection.api.ServiceCollector}
 * implementations have been migrated to use the {@link org.opennms.netmgt.collection.support.builder.CollectionSetBuilder}.
 * At which point we could move the resource lookup and persistence logic outside the collectors and into collectd.
 *
 * @author jwhite
 */
public class ResourceTypeMapper {

    private static ResourceTypeMapper instance;

    private Function<String,ResourceType> mapper;

    private ResourceTypeMapper(){}

    public static ResourceTypeMapper getInstance(){
        if(instance == null){
            instance = new ResourceTypeMapper();
        }
        return instance;
    }

    public void setResourceTypeMapper(Function<String,ResourceType> mapper) {
        this.mapper = mapper;
    }

    public ResourceType getResourceType(String name) {
        if (mapper != null) {
            return mapper.apply(name);
        }
        return null;
    }

    public ResourceType getResourceTypeWithFallback(String name, String fallback) {
        if (mapper == null) {
            return null;
        }
        
        ResourceType rt = mapper.apply(name);
        if (rt != null) {
            return rt;
        }

        if (fallback != null) {
            // We didn't find the resource type, but a fallback was set
            // so try looking that one up
            rt = mapper.apply(fallback);
            if (rt != null) {
                // We found the fallback type, so we'll use this one
                // but rename it to be the same as the originally requested type
                return new DelegatingResourceType(rt) {
                    @Override
                    public String getName() {
                        return name;
                    }
                };
            }
        }

        return null;
    }
}
