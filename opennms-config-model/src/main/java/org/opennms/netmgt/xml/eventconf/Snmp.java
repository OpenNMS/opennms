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
package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The SNMP information from the trap
 */
@XmlRootElement(name="snmp")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_id", "m_idText", "m_version", "m_specific", "m_generic", "m_community", "m_trapOID"})
public class Snmp implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The SNMP enterprise ID
     */
    // @NotNull
    @XmlElement(name="id", required=true)
    private String m_id;

    @XmlElement(name="trapoid")
    private String m_trapOID;

    /**
     * The SNMP enterprise ID text
     */
    @XmlElement(name="idtext", required=false)
    private String m_idText;

    /**
     * The SNMP version
     */
    // @NotNull
    @XmlElement(name="version", required=true)
    private String m_version;

    /**
     * The specific trap number
     */
    @XmlElement(name="specific", required=false)
    private Integer m_specific;

    /**
     * The generic trap number
     */
    @XmlElement(name="generic", required=false)
    private Integer m_generic;

    /**
     * The community name
     */
    @XmlElement(name="community", required=false)
    private String m_community;

    /** The SNMP enterprise ID */
    public String getId() {
        return m_id;
    }

    public void setId(final String id) {
        m_id = ConfigUtils.assertNotEmpty(id, "id");
    }

    public String getTrapOID() {
        return m_trapOID;
    }

    public void setTrapOID(String m_trapOID) {
        this.m_trapOID = m_trapOID;
    }

    /** The SNMP enterprise ID text */
    public String getIdtext() {
        return m_idText;
    }

    public void setIdtext(final String idText) {
        m_idText = ConfigUtils.normalizeString(idText);
    }

    /** The SNMP version */
    public String getVersion() {
        return m_version;
    }

    public void setVersion(final String version) {
        m_version = ConfigUtils.assertNotEmpty(version, "version");
    }

    /** The specific trap number */
    public Integer getSpecific() {
        return m_specific;
    }

    public void setSpecific(final Integer specific) {
        m_specific = specific;
    }

    /** The generic trap number. */
    public Integer getGeneric() {
        return m_generic;
    }

    public void setGeneric(final Integer generic) {
        m_generic = generic;
    }

    public String getCommunity() {
        return m_community;
    }

    public void setCommunity(final String community) {
        m_community = ConfigUtils.normalizeString(community);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_id, m_idText, m_version, m_specific, m_generic, m_community, m_trapOID);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Snmp) {
            final Snmp that = (Snmp) obj;
            return Objects.equals(this.m_id, that.m_id) &&
                    Objects.equals(this.m_idText, that.m_idText) &&
                    Objects.equals(this.m_version, that.m_version) &&
                    Objects.equals(this.m_specific, that.m_specific) &&
                    Objects.equals(this.m_generic, that.m_generic) &&
                    Objects.equals(this.m_community, that.m_community) &&
                    Objects.equals(this.m_trapOID, that.m_trapOID);
        }
        return false;
    }

}
