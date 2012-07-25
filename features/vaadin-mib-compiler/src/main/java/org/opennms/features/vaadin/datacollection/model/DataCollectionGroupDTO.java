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
 * The Class DataCollectionGroupDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class DataCollectionGroupDTO implements Serializable {

    /** Data collector group name. */
    private String m_name;

    /** List of Custom resource types. */
    private List<ResourceTypeDTO> m_resourceTypes;

    /** List of MIB object groups. */
    private List<GroupDTO> m_groups;

    /** List of System Definitions. */
    private List<SystemDefDTO> m_systemDefs;


    /**
     * Instantiates a new Data Collection Group DTO.
     */
    public DataCollectionGroupDTO() {
        super();
        m_resourceTypes = new ArrayList<ResourceTypeDTO>();
        m_groups = new ArrayList<GroupDTO>();
        m_systemDefs = new ArrayList<SystemDefDTO>();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets the groups collection.
     *
     * @return the groups collection
     */
    public List<GroupDTO> getGroupCollection() {
        return m_groups;
    }

    /**
     * Gets the resource types collection.
     *
     * @return the resource types collection
     */
    public List<ResourceTypeDTO> getResourceTypeCollection() {
        return m_resourceTypes;
    }

    /**
     * Gets the system definitions collection.
     *
     * @return the system definitions collection
     */
    public List<SystemDefDTO> getSystemDefCollection() {
        return m_systemDefs;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(final String name) {
        this.m_name = name == null ? null : name.intern();
    }

    /**
     * Sets the groups collection.
     *
     * @param groups the new groups collection
     */
    public void setGroupCollection(final List<GroupDTO> groups) {
        this.m_groups = groups;
    }

    /**
     * Sets the resource types collection.
     *
     * @param resourceTypes the new resource type collection
     */
    public void setResourceTypeCollection(final List<ResourceTypeDTO> resourceTypes) {
        this.m_resourceTypes = resourceTypes;
    }

    /**
     * Sets the system definitions collection.
     *
     * @param systemDefs the new system definitions collection
     */
    public void setSystemDefCollection(final List<SystemDefDTO> systemDefs) {
        this.m_systemDefs = systemDefs;
    }

}
