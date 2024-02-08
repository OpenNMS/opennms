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

import java.util.ArrayList;
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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/http-datacollection}rrd"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/http-datacollection}uris"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "m_rrd",
        "m_uris"
})
@XmlRootElement(name = "http-collection")
@ValidateUsing("http-datacollection-config.xsd")
public class HttpCollection {

    @XmlElement(name = "rrd", required = true)
    protected Rrd m_rrd;

    @XmlElementWrapper(name="uris")
    @XmlElement(name="uri", required = true)
    protected List<Uri> m_uris = new ArrayList<>();

    @XmlAttribute(name = "name")
    protected String m_name;

    public Rrd getRrd() {
        return m_rrd;
    }

    public void setRrd(final Rrd value) {
        m_rrd = ConfigUtils.assertNotNull(value, "rrd");
    }

    public List<Uri> getUris() {
        return m_uris;
    }

    public void setUris(final List<Uri> value) {
        if (value == m_uris) return;
        m_uris.clear();
        if (value != null) m_uris.addAll(value);
    }

    public String getName() {
        return m_name == null? "default" : m_name;
    }

    public void setName(final String value) {
        m_name = ConfigUtils.normalizeString(value);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HttpCollection)) {
            return false;
        }
        final HttpCollection that = (HttpCollection) other;
        return Objects.equals(this.m_rrd, that.m_rrd)
                && Objects.equals(this.m_uris, that.m_uris)
                && Objects.equals(this.m_name, that.m_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_rrd, m_uris, m_name);
    }

}
