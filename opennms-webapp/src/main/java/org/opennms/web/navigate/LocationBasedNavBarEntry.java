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

package org.opennms.web.navigate;

import javax.servlet.http.HttpServletRequest;

public class LocationBasedNavBarEntry implements NavBarEntry {
    private String m_locationMatch;
    private String m_url;
    private String m_name;
 
    public String getLocationMatch() {
        return m_locationMatch;
    }

    public void setLocationMatch(String locationMatch) {
        m_locationMatch = locationMatch;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.navigate.NavBarEntry#getURL()
     */
    public String getUrl() {
        return m_url;
    }

    public void setUrl(String url) {
        m_url = url;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.navigate.NavBarEntry#getName()
     */
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.navigate.NavBarEntry#evaluate(javax.servlet.http.HttpServletRequest)
     */
    public DisplayStatus evaluate(HttpServletRequest request) {
        return isLinkMatches(request) ? DisplayStatus.DISPLAY_NO_LINK : DisplayStatus.DISPLAY_LINK;
    }

    protected boolean isLinkMatches(HttpServletRequest request) {
        return m_locationMatch.equals(request.getParameter("location"));
    }
}
