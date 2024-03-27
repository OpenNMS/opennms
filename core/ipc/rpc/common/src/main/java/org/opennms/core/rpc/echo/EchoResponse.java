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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RpcResponse;

@XmlRootElement(name="echo-response")
@XmlAccessorType(XmlAccessType.NONE)
public class EchoResponse implements RpcResponse {

    @XmlAttribute(name="id")
    private Long id;

    @XmlAttribute(name="error")
    private String error;

    @XmlAttribute(name="message")
    private String message;

    @XmlElement(name="body", required=false)
    private String body;

    public EchoResponse() { }

    public EchoResponse(String message) {
        this.message = message;
    }

    public EchoResponse(Throwable t) {
        this.error = RemoteExecutionException.toErrorMessage(t);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, error);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final EchoResponse other = (EchoResponse) obj;
        return Objects.equals(this.id, other.id) &&
                Objects.equals(this.message, other.message) &&
                Objects.equals(this.body, other.body) &&
                Objects.equals(this.error, other.error);
    }

    @Override
    public String toString() {
        return String.format("EchoResponse[id=%d, message=%s, body=%s error=%s]",
                id, body, message, error);
    }
}
