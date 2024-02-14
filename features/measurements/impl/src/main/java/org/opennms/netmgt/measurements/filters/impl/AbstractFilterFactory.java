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
package org.opennms.netmgt.measurements.filters.impl;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterFactory;
import org.opennms.netmgt.measurements.api.FilterInfo;
import org.opennms.netmgt.measurements.api.FilterParam;
import org.opennms.netmgt.measurements.model.FilterDef;
import org.opennms.netmgt.measurements.model.FilterParamDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Helps automate the creation of {@link Filter} objects using the annotated parameters. 
 *
 * @author jwhite
 */
public abstract class AbstractFilterFactory<T extends Filter> implements FilterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFilterFactory.class);
    private final Class<T> type;
    private final FilterInfo info;

    public AbstractFilterFactory(Class<T> type) {
         this.type = type;
         info = type.getAnnotation(FilterInfo.class);
         Preconditions.checkState(info != null, "Filter is missing a FilterInfo annotation");
    }

    @Override
    public Class<T> getFilterType() {
        return type;
    }

    @Override
    public Filter getFilter(FilterDef filterDef) {
        if (!info.name().equalsIgnoreCase(filterDef.getName()) &&
                !type.getCanonicalName().equals(filterDef.getName())) {
            return null;
        }

        // Map the parameters by name, last one wins
        Map<String, String> parameterMap = Maps.newHashMap();
        for (FilterParamDef param : filterDef.getParameters()) {
            parameterMap.put(param.getKey(), param.getValue());
        }

        // Keep track of the parameters that we set, but not used by the fitler
        Set<String> unusedParams = Sets.newHashSet();
        unusedParams.addAll(parameterMap.keySet());

        T filter;
        try {
            filter = type.newInstance();
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
            if (parameterMap.containsKey(filterParam.key())) {
                effectiveValueAsStr = parameterMap.get(filterParam.key());
            } else if (!filterParam.required()) {
                effectiveValueAsStr = filterParam.value();
            } else {
                throw new IllegalArgumentException("Parameter with key '" + filterParam.key() + "' is required, but no value was given.");
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
                field.set(filter, effectiveValue);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw Throwables.propagate(e);
            }

            // We've used this parameter
            unusedParams.remove(filterParam.key());
        }

        if (unusedParams.size() > 0) {
            LOG.warn("The parameters with the following names were set, but not used by the filter: {}", unusedParams);
        }

        return filter;
    }

}
