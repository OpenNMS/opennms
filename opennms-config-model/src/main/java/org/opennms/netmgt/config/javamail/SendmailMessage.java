/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    @XmlAttribute(name="reply-to")
    private String m_replyTo;

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

    public String getReplyTo() {
        return m_replyTo;
    }

    public void setReplyTo(final String replyTo) {
        m_replyTo = ConfigUtils.normalizeString(replyTo);
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
