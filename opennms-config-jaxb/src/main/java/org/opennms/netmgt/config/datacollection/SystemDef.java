/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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
