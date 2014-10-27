/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;


public class MockSearchConsumer /*implements SearchConsumer */ {
    private String m_search;
    private int m_minimumSeverity;

    public MockSearchConsumer(final String search, final int minimumSeverity) {
        m_search = search;
        m_minimumSeverity = minimumSeverity;
    }

    public String getSearchString() {
        return m_search;
    }

    public void setSearchString(String searchString) {
        m_search = searchString;
    }

    public int getMinimumSeverity() {
        return m_minimumSeverity;
    }

    public void setMinimumSeverity(int minSeverity) {
        m_minimumSeverity = minSeverity;
    }

    public boolean isSearching() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void refresh() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    public void clearSearch() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
