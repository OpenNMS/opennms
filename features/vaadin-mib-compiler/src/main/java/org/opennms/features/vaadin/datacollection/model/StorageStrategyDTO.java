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
 * Selects a StorageStrategy that decides where data is stored.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class StorageStrategyDTO implements Serializable {

    /**
     * Java class name of the class that implements the StorageStrategy.
     */
    private String m_clazz;

    /** list of parameters to pass to the strategy for strategy-specific configuration information. */
    private List<ParameterDTO> m_parameters;

    /**
     * Instantiates a new Storage Strategy DTO.
     */
    public StorageStrategyDTO() {
        super();
        m_parameters = new ArrayList<ParameterDTO>();
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    public String getClazz() {
        return m_clazz;
    }

    /**
     * Gets the parameter collection.
     *
     * @return the parameter collection
     */
    public List<ParameterDTO> getParameterCollection() {
        return m_parameters;
    }

    /**
     * Sets the class name.
     *
     * @param clazz the new class name
     */
    public void setClazz(final String clazz) {
        m_clazz = clazz == null ? null : clazz.intern();
    }

    /**
     * Sets the parameter collection.
     *
     * @param parameters the new parameter collection
     */
    public void setParameterCollection(final List<ParameterDTO> parameters) {
        m_parameters = parameters;
    }

}
