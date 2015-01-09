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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;

public class NavBarModel {
    private final HttpServletRequest m_request;
    private final Map<NavBarEntry, DisplayStatus> m_entries;
    private final List<Map.Entry<NavBarEntry, DisplayStatus>> m_entryList;

    public NavBarModel(final HttpServletRequest request, final Map<NavBarEntry, DisplayStatus> entries) {
        m_request = request;
        m_entries = entries;
        m_entryList = Lists.newArrayList(m_entries.entrySet());
    }

    public HttpServletRequest getRequest() {
        return m_request;
    }

    public Map<NavBarEntry, DisplayStatus> getEntries() {
        return m_entries;
    }

    /** Allows the Freemarker template by the NavBarController to iterate
     *  over the map entries.
     */
    public List<Map.Entry<NavBarEntry, DisplayStatus>> getEntryList() {
        return m_entryList;
    }
}
