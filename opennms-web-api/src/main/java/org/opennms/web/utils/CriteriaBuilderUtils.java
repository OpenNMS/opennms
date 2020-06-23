/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
