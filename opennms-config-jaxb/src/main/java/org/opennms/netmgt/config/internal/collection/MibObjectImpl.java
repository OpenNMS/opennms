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
package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.config.api.collection.IGroup;
import org.opennms.netmgt.config.api.collection.IMibObject;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpObjIdXmlAdapter;

/**
 *  &lt;mibObj oid=".1.3.6.1.2.1.10.132.2" instance="0" alias="coffeePotCapacity" type="integer" /&gt;
 *  
 * @author brozow
 *
 */
@XmlRootElement(name="mibObj")
@XmlAccessorType(XmlAccessType.NONE)
public class MibObjectImpl implements IMibObject {
    @XmlAttribute(name="oid")
    @XmlJavaTypeAdapter(SnmpObjIdXmlAdapter.class)
    private SnmpObjId m_oid;

    @XmlAttribute(name="instance")
    private String m_instance;

    @XmlAttribute(name="alias")
    private String m_alias;

    @XmlAttribute(name="type")
    private String m_type;

    @XmlTransient
    private GroupImpl m_group;

    public MibObjectImpl() {
    }

    public MibObjectImpl(final String oid, final String instance, final String alias, final String type) {
        m_oid = SnmpObjId.get(oid);
        m_instance = instance;
        m_alias = alias;
        m_type = type;
    }

    public SnmpObjId getOid() {
        return m_oid;
    }

    public void setOid(SnmpObjId oid) {
        m_oid = oid;
    }

    public void setOid(final String oid) {
        m_oid = SnmpObjId.get(oid);
    }

    public String getAlias() {
        return m_alias;
    }

    public void setAlias(String alias) {
        m_alias = alias;
    }

    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = type;
    }

    public String getInstance() {
        return m_instance;
    }

    public void setInstance(String instance) {
        m_instance = instance;
    }

    public IGroup getGroup() {
        return (IGroup) m_group;
    }

    public void setGroup(final IGroup group) {
        m_group = GroupImpl.asGroup(group);
    }

    public static MibObjectImpl[] asMibObjects(final IMibObject[] mibObjects) {
        if (mibObjects == null) return null;

        final MibObjectImpl[] newMibObjects = new MibObjectImpl[mibObjects.length];
        for (int i=0; i < mibObjects.length; i++) {
            newMibObjects[i] = MibObjectImpl.asMibObject(mibObjects[i]);
        }
        return newMibObjects;
    }

    private static MibObjectImpl asMibObject(final IMibObject obj) {
        if (obj == null) return null;
        final MibObjectImpl mibObject = new MibObjectImpl();
        mibObject.setOid(obj.getOid());
        mibObject.setAlias(obj.getAlias());
        mibObject.setType(obj.getType());
        mibObject.setInstance(obj.getInstance());
        mibObject.setGroup(obj.getGroup());
        return mibObject;
    }

    @Override
    public String toString() {
        return "MibObjectImpl [oid=" + m_oid + ", alias=" + m_alias + ", type=" + m_type + ", instance=" + m_instance + ", group=" + m_group + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_alias == null) ? 0 : m_alias.hashCode());
        result = prime * result + ((m_group == null) ? 0 : m_group.hashCode());
        result = prime * result + ((m_instance == null) ? 0 : m_instance.hashCode());
        result = prime * result + ((m_oid == null) ? 0 : m_oid.hashCode());
        result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
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
        if (!(obj instanceof MibObjectImpl)) {
            return false;
        }
        final MibObjectImpl other = (MibObjectImpl) obj;
        if (m_alias == null) {
            if (other.m_alias != null) {
                return false;
            }
        } else if (!m_alias.equals(other.m_alias)) {
            return false;
        }
        if (m_group == null) {
            if (other.m_group != null) {
                return false;
            }
        } else if (!m_group.equals(other.m_group)) {
            return false;
        }
        if (m_instance == null) {
            if (other.m_instance != null) {
                return false;
            }
        } else if (!m_instance.equals(other.m_instance)) {
            return false;
        }
        if (m_oid == null) {
            if (other.m_oid != null) {
                return false;
            }
        } else if (!m_oid.equals(other.m_oid)) {
            return false;
        }
        if (m_type == null) {
            if (other.m_type != null) {
                return false;
            }
        } else if (!m_type.equals(other.m_type)) {
            return false;
        }
        return true;
    }

}

