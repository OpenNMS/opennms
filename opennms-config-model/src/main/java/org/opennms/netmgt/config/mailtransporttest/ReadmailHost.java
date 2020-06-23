/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.mailtransporttest;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Define the host and port of the sendmail server. If you don't,
 * defaults will be used and ${ipaddr} is replaced with the IP address of the service.
 */

@XmlRootElement(name="readmail-host")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class ReadmailHost implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="host")
    private String m_host;

    @XmlAttribute(name="port")
    private Long m_port;

    /**
     * Basically attributes that help setup the javamailer's
     * confusion set of properties.
     */
    @XmlElement(name="readmail-protocol", required=true)
    private ReadmailProtocol m_readmailProtocol;

    public ReadmailHost() {
    }

    public ReadmailHost(final String host, final Long port) {
        setHost(host);
        setPort(port);
    }

    public String getHost() {
        return m_host == null? "${ipaddr}" : m_host;
    }

    public void setHost(final String host) {
        m_host = ConfigUtils.normalizeString(host);
    }

    public Long getPort() {
        return m_port == null? 110 : m_port;
    }

    public void setPort(final Long port) {
        m_port = port;
    }

    public ReadmailProtocol getReadmailProtocol() {
        return m_readmailProtocol;
    }

    public void setReadmailProtocol(final ReadmailProtocol readmailProtocol) {
        m_readmailProtocol = ConfigUtils.assertNotNull(readmailProtocol, "readmail-protocol");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_host, m_port, m_readmailProtocol);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;

        if (obj instanceof ReadmailHost) {
            final ReadmailHost that = (ReadmailHost)obj;
            return Objects.equals(this.m_host, that.m_host) &&
                    Objects.equals(this.m_port, that.m_port) &&
                    Objects.equals(this.m_readmailProtocol, that.m_readmailProtocol);
        }
        return false;
    }

}
