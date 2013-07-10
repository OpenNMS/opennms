/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * The Class XmlGroup.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="xml-group")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGroup implements Serializable, Comparable<XmlGroup> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2716588565159391498L;

    /** The Constant OF_XML_OBJECTS. */
    @XmlTransient
    private static final XmlObject[] OF_XML_OBJECTS = new XmlObject[0];

    /** The group name. */
    @XmlAttribute(name="name", required=true)
    private String m_name;

    /** The resource type. */
    @XmlAttribute(name="resource-type", required=true)
    private String m_resourceType;

    /** The resource XPath. */
    @XmlAttribute(name="resource-xpath", required=true)
    private String m_resourceXpath;

    /** The key XPath (for resource instance). */
    @XmlAttribute(name="key-xpath", required=false)
    private String m_keyXpath;

    /** The Resource Time XPath (for RRD updates). */
    @XmlAttribute(name="timestamp-xpath", required=false)
    private String m_timestampXpath;

    /** The Resource Time Format (for RRD updates). */
    @XmlAttribute(name="timestamp-format", required=false)
    private String m_timestampFormat;

    /** The XML objects list. */
    @XmlElement(name="xml-object", required=true)
    private List<XmlObject> m_xmlObjects = new ArrayList<XmlObject>();
    
    /** The m_xml resource key. */
    @XmlElement(name="resource-key", required=false)
    private XmlResourceKey m_xmlResourceKey;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Gets the XML objects.
     *
     * @return the XML objects
     */
    public List<XmlObject> getXmlObjects() {
        return m_xmlObjects;
    }

    /**
     * Sets the XML objects.
     *
     * @param xmlObjects the new XML objects
     */
    public void setXmlObjects(List<XmlObject> xmlObjects) {
        m_xmlObjects = xmlObjects;
    }

    /**
     * Adds a new XML object.
     *
     * @param xmlObject the XML object
     */
    public void addXmlObject(XmlObject xmlObject) {
        m_xmlObjects.add(xmlObject);
    }

    /**
     * Removes a XML object.
     *
     * @param xmlObject the XML object
     */
    public void removeXmlObject(XmlObject xmlObject) {
        m_xmlObjects.remove(xmlObject);
    }

    /**
     * Removes a XML object by name.
     *
     * @param name the XML object name
     */
    public void removeObjectByName(String name) {
        for (Iterator<XmlObject> itr = m_xmlObjects.iterator(); itr.hasNext(); ) {
            XmlObject column = itr.next();
            if(column.getName().equals(name)) {
                m_xmlObjects.remove(column);
                return;
            }
        }
    }

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    public String getResourceType() {
        return m_resourceType;
    }

    /**
     * Sets the resource type.
     *
     * @param resourceType the new resource type
     */
    public void setResourceType(String resourceType) {
        m_resourceType = resourceType;
    }

    /**
     * Gets the resource XPath.
     *
     * @return the resource XPath
     */
    public String getResourceXpath() {
        return m_resourceXpath;
    }

    /**
     * Sets the resource XPath.
     *
     * @param resourceXpath the new resource XPath
     */
    public void setResourceXpath(String resourceXpath) {
        this.m_resourceXpath = resourceXpath;
    }

    /**
     * Gets the key XPath (for resource instance).
     * 
     * @return the key XPath
     */
    public String getKeyXpath() {
        return m_keyXpath;
    }

    /**
     * Sets the key XPath.
     *
     * @param keyXpath the new key XPath
     */
    public void setKeyXpath(String keyXpath) {
        this.m_keyXpath = keyXpath;
    }

    /**
     * Gets the timestamp XPath.
     *
     * @return the timestamp XPath
     */
    public String getTimestampXpath() {
        return m_timestampXpath;
    }

    /**
     * Sets the timestamp XPath.
     *
     * @param timestampXpath the new timestamp XPath
     */
    public void setTimestampXpath(String timestampXpath) {
        this.m_timestampXpath = timestampXpath;
    }

    /**
     * Gets the timestamp format.
     *
     * @return the timestamp format
     */
    public String getTimestampFormat() {
        return m_timestampFormat;
    }

    /**
     * Sets the timestamp format.
     *
     * @param timestampFormat the new timestamp format
     */
    public void setTimestampFormat(String timestampFormat) {
        this.m_timestampFormat = timestampFormat;
    }

    /**
     * Gets the if type.
     *
     * @return the if type
     */
    public String getIfType() {
        return m_resourceType.equals("node") ? "ignore" : "all";
    }

    /**
     * Gets the XML resource key.
     *
     * @return the XML resource key
     */
    public XmlResourceKey getXmlResourceKey() {
        return m_xmlResourceKey;
    }

    /**
     * Sets the XML resource key.
     *
     * @param xmlResourceKey the new XML resource key
     */
    public void setXmlResourceKey(XmlResourceKey xmlResourceKey) {
        this.m_xmlResourceKey = xmlResourceKey;
    }
    
    /**
     * Checks for multiple resource key.
     *
     * @return true, if successful
     */
    public boolean hasMultipleResourceKey() {
        return m_xmlResourceKey != null && !m_xmlResourceKey.getKeyXpathList().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(XmlGroup obj) {
        return new CompareToBuilder()
        .append(getName(), obj.getName())
        .append(getResourceType(), obj.getResourceType())
        .append(getResourceXpath(), obj.getResourceXpath())
        .append(getKeyXpath(), obj.getKeyXpath())
        .append(getXmlObjects().toArray(OF_XML_OBJECTS), obj.getXmlObjects().toArray(OF_XML_OBJECTS))
        .toComparison();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XmlGroup) {
            XmlGroup other = (XmlGroup) obj;
            return new EqualsBuilder()
            .append(getName(), other.getName())
            .append(getResourceType(), other.getResourceType())
            .append(getResourceXpath(), other.getResourceXpath())
            .append(getKeyXpath(), other.getKeyXpath())
            .append(getXmlObjects().toArray(OF_XML_OBJECTS), other.getXmlObjects().toArray(OF_XML_OBJECTS))
            .isEquals();
        }
        return false;
    }
}
