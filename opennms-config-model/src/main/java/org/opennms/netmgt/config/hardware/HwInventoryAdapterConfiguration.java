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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The Class HwInventoryAdapterConfiguration.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="hardware-inventory-adapter-configuration")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("snmp-hardware-inventory-adapter-configuration.xsd")
public class HwInventoryAdapterConfiguration implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    List<HwExtension> m_extensions = new ArrayList<>();

    @XmlElement(name="hw-extension")
    public List<HwExtension> getExtensions() {
        return m_extensions;
    }

    public void setExtensions(final List<HwExtension> extensions) {
        if (extensions == m_extensions) return;
        m_extensions.clear();
        if (extensions != null) m_extensions.addAll(extensions);
    }

    public void addExtension(final HwExtension extension) {
        m_extensions.add(extension);
    }

    /**
     * Gets the vendor OID.
     *
     * @param nodeSysOid the node system OID
     * @return the vendor OID
     */
    public List<SnmpObjId> getVendorOid(final String nodeSysOid) {
        final List<SnmpObjId> vendorOidList = new ArrayList<>();
        for (final HwExtension ext : getExtensions()) {
            if (nodeSysOid.startsWith(ext.getSysOidMask())) {
                for (final MibObj obj : ext.getMibObjects()) {
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
        for (final HwExtension ext : getExtensions()) {
            for (final MibObj obj : ext.getMibObjects()) {
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
            return Objects.equals(this.m_extensions, that.m_extensions);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_extensions);
    }
    @Override
    public String toString() {
        return "HwInventoryAdapterConfiguration [extensions=" + m_extensions + "]";
    }

}
