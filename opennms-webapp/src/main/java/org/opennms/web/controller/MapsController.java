/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.MenuDropdownNavBarEntry;
import org.opennms.web.navigate.NavBarEntry;
import org.opennms.web.navigate.NavBarModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class MapsController extends AbstractController implements InitializingBean {
    private MenuDropdownNavBarEntry m_mapMenuEntries;

    @Override
    public void afterPropertiesSet() {
        Assert.state(m_mapMenuEntries != null, "mapMenuEntries property has not been set");
    }

    public MenuDropdownNavBarEntry getMapMenuEntries() {
        return m_mapMenuEntries;
    }

    public void setMapMenuEntries(final MenuDropdownNavBarEntry entries) {
        m_mapMenuEntries = entries;
    }

    private NavBarModel createNavBarModel(final HttpServletRequest request) {
        final Map<NavBarEntry, DisplayStatus> navBar = new LinkedHashMap<NavBarEntry, DisplayStatus>();

        for (final NavBarEntry entry : m_mapMenuEntries.getEntries()) {
            navBar.put(entry, entry.evaluate(request));
        }

        return new NavBarModel(request, navBar);
    }


    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return new ModelAndView("maps", "entries", createNavBarModel(request));
    }

}
