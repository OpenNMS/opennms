/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.ui;

import org.opennms.web.navigate.PageNavEntry;

/**
 * Navigation entry for displaying a link to the surveillance view configuration page.
 *
 * @author Christian Pape
 */
public class AdminPageNavEntry implements PageNavEntry {
    /**
     * name of this PageNavEntry
     */
    private String m_name;
    /**
     * URL of this PageNavEntry
     */
    private String m_url;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the PageNavEntry.
     *
     * @param name the name to be set
     */
    public void setName(final String name) {
        this.m_name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return m_url;
    }

    /**
     * Sets the URL of this PageNavEntry.
     *
     * @param url the URL to be set
     */
    public void setUrl(final String url) {
        this.m_url = url;
    }
}