/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
