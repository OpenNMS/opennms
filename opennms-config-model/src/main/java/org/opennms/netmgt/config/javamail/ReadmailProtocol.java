/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.javamail;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The Class ReadmailProtocol.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="readmail-protocol", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("javamail-configuration.xsd")
public class ReadmailProtocol implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="transport")
    private String m_transport;

    @XmlAttribute(name="ssl-enable")
    private Boolean m_sslEnable;

    @XmlAttribute(name="start-tls")
    private Boolean m_startTls;

    public ReadmailProtocol() {
    }

    public String getTransport() {
        return m_transport == null ? "pop3" : m_transport;
    }

    public void setTransport(final String transport) {
        m_transport = ConfigUtils.normalizeString(transport);
    }

    public Boolean isSslEnable() {
        return m_sslEnable == null ? Boolean.FALSE : m_sslEnable;
    }

    public void setSslEnable(final Boolean sslEnable) {
        m_sslEnable = sslEnable;
    }

    public Boolean isStartTls() {
        return m_startTls == null ? Boolean.FALSE : m_startTls;
    }

    public void setStartTls(final Boolean startTls) {
        m_startTls = startTls;
    }

    @Override()
    public int hashCode() {
        return Objects.hash(m_transport, m_sslEnable, m_startTls);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ReadmailProtocol) {
            final ReadmailProtocol that = (ReadmailProtocol)obj;
            return Objects.equals(this.m_transport, that.m_transport)
                    && Objects.equals(this.m_sslEnable, that.m_sslEnable)
                    && Objects.equals(this.m_startTls, that.m_startTls);
        }
        return false;
    }

}
