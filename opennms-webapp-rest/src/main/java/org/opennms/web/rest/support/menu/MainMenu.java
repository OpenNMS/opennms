/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support.menu;

import java.util.ArrayList;
import java.util.List;

public class MainMenu {
    public String baseHref;
    public String homeUrl;
    public String formattedTime;
    public String noticeStatus;
    public String username;
    public String baseNodeUrl;
    public String copyrightDates;
    public String version;
    final public List<TileProviderItem> userTileProviders = new ArrayList<>();

    final public List<TopMenuEntry> menus = new ArrayList<>();
    public TopMenuEntry helpMenu;
    public TopMenuEntry selfServiceMenu;
    public TopMenuEntry userNotificationMenu;
    public MenuEntry provisionMenu;
    public MenuEntry flowsMenu;
    public MenuEntry configurationMenu; // aka admin menu, the "cogs"
    public Notices notices;

    public void addTopMenu(TopMenuEntry entry) {
        this.menus.add(entry);
    }
}
