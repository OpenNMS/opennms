/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
