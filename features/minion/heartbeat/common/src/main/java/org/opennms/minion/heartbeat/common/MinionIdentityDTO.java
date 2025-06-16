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
package org.opennms.minion.heartbeat.common;

import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.distributed.core.api.MinionIdentity;

@XmlRootElement(name = "minion")
@XmlAccessorType(XmlAccessType.FIELD)
public class MinionIdentityDTO implements Message {

    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "location")
    private String location;
    @XmlElement(name = "timestamp")
    private Date timestamp;
    @XmlElement(name = "version")
    private String version;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, timestamp, version);
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
                && Objects.equals(this.timestamp, other.timestamp)
                && Objects.equals(this.version, other.version);
    }

    @Override
    public String toString() {
        return String.format("MinionIdentityDTO[id=%s, location=%s, timestamp=%s, version=%s]", id, location, timestamp, version);
    }
}
