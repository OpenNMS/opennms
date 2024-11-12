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
package org.opennms.netmgt.trapd;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.network.InetAddressXmlAdapter;

@XmlRootElement(name = "trap-message-log")
@XmlAccessorType(XmlAccessType.FIELD)
public class TrapLogDTO implements Message {

    @XmlAttribute(name = "system-id", required = true)
    private String systemId;

    @XmlAttribute(name="location", required = true)
    private String location;

    @XmlAttribute(name = "trap-address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress trapAddress;

    @XmlElement(name = "messages")
    private List<TrapDTO> messages = new ArrayList<>();
    @XmlElement(name = "should-use-address-from-varbind")
    private Boolean shouldUseAddressFromVarbind;
    // Default constructor for Jaxb
    public TrapLogDTO() {

    }

    public TrapLogDTO(String systemId, String location, InetAddress trapAddress) {
        this.systemId = Objects.requireNonNull(systemId);
        this.location = Objects.requireNonNull(location);
        this.trapAddress = Objects.requireNonNull(trapAddress);
    }

    public List<TrapDTO> getMessages() {
        return messages;
    }

    public void addMessage(TrapDTO trapDTO) {
        this.messages.add(trapDTO);
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

    public InetAddress getTrapAddress() {
        return trapAddress;
    }

    public void setTrapAddress(InetAddress trapAddress) {
        this.trapAddress = trapAddress;
    }

    public void setMessages(List<TrapDTO> messages) {
        this.messages = messages;
    }

    public Boolean getShouldUseAddressFromVarbind() {
        return shouldUseAddressFromVarbind;
    }

    public void setShouldUseAddressFromVarbind(Boolean shouldUseAddressFromVarbind) {
        this.shouldUseAddressFromVarbind = shouldUseAddressFromVarbind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(systemId, location, trapAddress, messages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (getClass() != obj.getClass()) return false;
        final TrapLogDTO other = (TrapLogDTO) obj;
        boolean equals = Objects.equals(location, other.location)
                && Objects.equals(systemId, other.systemId)
                && Objects.equals(trapAddress, other.trapAddress)
                && Objects.equals(messages, other.messages);
        return equals;
    }
}
