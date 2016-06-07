/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.core.xml.ValidateUsing;

/**
 * The Class MibObjProperty.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="property", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("datacollection-config.xsd")
public class MibObjProperty {

    /** The instance. */
    @XmlAttribute(name="instance", required=true)
    private String instance;

    /** The source resource type. */
    @XmlAttribute(name="source-type", required=true)
    private String sourceResourceType;

    /** The source alias. */
    @XmlAttribute(name="source-alias", required=true)
    private String sourceAlias;

    /** The name. */
    @XmlAttribute(name="name", required=false)
    private String name; // Optional

    /** The index pattern. */
    @XmlAttribute(name="index-pattern", required=false)
    private String indexPattern; // Optional

    /** The class name. */
    @XmlAttribute(name="class-name", required=false)
    private String className; // Optional

    /** The group name. */
    @XmlTransient
    private String groupName;

    /**
     * Instantiates a new MibObj property.
     */
    public MibObjProperty() {}

    /**
     * Gets the single instance of MibObjProperty.
     *
     * @return single instance of MibObjProperty
     */
    public String getInstance() {
        return instance;
    }

    /**
     * Gets the source resource type.
     *
     * @return the source resource type
     */
    public String getSourceResourceType() {
        return sourceResourceType;
    }

    /**
     * Gets the source alias.
     *
     * @return the source alias
     */
    public String getSourceAlias() {
        return sourceAlias;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        if (name == null) {
            name = sourceAlias;
        }
        return name;
    }

    /**
     * Gets the index pattern.
     *
     * @return the index pattern
     */
    public String getIndexPattern() {
        return indexPattern;
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the group name.
     *
     * @return the group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the instance.
     *
     * @param instance the new instance
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }

    /**
     * Sets the source resource type.
     *
     * @param sourceResourceType the new source resource type
     */
    public void setSourceResourceType(String sourceResourceType) {
        this.sourceResourceType = sourceResourceType;
    }

    /**
     * Sets the source alias.
     *
     * @param sourceAlias the new source alias
     */
    public void setSourceAlias(String sourceAlias) {
        this.sourceAlias = sourceAlias;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the index pattern.
     *
     * @param indexPattern the new index pattern
     */
    public void setIndexPattern(String indexPattern) {
        this.indexPattern = indexPattern;
    }

    /**
     * Sets the class name.
     *
     * @param className the new class name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Sets the group name.
     *
     * @param groupName the new group name
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MibObjProperty [group=" + groupName + ", instance=" + instance
                + ", sourceResourceType=" + sourceResourceType
                + ", sourceAlias=" + sourceAlias + ", name=" + name
                + ", indexPattern=" + indexPattern + ", className="
                + className + "]";
    }

}
