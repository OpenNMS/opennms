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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The Class HwInventoryAdapterConfiguration.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="hardware-inventory-adapter-configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class HwInventoryAdapterConfiguration implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The extensions. */
    List<HwExtension> extensions = new ArrayList<HwExtension>();

    /**
     * Gets the extensions.
     *
     * @return the extensions
     */
    @XmlElement(name="hw-extension")
    public List<HwExtension> getExtensions() {
        return extensions;
    }

    /**
     * Sets the extensions.
     *
     * @param extensions the extensions
     */
    public void setExtensions(List<HwExtension> extensions) {
        this.extensions = extensions == null? new ArrayList<>() : extensions;
    }

    /**
     * Adds the extension.
     *
     * @param extension the extension
     */
    public void addExtension(HwExtension extension) {
        extensions.add(extension);
    }

    /**
     * Gets the vendor OID.
     *
     * @param nodeSysOid the node system OID
     * @return the vendor OID
     */
    public List<SnmpObjId> getVendorOid(String nodeSysOid) {
        final List<SnmpObjId> vendorOidList = new ArrayList<SnmpObjId>();
        for (HwExtension ext : getExtensions()) {
            if (nodeSysOid.startsWith(ext.getSysOidMask())) {
                for (MibObj obj : ext.getMibObjects()) {
                    vendorOidList.add(obj.getOid());
                }
            }
        }
        return vendorOidList;
    }

    /**
     * Gets the replacement map.
     *
     * @return the replacement map
     */
    public Map<String,String> getReplacementMap() {
        final Map<String,String> replacementMap = new HashMap<String,String>();
        for (HwExtension ext : getExtensions()) {
            for (MibObj obj : ext.getMibObjects()) {
                if (obj.getReplace().isPresent() && !obj.getReplace().get().trim().isEmpty()) {
                    replacementMap.put(obj.getAlias(), obj.getReplace().get());
                }
            }
        }
        return replacementMap;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj instanceof HwInventoryAdapterConfiguration) {
            final HwInventoryAdapterConfiguration that = (HwInventoryAdapterConfiguration)obj;
            return Objects.equals(this.extensions, that.extensions);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(extensions);
    }
    @Override
    public String toString() {
        return "HwInventoryAdapterConfiguration [extensions=" + extensions + "]";
    }

}
