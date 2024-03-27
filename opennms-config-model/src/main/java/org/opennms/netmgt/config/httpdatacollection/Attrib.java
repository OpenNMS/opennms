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
import org.opennms.netmgt.collection.api.AttributeType;
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
 *       &lt;attribute name="alias" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="match-group" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int"&gt;
 *             &lt;minInclusive value="1"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="type" use="required" type="{http://xmlns.opennms.org/xsd/config/http-datacollection}allowed-types" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "attrib")
@ValidateUsing("http-datacollection-config.xsd")
public class Attrib implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "alias", required = true)
    protected String m_alias;

    @XmlAttribute(name = "match-group", required = true)
    protected int m_matchGroup;

    @XmlAttribute(name = "type", required = true)
    protected AttributeType m_type;

    public String getAlias() {
        return m_alias;
    }

    public void setAlias(final String value) {
        m_alias = ConfigUtils.assertNotEmpty(value, "alias");
    }

    public int getMatchGroup() {
        return m_matchGroup;
    }

    public void setMatchGroup(final int value) {
        m_matchGroup = value;
    }

    public AttributeType getType() {
        return m_type;
    }

    public void setType(final AttributeType value) {
        m_type = ConfigUtils.assertNotNull(value, "type");
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Attrib)) {
            return false;
        }
        final Attrib that = (Attrib) other;
        return Objects.equals(this.m_alias, that.m_alias)
                && Objects.equals(this.m_matchGroup, that.m_matchGroup)
                && Objects.equals(this.m_type, that.m_type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_alias, m_matchGroup, m_type);
    }
}
