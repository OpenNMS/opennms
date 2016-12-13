/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class is a {@link Callable} task that is responsible for converting
 * the syslog payload into an OpenNMS event by using the {@link ConvertToEvent}
 * class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SyslogDTO {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogDTO.class);

    private String m_systemId;
    private String m_location;
    private InetAddress m_sourceAddress;
    private int m_port;
    private ByteBuffer m_bytes;

    /**
     * No-arg constructor so that we can preallocate this object for use with
     * an LMAX Disruptor or use it with JAXB.
     */
    public SyslogDTO() {
    }


    public SyslogDTO(final InetAddress sourceAddress, final int port, final ByteBuffer bytes,  final String systemId, final String location) {
        if (systemId == null) {
            throw new IllegalArgumentException("System ID cannot be null");
        } else if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        } else if (sourceAddress == null) {
            throw new IllegalArgumentException("Source address cannot be null");
        } else if (bytes == null) {
            throw new IllegalArgumentException("Bytes cannot be null");
        }

        m_systemId = systemId;
        m_location = location;
        m_sourceAddress = sourceAddress;
        m_port = port;
        m_bytes = bytes;

    }

    @XmlAttribute
    public String getSystemId() {
        return m_systemId;
    }

    public void setSystemId(String systemId) {
        m_systemId = systemId;
    }

    @XmlAttribute
    public String getLocation() {
        return m_location;
    }

    public void setLocation(String location) {
        m_location = location;
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getSourceAddress() {
        return m_sourceAddress;
    }

    public void setSourceAddress(InetAddress sourceAddress) {
        m_sourceAddress = sourceAddress;
    }

    @XmlAttribute
    public int getPort() {
        return m_port;
    }

    public void setPort(int port) {
        m_port = port;
    }

    public ByteBuffer getByteBuffer() {
        return m_bytes;
    }

    public void setByteBuffer(ByteBuffer bytes) {
        m_bytes = bytes;
    }

    @XmlAttribute
    public byte[] getBytes() {
        m_bytes.rewind();
        byte[] retval = new byte[m_bytes.remaining()];
        m_bytes.get(retval);
        m_bytes.rewind();
        return retval;
    }

    public void setBytes(byte[] bytes) {
        m_bytes = ByteBuffer.wrap(bytes);
    }

}
