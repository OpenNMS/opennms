/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.echo;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RpcRequest;

@XmlRootElement(name="echo-request")
@XmlAccessorType(XmlAccessType.NONE)
public class EchoRequest implements RpcRequest {

    @XmlAttribute(name="id")
    private Long id;

    @XmlAttribute(name="message")
    private String message;

    @XmlAttribute(name="location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlAttribute(name="delay")
    private Long delay;

    @XmlAttribute(name="throw")
    private boolean shouldThrow;

    private Long timeToLiveMs;

    public EchoRequest() { }

    public EchoRequest(String message) {
        this.message = message;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    public void setTimeToLiveMs(Long timeToLiveMs) {
        this.timeToLiveMs = timeToLiveMs;
    }

    @Override
    public Long getTimeToLiveMs() {
        return timeToLiveMs;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Long getDelay() {
        return delay;
    }

    public void shouldThrow(boolean shouldThrow) {
        this.shouldThrow = shouldThrow;
    }

    public boolean shouldThrow() {
        return shouldThrow;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, location, delay,
                shouldThrow, systemId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final EchoRequest other = (EchoRequest) obj;
        return Objects.equals(this.id, other.id) &&
                Objects.equals(this.message, other.message) &&
                Objects.equals(this.location, other.location) &&
                Objects.equals(this.delay, other.delay) &&
                Objects.equals(this.shouldThrow, other.shouldThrow) &&
                Objects.equals(this.systemId, other.systemId);
    }

    @Override
    public String toString() {
        return String.format("EchoRequest[id=%d, message=%s, location=%s, systemId=%s, delay=%s, shouldThrow=%s]",
                id, message, location, systemId, delay, shouldThrow);
    }
}
