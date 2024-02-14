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
package org.opennms.netmgt.config.wmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wmi-datacollection}attrib" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="wmiClass" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="keyvalue" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="recheckInterval" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="ifType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="resourceType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="wmiNamespace" type="{http://www.w3.org/2001/XMLSchema}string" default="root/cimv2" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "", propOrder = {
        "m_attribs"
})
@XmlRootElement(name = "wpm")
@ValidateUsing("wmi-datacollection.xsd")
public class Wpm implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "attrib")
    protected List<Attrib> m_attribs = new ArrayList<>();

    @XmlAttribute(name = "name", required = true)
    protected String m_name;

    @XmlAttribute(name = "wmiClass", required = true)
    protected String m_wmiClass;

    @XmlAttribute(name = "keyvalue", required = true)
    protected String m_keyvalue;

    @XmlAttribute(name = "recheckInterval", required = true)
    protected Integer m_recheckInterval;

    @XmlAttribute(name = "ifType", required = true)
    protected String m_ifType;

    @XmlAttribute(name = "resourceType", required = true)
    protected String m_resourceType;

    @XmlAttribute(name = "wmiNamespace")
    protected String m_wmiNamespace;

    public List<Attrib> getAttribs() {
        return m_attribs;
    }

    public void setAttribs(final List<Attrib> attribs) {
        if (attribs == m_attribs) return;
        m_attribs.clear();
        if (attribs != null) m_attribs.addAll(attribs);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getWmiClass() {
        return m_wmiClass;
    }

    public void setWmiClass(final String wmiClass) {
        m_wmiClass = ConfigUtils.assertNotEmpty(wmiClass, "wmiClass");
    }

    public String getKeyvalue() {
        return m_keyvalue;
    }

    public void setKeyvalue(final String keyvalue) {
        m_keyvalue = ConfigUtils.assertNotEmpty(keyvalue, "keyvalue");
    }

    public Integer getRecheckInterval() {
        return m_recheckInterval;
    }

    public void setRecheckInterval(final Integer recheckInterval) {
        m_recheckInterval = ConfigUtils.assertNotNull(recheckInterval, "recheckInterval");
    }

    public String getIfType() {
        return m_ifType;
    }

    public void setIfType(final String ifType) {
        m_ifType = ConfigUtils.assertNotEmpty(ifType, "ifType");
    }

    public String getResourceType() {
        return m_resourceType;
    }

    public void setResourceType(final String resourceType) {
        m_resourceType = ConfigUtils.assertNotEmpty(resourceType, "resourceType");
    }

    public String getWmiNamespace() {
        return m_wmiNamespace == null? "root/cimv2" : m_wmiNamespace;
    }

    public void setWmiNamespace(final String wmiNamespace) {
        m_wmiNamespace = ConfigUtils.normalizeString(wmiNamespace);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Wpm)) {
            return false;
        }
        final Wpm that = (Wpm) obj;
        return Objects.equals(this.m_attribs, that.m_attribs) &&
                Objects.equals(this.m_name, that.m_name) &&
                Objects.equals(this.m_wmiClass, that.m_wmiClass) &&
                Objects.equals(this.m_keyvalue, that.m_keyvalue) &&
                Objects.equals(this.m_recheckInterval, that.m_recheckInterval) &&
                Objects.equals(this.m_ifType, that.m_ifType) &&
                Objects.equals(this.m_resourceType, that.m_resourceType) &&
                Objects.equals(this.m_wmiNamespace, that.m_wmiNamespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_attribs,
                            m_name,
                            m_wmiClass,
                            m_keyvalue,
                            m_recheckInterval,
                            m_ifType,
                            m_resourceType,
                            m_wmiNamespace);
    }

}
