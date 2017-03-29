/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.threshd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the thresholds.xml configuration file.
 */
@XmlRootElement(name = "thresholding-config")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public class ThresholdingConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Thresholding group element
     */
    @XmlElement(name = "group")
    private List<Group> m_groups = new ArrayList<>();

    public ThresholdingConfig() { }

    public List<Group> getGroups() {
        return m_groups;
    }

    public void setGroups(final List<Group> groups) {
        if (groups == m_groups) return;
        m_groups.clear();
        if (groups != null) m_groups.addAll(groups);
    }

    public void addGroup(final Group group) {
        m_groups.add(group);
    }

    public boolean removeGroup(final Group group) {
        return m_groups.remove(group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_groups);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ThresholdingConfig) {
            final ThresholdingConfig that = (ThresholdingConfig)obj;
            return Objects.equals(this.m_groups, that.m_groups);
        }
        return false;
    }

}
