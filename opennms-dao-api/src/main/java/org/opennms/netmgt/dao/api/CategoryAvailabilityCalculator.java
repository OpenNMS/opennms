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
package org.opennms.netmgt.dao.api;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.availability.CategoryAvailability;

/**
 * Computes service availability for a category of nodes over a time window.
 *
 * A category is defined the way categories.xml defines it: a filter rule that
 * selects the member nodes, plus an optional list of service names that limits
 * which services on those nodes count. Implementations must not materialize
 * the member node set outside the database; the computation has to hold up
 * for categories that match every node in a very large installation.
 */
public interface CategoryAvailabilityCalculator {

    /**
     * Calculate availability for one category.
     *
     * @param label        label to carry on the result
     * @param filterRule   filter rule selecting the member nodes; for a
     *                     categories.xml category this is the group's common
     *                     rule ANDed with the category's own rule
     * @param serviceNames service names to cover; null or empty covers every
     *                     service. A name starting with a tilde is treated as
     *                     a regular expression against the service name.
     * @param windowStart  start of the window, inclusive
     * @param windowEnd    end of the window, inclusive; normally "now"
     * @return the category availability with one entry per member node,
     *         including nodes that have no covered services
     * @throws IllegalArgumentException if the rule cannot be parsed or the
     *         window is empty
     */
    CategoryAvailability calculate(String label, String filterRule, Collection<String> serviceNames, Date windowStart, Date windowEnd);
}
