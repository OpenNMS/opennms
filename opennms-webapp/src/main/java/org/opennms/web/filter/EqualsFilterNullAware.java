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

/** Allows null as value for equals operations. */
public abstract class EqualsFilterNullAware extends OneArgFilter<String> {

    public EqualsFilterNullAware(final String filterType, final SQLType<String> type, final String fieldName, final String propertyName, final String value){
        super(filterType, type, fieldName, propertyName, toValueAllowNull(value));
    }

    private static String toValueAllowNull(String value) {
        return "null".equals(value) ? null : value;
    }

    @Override
    public Criterion getCriterion() {
        if(getValue() == null) {
            return Restrictions.isNull(getPropertyName());
        }
        return Restrictions.eq(getPropertyName(), getValue());
    }

    @Override
    public String getSQLTemplate() {
        if(getValue() == null) {
            return " " + getSQLFieldName() + " IS NULL ";
        }
        return " " + getSQLFieldName() + " =  %s ";
    }

}
