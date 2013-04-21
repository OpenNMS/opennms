/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.web.api.OnmsHeaderProvider;
import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.NavBarEntry;
import org.opennms.web.navigate.NavBarModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>NavBarController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NavBarController extends AbstractController implements InitializingBean, OnmsHeaderProvider {
    private List<NavBarEntry> m_navBarItems;
    
    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_navBarItems != null, "navBarItems property has not been set");
    }

    /** {@inheritDoc} */
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

    /**
     * <p>getNavBarItems</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<NavBarEntry> getNavBarItems() {
        return m_navBarItems;
    }

    /**
     * <p>setNavBarItems</p>
     *
     * @param navBarItems a {@link java.util.List} object.
     */
    public void setNavBarItems(List<NavBarEntry> navBarItems) {
        m_navBarItems = navBarItems;
    }

    @Override
    public String getHeaderHtml(HttpServletRequest request) {
        return createHeaderHtml(request);
    }
    
    private String createHeaderHtml(HttpServletRequest request) {
        return "<div id='header'>" +
              "<h1 id='headerlogo'><a href='index.jsp'><img src=\"../images/logo.png\" alt='OpenNMS Web Console Home'></a></h1>" +
          "<div id='headerinfo'>" +
          "<h2>Topology Map</h2>" +
          "<p align=\"right\" >" + 
          "User: <a href=\"/opennms/account/selfService/index.jsp\" title=\"Account self-service\"><strong>" + request.getRemoteUser() + "</strong></a>" +
          "&nbsp;(Notices " + getNoticeStatus() + " )" + 
          " - <a href=\"opennms/j_spring_security_logout\">Log out</a><br></p>"+
          "</div>" +
          "<div id='headernavbarright'>" +
          "<div class='navbar'>" +
          createNavBarHtml(request) +
          "</div>" +
          "</div>" +
          "<div class='spacer'><!-- --></div>" +
          "</div>";
    }

    private String getNoticeStatus() {
        String noticeStatus;
        try {
            noticeStatus = NotifdConfigFactory.getPrettyStatus();
            if ("Off".equals(noticeStatus)) {
              noticeStatus="<b id=\"notificationOff\">Off</b>";
            } else {
              noticeStatus="<b id=\"notificationOn\">On</b>";
            }
        } catch (Throwable t) {
            noticeStatus = "<b id=\"notificationOff\">Unknown</b>";
        }
        return noticeStatus;
    }

    private String createNavBarHtml(HttpServletRequest request) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<ul>");
        
        for (NavBarEntry entry : getNavBarItems()) {
            if(entry.evaluate(request) == DisplayStatus.DISPLAY_LINK) {
                strBuilder.append("<li><a href=\"" + entry.getUrl() +  "\" >" + entry.getName() + "</a></li>");
            }
        }
        strBuilder.append("</ul>");
        return strBuilder.toString();
    }
}
