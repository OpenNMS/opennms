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
package org.opennms.netmgt.config.tl1d;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "tl1-element")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("tl1d-configuration.xsd")
public class Tl1Element implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_USERID = "opennms";
    private static final String DEFAULT_PASSWORD = "opennms";
    private static final String DEFAULT_TL1_CLIENT_API = "org.opennms.netmgt.tl1d.Tl1ClientImpl";
    private static final String DEFAULT_TL1_MESSAGE_PARSER = "org.opennms.netmgt.tl1d.Tl1AutonomousMessageProcessor";

    @XmlAttribute(name = "host", required = true)
    private String m_host;

    @XmlAttribute(name = "port")
    private Integer m_port;

    @XmlAttribute(name = "userid")
    private String m_userid;

    @XmlAttribute(name = "password")
    private String m_password;

    @XmlAttribute(name = "tl1-client-api")
    private String m_tl1ClientApi;

    @XmlAttribute(name = "tl1-message-parser")
    private String m_tl1MessageParser;

    @XmlAttribute(name = "reconnect-delay")
    private Long m_reconnectDelay;

    public String getHost() {
        return m_host;
    }

    public void setHost(final String host) {
        m_host = ConfigUtils.assertNotEmpty(host, "host");
    }

    public Integer getPort() {
        return m_port != null ? m_port : 502;
    }

    public void setPort(final Integer port) {
        m_port = port;
    }

    public String getUserid() {
        return m_userid != null ? m_userid : DEFAULT_USERID;
    }

    public void setUserid(final String userid) {
        m_userid = ConfigUtils.normalizeAndTrimString(userid);
    }

    public String getPassword() {
        return m_password != null ? m_password : DEFAULT_PASSWORD;
    }

    public void setPassword(final String password) {
        m_password = ConfigUtils.normalizeString(password);
    }

    public String getTl1ClientApi() {
        return m_tl1ClientApi != null ? m_tl1ClientApi : DEFAULT_TL1_CLIENT_API;
    }

    public void setTl1ClientApi(final String tl1ClientApi) {
        this.m_tl1ClientApi = ConfigUtils.normalizeString(tl1ClientApi);
    }

    public String getTl1MessageParser() {
        return m_tl1MessageParser != null ? m_tl1MessageParser : DEFAULT_TL1_MESSAGE_PARSER;
    }

    public void setTl1MessageParser(final String tl1MessageParser) {
        m_tl1MessageParser = ConfigUtils.normalizeString(tl1MessageParser);
    }

    public Long getReconnectDelay() {
        return m_reconnectDelay != null ? m_reconnectDelay : 30000L;
    }

    public void setReconnectDelay(final Long reconnectDelay) {
        m_reconnectDelay = reconnectDelay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_host, 
                            m_port, 
                            m_userid, 
                            m_password, 
                            m_tl1ClientApi, 
                            m_tl1MessageParser, 
                            m_reconnectDelay);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Tl1Element) {
            final Tl1Element that = (Tl1Element)obj;
            return Objects.equals(this.m_host, that.m_host)
                    && Objects.equals(this.m_port, that.m_port)
                    && Objects.equals(this.m_userid, that.m_userid)
                    && Objects.equals(this.m_password, that.m_password)
                    && Objects.equals(this.m_tl1ClientApi, that.m_tl1ClientApi)
                    && Objects.equals(this.m_tl1MessageParser, that.m_tl1MessageParser)
                    && Objects.equals(this.m_reconnectDelay, that.m_reconnectDelay);
        }
        return false;
    }

}
