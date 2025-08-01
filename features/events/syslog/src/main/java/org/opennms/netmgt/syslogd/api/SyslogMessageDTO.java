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
package org.opennms.netmgt.syslogd.api;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opennms.core.xml.ByteBufferXmlAdapter;


@XmlRootElement(name = "syslog-message")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogMessageDTO {

    @XmlAttribute(name = "timestamp")
    private Date timestamp;

    @XmlValue
    @XmlJavaTypeAdapter(ByteBufferXmlAdapter.class)
    private ByteBuffer bytes;

    public SyslogMessageDTO() { }

    public SyslogMessageDTO(ByteBuffer bytes) {
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
        if (!(other instanceof SyslogMessageDTO)) {
            return false;
        }
        SyslogMessageDTO castOther = (SyslogMessageDTO) other;
        return Objects.equals(timestamp, castOther.timestamp)
                && Objects.equals(bytes, castOther.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, bytes);
    }
}
