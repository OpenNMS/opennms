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
