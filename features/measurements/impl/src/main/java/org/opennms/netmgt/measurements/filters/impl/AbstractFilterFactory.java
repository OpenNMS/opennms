/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.filters.impl;

import java.lang.reflect.Field;
import java.util.Map;

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterConfig;
import org.opennms.netmgt.measurements.api.FilterFactory;
import org.opennms.netmgt.measurements.api.FilterParam;
import org.opennms.netmgt.measurements.model.FilterDefinition;
import org.opennms.netmgt.measurements.model.FilterParameter;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * Helps automate the creation of {@link FilterConfig} objects using the annotated parameters. 
 *
 * @author jwhite
 */
public abstract class AbstractFilterFactory<T extends FilterConfig> implements FilterFactory {
    
    private final Class<T> type;

    public AbstractFilterFactory(Class<T> type) {
         this.type = type;
    }

    public abstract boolean supports(FilterDefinition filterDef);

    public abstract Filter createFilter(T config);

    @Override
    public Class<T> getFilterConfigType() {
        return type;
    }

    @Override
    public Filter getFilter(FilterDefinition filterDef) {
        if (!supports(filterDef)) {
            return null;
        }

        // Map the parameters by name, last one wins
        Map<String, String> parameterMap = Maps.newHashMap();
        for (FilterParameter param : filterDef.getParameters()) {
            parameterMap.put(param.getName(), param.getValue());
        }

        T filterConfig;
        try {
            filterConfig = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }

        for(Field field : type.getDeclaredFields()) {
            FilterParam filterParam = field.getAnnotation(FilterParam.class);

            // Skip fields that are not annotated
            if (filterParam == null) {
                continue;
            }

            // Determine whether we use the default or user supplied value
            String effectiveValueAsStr = null;
            if (parameterMap.containsKey(filterParam.name())) {
                effectiveValueAsStr = parameterMap.get(filterParam.name());
            } else if (!filterParam.required()) {
                effectiveValueAsStr = filterParam.value();
            } else {
                throw new IllegalArgumentException("Parameter with name '" + filterParam.name() + "' is required, but no value was given.");
            }

            // Convert the value to the appropriate type
            Object effectiveValue = effectiveValueAsStr;
            if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                effectiveValue = Boolean.valueOf(effectiveValueAsStr);
            } else if (field.getType() == Double.class || field.getType() == double.class) {
                effectiveValue = Double.valueOf(effectiveValueAsStr);
            } else if (field.getType() == Integer.class || field.getType() == int.class) {
                effectiveValue = Integer.valueOf(effectiveValueAsStr);
            } else if (field.getType() == Long.class || field.getType() == long.class) {
                effectiveValue = Long.valueOf(effectiveValueAsStr);
            }

            // Set the field's value
            try {
                field.setAccessible(true);
                field.set(filterConfig, effectiveValue);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw Throwables.propagate(e);
            }
        }

        return createFilter(filterConfig);
    }

}
