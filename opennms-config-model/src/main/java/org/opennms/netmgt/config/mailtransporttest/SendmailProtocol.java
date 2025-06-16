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
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Basically attributes that help setup the javamailer's confusion
 * set of properties.
 */

@XmlRootElement(name="sendmail-protocol")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class SendmailProtocol implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="char-set")
    private String m_charSet;

    @XmlAttribute(name="mailer")
    private String m_mailer;

    @XmlAttribute(name="message-content-type")
    private String m_messageContentType;

    @XmlAttribute(name="message-encoding")
    private String m_messageEncoding;

    @XmlAttribute(name="quit-wait")
    private Boolean m_quitWait;

    @XmlAttribute(name="transport")
    private String m_transport;

    @XmlAttribute(name="ssl-enable")
    private Boolean m_sslEnable;

    @XmlAttribute(name="start-tls")
    private Boolean m_startTls;

    public SendmailProtocol() {
    }

    public String getCharSet() {
        return m_charSet == null? "us-ascii" : m_charSet;
    }

    public void setCharSet(final String charSet) {
        m_charSet = ConfigUtils.normalizeString(charSet);
    }

    public String getMailer() {
        return m_mailer == null? "smtpsend" : m_mailer;
    }

    public void setMailer(final String mailer) {
        m_mailer = ConfigUtils.normalizeString(mailer);
    }

    public String getMessageContentType() {
        return m_messageContentType == null? "text/plain" : m_messageContentType;
    }

    public void setMessageContentType(final String messageContentType) {
        m_messageContentType = ConfigUtils.normalizeString(messageContentType);
    }

    public String getMessageEncoding() {
        return m_messageEncoding == null? "7-bit" : m_messageEncoding;
    }

    public void setMessageEncoding(final String messageEncoding) {
        m_messageEncoding = ConfigUtils.normalizeString(messageEncoding);
    }

    public Boolean getQuitWait() {
        return m_quitWait == null? true : m_quitWait;
    }

    public void setQuitWait(final Boolean quitWait) {
        m_quitWait = quitWait;
    }

    public String getTransport() {
        return m_transport == null? "smtp" : m_transport;
    }

    public void setTransport(final String transport) {
        m_transport = transport;
    }

    public Boolean getSslEnable() {
        return m_sslEnable == null? false : m_sslEnable;
    }

    public void setSslEnable(final Boolean sslEnable) {
        m_sslEnable = sslEnable;
    }

    public Boolean getStartTls() {
        return m_startTls == null? false : m_startTls;
    }

    public void setStartTls(final Boolean startTls) {
        m_startTls = startTls;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_charSet,
                            m_mailer,
                            m_messageContentType,
                            m_messageEncoding,
                            m_quitWait,
                            m_transport,
                            m_sslEnable,
                            m_startTls);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SendmailProtocol) {
            final SendmailProtocol that = (SendmailProtocol) obj;
            return Objects.equals(this.m_charSet, that.m_charSet) &&
                    Objects.equals(this.m_mailer, that.m_mailer) &&
                    Objects.equals(this.m_messageContentType, that.m_messageContentType) &&
                    Objects.equals(this.m_messageEncoding, that.m_messageEncoding) &&
                    Objects.equals(this.m_quitWait, that.m_quitWait) &&
                    Objects.equals(this.m_transport, that.m_transport) &&
                    Objects.equals(this.m_sslEnable, that.m_sslEnable) &&
                    Objects.equals(this.m_startTls, that.m_startTls);
        }
        return false;
    }

}
