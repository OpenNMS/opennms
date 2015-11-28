/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.vaadin.masterpage;

import org.opennms.web.navigate.PageNavEntry;

// TODO create a vaadin-commons module where all the Header and AdminPageNavEntries can live, because currently they exist in each vaadin implementation
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
