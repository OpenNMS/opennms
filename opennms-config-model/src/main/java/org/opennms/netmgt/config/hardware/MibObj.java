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
package org.opennms.netmgt.config.hardware;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.OptionalStringAdapter;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The Class MibObj.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="MibObj")
@XmlType(propOrder={"oid", "type", "alias", "replace"})
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("snmp-hardware-inventory-adapter-configuration.xsd")
public class MibObj implements Serializable {
    private static final long serialVersionUID = 2L;

    /** The OID. */
    private SnmpObjId m_oid;

    /** The type or attribute class (defaults to 'string'). */
    private String m_type;

    private String m_alias;

    private String m_replace;

    public MibObj() {}

    public MibObj(final SnmpObjId oid, final String type, final String alias) {
        setOid(oid);
        setType(type);
        setAlias(alias);
    }

    public MibObj(final SnmpObjId oid, final String type, final String alias, final String replace) {
        this(oid, type, alias);
        setReplace(replace);
    }

    /**
     * Gets the SNMP object ID.
     *
     * @return the OID
     */
    @XmlAttribute(name="oid", required=true)
    @XmlJavaTypeAdapter(SnmpObjIdAdapter.class)
    public SnmpObjId getOid() {
        return m_oid;
    }

    /**
     * Sets the SNMP object ID.
     *
     * @param m_oid the SNMP object ID
     */
    public void setOid(final SnmpObjId oid) {
        m_oid = ConfigUtils.assertNotNull(oid, "OID");
    }

    @XmlAttribute(name="type", required=false)
    public String getType() {
        return m_type == null? "string" : m_type;
    }

    public void setType(final String type) {
        m_type = ConfigUtils.normalizeString(type);
    }

    @XmlAttribute(name="alias", required=true)
    public String getAlias() {
        return m_alias;
    }

    public void setAlias(final String alias) {
        m_alias = ConfigUtils.assertNotEmpty(alias, "alias");
    }

    @XmlAttribute(name="replace", required=false)
    @XmlJavaTypeAdapter(OptionalStringAdapter.class)
    public Optional<String> getReplace() {
        return Optional.ofNullable(m_replace);
    }

    public void setReplace(final Optional<String> replace) {
        setReplace(replace.orElse(null));
    }

    /**
     * <p>Must be a valid attribute of <code>org.opennms.netmgt.model.OnmsHwEntity</code>.
     * Otherwise, an IllegalArgumentException will be thrown.</p>
     */
    public void setReplace(final String replace) {
        if (replace != null && !replace.startsWith("entPhysical")) {
            throw new IllegalArgumentException("Invalid replace field " + replace);
        }
        m_replace = ConfigUtils.normalizeString(replace);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof MibObj) {
            final MibObj that = (MibObj)obj;
            return Objects.equals(this.m_oid, that.m_oid) &&
                    Objects.equals(this.m_type, that.m_type) &&
                    Objects.equals(this.m_alias, that.m_alias) &&
                    Objects.equals(this.m_replace, that.m_replace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_oid, m_type, m_alias, m_replace);
    }

    @Override
    public String toString() {
        return "MibObj [oid=" + m_oid + ", type=" + m_type + ", alias=" + m_alias + ", replace=" + m_replace + "]";
    }

}
