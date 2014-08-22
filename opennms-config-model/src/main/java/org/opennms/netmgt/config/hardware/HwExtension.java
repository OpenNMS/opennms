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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class HwExtension.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="hw-extension")
@XmlAccessorType(XmlAccessType.NONE)
public class HwExtension {

    /** The name. */
    private String name;

    /** The system OID mask. */
    private String sysOidMask;

    /** The MIB objects. */
    private List<MibObj> mibObjects = new ArrayList<MibObj>();

    /**
     * Gets the name.
     *
     * @return the name
     */
    @XmlAttribute(name="name", required=true)
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the system OID mask.
     *
     * @return the system OID mask
     */
    @XmlAttribute(name="sysOidMask", required=true)
    public String getSysOidMask() {
        return sysOidMask;
    }

    /**
     * Sets the system OID mask.
     *
     * @param sysOidMask the system OID mask
     */
    public void setSysOidMask(String sysOidMask) {
        this.sysOidMask = sysOidMask;
    }

    /**
     * Gets the MIB objects.
     *
     * @return the MIB objects
     */
    @XmlElement(name="mibObj", required=true)
    public List<MibObj> getMibObjects() {
        return mibObjects;
    }

    /**
     * Sets the MIB objects.
     *
     * @param mibObjects the MIB objects
     */
    public void setMibObjects(List<MibObj> mibObjects) {
        this.mibObjects = mibObjects;
    }

    /**
     * Adds the MIB object.
     *
     * @param mibObj the MIB object
     */
    public void addMibObject(MibObj mibObj) {
        mibObjects.add(mibObj);
    }
}
