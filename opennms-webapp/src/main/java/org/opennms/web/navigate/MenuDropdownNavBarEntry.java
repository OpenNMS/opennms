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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class MenuDropdownNavBarEntry implements NavBarEntry {
    private String m_name = null;
    private String m_contents = null;
    private List<NavBarEntry> m_entries = null;
 
    /**
     * <p>getUrl</p>
     *
     * This should never get called, since we render all sub-links
     * in getName();
     */
    public String getUrl() {
        return null;
    }

    /**
     * <p>getName</p>
     *
     * @return The text containing the menu entry/entries.
     */
    public String getName() {
        if (m_name == null || m_contents == null) return "";
        return "<div class=\"nav-dropdown\"><a href=\"#\" class=\"nav-dropdown\">" + m_name + " <span class=\"nav-item\">\u25BC</span></a><ul>" + m_contents + "</ul></div>";
    }
    
    public void setName(final String name) {
        m_name = name;
    }

    public List<NavBarEntry> getEntries() {
        return m_entries;
    }

    public void setEntries(final List<NavBarEntry> entries) {
        m_entries = entries;
    }

    /**
     * If there are any {@link NavBarEntry} objects in this
     * dropdown object, return DISPLAY_NO_LINK (since the
     * individual entries will handle their own)
     */
    public DisplayStatus evaluate(final HttpServletRequest request) {
        boolean display = false;
        if (m_entries != null) {
            final StringBuilder sb = new StringBuilder();
            for (final NavBarEntry entry : m_entries) {
                final DisplayStatus status = entry.evaluate(request);
                switch (status) {
                    case DISPLAY_LINK:
                        sb.append("<li><a href=\"" + entry.getUrl() + "\">" + entry.getName() + "</a></li>");
                        display = true;
                        break;
                    case DISPLAY_NO_LINK:
                        sb.append("<li>" + entry.getName() + "</li>");
                        display = true;
                        break;
                    default:
                        break;
                }
            }
            m_contents = sb.toString();
        }
        return display? DisplayStatus.DISPLAY_NO_LINK : DisplayStatus.NO_DISPLAY;
    }
}
