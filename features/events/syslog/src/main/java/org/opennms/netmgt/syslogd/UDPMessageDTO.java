package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;

import java.util.Objects;

@XmlRootElement(name = "udp-message")
@XmlAccessorType(XmlAccessType.FIELD)
public class UDPMessageDTO {

    @XmlAttribute(name = "timestamp")
    private Date timestamp;
    @XmlValue
    @XmlJavaTypeAdapter(ByteBufferXmlAdapter.class)
    private ByteBuffer bytes;

    public UDPMessageDTO() { }

    public UDPMessageDTO(ByteBuffer bytes) {
        this.timestamp = new Date();
        this.bytes = bytes;
    }

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public ByteBuffer getBytes() {
        return bytes;
    }
    public void setBytes(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof UDPMessageDTO)) {
            return false;
        }
        UDPMessageDTO castOther = (UDPMessageDTO) other;
        return Objects.equals(timestamp, castOther.timestamp) && Objects.equals(bytes, castOther.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, bytes);
    }
}
