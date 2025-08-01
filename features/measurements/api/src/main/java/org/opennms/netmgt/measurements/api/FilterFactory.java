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

import org.opennms.netmgt.measurements.model.FilterDef;

/**
 * Used to instantiate a {@link org.opennms.netmgt.measurements.api.Filter}
 * from the corresponding filter definition.
 *
 * @author jwhite
 */
public interface FilterFactory {
    /**
     * Retrieves the appropriate {@link Filter} for the given configuration.
     *
     * @param filterDef
     *   a filter definition
     * @return
     *   null if this factory doesn't support the filter in question
     */
    Filter getFilter(FilterDef filterDef);

    /**
     * Retrieves a reference to the associated {@link Filter} type.
     *
     * @return
     *   the filter configuration 
     */
    Class<? extends Filter> getFilterType();
}
