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
package org.opennms.web.rest.support;

import java.util.function.Function;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;

/**
 * Specific {@link CriteriaBehavior} for IP address values that can handle
 * {@code iplike} String values.
 * 
 * NOTE: Because {@link CriteriaBuilder#iplike(String, Object)} uses
 * {@link Restrictions#iplike(String, Object)} and it uses
 * {@link org.hibernate.criterion.Restrictions#sqlRestriction(String)},
 * this behavior can only be used against the root alias. If we enhanced
 * the Criteria API to expose subcriteria for aliases than it might
 * be possible to leverage iplike against aliased properties.
 */
public class IpLikeCriteriaBehavior extends CriteriaBehavior<String> {

    public IpLikeCriteriaBehavior(String name) {
        this(name, (b,v,c,w)-> {
            switch(c) {
            case EQUALS:
                // Undo the FIQL wildcard replacements
                b.iplike(name, ((String)v).replaceAll("%", "*"));
                break;
            case NOT_EQUALS:
                // Undo the FIQL wildcard replacements
                b.not().iplike(name, ((String)v).replaceAll("%", "*"));
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type for iplike expression: " + c.toString());
            }
        });
    }

    protected IpLikeCriteriaBehavior(String name, BeforeVisit beforeVisit) {
        super(name, Function.identity(), beforeVisit);
        // Skip normal processing for this property since we're applying
        // the filter in the BeforeVisit operation.
        super.setSkipPropertyByDefault(true);
    }
}
