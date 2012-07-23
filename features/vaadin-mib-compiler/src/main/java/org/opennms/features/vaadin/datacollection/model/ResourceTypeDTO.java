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

/**
 * A custom resource type.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class ResourceTypeDTO implements Serializable {

    /** Resource type name. */
    private String m_name;

    /** Resource type label (this is what users see in the webUI). */
    private String m_label;

    /** Resource label expression (this is what users see in the webUI for each resource of this type). */
    private String m_resourceLabel;

    /**
     * Selects a PersistenceSelectorStrategy that decides which data is persisted and which is not.
     */
    private PersistenceSelectorStrategyDTO m_persistenceSelectorStrategy;

    /**
     * Selects a StorageStrategy that decides where data is stored.
     */
    private StorageStrategyDTO m_storageStrategy;


    /**
     * Instantiates a new Resource Type DTO.
     */
    public ResourceTypeDTO() {
        super();
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return m_label;
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
     * Gets the persistence selector strategy.
     *
     * @return the persistence selector strategy
     */
    public PersistenceSelectorStrategyDTO getPersistenceSelectorStrategy() {
        return m_persistenceSelectorStrategy;
    }

    /**
     * Gets the resource label.
     *
     * @return the resource label
     */
    public String getResourceLabel() {
        return m_resourceLabel;
    }

    /**
     * Gets the storage strategy.
     *
     * @return the storage strategy
     */
    public StorageStrategyDTO getStorageStrategy() {
        return m_storageStrategy;
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     */
    public void setLabel(final String label) {
        m_label = label == null ? null : label.intern();
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(final String name) {
        m_name = name == null ? null : name.intern();
    }

    /**
     * Sets the persistence selector strategy.
     *
     * @param strategy the new persistence selector strategy
     */
    public void setPersistenceSelectorStrategy(final PersistenceSelectorStrategyDTO strategy) {
        m_persistenceSelectorStrategy = strategy;
    }

    /**
     * Sets the resource label.
     *
     * @param resourceLabel the new resource label
     */
    public void setResourceLabel(final String resourceLabel) {
        m_resourceLabel = resourceLabel == null ? null : resourceLabel.intern();
    }

    /**
     * Sets the storage strategy.
     *
     * @param strategy the new storage strategy
     */
    public void setStorageStrategy(final StorageStrategyDTO strategy) {
        m_storageStrategy = strategy;
    }

}
