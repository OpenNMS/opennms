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
package org.opennms.netmgt.snmp.proxy.common;

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

@XmlRootElement(name="snmp-response")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpMultiResponseDTO implements RpcResponse {

    @XmlAttribute(name="error")
    private String error;

    @XmlElement(name="response")
    private List<SnmpResponseDTO> responses = new ArrayList<>(0);

    public SnmpMultiResponseDTO() { }

    public SnmpMultiResponseDTO(Throwable ex) {
        this.error = RemoteExecutionException.toErrorMessage(ex);
    }

    public void setResponses(List<SnmpResponseDTO> responses) {
        this.responses = responses;
    }

    public List<SnmpResponseDTO> getResponses() {
        return responses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(responses, error);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SnmpMultiResponseDTO other = (SnmpMultiResponseDTO) obj;
        return Objects.equals(this.responses, other.responses) &&
                Objects.equals(this.error, other.error);
    }

    @Override
    public String getErrorMessage() {
        return error;
    }
}
