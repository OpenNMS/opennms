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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.internal.collection.DatacollectionConfigVisitor;

/**
 * a grouping of SNMP related RRD parms, MIB object groups
 *  and sysoid based system definitions.
 */

@XmlRootElement(name="snmp-collection", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class SnmpCollection implements Serializable, Cloneable {
    private static final long serialVersionUID = -6632949516175500912L;

    /**
     * collector name
     */
    @XmlAttribute(name="name", required=true)
    private String m_name;

    @XmlAttribute(name="maxVarsPerPdu")
    private Integer m_maxVarsPerPdu;

    /**
     * indicates if collected SNMP data is to be stored for "all" interfaces
     * or only for the "primary" interface.
     */
    @XmlAttribute(name="snmpStorageFlag", required=true)
    private String m_snmpStorageFlag;

    /**
     * RRD parms
     */
    @XmlElement(name="rrd", required=true)
    private Rrd m_rrd;

    /**
     * Include Collection by specifying
     *  System Definition Name or Data Collection Group Name.
     */
    @XmlElement(name="include-collection")
    private List<IncludeCollection> m_includeCollections = new ArrayList<>();

    /**
     * Custom resource types
     */
    @XmlElement(name="resourceType")
    private List<ResourceType> m_resourceTypes = new ArrayList<>();

    /**
     * MIB object groups
     */
    @XmlElement(name="groups")
    private Groups m_groups;

    /**
     * sysOid-based systems
     */
    @XmlElement(name="systems")
    private Systems m_systems;


    public SnmpCollection() {
        super();
    }

    /**
     * collector name
     */
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name.intern();
    }

    public Integer getMaxVarsPerPdu() {
        return m_maxVarsPerPdu;
    }

    public void setMaxVarsPerPdu(final Integer maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    /**
     * indicates if collected SNMP data is to be stored for "all" interfaces
     * or only for the "primary" interface.
     */
    public String getSnmpStorageFlag() {
        return m_snmpStorageFlag;
    }

    public void setSnmpStorageFlag(final String snmpStorageFlag) {
        m_snmpStorageFlag = snmpStorageFlag.intern();
    }

    /**
     * RRD parms
     */
    public Rrd getRrd() {
        return m_rrd;
    }

    public void setRrd(final Rrd rrd) {
        m_rrd = rrd;
    }

    public List<IncludeCollection> getIncludeCollections() {
        if (m_includeCollections == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_includeCollections);
        }
    }

    public void setIncludeCollections(final List<IncludeCollection> includeCollections) {
        m_includeCollections = new ArrayList<IncludeCollection>(includeCollections);
    }

    public void addIncludeCollection(final IncludeCollection includeCollection) throws IndexOutOfBoundsException {
        m_includeCollections.add(includeCollection);
    }

    public boolean removeIncludeCollection(final IncludeCollection includeCollection) {
        return m_includeCollections.remove(includeCollection);
    }

    public List<ResourceType> getResourceTypes() {
        if (m_resourceTypes == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_resourceTypes);
        }
    }

    public void setResourceTypes(final List<ResourceType> resourceTypes) {
        m_resourceTypes = new ArrayList<ResourceType>(resourceTypes);
    }

    public void addResourceType(final ResourceType resourceType) throws IndexOutOfBoundsException {
        m_resourceTypes.add(resourceType);
    }

    public boolean removeResourceType(final ResourceType resourceType) {
        return m_resourceTypes.remove(resourceType);
    }

    /**
     * MIB object groups
     */
    public Groups getGroups() {
        return m_groups;
    }

    public void setGroups(final Groups groups) {
        m_groups = groups;
    }

    /**
     * sysOid-based systems
     */
    public Systems getSystems() {
        return m_systems;
    }

    public void setSystems(final Systems systems) {
        m_systems = systems;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_groups == null) ? 0 : m_groups.hashCode());
        result = prime * result + ((m_includeCollections == null) ? 0 : m_includeCollections.hashCode());
        result = prime * result + ((m_maxVarsPerPdu == null) ? 0 : m_maxVarsPerPdu.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_resourceTypes == null) ? 0 : m_resourceTypes.hashCode());
        result = prime * result + ((m_rrd == null) ? 0 : m_rrd.hashCode());
        result = prime * result + ((m_snmpStorageFlag == null) ? 0 : m_snmpStorageFlag.hashCode());
        result = prime * result + ((m_systems == null) ? 0 : m_systems.hashCode());
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
        if (!(obj instanceof SnmpCollection)) {
            return false;
        }
        final SnmpCollection other = (SnmpCollection) obj;
        if (m_groups == null) {
            if (other.m_groups != null) {
                return false;
            }
        } else if (!m_groups.equals(other.m_groups)) {
            return false;
        }
        if (m_includeCollections == null) {
            if (other.m_includeCollections != null) {
                return false;
            }
        } else if (!m_includeCollections.equals(other.m_includeCollections)) {
            return false;
        }
        if (m_maxVarsPerPdu == null) {
            if (other.m_maxVarsPerPdu != null) {
                return false;
            }
        } else if (!m_maxVarsPerPdu.equals(other.m_maxVarsPerPdu)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_resourceTypes == null) {
            if (other.m_resourceTypes != null) {
                return false;
            }
        } else if (!m_resourceTypes.equals(other.m_resourceTypes)) {
            return false;
        }
        if (m_rrd == null) {
            if (other.m_rrd != null) {
                return false;
            }
        } else if (!m_rrd.equals(other.m_rrd)) {
            return false;
        }
        if (m_snmpStorageFlag == null) {
            if (other.m_snmpStorageFlag != null) {
                return false;
            }
        } else if (!m_snmpStorageFlag.equals(other.m_snmpStorageFlag)) {
            return false;
        }
        if (m_systems == null) {
            if (other.m_systems != null) {
                return false;
            }
        } else if (!m_systems.equals(other.m_systems)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SnmpCollection [name=" + m_name + ", maxVarsPerPdu=" + m_maxVarsPerPdu + ", snmpStorageFlag=" + m_snmpStorageFlag + ", rrd=" + m_rrd + ", includeCollections="
                + m_includeCollections + ", resourceTypes=" + m_resourceTypes + ", groups=" + m_groups + ", systems=" + m_systems + "]";
    }

    public void visit(final DatacollectionConfigVisitor visitor) {
        visitor.visitSnmpCollection(this);

        if (m_includeCollections != null) {
            for (final IncludeCollection collection : m_includeCollections) {
                collection.visit(visitor);
            }
        }

        if (m_groups != null && m_groups.getGroups() != null) {
            for (final Group group : m_groups.getGroups()) {
                group.visit(visitor);
            }
        }

        if (m_systems != null && m_systems.getSystemDefs() != null) {
            for (final SystemDef def : m_systems.getSystemDefs()) {
                def.visit(visitor);
            }
        }

        if (m_resourceTypes != null) {
            for (final ResourceType type : m_resourceTypes) {
                type.visit(visitor);
            }
        }

        visitor.visitSnmpCollectionComplete();
    }

    @Override
    public SnmpCollection clone() {
        final SnmpCollection newCollection = new SnmpCollection();
        newCollection.setGroups(getGroups());
        newCollection.setIncludeCollections(getIncludeCollections());
        newCollection.setMaxVarsPerPdu(getMaxVarsPerPdu());
        newCollection.setName(getName());
        newCollection.setResourceTypes(getResourceTypes());
        newCollection.setRrd(getRrd());
        newCollection.setSnmpStorageFlag(getSnmpStorageFlag());
        newCollection.setSystems(getSystems());
        return newCollection;
    }
}
