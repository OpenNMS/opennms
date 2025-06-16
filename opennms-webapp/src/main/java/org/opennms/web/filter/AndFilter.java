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
package org.opennms.web.filter;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * AndFilter
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class AndFilter extends ConditionalFilter {
    
    /**
     * <p>Constructor for AndFilter.</p>
     *
     * @param filters a {@link org.opennms.web.filter.Filter} object.
     */
    public AndFilter(Filter...filters) {
        super("AND", filters);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        Conjunction conjunction = Restrictions.conjunction();
        
        for(Filter filter : getFilters()) {
            conjunction.add(filter.getCriterion());
        }
        
        return conjunction;
    }

}
