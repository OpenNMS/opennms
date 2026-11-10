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
import org.hibernate.type.StringType;
import org.opennms.core.utils.IplikeSqlTranslator;


/**
 * Encapsulates all interface filtering functionality.
 */
public abstract class IPLikeFilter extends OneArgFilter<String> {

    /**
     * <p>Constructor for IPLikeFilter.</p>
     *
     * @param filterType a {@link java.lang.String} object.
     * @param fieldName a {@link java.lang.String} object.
     * @param propertyName a {@link java.lang.String} object.
     * @param ipLikePattern a {@link java.lang.String} object.
     */
    public IPLikeFilter(final String filterType, final String fieldName, final String propertyName, final String ipLikePattern) {
        super(filterType, SQLType.STRING, fieldName, propertyName, ipLikePattern);
    }

    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        // stays on iplike(): the template contract requires exactly one %s
        // consumed as a bound JDBC parameter (bindParam is final and always
        // binds one), which a translated predicate cannot satisfy
        return " IPLIKE(" + getSQLFieldName() + ", %s) ";
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        // translatable match expressions become native-inet range predicates
        // answered through an expression index; the rest call iplike()
        final String nativePredicate = IplikeSqlTranslator.toSqlPredicate(getValue(), "{alias}." + getPropertyName());
        if (nativePredicate != null) {
            return Restrictions.sqlRestriction(nativePredicate);
        }
        return Restrictions.sqlRestriction("iplike( {alias}."+getPropertyName()+", ?)", getValue(), StringType.INSTANCE);
    }



}
