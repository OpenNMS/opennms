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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.internal.collection.DatacollectionConfigVisitor;

/**
 * a MIB object group
 */

@XmlRootElement(name="group", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_name", "m_ifType", "m_mibObjects", "m_mibObjProperties", "m_includeGroups"})
@ValidateUsing("datacollection-config.xsd")
public class Group implements Serializable {
    private static final long serialVersionUID = -4798682461748675616L;

    /**
     * group name
     */
    @XmlAttribute(name="name", required=true)
    private String m_name;

    /**
     * <p>
     * Interface type.
     * </p>
     * <p>Indicates the interface types from which the groups MIB objects are to
     * be collected.</p>
     * <p>
     * Supports individual ifType values or comma-separated list of ifType
     * values in addition to "all" and "ignore" key words.
     * </p>
     * <p>
     * For example:
     * </p>
     * <ul>
     * <li>"6" indicates that OIDs from this MIB group are to be collected
     * only for ethernet interfaces (ifType = 6)</li>
     * <li>"6,22" indicates that OIDs from this MIB group are to be collected
     * only for ethernet and serial interfaces</li>
     * <li>"all" indicates that the OIDs from this MIB group are to be
     * collected for all interfaces regardless of ifType</li>
     * <li>"ignore" indicates that OIDs from this MIB group are node-level
     * objects.</li>
     * </ul>
     * <p>
     * Sample ifType descriptions/values: (Refer to
     * http://www.iana.org/assignments/ianaiftype-mib for a comprehensive
     * list.)
     * </p>
     * <ul>
     * <li>ethernetCsmacd 6</li>
     * <li>iso8825TokenRing 9</li>
     * <li>fddi 15</li>
     * <li>sdlc 17</li>
     * <li>basicISDN 20</li>
     * <li>primaryISDN 21</li>
     * <li>propPointToPointSerial 22</li>
     * <li>ppp 23</li>
     * <li>atm 37</li>
     * <li>sonet 39</li>
     * <li>opticalChannel 195</li>
     * </ul>
     */
    @XmlAttribute(name="ifType", required=true)
    private String m_ifType;

    /**
     * a MIB object
     */
    @XmlElement(name="mibObj")
    private List<MibObj> m_mibObjects = new ArrayList<>();

    /**
     * sub group
     */
    @XmlElement(name="includeGroup")
    private List<String> m_includeGroups = new ArrayList<>();

    @XmlElement(name="property")
    private List<MibObjProperty> m_mibObjProperties = new ArrayList<>();

    public Group() {
        super();
    }

    public Group(final String name) {
        super();
        m_name = name;
    }

    /**
     * group name
     */
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name == null? null : name.intern();
    }

    /**
     * <p>
     * Interface type.
     * </p>
     * <p>Indicates the interface types from which the groups MIB objects are to
     * be collected.</p>
     * <p>
     * Supports individual ifType values or comma-separated list of ifType
     * values in addition to "all" and "ignore" key words.
     * </p>
     * <p>
     * For example:
     * </p>
     * <ul>
     * <li>"6" indicates that OIDs from this MIB group are to be collected
     * only for ethernet interfaces (ifType = 6)</li>
     * <li>"6,22" indicates that OIDs from this MIB group are to be collected
     * only for ethernet and serial interfaces</li>
     * <li>"all" indicates that the OIDs from this MIB group are to be
     * collected for all interfaces regardless of ifType</li>
     * <li>"ignore" indicates that OIDs from this MIB group are node-level
     * objects.</li>
     * </ul>
     * <p>
     * Sample ifType descriptions/values: (Refer to
     * http://www.iana.org/assignments/ianaiftype-mib for a comprehensive
     * list.)
     * </p>
     * <ul>
     * <li>ethernetCsmacd 6</li>
     * <li>iso8825TokenRing 9</li>
     * <li>fddi 15</li>
     * <li>sdlc 17</li>
     * <li>basicISDN 20</li>
     * <li>primaryISDN 21</li>
     * <li>propPointToPointSerial 22</li>
     * <li>ppp 23</li>
     * <li>atm 37</li>
     * <li>sonet 39</li>
     * <li>opticalChannel 195</li>
     * </ul>
     */
    public String getIfType() {
        return m_ifType;
    }

    public void setIfType(final String ifType) {
        m_ifType = ifType == null? null : ifType.intern();
    }

    public List<MibObj> getMibObjs() {
        if (m_mibObjects == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_mibObjects);
        }
    }

    public void setMibObjs(final List<MibObj> mibObjs) {
        m_mibObjects = new ArrayList<MibObj>(mibObjs);
    }

    public void addMibObj(final MibObj mibObj) throws IndexOutOfBoundsException {
        m_mibObjects.add(mibObj);
    }

    public boolean removeMibObj(final MibObj mibObj) {
        return m_mibObjects.remove(mibObj);
    }

    public List<String> getIncludeGroups() {
        if (m_includeGroups == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_includeGroups);
        }
    }

    public void setIncludeGroups(final List<String> includeGroups) {
        m_includeGroups = new ArrayList<String>(includeGroups);
    }

    public void addIncludeGroup(final String includeGroup) throws IndexOutOfBoundsException {
        m_includeGroups.add(includeGroup == null? null : includeGroup.intern());
    }

    public boolean removeIncludeGroup(final String includeGroup) {
        return m_includeGroups.remove(includeGroup);
    }

    public List<MibObjProperty> getProperties() {
        return m_mibObjProperties;
    }

    public void setProperties(List<MibObjProperty> mibObjProperties) {
        this.m_mibObjProperties = mibObjProperties;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_ifType == null) ? 0 : m_ifType.hashCode());
        result = prime * result + ((m_includeGroups == null) ? 0 : m_includeGroups.hashCode());
        result = prime * result + ((m_mibObjects == null) ? 0 : m_mibObjects.hashCode());
        result = prime * result + ((m_mibObjProperties == null) ? 0 : m_mibObjProperties.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
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
        if (!(obj instanceof Group)) {
            return false;
        }
        final Group other = (Group) obj;
        if (m_ifType == null) {
            if (other.m_ifType != null) {
                return false;
            }
        } else if (!m_ifType.equals(other.m_ifType)) {
            return false;
        }
        if (m_includeGroups == null) {
            if (other.m_includeGroups != null) {
                return false;
            }
        } else if (!m_includeGroups.equals(other.m_includeGroups)) {
            return false;
        }
        if (m_mibObjects == null) {
            if (other.m_mibObjects != null) {
                return false;
            }
        } else if (!m_mibObjects.equals(other.m_mibObjects)) {
            return false;
        }
        if (m_mibObjProperties == null) {
            if (other.m_mibObjProperties != null) {
                return false;
            }
        } else if (!m_mibObjProperties.equals(other.m_mibObjProperties)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Group [name=" + m_name + ", ifType=" + m_ifType + ", mibObjects=" + m_mibObjects + ", includeGroups=" + m_includeGroups + ", mibObjProperties=" + m_mibObjProperties + "]";
    }

    public void visit(final DatacollectionConfigVisitor visitor) {
        visitor.visitGroup(this);

        if (m_mibObjects != null) {
            for (final MibObj obj : m_mibObjects) {
                obj.visit(visitor);
            }
        }

        visitor.visitGroupComplete();
    }

}
