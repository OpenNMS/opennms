/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.hardware;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The Class MibObj.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MibObj {

    /** The OID. */
    private String oid;

    /** The type. */
    private String type;

    /** The alias. */
    private String alias;

    /**
     * Gets the OID.
     *
     * @return the OID
     */
    @XmlAttribute(name="oid", required=true)
    public String getOid() {
        return oid;
    }

    /**
     * Gets the SNMP OID id.
     *
     * @return the SNMP OID id
     */
    public SnmpObjId getSnmpObjId() {
        return SnmpObjId.get(oid);
    }

    /**
     * Sets the OID.
     *
     * @param oid the OID
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @XmlAttribute(name="type", required=false)
    public String getType() {
        return type;
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

}
