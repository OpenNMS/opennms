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
