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

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterFactory;
import org.opennms.netmgt.measurements.model.FilterDef;

import com.google.common.collect.RowSortedTable;

/**
 * A no-op filter used for testing.
 *
 * @author jwhite
 */
public class NoOpFilter implements FilterFactory {

    public static final String FILTER_NAME = "NoOp";

    @Override
    public Filter getFilter(FilterDef filterDef) {
        if (FILTER_NAME.equalsIgnoreCase(filterDef.getName())) {
            return new Filter() {
                @Override
                public void filter(RowSortedTable<Long, String, Double> dsAsTable) {
                    // pass
                }
            };
        };
        return null;
    }

    @Override
    public Class<? extends Filter> getFilterType() {
        return null;
    }
}
