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

package org.opennms.netmgt.config.syslogd;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "syslogd-configuration-group")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("syslog.xsd")
public class SyslogdConfigurationGroup implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElementWrapper(name = "ueiList", required=false)
    @XmlElement(name = "ueiMatch")
    private List<UeiMatch> m_ueiMatches = new ArrayList<>();

    @XmlElementWrapper(name = "hideMessage", required=false)
    @XmlElement(name = "hideMatch")
    private List<HideMatch> m_hideMessages = new ArrayList<>();

    public SyslogdConfigurationGroup() {
    }

    public List<UeiMatch> getUeiMatches() {
        return m_ueiMatches;
    }

    public void setUeiMatches(final List<UeiMatch> ueiList) {
        if (ueiList == m_ueiMatches) return;
        m_ueiMatches.clear();
        if (ueiList != null) m_ueiMatches.addAll(ueiList);
    }

    public void addUeiMatch(final UeiMatch ueiMatch) {
        m_ueiMatches.add(ueiMatch);
    }

    public boolean removeUeiMatch(final UeiMatch ueiMatch) {
        return m_ueiMatches.remove(ueiMatch);
    }

    public List<HideMatch> getHideMatches() {
        return m_hideMessages;
    }

    public void setHideMatches(final List<HideMatch> hideMessages) {
        if (hideMessages == m_hideMessages) return;
        m_hideMessages.clear();
        if (hideMessages != null) m_hideMessages.addAll(hideMessages);
    }

    public void addHideMatch(final HideMatch hideMatch) {
        m_hideMessages.add(hideMatch);
    }

    public boolean removeHideMatch(final HideMatch hideMatch) {
        return m_hideMessages.remove(hideMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_ueiMatches, m_hideMessages);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof SyslogdConfigurationGroup) {
            final SyslogdConfigurationGroup that = (SyslogdConfigurationGroup)obj;
            return Objects.equals(this.m_ueiMatches, that.m_ueiMatches)
                    && Objects.equals(this.m_hideMessages, that.m_hideMessages);
        }
        return false;
    }
}
