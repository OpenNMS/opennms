/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.NavBarEntry;
import org.opennms.web.navigate.NavBarModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class NavBarController extends AbstractController implements InitializingBean {
    private List<NavBarEntry> m_navBarItems;
    
    public void afterPropertiesSet() {
        Assert.state(m_navBarItems != null, "navBarItems property has not been set");
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("navBar", "model", createNavBarModel(request));
    }

    private NavBarModel createNavBarModel(HttpServletRequest request) {
        Map<NavBarEntry, DisplayStatus> navBar = new LinkedHashMap<NavBarEntry, DisplayStatus>();
        
        for (NavBarEntry entry : getNavBarItems()) {
            navBar.put(entry, entry.evaluate(request));
        }

        return new NavBarModel(navBar);
    }

    public List<NavBarEntry> getNavBarItems() {
        return m_navBarItems;
    }

    public void setNavBarItems(List<NavBarEntry> navBarItems) {
        m_navBarItems = navBarItems;
    }
}
