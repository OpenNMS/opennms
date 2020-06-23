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

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.internal.collection.DatacollectionConfigVisitor;

/**
 * Class IncludeCollection.
 */

@XmlRootElement(name="include-collection", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class IncludeCollection implements Serializable {
    private static final long serialVersionUID = 1736111882579250557L;

    /**
     * System Definition Name
     */
    @XmlAttribute(name="systemDef")
    private String m_systemDef;

    /**
     * Data Collection Group Name
     */
    @XmlAttribute(name="dataCollectionGroup")
    private String m_dataCollectionGroup;

    /**
     * Exclude filter uses regular expression syntax to avoid
     * certain system definitions
     */
    @XmlElement(name="exclude-filter")
    private List<String> m_excludeFilters = new ArrayList<>();

    public IncludeCollection() {
        super();
    }

    /**
     * System Definition Name
     */
    public String getSystemDef() {
        return m_systemDef;
    }

    public void setSystemDef(final String systemDef) {
        m_systemDef = systemDef.intern();
    }

    /**
     * Data Collection Group Name
     */
    public String getDataCollectionGroup() {
        return m_dataCollectionGroup;
    }

    public void setDataCollectionGroup(final String dataCollectionGroup) {
        m_dataCollectionGroup = dataCollectionGroup.intern();
    }

    public List<String> getExcludeFilters() {
        if (m_excludeFilters == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_excludeFilters);
        }
    }

    public void setExcludeFilters(final List<String> excludeFilters) {
        m_excludeFilters = new ArrayList<String>(excludeFilters);
    }

    public void addExcludeFilter(final String excludeFilter) throws IndexOutOfBoundsException {
        m_excludeFilters.add(excludeFilter);
    }

    public boolean removeExcludeFilter(final String excludeFilter) {
        return m_excludeFilters.remove(excludeFilter);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_dataCollectionGroup == null) ? 0 : m_dataCollectionGroup.hashCode());
        result = prime * result + ((m_excludeFilters == null) ? 0 : m_excludeFilters.hashCode());
        result = prime * result + ((m_systemDef == null) ? 0 : m_systemDef.hashCode());
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
        if (!(obj instanceof IncludeCollection)) {
            return false;
        }
        final IncludeCollection other = (IncludeCollection) obj;
        if (m_dataCollectionGroup == null) {
            if (other.m_dataCollectionGroup != null) {
                return false;
            }
        } else if (!m_dataCollectionGroup.equals(other.m_dataCollectionGroup)) {
            return false;
        }
        if (m_excludeFilters == null) {
            if (other.m_excludeFilters != null) {
                return false;
            }
        } else if (!m_excludeFilters.equals(other.m_excludeFilters)) {
            return false;
        }
        if (m_systemDef == null) {
            if (other.m_systemDef != null) {
                return false;
            }
        } else if (!m_systemDef.equals(other.m_systemDef)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IncludeCollection [systemDef=" + m_systemDef + ", dataCollectionGroup=" + m_dataCollectionGroup + ", excludeFilters=" + m_excludeFilters + "]";
    }

    public void visit(final DatacollectionConfigVisitor visitor) {
        visitor.visitIncludeCollection(this);
        visitor.visitIncludeCollectionComplete();
    }

}
