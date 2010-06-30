/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Dec 08: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 Daniel J. Gregor, Jr.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.navigate;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>LocationBasedNavBarEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class LocationBasedNavBarEntry implements NavBarEntry {
    private String m_locationMatch;
    private String m_url;
    private String m_name;
 
    /**
     * <p>getLocationMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLocationMatch() {
        return m_locationMatch;
    }

    /**
     * <p>setLocationMatch</p>
     *
     * @param locationMatch a {@link java.lang.String} object.
     */
    public void setLocationMatch(String locationMatch) {
        m_locationMatch = locationMatch;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.navigate.NavBarEntry#getURL()
     */
    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUrl() {
        return m_url;
    }

    /**
     * <p>setUrl</p>
     *
     * @param url a {@link java.lang.String} object.
     */
    public void setUrl(String url) {
        m_url = url;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.navigate.NavBarEntry#getName()
     */
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.navigate.NavBarEntry#evaluate(javax.servlet.http.HttpServletRequest)
     */
    /** {@inheritDoc} */
    public DisplayStatus evaluate(HttpServletRequest request) {
        return isLinkMatches(request) ? DisplayStatus.DISPLAY_NO_LINK : DisplayStatus.DISPLAY_LINK;
    }

    /**
     * <p>isLinkMatches</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a boolean.
     */
    protected boolean isLinkMatches(HttpServletRequest request) {
        return m_locationMatch.equals(request.getParameter("location"));
    }
}
