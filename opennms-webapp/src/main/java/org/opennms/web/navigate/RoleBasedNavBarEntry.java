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

import com.google.common.base.Strings;
import java.util.Arrays;

public class RoleBasedNavBarEntry extends LocationBasedNavBarEntry {
    /** comma-separated list of roles */
    private String roles;

    public String getRoles() {
        return this.roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    /** {@inheritDoc} */
    @Override
    public DisplayStatus evaluate(MenuContext context) {
        if (!Strings.isNullOrEmpty(this.roles)) {
            boolean anyMatch = Arrays.stream(this.roles.split(",")).map(String::trim).anyMatch(context::isUserInRole);

            return anyMatch ? super.evaluate(context) : DisplayStatus.NO_DISPLAY;
        }

        return DisplayStatus.NO_DISPLAY;
    }
}
