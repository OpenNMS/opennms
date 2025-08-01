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
package org.opennms.netmgt.config.datacollection;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.netmgt.config.internal.collection.DatacollectionConfigVisitor;

/**
 * system definition
 */

@XmlRootElement(name="systemDef", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"name", "sysoid", "sysoidMask", "ipList", "collect"})
public class SystemDef implements Serializable {
    private static final long serialVersionUID = 656006979873221835L;

    /*
     * Note that we do not do JAXB field definitions like we usually do, since
     * we have to do some trickery to remain compatible with the XSD which expects
     * a <choice> between sysoid and sysoidmask, and there's basically no way to
     * implement that with JAXB when the types are the same.
     */

    /**
     * Field _name.
     */
    private String m_name;

    /**
     * Field _systemDefChoice.
     */
    private SystemDefChoice m_systemDefChoice = new SystemDefChoice();

    /**
     * list of IP address or IP address mask values to
     *  which this system definition applies.
     */
    private IpList m_ipList;

    /**
     * container for list of MIB groups to be collected
     *  for the system
     */
    private Collect m_collect;


    public SystemDef() {
        super();
    }

    public SystemDef(final String name) {
        super();
        m_name = name;
    }

    @XmlAttribute(name="name", required=true)
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name.intern();
    }

    public SystemDefChoice getSystemDefChoice() {
        return m_systemDefChoice;
    }

    public void setSystemDefChoice(final SystemDefChoice systemDefChoice) {
        m_systemDefChoice = systemDefChoice;
    }

    /* Make compatible with JAXB by proxying SystemDefChoice */
    @XmlElement(name="sysoid")
    public String getSysoid() {
        return m_systemDefChoice == null? null : m_systemDefChoice.getSysoid();
    }
    public void setSysoid(final String sysoid) {
        if (m_systemDefChoice == null) m_systemDefChoice = new SystemDefChoice();
        m_systemDefChoice.setSysoid(sysoid);
        m_systemDefChoice.setSysoidMask(null);
    }
    @XmlElement(name="sysoidMask")
    public String getSysoidMask() {
        return m_systemDefChoice == null? null : m_systemDefChoice.getSysoidMask();
    }
    public void setSysoidMask(final String sysoidMask) {
        if (m_systemDefChoice == null) m_systemDefChoice = new SystemDefChoice();
        m_systemDefChoice.setSysoid(null);
        m_systemDefChoice.setSysoidMask(sysoidMask);
    }
    
    /**
     * list of IP address or IP address mask values to which this system
     * definition applies.
     */
    @XmlElement(name="ipList")
    public IpList getIpList() {
        return m_ipList;
    }

    public void setIpList(final IpList ipList) {
        m_ipList = ipList;
    }

    /**
     * container for list of MIB groups to be collected for the system
     */
    @XmlElement(name="collect")
    public Collect getCollect() {
        return m_collect;
    }

    public void setCollect(final Collect collect) {
        m_collect = collect;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_collect == null) ? 0 : m_collect.hashCode());
        result = prime * result + ((m_ipList == null) ? 0 : m_ipList.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_systemDefChoice == null) ? 0 : m_systemDefChoice.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SystemDef)) {
            return false;
        }
        final SystemDef other = (SystemDef) obj;
        if (m_collect == null) {
            if (other.m_collect != null) {
                return false;
            }
        } else if (!m_collect.equals(other.m_collect)) {
            return false;
        }
        if (m_ipList == null) {
            if (other.m_ipList != null) {
                return false;
            }
        } else if (!m_ipList.equals(other.m_ipList)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_systemDefChoice == null) {
            if (other.m_systemDefChoice != null) {
                return false;
            }
        } else if (!m_systemDefChoice.equals(other.m_systemDefChoice)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SystemDef [name=" + m_name + ", systemDefChoice=" + m_systemDefChoice + ", ipList=" + m_ipList + ", collect=" + m_collect + "]";
    }

    public void visit(final DatacollectionConfigVisitor visitor) {
        visitor.visitSystemDef(this);
        
        if (m_ipList != null) {
            m_ipList.visit(visitor);
        }

        if (m_collect != null) {
            m_collect.visit(visitor);
        }

        visitor.visitSystemDefComplete();
    }
}
