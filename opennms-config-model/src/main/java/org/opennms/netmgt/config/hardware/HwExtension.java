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
