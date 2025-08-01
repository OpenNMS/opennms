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
package org.opennms.web.rest.support.menu;

import java.util.List;

/**
 * Context that can be passed to a MenuProvider.
 * This can be used in unit or smoke tests to provide alternative implementations.
 */
public interface MenuRequestContext {
    String getRemoteUser();

    String calculateUrlBase();

    boolean isUserInRole(String role);

    boolean isUserInAnyRole(List<String> roles);

    String getFormattedDateTime();

    String getFormattedDate();

    String getFormattedTime();

    String getNoticeStatus();

    String getSystemProperty(String name, String def);
}
