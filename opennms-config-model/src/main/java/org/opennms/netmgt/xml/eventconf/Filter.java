/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name="filter")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Filter implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="eventparm", required=true)
    private String m_eventparm;

    @XmlAttribute(name="pattern", required=true)
    private String m_pattern;

    @XmlAttribute(name="replacement", required=true)
    private String m_replacement;

    public String getEventparm() {
        return m_eventparm;
    }

    public void setEventparm(final String eventparm) {
        m_eventparm = ConfigUtils.assertNotNull(eventparm, "eventparm").intern();
    }

    public String getPattern() {
        return m_pattern;
    }

    public void setPattern(final String pattern) {
        m_pattern = ConfigUtils.assertNotNull(pattern, "pattern").intern();
    }

    public String getReplacement() {
        return m_replacement;
    }

    public void setReplacement(final String replacement) {
        m_replacement = ConfigUtils.assertNotNull(replacement, "replacement").intern();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_eventparm, m_pattern, m_replacement);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Filter) {
            final Filter that = (Filter) obj;
            return Objects.equals(this.m_eventparm, that.m_eventparm) &&
                    Objects.equals(this.m_pattern, that.m_pattern) &&
                    Objects.equals(this.m_replacement, that.m_replacement);
        }
        return false;
    }

}
