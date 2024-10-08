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

/**
 * The Class XmlSource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="xml-source")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlSource extends XmlGroups implements Serializable, Comparable<XmlSource>, Cloneable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -9220561601381984080L;

    /** The Constant OF_XML_GROUPS. */
    @XmlTransient
    private static final XmlGroup[] OF_XML_GROUPS = new XmlGroup[0];

    /** Import Groups List. */
    @XmlElement(name="import-groups", required=false)
    private List<String> m_importGroupsList = new ArrayList<>();

    /** The request object. */
    @XmlElement(name="request", required=false)
    private Request m_request;

    /** The source URL. */
    @XmlAttribute(name="url", required=true)
    private String m_url;

    /**
     * Instantiates a new XML source.
     */
    public XmlSource() {
    }

    public XmlSource(XmlSource copy) {
        super(copy);
        m_importGroupsList.addAll(copy.m_importGroupsList);
        m_request = copy.m_request != null ? copy.m_request.clone() : null;
        m_url = copy.m_url;
    }

    /**
     * Gets the URL.
     *
     * @return the URL
     */
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
     * Gets the request.
     *
     * @return the request
     */
    public Request getRequest() {
        return m_request;
    }

    /**
     * Sets the request.
     *
     * @param request the new request
     */
    public void setRequest(Request request) {
        this.m_request = request;
    }

    /**
     * Gets the import groups list.
     *
     * @return the import groups list
     */
    public List<String> getImportGroupsList() {
        return m_importGroupsList;
    }

    /**
     * Sets the import groups list.
     *
     * @param importGroupsList the new import groups list
     */
    public void setImportGroupsList(List<String> importGroupsList) {
        this.m_importGroupsList = importGroupsList;
    }

    /**
     * Checks for import groups.
     *
     * @return true, if successful
     */
    public boolean hasImportGroups() {
        return m_importGroupsList != null && !m_importGroupsList.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
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

    @Override
    public XmlSource clone() {
        return new XmlSource(this);
    }
}
