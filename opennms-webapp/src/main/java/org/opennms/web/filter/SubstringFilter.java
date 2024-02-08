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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

/**
 * <p>Abstract SubstringFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class SubstringFilter extends OneArgFilter<String> {

    /**
     * <p>Constructor for SubstringFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public SubstringFilter(String filterType, String fieldName, String propertyName, String value) {
        super(filterType, SQLType.STRING, fieldName, propertyName, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.ilike(getPropertyName(), getValue(), MatchMode.ANYWHERE);
    }

    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " ILIKE %s ";
    }

    /** {@inheritDoc} */
    @Override
    public String getBoundValue(String value) {
        return '%' + value + '%';
    }

    /** {@inheritDoc} */
    @Override
    public String formatValue(String value) {
        return super.formatValue('%'+value+'%');
    }
    
    

}
