//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2006 The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.controller;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.SurveillanceViewConfigDao;
import org.opennms.web.Util;
import org.opennms.web.acegisecurity.Authentication;
import org.opennms.web.navigate.NavBarEntry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class NavBarController extends AbstractController implements InitializingBean {

    private SurveillanceViewConfigDao m_surveillanceViewConfigDao;
    private LocationMonitorDao m_locationMonitorDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return new ModelAndView("navBar", "model", createNavBarModel(request));
    }
    
    public void setSurveillanceViewConfigDao(SurveillanceViewConfigDao dao) {
        m_surveillanceViewConfigDao = dao;
    }
    
    public void setLocationMonitorDao(LocationMonitorDao dao) {
        m_locationMonitorDao = dao;
    }
    
    public void afterPropertiesSet() {
        Assert.state(m_surveillanceViewConfigDao != null, "surveillanceViewConfigDao property has not been set");
        Assert.state(m_locationMonitorDao != null, "locationMonitorDao property has not been set");
    }

    private NavBarModel createNavBarModel(HttpServletRequest request) {
        String mapEnableLocation = Vault.getHomeDir() + File.separator
                + "etc" + File.separator + "map.enable";
        File mapEnableFile = new File(mapEnableLocation);

        String vulnEnableLocation = Vault.getHomeDir() + File.separator
                + "etc" + File.separator + "vulnerabilities.enable";
        File vulnEnableFile = new File(vulnEnableLocation);

        LinkedList<NavBarEntry> navBar = new LinkedList<NavBarEntry>();
        navBar.add(new NavBarEntry("nodelist", "element/nodeList.htm",
                                   "Node List"));
        navBar.add(new NavBarEntry("element", "element/index.jsp", "Search"));
        navBar.add(new NavBarEntry("outages", "outage/index.jsp", "Outages"));
        navBar.add(new NavBarEntry("pathOutage", "pathOutage/index.jsp",
                                   "Path Outages"));
        navBar.add(new NavBarEntry("event", "event/index.jsp", "Events"));
        navBar.add(new NavBarEntry("alarm", "alarm/index.jsp", "Alarms"));
        navBar.add(new NavBarEntry("notification", "notification/index.jsp",
                                   "Notification"));
        navBar.add(new NavBarEntry("asset", "asset/index.jsp", "Assets"));
        navBar.add(new NavBarEntry("report", "report/index.jsp", "Reports"));
        navBar.add(new NavBarEntry("chart", "charts/index.jsp", "Charts"));
        
        if (m_surveillanceViewConfigDao.getViews().getViewCount() > 0 &&
                m_surveillanceViewConfigDao.getDefaultView() != null) {
            String viewName =
                m_surveillanceViewConfigDao.getDefaultView().getName();
            navBar.add(new NavBarEntry("surveillance",
                                       "surveillanceView.htm?viewName="
                                           + Util.htmlify(viewName),
                                       "Surveillance"));
        }
        
        if (m_locationMonitorDao.findAllMonitoringLocationDefinitions().size() > 0) {
            navBar.add(new NavBarEntry("distributedstatus",
                                       "distributedStatusSummary.htm",
                                       "Distributed Status"));
        }
        
        if (vulnEnableFile.exists()) {
            navBar.add(new NavBarEntry("vulnerability",
                                       "vulnerability/index.jsp",
                                       "Vulnerabilities"));
        }
        if (mapEnableFile.exists()) {
            navBar.add(new NavBarEntry("map", "Index.map", "Map"));
        }
        if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
            navBar.add(new NavBarEntry("admin", "admin/index.jsp", "Admin"));
        }
        navBar.add(new NavBarEntry("help", "help/index.jsp", "Help"));

        return new NavBarModel(navBar, request.getParameter("location"));
    }

    public class NavBarModel {
        List<NavBarEntry> m_entries;

        String m_location;

        public NavBarModel(List<NavBarEntry> entries, String location) {
            m_entries = entries;
            m_location = location;
        }

        public List<NavBarEntry> getEntries() {
            return m_entries;
        }

        public String getLocation() {
            return m_location;
        }

    }
}
