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

package org.opennms.netmgt.config.notifd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "auto-acknowledge-alarm")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifd-configuration.xsd")
public class AutoAcknowledgeAlarm implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_RESOLUTION_PREFIX = "RESOLVED: ";

    @XmlAttribute(name = "resolution-prefix")
    private String m_resolutionPrefix;

    @XmlAttribute(name = "notify")
    private Boolean m_notify;

    @XmlElement(name = "uei")
    private List<String> m_ueis = new ArrayList<>();

    public AutoAcknowledgeAlarm() { }

    public String getResolutionPrefix() {
        return m_resolutionPrefix != null ? m_resolutionPrefix : DEFAULT_RESOLUTION_PREFIX;
    }

    public void setResolutionPrefix(final String resolutionPrefix) {
        m_resolutionPrefix = resolutionPrefix;
    }

    public Boolean getNotify() {
        return m_notify != null ? m_notify : Boolean.valueOf("true");
    }

    public void setNotify(final Boolean notify) {
        m_notify = notify;
    }

    public List<String> getUeis() {
        return m_ueis;
    }

    public void setUei(final List<String> ueis) {
        m_ueis.clear();
        m_ueis.addAll(ueis);
    }

    public void addUei(final String uei) {
        m_ueis.add(uei);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof AutoAcknowledgeAlarm) {
            final AutoAcknowledgeAlarm that = (AutoAcknowledgeAlarm)obj;
            return Objects.equals(this.m_resolutionPrefix, that.m_resolutionPrefix)
                && Objects.equals(this.m_notify, that.m_notify)
                && Objects.equals(this.m_ueis, that.m_ueis);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            m_resolutionPrefix, 
            m_notify, 
            m_ueis);
    }

}
