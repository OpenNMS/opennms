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
package org.opennms.core.criteria.restrictions;

import java.util.Collection;

import org.opennms.core.criteria.restrictions.SqlRestriction.Type;

public abstract class Restrictions {

    private static final Restriction[] EMPTY_RESTRICTION_ARRAY = new Restriction[0];

    public static NullRestriction isNull(final String attribute) {
        return new NullRestriction(attribute);
    }

    public static NotNullRestriction isNotNull(final String attribute) {
        return new NotNullRestriction(attribute);
    }

    public static EqRestriction id(final Integer id) {
        return eq("id", id);
    }

    public static EqRestriction eq(final String attribute, final Object comparator) {
        return new EqRestriction(attribute, comparator);
    }

    public static EqPropertyRestriction eqProperty(final String attribute, final Object comparator) {
        return new EqPropertyRestriction(attribute, comparator);
    }

    public static Restriction ne(final String attribute, final Object comparator) {
        return new NeRestriction(attribute, comparator);
    }

    public static GtRestriction gt(final String attribute, final Object comparator) {
        return new GtRestriction(attribute, comparator);
    }

    public static GeRestriction ge(final String attribute, final Object comparator) {
        return new GeRestriction(attribute, comparator);
    }

    public static LtRestriction lt(final String attribute, final Object comparator) {
        return new LtRestriction(attribute, comparator);
    }

    public static LeRestriction le(final String attribute, final Object comparator) {
        return new LeRestriction(attribute, comparator);
    }

    public static LikeRestriction like(final String attribute, final Object comparator) {
        return new LikeRestriction(attribute, comparator);
    }

    public static IlikeRestriction ilike(final String attribute, final Object comparator) {
        return new IlikeRestriction(attribute, comparator);
    }

    public static IplikeRestriction iplike(final String attribute, final Object comparator) {
        return new IplikeRestriction(attribute, comparator);
    }

    public static InRestriction in(final String attribute, final Collection<?> collection) {
        return new InRestriction(attribute, collection);
    }

    public static BetweenRestriction between(final String attribute, final Object begin, final Object end) {
        return new BetweenRestriction(attribute, begin, end);
    }

    public static NotRestriction not(final Restriction restriction) {
        return new NotRestriction(restriction);
    }

    public static AllRestriction and(final Restriction... restrictions) {
        return new AllRestriction(restrictions);
    }

    public static AllRestriction multipleAnd(final Restriction... restrictions) {
        return new AllRestriction(true, restrictions);
    }

    public static AnyRestriction or(final Restriction... restrictions) {
        return new AnyRestriction(restrictions);
    }

    public static AllRestriction all(final Restriction... restrictions) {
        return new AllRestriction(restrictions);
    }

    
    public static AllRestriction all(final Collection<Restriction> restrictions) {
        return new AllRestriction(restrictions.toArray(EMPTY_RESTRICTION_ARRAY));
    }

    public static AnyRestriction any(final Restriction... restrictions) {
        return new AnyRestriction(restrictions);
    }

    public static AnyRestriction any(final Collection<Restriction> restrictions) {
        return new AnyRestriction(restrictions.toArray(EMPTY_RESTRICTION_ARRAY));
    }

    public static AttributeRestriction sql(final String sql) {
        return new SqlRestriction(sql);
    }

    public static AttributeRestriction sql(final String sql, Object parameter, Type type) {
        return new SqlRestriction(sql, parameter, type);
    }

    public static AttributeRestriction sql(final String sql, Object[] parameters, Type[] types) {
        return new SqlRestriction(sql, parameters, types);
    }

    public static RegExpRestriction regExp(String attribute, String comparator) {
        return new RegExpRestriction(attribute, comparator);
    }
}
