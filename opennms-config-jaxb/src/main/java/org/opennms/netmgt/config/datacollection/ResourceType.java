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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.internal.collection.DatacollectionConfigVisitor;

/**
 * a custom resource type
 */

@XmlRootElement(name="resourceType", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_name", "m_label", "m_resourceLabel", "m_persistenceSelectorStrategy", "m_storageStrategy"})
@ValidateUsing("datacollection-config.xsd")
public class ResourceType implements Serializable, org.opennms.netmgt.collection.api.ResourceType {
    private static final long serialVersionUID = -3663855168780520748L;

    /**
     * resource type name
     */
    @XmlAttribute(name="name", required=true)
    private String m_name;

    /**
     * resource type label (this is what users see in the webUI)
     */
    @XmlAttribute(name="label", required=true)
    private String m_label;

    /**
     * resource label expression (this is what users see in the webUI for each
     * resource of this type)
     */
    @XmlAttribute(name="resourceLabel")
    private String m_resourceLabel;

    /**
     * Selects a PersistenceSelectorStrategy that decides which data is
     * persisted and which is not.
     */
    @XmlElement(name="persistenceSelectorStrategy")
    private PersistenceSelectorStrategy m_persistenceSelectorStrategy;

    /**
     * Selects a StorageStrategy that decides where data is stored.
     */
    @XmlElement(name="storageStrategy")
    private StorageStrategy m_storageStrategy;

    /**
     * resource type name
     */
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name.intern();
    }

    /**
     * resource type label (this is what users see in the webUI)
     */
    public String getLabel() {
        return m_label;
    }

    public void setLabel(final String label) {
        m_label = label.intern();
    }

    /**
     * resource label expression (this is what users see in the webUI for each
     * resource of this type)
     */
    public String getResourceLabel() {
        return m_resourceLabel;
    }

    public void setResourceLabel(final String resourceLabel) {
        m_resourceLabel = resourceLabel.intern();
    }

    /**
     * Selects a PersistenceSelectorStrategy that decides which data is
     * persisted and which is not.
     */
    public PersistenceSelectorStrategy getPersistenceSelectorStrategy() {
        return m_persistenceSelectorStrategy;
    }

    public void setPersistenceSelectorStrategy(final PersistenceSelectorStrategy strategy) {
        m_persistenceSelectorStrategy = strategy;
    }

    /**
     * Selects a StorageStrategy that decides where data is stored.
     */
    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
    }

    public void setStorageStrategy(final StorageStrategy strategy) {
        m_storageStrategy = strategy;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_label == null) ? 0 : m_label.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_persistenceSelectorStrategy == null) ? 0 : m_persistenceSelectorStrategy.hashCode());
        result = prime * result + ((m_resourceLabel == null) ? 0 : m_resourceLabel.hashCode());
        result = prime * result + ((m_storageStrategy == null) ? 0 : m_storageStrategy.hashCode());
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
        if (!(obj instanceof ResourceType)) {
            return false;
        }
        final ResourceType other = (ResourceType) obj;
        if (m_label == null) {
            if (other.m_label != null) {
                return false;
            }
        } else if (!m_label.equals(other.m_label)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_persistenceSelectorStrategy == null) {
            if (other.m_persistenceSelectorStrategy != null) {
                return false;
            }
        } else if (!m_persistenceSelectorStrategy.equals(other.m_persistenceSelectorStrategy)) {
            return false;
        }
        if (m_resourceLabel == null) {
            if (other.m_resourceLabel != null) {
                return false;
            }
        } else if (!m_resourceLabel.equals(other.m_resourceLabel)) {
            return false;
        }
        if (m_storageStrategy == null) {
            if (other.m_storageStrategy != null) {
                return false;
            }
        } else if (!m_storageStrategy.equals(other.m_storageStrategy)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ResourceType [name=" + m_name + ", label=" + m_label + ", resourceLabel=" + m_resourceLabel + ", persistenceSelectorStrategy=" + m_persistenceSelectorStrategy
                + ", storageStrategy=" + m_storageStrategy + "]";
    }

    public void visit(final DatacollectionConfigVisitor visitor) {
        visitor.visitResourceType(this);
        visitor.visitResourceTypeComplete();
    }

}
