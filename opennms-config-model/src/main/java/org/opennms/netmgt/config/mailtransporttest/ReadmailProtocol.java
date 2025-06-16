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

@XmlRootElement(name="readmail-protocol")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("mail-transport-test.xsd")
public class ReadmailProtocol implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="transport")
    private String m_transport;

    @XmlAttribute(name="ssl-enable")
    private Boolean m_sslEnable;

    @XmlAttribute(name="start-tls")
    private Boolean m_startTLS;

    public ReadmailProtocol() {
    }

    public ReadmailProtocol(final String transport, final Boolean sslEnabled, final Boolean startTls) {
        setTransport(transport);
        setSslEnabled(sslEnabled);
        setStartTLS(startTls);
    }

    public String getTransport() {
        return m_transport == null? "pop3" : m_transport;
    }

    public void setTransport(final String transport) {
        m_transport = ConfigUtils.normalizeString(transport);
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
