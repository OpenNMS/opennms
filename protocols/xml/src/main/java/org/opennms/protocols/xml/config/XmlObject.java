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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.opennms.netmgt.collection.api.AttributeType;

/**
 * The Class XmlObject.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="xml-object")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlObject implements Serializable, Comparable<XmlObject>, Cloneable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -774378322863486535L;

    /** The object name (or alias). */
    @XmlAttribute(name="name", required=true)
    private String m_name;

    /** The data type. */
    @XmlAttribute(name="type", required=true)
    private AttributeType m_dataType;

    /** The XPath. */
    @XmlAttribute(name="xpath", required=true)
    private String m_xpath;

    @XmlElement(name="xml-mapping")
    private List<XmlMapping> xmlMappings = new ArrayList<>();

    @XmlTransient
    private static final XmlMapping[] OF_XML_MAPPINGS = new XmlMapping[0];

    /**
     * Instantiates a new XML object.
     */
    public XmlObject() { }

    /**
     * Instantiates a new XML object.
     *
     * @param name the object name
     * @param dataType the data type
     */
    public XmlObject(String name, AttributeType dataType) {
        m_name = name;
        m_dataType = dataType;
    }

    public XmlObject(XmlObject copy) {
        m_name = copy.m_name;
        m_dataType = copy.m_dataType;
        m_xpath = copy.m_xpath;
        copy.xmlMappings.stream().forEach(o -> xmlMappings.add(o.clone()));
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
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public AttributeType getDataType() {
        return m_dataType;
    }

    /**
     * Sets the data type.
     *
     * @param dataType the new data type
     */
    public void setDataType(AttributeType dataType) {
        m_dataType = dataType;
    }

    /**
     * Gets the XPath.
     *
     * @return the XPath
     */
    public String getXpath() {
        return m_xpath;
    }

    /**
     * Sets the XPath.
     *
     * @param xpath the new XPath
     */
    public void setXpath(String xpath) {
        m_xpath = xpath;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(XmlObject obj) {
        return new CompareToBuilder()
        .append(getName(), obj.getName())
        .append(getDataType(), obj.getDataType())
        .append(getXpath(), obj.getXpath())
        .append(getXmlMappings().toArray(OF_XML_MAPPINGS), obj.getXmlMappings().toArray(OF_XML_MAPPINGS))
        .toComparison();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XmlObject) {
            XmlObject other = (XmlObject) obj;
            return new EqualsBuilder()
            .append(getName(), other.getName())
            .append(getDataType(), other.getDataType())
            .append(getXpath(), other.getXpath())
            .append(getXmlMappings().toArray(OF_XML_MAPPINGS), other.getXmlMappings().toArray(OF_XML_MAPPINGS))
            .isEquals();
        }
        return false;
    }

    public List<XmlMapping> getXmlMappings() {
        return xmlMappings;
    }

    public void setXmlMappings(final List<XmlMapping> xmlMappings) {
        this.xmlMappings = xmlMappings;
    }


    public String map(final String from) {
        String defaultMapping = from;
        if (xmlMappings != null) {
            for(final XmlMapping xmlMapping : xmlMappings) {
                if (xmlMapping.getFrom() == null) {
                    defaultMapping = xmlMapping.getTo();
                } else {
                    if (xmlMapping.getFrom().equals(from)) {
                        return xmlMapping.getTo();
                    }
                }
            }
        }
        return defaultMapping;
    }

    @Override
    public XmlObject clone() {
        return new XmlObject(this);
    }
}
