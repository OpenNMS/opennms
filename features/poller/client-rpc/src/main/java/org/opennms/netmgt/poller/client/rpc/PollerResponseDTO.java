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
package org.opennms.netmgt.poller.client.rpc;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerResponse;

@XmlRootElement(name = "poller-response")
@XmlAccessorType(XmlAccessType.NONE)
public class PollerResponseDTO implements RpcResponse, PollerResponse {

    @XmlAttribute(name="error")
    private String error;

    @XmlElement(name = "poll-status")
    private PollStatus pollStatus;

    public PollerResponseDTO() {
        // no-arg constructor for JAXB
    }

    public PollerResponseDTO(Throwable ex) {
        this.error = RemoteExecutionException.toErrorMessage(ex);
    }

    public PollerResponseDTO(PollStatus pollStatus) {
        this.pollStatus = pollStatus;
    }

    @Override
    public PollStatus getPollStatus() {
        return pollStatus;
    }

    public void setPollStatus(PollStatus pollStatus) {
        this.pollStatus = pollStatus;
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pollStatus, error);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PollerResponseDTO other = (PollerResponseDTO) obj;
        return Objects.equals(this.pollStatus, other.pollStatus) &&
                Objects.equals(this.error, other.error);
    }

}
