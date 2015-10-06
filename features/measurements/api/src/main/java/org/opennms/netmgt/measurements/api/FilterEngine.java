/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.api;

import java.util.List;
import java.util.ServiceLoader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.RowSortedTable;

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterFactory;
import org.opennms.netmgt.measurements.model.FilterDefinition;

/**
 * Used to apply a series of {@link Filter} to a {@link RowSortedTable}. 
 *
 * @author jwhite
 */
public class FilterEngine {

    private final static ServiceLoader<FilterFactory> filterFactories = ServiceLoader.load(FilterFactory.class);

    /**
     * Applies the given filter.
     */
    public void filter(FilterDefinition filterDef, RowSortedTable<Long, String, Double> table) throws Exception {
        Preconditions.checkNotNull(filterDef, "filterDef argument");
        Preconditions.checkNotNull(table, "table argument");

        filter(Lists.newArrayList(filterDef), table);
    }

    /**
     * Successively applies all of the filters.
     */
    public void filter(final List<FilterDefinition> filterDefinitions,
                       final RowSortedTable<Long, String, Double> table) throws Exception {
        Preconditions.checkNotNull(filterDefinitions, "filterDefinitions argument");
        Preconditions.checkNotNull(table, "table argument");

        for (FilterDefinition filterDef : filterDefinitions) {
            Filter filter = getFilter(filterDef);
            if (filter == null) {
                throw new Exception("No filter implementation found for " + filterDef.getName());
            }
            filter.filter(table);
        }
    }

    /**
     * Retrieves a {@link Filter} that supports the given filter definition.
     *
     * @return null if no suitable {@link Filter}  was found
     */
    private Filter getFilter(FilterDefinition filterDef) {
        for (FilterFactory module : filterFactories) {
            Filter filter = module.getFilter(filterDef);
            if (filter != null) {
                return filter;
            }
        }
        return null;
    }
}
