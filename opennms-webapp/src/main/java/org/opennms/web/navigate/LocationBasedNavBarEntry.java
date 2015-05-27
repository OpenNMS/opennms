/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.navigate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class LocationBasedNavBarEntry implements NavBarEntry {
    private String m_locationMatch;
    private String m_url;
    private String m_name;
    private List<NavBarEntry> m_entries;

    @Override
    public String getDisplayString() {
        return m_name;
    }

    @Override
    public String getUrl() {
        return m_url;
    }
    public void setUrl(String url) {
        m_url = url;
    }

    @Override
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }

    @Override
    public List<NavBarEntry> getEntries() {
        return m_entries;
    }
    public void setEntries(final List<NavBarEntry> entries) {
        m_entries = entries;
    }

    @Override
    public boolean hasEntries() {
        return m_entries != null && m_entries.size() > 0;
    }

    @Override
    public DisplayStatus evaluate(HttpServletRequest request) {
        return isLinkMatches(request) ? DisplayStatus.DISPLAY_NO_LINK : DisplayStatus.DISPLAY_LINK;
    }

    public String getLocationMatch() {
        return m_locationMatch;
    }
    public void setLocationMatch(String locationMatch) {
        m_locationMatch = locationMatch;
    }

    protected boolean isLinkMatches(HttpServletRequest request) {
        return m_locationMatch.equals(request.getParameter("location"));
    }
}
