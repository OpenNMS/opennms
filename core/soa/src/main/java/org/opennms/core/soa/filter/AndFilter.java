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
package org.opennms.core.soa.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.opennms.core.soa.Filter;

/**
 * AndFilter
 *
 * @author brozow
 */
public class AndFilter extends AbstractFilter {
    
    List<Filter> m_filters;

    public AndFilter(List<Filter> filters) {
        m_filters = filters;
    }
    
    public AndFilter(Filter... filters) {
        this(Arrays.asList(filters));
    }

    @Override
    public boolean match(Map<String, String> properties) {
        for(Filter f : m_filters) {
            if (!f.match(properties)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("(&");
        for(Filter f : m_filters) {
            buf.append(f);
        }
        buf.append(")");
        return buf.toString();
    }

}
