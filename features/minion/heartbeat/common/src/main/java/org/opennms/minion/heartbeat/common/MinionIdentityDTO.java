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

package org.opennms.minion.heartbeat.common;

import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.minion.core.api.MinionIdentity;

@XmlRootElement(name = "minion")
@XmlAccessorType(XmlAccessType.FIELD)
public class MinionIdentityDTO implements Message {

    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "location")
    private String location;
    @XmlElement(name = "timestamp")
    private Date timestamp;

    public MinionIdentityDTO() {

    }

    public MinionIdentityDTO(MinionIdentity minion) {
        id = minion.getId();
        location = minion.getLocation();
        timestamp = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MinionIdentityDTO other = (MinionIdentityDTO) obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.location, other.location)
                && Objects.equals(this.timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("MinionIdentityDTO[id=%s, location=%s, timestamp=%s]", id, location, timestamp);
    }
}
