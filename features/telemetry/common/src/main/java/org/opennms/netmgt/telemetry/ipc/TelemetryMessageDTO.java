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

import org.opennms.core.xml.ByteBufferXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;

@XmlRootElement(name = "telemetry-message")
@XmlAccessorType(XmlAccessType.NONE)
public class TelemetryMessageDTO {

    @XmlAttribute(name = "timestamp")
    private Date timestamp;

    @XmlValue
    @XmlJavaTypeAdapter(ByteBufferXmlAdapter.class)
    private ByteBuffer bytes;

    public TelemetryMessageDTO() { }

    public TelemetryMessageDTO(ByteBuffer bytes) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelemetryMessageDTO that = (TelemetryMessageDTO) o;
        return Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, bytes);
    }

    @Override
    public String toString() {
        return "TelemetryMessageDTO{" +
                "timestamp=" + timestamp +
                ", bytes=" + bytes +
                '}';
    }
}
