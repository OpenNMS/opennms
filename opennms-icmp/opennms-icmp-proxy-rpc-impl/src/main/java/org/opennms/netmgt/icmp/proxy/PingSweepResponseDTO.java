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
package org.opennms.netmgt.icmp.proxy;

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

@XmlRootElement(name = "ping-sweep-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class PingSweepResponseDTO implements RpcResponse {

    @XmlAttribute(name="error")
    private String error;

    @XmlElement(name = "pinger-result")
    private List<PingSweepResultDTO> pingSweepResult = new ArrayList<>(0);

    public PingSweepResponseDTO() { }

    public PingSweepResponseDTO(Throwable ex) {
        error = RemoteExecutionException.toErrorMessage(ex);
    }

    public List<PingSweepResultDTO> getPingSweepResult() {
        return pingSweepResult;
    }

    public void setPingSweepResult(List<PingSweepResultDTO> pingSweepResult) {
        this.pingSweepResult = pingSweepResult;
    }

    public void addPingSweepResult(PingSweepResultDTO pingSweepResult) {
        this.pingSweepResult.add(pingSweepResult);
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pingSweepResult, error);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PingSweepResponseDTO other = (PingSweepResponseDTO) obj;
        return Objects.equals(this.pingSweepResult, other.pingSweepResult) &&
                Objects.equals(this.error, other.error);
    }

}
