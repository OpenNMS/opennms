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
package org.opennms.web.utils;

import java.util.Objects;

import org.opennms.core.criteria.CriteriaBuilder;

public class CriteriaBuilderUtils {

    public static CriteriaBuilder buildFrom(Class<?> clazz, QueryParameters queryParameters) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(queryParameters);

        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(clazz);

        applyQueryParameters(criteriaBuilder, queryParameters);

        return criteriaBuilder;
    }

    public static void applyQueryParameters(CriteriaBuilder builder, QueryParameters queryParameters) {
        Objects.requireNonNull(builder);
        Objects.requireNonNull(queryParameters);

        builder.limit(queryParameters.getLimit());
        if (queryParameters.getOffset() != null) {
            builder.offset(queryParameters.getOffset());
        }
        if (queryParameters.getOrder() != null) {
            builder.clearOrder();
            builder.orderBy(queryParameters.getOrder().getColumn(), !queryParameters.getOrder().isDesc());
        }
    }
}
