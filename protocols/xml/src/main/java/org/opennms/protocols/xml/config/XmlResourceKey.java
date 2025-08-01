/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
public class XmlResourceKey implements Serializable, Comparable<XmlResourceKey>, Cloneable {


    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2666286031765770432L;

    /** The key-path list. */
    @XmlElement(name="key-xpath", required=true)
    private List<String> m_keyXpathList = new ArrayList<>();

    /**
     * Instantiates a new XML object.
     */
    public XmlResourceKey() { }

    public XmlResourceKey(XmlResourceKey copy) {
        m_keyXpathList.addAll(copy.m_keyXpathList);
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

    @Override
    public XmlResourceKey clone() {
        return new XmlResourceKey(this);
    }
}
