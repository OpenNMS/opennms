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
package org.opennms.netmgt.measurements.api;

import java.util.List;
import java.util.ServiceLoader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.RowSortedTable;

import org.opennms.netmgt.measurements.api.exceptions.FilterException;
import org.opennms.netmgt.measurements.model.FilterDef;
import org.opennms.netmgt.measurements.model.FilterMetaData;
import org.springframework.stereotype.Component;

/**
 * Used to apply a series of {@link Filter} to a {@link RowSortedTable}. 
 *
 * @author jwhite
 */
@Component("filterEngine")
public class FilterEngine {

    private final static ServiceLoader<FilterFactory> filterFactories = ServiceLoader.load(FilterFactory.class);

    private List<FilterMetaData> filterMetaDatas = null;

    /**
     * Applies the given filter.
     */
    public void filter(FilterDef filterDef, RowSortedTable<Long, String, Double> table) throws Exception {
        Preconditions.checkNotNull(filterDef, "filterDef argument");
        Preconditions.checkNotNull(table, "table argument");

        filter(Lists.newArrayList(filterDef), table);
    }

    /**
     * Successively applies all of the filters.
     */
    public void filter(final List<FilterDef> filterDefinitions, final RowSortedTable<Long, String, Double> table) throws FilterException {
        Preconditions.checkNotNull(filterDefinitions, "filterDefinitions argument");
        Preconditions.checkNotNull(table, "table argument");

        for (FilterDef filterDef : filterDefinitions) {
            Filter filter = getFilter(filterDef);
            if (filter == null) {
                throw new FilterException("No filter implementation found for {}", filterDef.getName());
            }
            try {
                filter.filter(table);
            } catch (Throwable t) {
                throw new FilterException(t, "An error occurred while applying filter {}", t.getMessage());
            }
        }
    }

    /**
     * Retrieves a {@link Filter} that supports the given filter definition.
     *
     * @return null if no suitable {@link Filter}  was found
     */
    private Filter getFilter(FilterDef filterDef) {
        for (FilterFactory module : filterFactories) {
            Filter filter = module.getFilter(filterDef);
            if (filter != null) {
                return filter;
            }
        }
        return null;
    }

    public synchronized List<FilterMetaData> getFilterMetaData() {
        if (filterMetaDatas != null) {
            return filterMetaDatas;
        }

        filterMetaDatas = Lists.newArrayList();
        for (FilterFactory module : filterFactories) {
            filterMetaDatas.add(new FilterMetaData(module.getFilterType()));
        }
        return filterMetaDatas;
    }

    public FilterMetaData getFilterMetaData(String filterName) {
        // The list of filters should be relatively small, so we afford
        // to iterate over the results when requested
        for (FilterMetaData metaData : getFilterMetaData()) {
            if (metaData.getName().equalsIgnoreCase(filterName) ||
                    metaData.getCanonicalName().equals(filterName)) {
                return metaData;
            }
        }
        return null;
    }
}
