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
