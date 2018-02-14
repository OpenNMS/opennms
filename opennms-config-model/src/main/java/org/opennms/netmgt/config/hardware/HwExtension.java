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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The Class HwExtension.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="hw-extension")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("snmp-hardware-inventory-adapter-configuration.xsd")
public class HwExtension implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private String m_name;

    /** The system OID mask. */
    private String m_sysOidMask;

    /** The MIB objects. */
    private List<MibObj> m_mibObjects = new ArrayList<>();

    public HwExtension() {}

    public HwExtension(final String name, final String sysOidMask) {
        setName(name);
        setSysOidMask(sysOidMask);
    }

    @XmlAttribute(name="name", required=true)
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    /**
     * Gets the system OID mask.
     *
     * @return the system OID mask
     */
    @XmlAttribute(name="sysOidMask", required=true)
    public String getSysOidMask() {
        return m_sysOidMask;
    }

    /**
     * Sets the system OID mask.
     *
     * @param m_sysOidMask the system OID mask
     */
    public void setSysOidMask(String sysOidMask) {
        m_sysOidMask = ConfigUtils.assertNotEmpty(sysOidMask, "sysOidMask");
    }

    /**
     * Gets the MIB objects.
     *
     * @return the MIB objects
     */
    @XmlElement(name="mibObj", required=true)
    public List<MibObj> getMibObjects() {
        return m_mibObjects;
    }

    /**
     * Gets the MIB object by alias.
     *
     * @param m_name the m_name
     * @return the MIB object by alias
     */
    public MibObj getMibObjectByAlias(final String name) {
        return m_mibObjects.stream().filter(obj -> {
            return name.equals(obj.getAlias());
        }).findFirst().orElse(null);
    }

    /**
     * Gets the MIB object by OID.
     *
     * @param oid the OID
     * @return the MIB object by OID
     */
    public MibObj getMibObjectByOid(final String oid) {
        return m_mibObjects.stream().filter(obj -> {
            return oid.equals(obj.getOid().toString());
        }).findFirst().orElse(null);
    }

    /**
     * Sets the MIB objects.
     *
     * @param m_mibObjects the MIB objects
     */
    public void setMibObjects(final List<MibObj> mibObjects) {
        if (mibObjects == m_mibObjects) return;
        m_mibObjects.clear();
        if (mibObjects != null) m_mibObjects.addAll(mibObjects);
    }

    /**
     * Adds the MIB object.
     *
     * @param mibObj the MIB object
     */
    public void addMibObject(final MibObj mibObj) {
        m_mibObjects.add(mibObj);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof HwExtension) {
            final HwExtension that = (HwExtension)obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_sysOidMask, that.m_sysOidMask) &&
                    Objects.equals(this.m_mibObjects, that.m_mibObjects);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_sysOidMask, m_mibObjects);
    }

    @Override
    public String toString() {
        return "HwExtension [m_name=" + m_name + ", m_sysOidMask=" + m_sysOidMask + ", m_mibObjects=" + m_mibObjects + "]";
    }

}
