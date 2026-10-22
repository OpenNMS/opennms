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

import java.net.InetAddress;
import java.util.function.Function;

import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.opennms.core.criteria.CriteriaBuilder;

/**
 * {@link CriteriaBehavior} for a root-alias {@code InetAddress} property that
 * accepts both literal addresses and {@code iplike} patterns: a literal keeps
 * the converter's {@code InetAddress} equality (the user type compares IPv6
 * correctly), while a wildcard value is applied as an {@code iplike}
 * restriction on the given column and the property itself is skipped.
 */
public class IpAddressCriteriaBehavior extends CriteriaBehavior<Object> {

    private static final Function<String,Object> CONVERTER = value ->
            // '*' has already been rewritten to '%' by the time a converter runs
            value.contains("%") ? value : CriteriaValueConverters.INET_ADDRESS_CONVERTER.apply(value);

    /**
     * @param column the SQL column {@code iplike} is applied to (the
     *               restriction names it directly, not the JPA attribute)
     */
    public IpAddressCriteriaBehavior(final String column) {
        super(null, CONVERTER, (b, v, c, w) -> {
            if (!w) {
                return;
            }
            final String pattern = ((String) v).replaceAll("%", "*");
            switch (c) {
                case EQUALS:
                    b.iplike(column, pattern);
                    break;
                case NOT_EQUALS:
                    b.not().iplike(column, pattern);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal condition type for iplike expression: " + c);
            }
        });
    }

    @Override
    public boolean shouldSkipProperty(final ConditionType condition, final boolean wildcard) {
        return wildcard;
    }
}
