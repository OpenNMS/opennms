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
package org.opennms.netmgt.config.mailtransporttest;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Define the to, from, subject, and body of a message. If not
 * defined, one will be defined for your benefit (or confusion ;-)
 */

@XmlRootElement(name="sendmail-message")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
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
    }

    public String getTo() {
        return m_to == null? "root@localhost" : m_to;
    }

    public void setTo(final String to) {
        m_to = to;
    }

    public String getFrom() {
        return m_from == null? "root@[127.0.0.1]" : m_from;
    }

    public void setFrom(final String from) {
        m_from = from;
    }

    public String getSubject() {
        return m_subject == null? "OpenNMS Test Message" : m_subject;
    }

    public void setSubject(final String subject) {
        m_subject = subject;
    }

    public String getBody() {
        return m_body == null? "This is an OpenNMS test message." : m_body;
    }

    public void setBody(final String body) {
        m_body = body;
    }

    public int hashCode() {
        return Objects.hash(m_to, m_from, m_subject, m_body);
    }

    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;

        if (obj instanceof SendmailMessage) {
            final SendmailMessage that = (SendmailMessage)obj;
            return Objects.equals(this.m_to, that.m_to) &&
                    Objects.equals(this.m_from, that.m_from) &&
                    Objects.equals(this.m_subject, that.m_subject) &&
                    Objects.equals(this.m_body, that.m_body);
        }
        return false;
    }

}
