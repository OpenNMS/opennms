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

package org.opennms.netmgt.provision.service.jmx;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.opennms.netmgt.provision.service.jmx.annotation.Composite;
import org.opennms.netmgt.provision.service.jmx.parser.TypeDescriptor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Helper to create a {@link CompositeType} or {@link TabularType} from {@link Composite} annotated beans.
 *
 * @author mvrueden
 */
public class JmxUtils {

    private static final LoadingCache<Class<?>, TypeDescriptor> cache = CacheBuilder.<Class<?>, TypeDescriptor>newBuilder()
            .build(new CacheLoader<Class<?>, TypeDescriptor>() {
                @Override
                public TypeDescriptor load(Class key) throws Exception {
                    return new TypeDescriptor(key);
                }
            });

    public static CompositeType createCompositeType(Class<?> type) {
        final TypeDescriptor typeDescriptor = getFromCache(type);
        return typeDescriptor.getCompositeType();
    }

    public static TabularType createTableType(Class<?> type) {
        final TypeDescriptor typeDescriptor = getFromCache(type);
        final TabularType tabularType = typeDescriptor.getTabularType();
        return tabularType;
    }

    public static CompositeData toCompositeData(Object dataBean) {
        Objects.requireNonNull(dataBean);

        TypeDescriptor typeDescriptor = getFromCache(dataBean.getClass());
        return typeDescriptor.getCompositeData(dataBean);
    }

    public static <T> TabularData toTabularData(List<T> beans) {
        if (beans.isEmpty()) {
            return null; // nothing we can do
        }
        TypeDescriptor typeDescriptor = getFromCache(beans.get(0).getClass());
        TabularType tableType = typeDescriptor.getTabularType();
        TabularDataSupport tableData = new TabularDataSupport(tableType);
        beans.stream().forEach(bean -> tableData.put(typeDescriptor.getCompositeData(bean)));
        return tableData;
    }

    private static TypeDescriptor getFromCache(Class<?> type) {
        try {
            return cache.get(type);
        } catch (ExecutionException e) {
            // something went wrong, just do not cache and return something at least
            return new TypeDescriptor(type);
        }
    }

}
