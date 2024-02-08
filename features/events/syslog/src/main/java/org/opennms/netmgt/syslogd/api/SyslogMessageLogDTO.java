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
package org.opennms.netmgt.syslogd.api;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.network.InetAddressXmlAdapter;

import java.util.Objects;

@XmlRootElement(name = "syslog-message-log")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogMessageLogDTO implements Message {
    @XmlAttribute(name = "source-address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress sourceAddress;
    @XmlAttribute(name = "source-port")
    private int sourcePort;
    @XmlAttribute(name = "system-id")
    private String systemId;
    @XmlAttribute(name = "location")
    private String location;
    @XmlElement(name = "messages")
    private List<SyslogMessageDTO> messages;

    public SyslogMessageLogDTO() {
        messages = new ArrayList<>(0);
    }

    public SyslogMessageLogDTO(String location, String systemId, InetSocketAddress source) {
        this(location, systemId, source, new ArrayList<>(0));
    }

    public SyslogMessageLogDTO(String location, String systemId, InetSocketAddress source, List<SyslogMessageDTO> messages) {
        this.location = location;
        this.systemId = systemId;
        this.sourceAddress = source.getAddress();
        this.sourcePort = source.getPort();
        this.messages = messages;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(InetAddress sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<SyslogMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<SyslogMessageDTO> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof SyslogMessageLogDTO)) {
            return false;
        }
        SyslogMessageLogDTO castOther = (SyslogMessageLogDTO) other;
        return Objects.equals(sourceAddress, castOther.sourceAddress)
                && Objects.equals(sourcePort, castOther.sourcePort) && Objects.equals(systemId, castOther.systemId)
                && Objects.equals(location, castOther.location) && Objects.equals(messages, castOther.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAddress, sourcePort, systemId, location, messages);
    }

}
