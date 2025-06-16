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
package org.opennms.web.outage;

import java.util.List;

import org.opennms.web.filter.Filter;

/**
 * Convenience data structure for holding the arguments to an outage query.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class OutageQueryParms extends Object {
    public SortStyle sortStyle;

    public OutageType outageType;

    public List<Filter> filters;

    public int limit;

    public int multiple;

    /**
     * Convert the internal (and useful) ArrayList filters object into an array
     * of Filter instances.
     *
     * @return an array of org$opennms$web$filter$Filter objects.
     */
    public Filter[] getFilters() {
        return this.filters.toArray(new Filter[this.filters.size()]);
    }
}
