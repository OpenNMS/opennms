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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.config.api.collection.IGroup;
import org.opennms.netmgt.config.api.collection.ISystemDef;
import org.opennms.netmgt.config.api.collection.ITable;


/**
 *  &lt;systemDef name="Enterprise"&gt;
 *    &lt;sysoidMask&gt;.1.3.6.1.4.1.&lt;/sysoidMask&gt;
 *    &lt;collect&gt;
 *      &lt;include&gt;mib2-host-resources-storage&lt;/include&gt;
 *      &lt;include&gt;mib2-coffee-rfc2325&lt;/include&gt;
 *    &lt;/collect&gt;
 *  &lt;/systemDef&gt;
 *   
 * @author brozow
 *
 */
@XmlRootElement(name="datacollection-group")
@XmlAccessorType(XmlAccessType.NONE)
public class SystemDefImpl implements ISystemDef {

    @XmlAttribute(name="name")
    private String m_name;

    @XmlElement(name="sysoidMask")
    private String m_sysoidMask;

    @XmlElement(name="sysoid")
    private String m_sysoid;

    @XmlElementWrapper(name="collect")
    @XmlElement(name="include")
    private String[] m_includes;

    @XmlTransient
    private TableImpl[] m_tables;

    @XmlTransient
    private GroupImpl[] m_groups;

    public SystemDefImpl() {}

    public SystemDefImpl(final String name) {
        m_name = name;
    }

    @Override
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    @Override
    public String getSysoidMask() {
        return m_sysoidMask;
    }

    public void setSysoidMask(String sysoidMask) {
        m_sysoidMask = sysoidMask;
    }

    @Override
    public String getSysoid() {
        return m_sysoid;
    }

    public void setSysoid(String sysoid) {
        m_sysoid = sysoid;
    }

    @Override
    public String[] getIncludes() {
        return m_includes;
    }

    public void setIncludes(String[] includes) {
        m_includes = includes == null? null : includes.clone();
    }

    public IGroup[] getGroups() {
        return (IGroup[])m_groups;
    }

    public void setGroups(final IGroup[] groups) {
        m_groups = GroupImpl.asGroups(groups);
    }
    
    public ITable[] getTables() {
        return (ITable[])m_tables;
    }
    
    public void setTables(final ITable[] tables) {
        m_tables = TableImpl.asTables(tables);
    }

    @Override
    public String toString() {
        return "SystemDefImpl [name=" + m_name + ", sysoidMask=" + m_sysoidMask + ", sysoid=" + m_sysoid + ", includes=" + Arrays.toString(m_includes) + ", tables=" + Arrays.toString(m_tables)
                + ", groups=" + Arrays.toString(m_groups) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_groups);
        result = prime * result + Arrays.hashCode(m_includes);
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_sysoid == null) ? 0 : m_sysoid.hashCode());
        result = prime * result + ((m_sysoidMask == null) ? 0 : m_sysoidMask.hashCode());
        result = prime * result + Arrays.hashCode(m_tables);
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
        if (!(obj instanceof SystemDefImpl)) {
            return false;
        }
        final SystemDefImpl other = (SystemDefImpl) obj;
        if (!Arrays.equals(m_groups, other.m_groups)) {
            return false;
        }
        if (!Arrays.equals(m_includes, other.m_includes)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_sysoid == null) {
            if (other.m_sysoid != null) {
                return false;
            }
        } else if (!m_sysoid.equals(other.m_sysoid)) {
            return false;
        }
        if (m_sysoidMask == null) {
            if (other.m_sysoidMask != null) {
                return false;
            }
        } else if (!m_sysoidMask.equals(other.m_sysoidMask)) {
            return false;
        }
        if (!Arrays.equals(m_tables, other.m_tables)) {
            return false;
        }
        return true;
    }

}
