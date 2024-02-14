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

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IGroup;
import org.opennms.netmgt.config.api.collection.IMibObject;

/**
 * &lt;group name="mib2-coffee-rfc2325"&gt;
 *      &lt;mibObj oid=".1.3.6.1.2.1.10.132.2" instance="0" alias="coffeePotCapacity" type="integer" /&gt;
 *      &lt;mibObj oid=".1.3.6.1.2.1.10.132.4.1.2" instance="0" alias="coffeePotLevel" type="integer" /&gt;
 *      &lt;mibObj oid=".1.3.6.1.2.1.10.132.4.1.6" instance="0" alias="coffeePotTemp" type="integer" /&gt;
 * &lt;/group&gt;
 * 
 * @author brozow
 *
 */
@XmlRootElement(name="group")
@XmlAccessorType(XmlAccessType.NONE)
public class GroupImpl implements IGroup {

    @XmlAttribute(name="name")
    private String m_name;

    @XmlElement(name="mibObj")
    private MibObjectImpl[] m_mibObjects = new MibObjectImpl[0];

    public GroupImpl() {
    }

    public GroupImpl(final String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public IMibObject[] getMibObjects() {
        return (IMibObject[]) m_mibObjects;
    }

    public void setMibObjects(final IMibObject[] mibObjects) {
        m_mibObjects = MibObjectImpl.asMibObjects(mibObjects);
    }

    public void addMibObject(final MibObjectImpl mibObject) {
        m_mibObjects = ArrayUtils.append(m_mibObjects, mibObject);
    }

    public static GroupImpl asGroup(final IGroup group) {
        if (group == null) return null;

        if (group instanceof GroupImpl) {
            return (GroupImpl)group;
        }

        final GroupImpl newGroup = new GroupImpl();
        newGroup.setName(group.getName());
        newGroup.setMibObjects(group.getMibObjects());
        return newGroup;
    }

    public static GroupImpl[] asGroups(final IGroup[] groups) {
        if (groups == null) return null;
        
        final GroupImpl[] newGroups = new GroupImpl[groups.length];
        for (int i=0; i < groups.length; i++) {
            newGroups[i] = GroupImpl.asGroup(groups[i]);
        }
        return newGroups;
    }

    @Override
    public String toString() {
        return "GroupImpl [name=" + m_name + ", mibObjects=" + Arrays.toString(m_mibObjects) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_mibObjects);
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
        if (!(obj instanceof GroupImpl)) {
            return false;
        }
        final GroupImpl other = (GroupImpl) obj;
        if (!Arrays.equals(m_mibObjects, other.m_mibObjects)) {
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
}
