/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * The Class XmlSource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlSource implements Serializable, Comparable<XmlSource> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -9220561601381984080L;

    /** The Constant OF_XML_GROUPS. */
    private static final XmlGroup[] OF_XML_GROUPS = new XmlGroup[0];

    /** The source URL. */
    @XmlAttribute(name="url", required=true)
    private String m_url;

    /** The user name for authentication. */
    @XmlAttribute(name="user-name")
    private String m_userName;

    /** The password for authentication. */
    @XmlAttribute(name="password")
    private String m_password;

    /** The XML groups list. */
    @XmlElement(name="xml-group")
    private List<XmlGroup> m_xmlGroups = new ArrayList<XmlGroup>();

    /**
     * Instantiates a new XML source.
     */
    public XmlSource() {
    }

    /**
     * Gets the URL.
     *
     * @return the URL
     */
    @XmlTransient
    public String getUrl() {
        return m_url;
    }

    /**
     * Sets the URL.
     *
     * @param url the new URL
     */
    public void setUrl(String url) {
        m_url = url;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    @XmlTransient
    public String getUserName() {
        return m_userName;
    }

    /**
     * Sets the user name.
     *
     * @param userName the new user name
     */
    public void setUserName(String userName) {
        this.m_userName = userName;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    @XmlTransient
    public String getPassword() {
        return m_password;
    }

    /**
     * Sets the password.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.m_password = password;
    }

    /**
     * Gets the XML groups.
     *
     * @return the XML groups
     */
    @XmlTransient
    public List<XmlGroup> getXmlGroups() {
        return m_xmlGroups;
    }

    /**
     * Sets the XML groups.
     *
     * @param xmlGroups the new XML groups
     */
    public void setXmlGroups(List<XmlGroup> xmlGroups) {
        m_xmlGroups = xmlGroups;
    }

    /**
     * Adds the XML group.
     *
     * @param group the group
     */
    public void addXmlGroup(XmlGroup group) {
        m_xmlGroups.add(group);
    }

    /**
     * Removes the XML group.
     *
     * @param group the group
     */
    public void removeXmlGroup(XmlGroup group) {
        m_xmlGroups.remove(group);
    }

    /**
     * Removes the group by name.
     *
     * @param name the name
     */
    public void removeGroupByName(String name) {
        for (Iterator<XmlGroup> itr = m_xmlGroups.iterator(); itr.hasNext();) {
            XmlGroup query = itr.next();
            if(query.getName().equals(name)) {
                m_xmlGroups.remove(query);
                return;
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(XmlSource obj) {
        return new CompareToBuilder()
        .append(getUrl(), obj.getUrl())
        .append(getXmlGroups().toArray(OF_XML_GROUPS), obj.getXmlGroups().toArray(OF_XML_GROUPS))
        .toComparison();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XmlSource) {
            XmlSource other = (XmlSource) obj;
            return new EqualsBuilder()
            .append(getUrl(), other.getUrl())
            .append(getXmlGroups().toArray(OF_XML_GROUPS), other.getXmlGroups().toArray(OF_XML_GROUPS))
            .isEquals();
        }
        return false;
    }

}
