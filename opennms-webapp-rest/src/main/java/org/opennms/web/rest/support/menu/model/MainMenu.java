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
package org.opennms.web.rest.support.menu.model;

import java.util.ArrayList;
import java.util.List;

public class MainMenu {
    public String templateName;
    public String baseHref;
    public String homeUrl;
    public String formattedDateTime;
    public String formattedDate;
    public String formattedTime;
    public String noticeStatus;
    public String username;
    public String baseNodeUrl;
    public Boolean zenithConnectEnabled;
    public String zenithConnectBaseUrl;
    public String zenithConnectRelativeUrl;
    public Boolean displayAddNodeButton;
    public Boolean sideMenuInitialExpand;
    public String copyrightDates;
    public String version;

    final public List<MenuEntry> menus = new ArrayList<>();
    public MenuEntry helpMenu;
    public MenuEntry selfServiceMenu;
    public MenuEntry userNotificationMenu;
    public MenuEntry provisionMenu;
    public MenuEntry flowsMenu;
    public MenuEntry configurationMenu; // aka admin menu, the "cogs"

    public void addTopMenu(MenuEntry entry) {
        this.menus.add(entry);
    }
}
