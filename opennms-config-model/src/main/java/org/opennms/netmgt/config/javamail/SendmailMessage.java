/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * The Class SendmailMessage.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="sendmail-message", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("javamail-configuration.xsd")
public class SendmailMessage implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="to")
    private String m_to;

    @XmlAttribute(name="from")
    private String m_from;

    @XmlAttribute(name="subject")
    private String m_subject;

    @XmlAttribute(name="body")
    private String m_body;

    public SendmailMessage() {
        setTo("root@localhost");
        setFrom("root@[127.0.0.1]");
        setSubject("OpenNMS Test Message");
        setBody("This is an OpenNMS test message.");
    }

    public String getTo() {
        return m_to == null ? "root@localhost" : m_to;
    }

    public void setTo(final String to) {
        m_to = ConfigUtils.normalizeString(to);
    }

    public String getFrom() {
        return m_from == null ? "root@[127.0.0.1]" : m_from;
    }

    public void setFrom(final String from) {
        m_from = ConfigUtils.normalizeString(from);
    }

    public String getSubject() {
        return m_subject == null ? "OpenNMS Test Message" : m_subject;
    }

    public void setSubject(final String subject) {
        m_subject = ConfigUtils.normalizeString(subject);
    }

    public String getBody() {
        return m_body == null ? "This is an OpenNMS test message." : m_body;
    }

    public void setBody(final String body) {
        m_body = ConfigUtils.normalizeString(body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_to, m_from, m_subject, m_body);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof SendmailMessage) {
            final SendmailMessage that = (SendmailMessage)obj;
            return Objects.equals(this.m_to, that.m_to)
                    && Objects.equals(this.m_from, that.m_from)
                    && Objects.equals(this.m_subject, that.m_subject)
                    && Objects.equals(this.m_body, that.m_body);
        }
        return false;
    }

}
