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
package org.opennms.core.rpc.echo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RpcRequest;

import io.opentracing.Span;

@XmlRootElement(name="echo-request")
@XmlAccessorType(XmlAccessType.NONE)
public class EchoRequest implements RpcRequest {

    @XmlAttribute(name="id")
    private Long id;

    @XmlAttribute(name="message")
    private String message;

    @XmlElement(name="body", required=false)
    private String body;

    @XmlAttribute(name="location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlAttribute(name="delay")
    private Long delay;

    @XmlAttribute(name="throw")
    private boolean shouldThrow;

    private Long timeToLiveMs;

    private Map<String, String> tracingInfo = new HashMap<>();

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

    public String getBody() {
        return body;
    }

    /**
     * Set body when there is large message typically >500KB
     * @param body set body
     */
    public void setBody(String body) {
        this.body = body;
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

    @Override
    public Map<String, String> getTracingInfo() {
        return tracingInfo;
    }

    @Override
    public Span getSpan() {
        return null;
    }

    public void addTracingInfo(String key, String value) {
        tracingInfo.put(key, value);
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
        return Objects.hash(id, message, body, location, delay,
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
                Objects.equals(this.body, other.body) &&
                Objects.equals(this.location, other.location) &&
                Objects.equals(this.delay, other.delay) &&
                Objects.equals(this.shouldThrow, other.shouldThrow) &&
                Objects.equals(this.systemId, other.systemId);
    }

    @Override
    public String toString() {
        return String.format("EchoRequest[id=%d, message=%s, body=%s location=%s, systemId=%s, delay=%s, shouldThrow=%s]",
                id, message, body, location, systemId, delay, shouldThrow);
    }
}
