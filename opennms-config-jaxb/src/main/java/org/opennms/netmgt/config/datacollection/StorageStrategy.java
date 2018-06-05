/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.datacollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.collection.api.StrategyDefinition;

/**
 * Selects a StorageStrategy that decides where data is stored.
 */

@XmlRootElement(name="storageStrategy", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
public class StorageStrategy implements StrategyDefinition, Serializable {
    private static final long serialVersionUID = 1260293428590611897L;

    /**
     * Java class name of the class that implements the
     * StorageStrategy.
     */
    @XmlAttribute(name="class", required=true)
    private String m_clazz;

    /**
     * list of parameters to pass to the strategy
     *  for strategy-specific configuration information
     */
    @XmlElement(name="parameter", type=org.opennms.netmgt.config.datacollection.Parameter.class)
    private List<Parameter> m_parameters = new ArrayList<>();

    public StorageStrategy() {
        super();
    }

    public StorageStrategy(final String clazz) {
        if (clazz != null)
            m_clazz = clazz.intern();
    }

    /**
     * Java class name of the class that implements the StorageStrategy.
     */
    public String getClazz() {
        return m_clazz;
    }

    public void setClazz(final String clazz) {
        m_clazz = clazz.intern();
    }

    public List<Parameter> getParameters() {
        if (m_parameters == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_parameters);
        }
    }

    public void setParameters(final List<Parameter> parameters) {
        m_parameters = new ArrayList<Parameter>(parameters);
    }

    public void addParameter(final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_clazz == null) ? 0 : m_clazz.hashCode());
        result = prime * result + ((m_parameters == null) ? 0 : m_parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof StorageStrategy)) {
            return false;
        }
        final StorageStrategy other = (StorageStrategy) obj;
        if (m_clazz == null) {
            if (other.m_clazz != null) {
                return false;
            }
        } else if (!m_clazz.equals(other.m_clazz)) {
            return false;
        }
        if (m_parameters == null) {
            if (other.m_parameters != null) {
                return false;
            }
        } else if (!m_parameters.equals(other.m_parameters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StorageStrategy [class=" + m_clazz + ", parameters=" + m_parameters + "]";
    }

}
