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
package org.opennms.netmgt.config.httpdatacollection;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/http-datacollection}url"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/http-datacollection}attributes" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "m_url",
        "m_attributes"
})
@XmlRootElement(name = "uri")
@ValidateUsing("http-datacollection-config.xsd")
public class Uri implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "url", required = true)
    protected Url m_url;
    @XmlElementWrapper(name="attributes")
    @XmlElement(name="attrib")
    protected List<Attrib> m_attributes;
    @XmlAttribute(name = "name", required = true)
    protected String m_name;

    public Url getUrl() {
        return m_url;
    }

    public void setUrl(final Url value) {
        m_url = ConfigUtils.assertNotNull(value, "url");
    }

    public List<Attrib> getAttributes() {
        return m_attributes == null? Collections.emptyList() : m_attributes;
    }

    public void setAttributes(final List<Attrib> value) {
        m_attributes = value;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String value) {
        m_name = ConfigUtils.assertNotEmpty(value, "name");
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Uri)) {
            return false;
        }
        final Uri that = (Uri) other;
        return Objects.equals(this.m_url, that.m_url)
                && Objects.equals(this.m_attributes, that.m_attributes)
                && Objects.equals(this.m_name, that.m_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_url, m_attributes, m_name);
    }

}
