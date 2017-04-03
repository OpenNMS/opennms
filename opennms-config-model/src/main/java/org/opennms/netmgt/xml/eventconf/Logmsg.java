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
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.NullStringAdapter;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The event logmsg with the destination attribute defining
 * if event is for display only, logonly, log and display or
 * neither. A destination attribute of 'donotpersist' indicates
 * that Eventd persist the event to the database. A value of
 * 'discardtraps' instructs the SNMP trap daemon to not create
 * events for incoming traps that match this event. The optional
 * notify attributed can be used to suppress notices on a
 * particular event (by default it is true - i.e. a notice
 * will be sent).
 */
@XmlRootElement(name="logmsg")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Logmsg implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlValue
    @XmlJavaTypeAdapter(NullStringAdapter.class)
    private String m_content;

    @XmlAttribute(name="notify", required=false)
    private Boolean m_notify;

    @XmlAttribute(name="dest", required=false)
    private LogDestType m_dest;

    public String getContent() {
        return m_content;
    }

    public void setContent(final String content) {
        m_content = ConfigUtils.normalizeAndInternString(content);
    }

    public Boolean getNotify() {
        return m_notify == null ? Boolean.TRUE : m_notify; // Default is true according to XSD
    }

    public void setNotify(final boolean notify) {
        m_notify = notify;
    }

    public LogDestType getDest() {
        return m_dest == null ? LogDestType.LOGNDISPLAY : m_dest;
    }

    public void setDest(final LogDestType dest) {
        m_dest = dest;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_content, m_notify, m_dest);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Logmsg) {
            final Logmsg that = (Logmsg) obj;
            return Objects.equals(this.m_content, that.m_content) &&
                    Objects.equals(this.m_notify, that.m_notify) &&
                    Objects.equals(this.m_dest, that.m_dest);
        }
        return false;
    }

}
