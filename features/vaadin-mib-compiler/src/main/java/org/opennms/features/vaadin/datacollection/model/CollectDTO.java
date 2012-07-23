/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.datacollection.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class CollectDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class CollectDTO implements Serializable {

    /** The include groups. */
    private List<String> m_includeGroups;

    /**
     * Instantiates a new Collect DTO.
     */
    public CollectDTO() {
        super();
        m_includeGroups = new ArrayList<String>();
    }

    /**
     * Gets the include group collection.
     *
     * @return the include group collection
     */
    public List<String> getIncludeGroupCollection() {
        return m_includeGroups;
    }

    /**
     * Sets the include group collection.
     *
     * @param includeGroups the new include group collection
     */
    public void setIncludeGroupCollection(final List<String> includeGroups) {
        this.m_includeGroups = includeGroups;
    }

}
