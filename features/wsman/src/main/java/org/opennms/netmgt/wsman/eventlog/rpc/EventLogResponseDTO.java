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
package org.opennms.netmgt.wsman.eventlog.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RpcResponse;

@XmlRootElement(name = "wsman-eventlog-response")
@XmlAccessorType(XmlAccessType.NONE)
public class EventLogResponseDTO implements RpcResponse {

    @XmlAttribute(name = "error")
    private String error;

    @XmlElement(name = "batch")
    private List<EventLogBatchDTO> batches = new ArrayList<>();

    public EventLogResponseDTO() {
    }

    public EventLogResponseDTO(Throwable ex) {
        error = RemoteExecutionException.toErrorMessage(ex);
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    public List<EventLogBatchDTO> getBatches() {
        return batches;
    }

    public void addBatch(EventLogBatchDTO batch) {
        batches.add(batch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, batches);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EventLogResponseDTO)) {
            return false;
        }
        final EventLogResponseDTO other = (EventLogResponseDTO) obj;
        return Objects.equals(error, other.error) && Objects.equals(batches, other.batches);
    }
}
