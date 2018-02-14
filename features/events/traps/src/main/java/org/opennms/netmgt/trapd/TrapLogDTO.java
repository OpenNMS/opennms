/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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
