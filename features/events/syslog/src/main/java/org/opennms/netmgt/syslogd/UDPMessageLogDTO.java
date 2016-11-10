package org.opennms.netmgt.syslogd;

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

@XmlRootElement(name = "udp-message-log")
@XmlAccessorType(XmlAccessType.FIELD)
public class UDPMessageLogDTO implements Message {
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
    private List<UDPMessageDTO> messages;

    public UDPMessageLogDTO() {
        messages = new ArrayList<>(0);
    }

    public UDPMessageLogDTO(String location, String systemId, InetSocketAddress source, List<UDPMessageDTO> messages) {
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

    public List<UDPMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<UDPMessageDTO> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof UDPMessageLogDTO)) {
            return false;
        }
        UDPMessageLogDTO castOther = (UDPMessageLogDTO) other;
        return Objects.equals(sourceAddress, castOther.sourceAddress)
                && Objects.equals(sourcePort, castOther.sourcePort) && Objects.equals(systemId, castOther.systemId)
                && Objects.equals(location, castOther.location) && Objects.equals(messages, castOther.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAddress, sourcePort, systemId, location, messages);
    }

}
