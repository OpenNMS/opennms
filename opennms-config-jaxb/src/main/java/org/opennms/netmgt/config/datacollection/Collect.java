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
import org.opennms.netmgt.config.internal.collection.DatacollectionConfigVisitor;

/**
 * container for list of MIB groups to be collected for the system
 */

@XmlRootElement(name="collect", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class Collect implements Serializable {
    private static final long serialVersionUID = 4612617249821481259L;

    @XmlElement(name="includeGroup")
    private List<String> m_includeGroups = new ArrayList<>();

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
        m_includeGroups.add(includeGroup);
    }

    public boolean removeIncludeGroup(final String includeGroup) {
        return m_includeGroups.remove(includeGroup);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_includeGroups == null) ? 0 : m_includeGroups.hashCode());
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
        if (!(obj instanceof Collect)) {
            return false;
        }
        final Collect other = (Collect) obj;
        if (m_includeGroups == null) {
            if (other.m_includeGroups != null) {
                return false;
            }
        } else if (!m_includeGroups.equals(other.m_includeGroups)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Collect [includeGroups=" + m_includeGroups + "]";
    }

    public void visit(final DatacollectionConfigVisitor visitor) {
        visitor.visitCollect(this);
        visitor.visitCollectComplete();
    }

}
