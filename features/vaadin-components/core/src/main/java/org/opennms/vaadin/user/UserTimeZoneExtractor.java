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
package org.opennms.vaadin.user;

import java.time.ZoneId;

import org.opennms.core.time.CentralizedDateTimeFormat;

import com.vaadin.ui.UI;

public final class UserTimeZoneExtractor {

    public static ZoneId extractUserTimeZoneIdOrNull(final UI ui) {
        // Verify if a ui is provided, still attached and has a session
        if (ui != null && ui.isAttached() && ui.getSession() != null && ui.getSession().getSession() != null) {
            // Only the wrapped session has the attribute set, the VaadinSession does not!
            return (ZoneId) ui.getSession().getSession().getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID);
        }
        return null;
    }
}
