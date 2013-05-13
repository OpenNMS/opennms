/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.navigate;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>LocationBasedNavBarEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
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
    @Override
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
    @Override
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
    @Override
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
