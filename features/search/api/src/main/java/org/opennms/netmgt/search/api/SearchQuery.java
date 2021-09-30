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

package org.opennms.netmgt.search.api;

import java.security.Principal;
import java.util.Objects;
import java.util.function.Function;

/**
 * The query the user performed plus some additional data to perform the search.
 *
 * @author mvrueden
 */
public class SearchQuery {
    public static final int DEFAULT_MAX_RESULT = 10;

    private String input;
    private int maxResults;
    private Principal principal;
    private Function<String, Boolean> userInRoleFunction;
    private String context;

    public SearchQuery(String input) {
        this.input = Objects.requireNonNull(input);
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        if (input != null) {
            this.input = input.trim();
        } else {
            this.input = null;
        }
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public void setUserInRoleFunction(Function<String, Boolean> userInRoleFunction) {
        this.userInRoleFunction = userInRoleFunction;
    }

    public boolean isUserInRole(String role) {
        if (userInRoleFunction != null) {
            return userInRoleFunction.apply(role);
        }
        return false;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }
}
