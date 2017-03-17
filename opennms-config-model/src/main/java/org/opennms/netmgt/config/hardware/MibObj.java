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
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The Class MibObj.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="MibObj")
@XmlType(propOrder={"oid", "type", "alias", "replace"})
@XmlAccessorType(XmlAccessType.NONE)
public class MibObj implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The OID. */
    private SnmpObjId oid;

    /** The type or attribute class (defaults to 'string'). */
    private String type;

    /** The alias. */
    private String alias;

    /** The replace. */
    private String replace;

    /**
     * The Constructor.
     */
    public MibObj() {}

    /**
     * The Constructor.
     *
     * @param oid the SNMP Object ID
     * @param type the type
     * @param alias the alias
     */
    public MibObj(SnmpObjId oid, String type, String alias) {
        this.oid = oid;
        this.type = type;
        this.alias = alias;
    }

    /**
     * The Constructor.
     *
     * @param oid the SNMP Object ID
     * @param type the type
     * @param alias the alias
     * @param replace the replace
     */
    public MibObj(SnmpObjId oid, String type, String alias, String replace) {
        this(oid, type, alias);
        if (!replace.startsWith("entPhysical")) {
            throw new IllegalArgumentException("Invalid replace field " + replace);
        }
        this.replace = replace;
    }

    /**
     * Gets the SNMP object ID.
     *
     * @return the OID
     */
    @XmlAttribute(name="oid", required=true)
    @XmlJavaTypeAdapter(SnmpObjIdAdapter.class)
    public SnmpObjId getOid() {
        return oid;
    }

    /**
     * Sets the SNMP object ID.
     *
     * @param oid the SNMP object ID
     */
    public void setOid(SnmpObjId oid) {
        this.oid = oid;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @XmlAttribute(name="type", required=false)
    public String getType() {
        return type == null? "string" : type;
    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    @XmlAttribute(name="alias", required=true)
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias.
     *
     * @param alias the alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Gets the replace.
     * 
     * @return the replace
     */
    @XmlAttribute(name="replace", required=false)
    @XmlJavaTypeAdapter(OptionalStringAdapter.class)
    public Optional<String> getReplace() {
        return Optional.ofNullable(replace);
    }

    public void setReplace(final Optional<String> replace) {
        setReplace(replace.orElse(null));
    }

    /**
     * Sets the replace.
     * <p>Most be a valid attribute of <code>org.opennms.netmgt.model.OnmsHwEntity</code>.
     * Otherwise, an IllegalArgumentException will be thrown.</p>
     *
     * @param replace the replace
     */
    public void setReplace(final String replace) {
        if (replace != null && !replace.startsWith("entPhysical")) {
            throw new IllegalArgumentException("Invalid replace field " + replace);
        }
        this.replace = replace;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj instanceof MibObj) {
            final MibObj that = (MibObj)obj;
            return Objects.equals(this.oid, that.oid) &&
                    Objects.equals(this.type, that.type) &&
                    Objects.equals(this.alias, that.alias) &&
                    Objects.equals(this.replace, that.replace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oid, type, alias, replace);
    }

    @Override
    public String toString() {
        return "MibObj [oid=" + oid + ", type=" + type + ", alias=" + alias + ", replace=" + replace + "]";
    }

}
