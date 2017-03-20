/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.mailtransporttest;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Basically attributes that help setup the javamailer's confusion
 * set of properties.
 */

@XmlRootElement(name="readmail-protocol")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReadmailProtocol implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="transport")
    private String m_transport;

    @XmlAttribute(name="ssl-enable")
    private Boolean m_sslEnable;

    @XmlAttribute(name="start-tls")
    private Boolean m_startTLS;

    public ReadmailProtocol() {
    }

    public ReadmailProtocol(final String transport, final Boolean sslEnable, final Boolean startTls) {
        m_transport = transport;
        m_sslEnable = sslEnable;
        m_startTLS = startTls;
    }

    public String getTransport() {
        return m_transport == null? "pop3" : m_transport;
    }

    public void setTransport(final String transport) {
        m_transport = transport;
    }

    public Boolean isSslEnabled() {
        return m_sslEnable == null? false : m_sslEnable;
    }

    public void setSslEnabled(final Boolean sslEnable) {
        m_sslEnable = sslEnable;
    }

    public Boolean shouldStartTLS() {
        return m_startTLS == null? false : m_startTLS;
    }

    public void setStartTLS(final Boolean startTLS) {
        m_startTLS = startTLS;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_transport, m_sslEnable, m_startTLS);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;

        if (obj instanceof ReadmailProtocol) {
            final ReadmailProtocol that = (ReadmailProtocol)obj;
            return Objects.equals(this.m_transport, that.m_transport) &&
                    Objects.equals(this.m_sslEnable, that.m_sslEnable) &&
                    Objects.equals(this.m_startTLS, that.m_startTLS);
        }
        return false;
    }

}
