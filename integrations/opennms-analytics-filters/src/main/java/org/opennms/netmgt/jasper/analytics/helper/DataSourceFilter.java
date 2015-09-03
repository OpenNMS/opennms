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

package org.opennms.netmgt.jasper.analytics.helper;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.RowSortedTable;

import org.opennms.netmgt.jasper.analytics.AnalyticsCommand;
import org.opennms.netmgt.jasper.analytics.Filter;
import org.opennms.netmgt.jasper.analytics.FilterFactory;

/**
 * Allows an {@link RowSortedTable} to be modified by analytics modules.
 *
 * The list of modules to run, and their options are set with
 * additional commands.
 *
 * The commands contains the following information:
 *   - moduleName is a unique name for the module
 *   - columnNameOrPrefix identifies the name of the column, or the prefix of the
 *   column name if there are multiple where the additional values will be stored
 *   - otherOptions are optional and specific to the module in question
 *
 * The modules are invoked in the same order as they appear in the analyticsCommandList.
 *
 *
 * @author jwhite
 */
public class DataSourceFilter {

    /**
     * A list of {@link FilterFactory} services that can be used to fetch analytics
     * filters to filter the measurements that are returned.
     */
    private final List<FilterFactory> filterFactories;

    public DataSourceFilter(List<FilterFactory> filterFactories) {
        Preconditions.checkArgument(filterFactories != null, "The filterFactories must not be null.");
        this.filterFactories = filterFactories;
    }

    public void filter(AnalyticsCommand command, RowSortedTable<Integer, String, Double> table) throws Exception {
        Preconditions.checkArgument(command != null, "command must not be null");
        Preconditions.checkArgument(table != null, "table must not be null");
        filter(Arrays.asList(new AnalyticsCommand[]{command}), table);
    }

    /**
     * Filters the given data source by successively applying
     * all of the analytics commands.
     */
    public void filter(final List<AnalyticsCommand> analyticsCommandList,
                       final RowSortedTable<Integer, String, Double> dsAsTable) throws Exception {
        for (AnalyticsCommand command : analyticsCommandList) {
            Filter filter = getFilter(command);
            if (filter == null) {
                throw new Exception("No analytics module found for " + command.getModule());
            }
            filter.filter(dsAsTable);
        }
    }

    /**
     * Retrieves an Enricher that supports the given analytics command
     *
     * @return null if no suitable Enricher was found
     * @throws Exception
     */
    private Filter getFilter(AnalyticsCommand command) throws Exception {
        for (FilterFactory module : filterFactories) {
            Filter filter = module.getFilter(command);
            if (filter != null) {
                return filter;
            }
        }
        return null;
    }
}

