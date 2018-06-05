/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.ipc;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.network.InetAddressXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "telemetry-message-log")
@XmlAccessorType(XmlAccessType.NONE)
public class TelemetryMessageLogDTO implements Message {
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
    private List<TelemetryMessageDTO> messages;

    public TelemetryMessageLogDTO() {
        messages = new ArrayList<>(0);
    }

    public TelemetryMessageLogDTO(String location, String systemId, InetSocketAddress source) {
        this(location, systemId, source, new ArrayList<>(0));
    }

    public TelemetryMessageLogDTO(String location, String systemId, InetSocketAddress source, List<TelemetryMessageDTO> messages) {
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

    public List<TelemetryMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<TelemetryMessageDTO> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelemetryMessageLogDTO that = (TelemetryMessageLogDTO) o;
        return sourcePort == that.sourcePort &&
                Objects.equals(sourceAddress, that.sourceAddress) &&
                Objects.equals(systemId, that.systemId) &&
                Objects.equals(location, that.location) &&
                Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAddress, sourcePort, systemId, location, messages);
    }

    @Override
    public String toString() {
        return "TelemetryMessageLogDTO{" +
                "sourceAddress=" + sourceAddress +
                ", sourcePort=" + sourcePort +
                ", systemId='" + systemId + '\'' +
                ", location='" + location + '\'' +
                ", messages=" + messages +
                '}';
    }
}
