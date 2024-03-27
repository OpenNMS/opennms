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
package org.opennms.web.navigate;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

public interface NavBarEntry {

    class NavBarContext implements MenuContext {
        private final String location;
        private final Function<String, Boolean> userInRoleFunction;

        public NavBarContext(String location, Function<String, Boolean> userInRoleFunction) {
            this.userInRoleFunction = Objects.requireNonNull(userInRoleFunction);
            this.location = location;
        }

        @Override
        public String getLocation() {
            return location;
        }

        @Override
        public boolean isUserInRole(String role) {
            return userInRoleFunction.apply(role);
        }
    }

    String getName();
    String getDisplayString();
    String getUrl();

    /**
     * If this navbar entry has sub-entries, return them.  May return null if there are no sub-entries.
     */
    List<NavBarEntry> getEntries();
    boolean hasEntries();

    DisplayStatus evaluate(MenuContext context);

    /**
     * Return an object that represents whether or not the entry should be displayed and/or linked.
     *
     * @deprecated use {@link #evaluate(MenuContext)} instead.
     */
    @Deprecated
    default DisplayStatus evaluate(HttpServletRequest request) {
        final String location = request.getParameter("location");
        final Function<String, Boolean> userInRoleFunction = s -> request.isUserInRole(s);
        return evaluate(new NavBarContext(location, userInRoleFunction));
    }
}
