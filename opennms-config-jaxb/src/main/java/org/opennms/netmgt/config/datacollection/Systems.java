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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name="systems", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class Systems implements Serializable {
    private static final long serialVersionUID = -7752059451224299921L;

    /**
     * list of system definitions
     */
    @XmlElement(name="systemDef")
    private List<SystemDef> m_systemDefs = new ArrayList<>();

    public Systems() {
        super();
    }

    public List<SystemDef> getSystemDefs() {
        if (m_systemDefs == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_systemDefs);
        }
    }

    public void setSystemDefs(final List<SystemDef> systemDefs) {
        m_systemDefs = new ArrayList<SystemDef>(systemDefs);
    }

    public void addSystemDef(final SystemDef systemDef) throws IndexOutOfBoundsException {
        m_systemDefs.add(systemDef);
    }

    public boolean removeSystemDef(final SystemDef systemDef) {
        return m_systemDefs.remove(systemDef);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_systemDefs == null) ? 0 : m_systemDefs.hashCode());
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
        if (!(obj instanceof Systems)) {
            return false;
        }
        final Systems other = (Systems) obj;
        if (m_systemDefs == null) {
            if (other.m_systemDefs != null) {
                return false;
            }
        } else if (!m_systemDefs.equals(other.m_systemDefs)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Systems [systemDefs=" + m_systemDefs + "]";
    }
}
