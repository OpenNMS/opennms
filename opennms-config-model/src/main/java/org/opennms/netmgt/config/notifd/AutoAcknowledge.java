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

@XmlRootElement(name = "auto-acknowledge")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifd-configuration.xsd")
public class AutoAcknowledge implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_RESOLUTION_PREFIX = "RESOLVED: ";

    @XmlAttribute(name = "resolution-prefix")
    private String m_resolutionPrefix;

    @XmlAttribute(name = "uei", required = true)
    private String m_uei;

    @XmlAttribute(name = "acknowledge", required = true)
    private String m_acknowledge;

    @XmlAttribute(name = "notify")
    private Boolean m_notify;

    @XmlElement(name = "match", required = true)
    private List<String> m_matches = new ArrayList<>();

    public AutoAcknowledge() { }

    public String getResolutionPrefix() {
        return m_resolutionPrefix != null ? m_resolutionPrefix : DEFAULT_RESOLUTION_PREFIX;
    }

    public void setResolutionPrefix(final String resolutionPrefix) {
        m_resolutionPrefix = resolutionPrefix;
    }

    public String getUei() {
        return m_uei;
    }

    public void setUei(final String uei) {
        if (uei == null) {
            throw new IllegalArgumentException("UEI is a required field!");
        }
        m_uei = uei;
    }

    public String getAcknowledge() {
        return m_acknowledge;
    }

    public void setAcknowledge(final String acknowledge) {
        if (acknowledge == null) {
            throw new IllegalArgumentException("'acknowledge' is a required field!");
        }
        m_acknowledge = acknowledge;
    }

    public Boolean getNotify() {
        return m_notify != null ? m_notify : Boolean.valueOf("true");
    }

    public void setNotify(final Boolean notify) {
        m_notify = notify;
    }

    public List<String> getMatches() {
        return m_matches;
    }

    public void setMatches(final List<String> matches) {
        m_matches.clear();
        m_matches.addAll(matches);
    }

    public void addMatch(final String match) {
        m_matches.add(match);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            m_resolutionPrefix, 
            m_uei, 
            m_acknowledge, 
            m_notify, 
            m_matches);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof AutoAcknowledge) {
            final AutoAcknowledge that = (AutoAcknowledge)obj;
            return Objects.equals(this.m_resolutionPrefix, that.m_resolutionPrefix)
                && Objects.equals(this.m_uei, that.m_uei)
                && Objects.equals(this.m_acknowledge, that.m_acknowledge)
                && Objects.equals(this.m_notify, that.m_notify)
                && Objects.equals(this.m_matches, that.m_matches);
        }
        return false;
    }

}
