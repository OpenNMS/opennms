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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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

    /** The alias. */
    @XmlAttribute(name="alias", required=false)
    private String alias;

    /** The class name. */
    @XmlAttribute(name="class-name", required=false)
    private String className;

    /** The parameters. */
    @XmlElement(name="parameter", required=false)
    private List<Parameter> parameters;

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
     * Gets the alias.
     *
     * @return the alias
     */
    public String getAlias() {
        return alias;
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
     * Gets the parameters.
     *
     * @return the parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Gets the value of an existing parameter.
     *
     * @param key the key
     * @return the parameter
     */
    public String getParameterValue(String key) {
        return getParameterValue(key, null);
    }

    /**
     * Gets the value of an existing parameter.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the parameter
     */
    public String getParameterValue(String key, String defaultValue) {
        for (Parameter p : parameters) {
            if (p.getKey().equals(key)) {
                return p.getValue();
            }
        }
        return defaultValue;
    }

    /**
     * Adds a new parameter.
     *
     * @param key the key
     * @param value the value
     */
    public void addParameter(String key, String value) {
        parameters.add(new Parameter(key, value));
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
     * Sets the alias.
     *
     * @param alias the new alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
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
     * Sets the parameters.
     *
     * @param parameters the new parameters
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instance == null) ? 0 : instance.hashCode());
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MibObjProperty)) {
            return false;
        }
        final MibObjProperty other = (MibObjProperty) obj;
        if (instance == null) {
            if (other.instance != null) {
                return false;
            }
        } else if (!instance.equals(other.instance)) {
            return false;
        }
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MibObjProperty [instance=" + instance + ", alias=" + alias
                + ", className=" + className + ", parameters=" + parameters
                + ", groupName=" + groupName + "]";
    }

}
