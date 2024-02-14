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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/** Allows null as a valid value for not equals filtering. */
public abstract class NotEqualsFilterNullAware extends OneArgFilter<String> {

    public NotEqualsFilterNullAware(final String filterType, final SQLType<String> type, final String fieldName, final String daoPropertyName, final String value) {
        super(filterType, type, fieldName, daoPropertyName, toValueAllowNull(value));
    }

    private static String toValueAllowNull(String value) {
        return "null".equals(value) ? null : value;
    }

    @Override
    public Criterion getCriterion() {
        if(getValue() == null) {
            return Restrictions.isNotNull(getPropertyName());
        }
        return Restrictions.or(Restrictions.ne(getPropertyName(), getValue()), Restrictions.isNull(getPropertyName()));
    }

    @Override
    public String getSQLTemplate() {
        if(getValue() == null) {
            return " " + getSQLFieldName() + " IS NOT NULL ";
        }
        return " " + getSQLFieldName() + " <> %s OR IS NULL";
    }

}
