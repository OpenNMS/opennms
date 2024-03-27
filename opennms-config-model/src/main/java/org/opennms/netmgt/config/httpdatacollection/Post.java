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
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="parm" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "post")
@ValidateUsing("http-datacollection-config.xsd")
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "parm", required = true)
    protected String m_parm;
    @XmlAttribute(name = "value", required = true)
    protected String m_value;

    public String getParm() {
        return m_parm;
    }

    public void setParm(final String value) {
        m_parm = ConfigUtils.assertNotEmpty(value, "parm");
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = ConfigUtils.assertNotEmpty(value, "value");
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Post)) {
            return false;
        }
        final Post that = (Post) other;
        return Objects.equals(this.m_parm, that.m_parm)
                && Objects.equals(this.m_value, that.m_value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_parm, m_value);
    }

}
