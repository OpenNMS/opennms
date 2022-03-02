/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.search.providers.action;

import java.util.Objects;
import java.util.function.Function;

import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.web.navigate.MenuContext;

/**
 * This {@link PrincipalCacheKey} object is required as besides the principal name,
 * we also require a delegate to the "isUserInRole"-Function.
 *
 * @author mvrueden
 */
class PrincipalCacheKey implements MenuContext {
    private final String principal;
    private final Function<String, Boolean> isUserInRoleFunction;

    PrincipalCacheKey(final SearchQuery query) {
        this(Objects.requireNonNull(query).getPrincipal().getName(), (role) -> query.isUserInRole(role));
    }

    private PrincipalCacheKey(final String principal, Function<String, Boolean> isUserInRoleFunction) {
        this.principal = Objects.requireNonNull(principal);
        this.isUserInRoleFunction = Objects.requireNonNull(isUserInRoleFunction);
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        Objects.requireNonNull(role);
        return isUserInRoleFunction.apply(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PrincipalCacheKey cacheKey = (PrincipalCacheKey) o;
        return Objects.equals(principal, cacheKey.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal);
    }
}
