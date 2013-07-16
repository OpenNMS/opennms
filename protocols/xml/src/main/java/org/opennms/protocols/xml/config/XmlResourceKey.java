/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * The Class XmlResourceKey.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="resource-key")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlResourceKey implements Serializable, Comparable<XmlResourceKey> {


    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2666286031765770432L;

    /** The key-path list. */
    @XmlElement(name="key-xpath", required=true)
    private List<String> m_keyXpathList = new ArrayList<String>();

    /**
     * Instantiates a new XML object.
     */
    public XmlResourceKey() {
        super();
    }

    /**
     * Gets the key XPath list.
     *
     * @return the key XPath list
     */
    public List<String> getKeyXpathList() {
        return m_keyXpathList;
    }

    /**
     * Sets the key XPath list.
     *
     * @param keyXpathList the new key XPath list
     */
    public void setKeyXpathList(List<String> keyXpathList) {
        this.m_keyXpathList = keyXpathList;
    }

    /**
     * Adds the key XPath.
     *
     * @param keyXpath the key XPath
     */
    public void addKeyXpath(String keyXpath) {
        m_keyXpathList.add(keyXpath);
    }

    /**
     * Removes the key XPath.
     *
     * @param keyXpath the key XPath
     */
    public void removeKeyXpath(String keyXpath) {
        m_keyXpathList.remove(keyXpath);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(XmlResourceKey obj) {
        return new CompareToBuilder()
        .append(getKeyXpathList(), obj.getKeyXpathList())
        .toComparison();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XmlResourceKey) {
            XmlResourceKey other = (XmlResourceKey) obj;
            return new EqualsBuilder()
            .append(getKeyXpathList(), other.getKeyXpathList())
            .isEquals();
        }
        return false;
    }
}
