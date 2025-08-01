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
package org.opennms.netmgt.provision.detector.client.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.netmgt.provision.DetectResults;

@XmlRootElement(name = "detector-response")
@XmlAccessorType(XmlAccessType.NONE)
public class DetectorResponseDTO implements DetectResults, RpcResponse {

    @XmlAttribute(name = "error")
    private String error;

    @XmlAttribute(name = "detected")
    private boolean detected;

    @XmlElement(name = "attribute")
    private List<DetectorAttributeDTO> attributes = new ArrayList<>();

    public DetectorResponseDTO() {
        // Default constructor for JAXB
    }

    public DetectorResponseDTO(DetectResults results) {
        setDetected(results.isServiceDetected());
        if (results.getServiceAttributes() != null) {
            addAttributes(results.getServiceAttributes());
        }
    }

    public DetectorResponseDTO(Throwable t) {
        setDetected(false);
        error = RemoteExecutionException.toErrorMessage(t);
    }

    public boolean isDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    public List<DetectorAttributeDTO> getAttributes() {
        return attributes;
    }

    public void addAttribute(String key, String value) {
        attributes.add(new DetectorAttributeDTO(key, value));
    }

    public void addAttributes(Map<String, String> attributes) {
        attributes.entrySet().stream()
            .forEach(e -> addAttribute(e.getKey(), e.getValue()));
    }

    public Map<String, String> getAttributesMap() {
        return attributes.stream().collect(Collectors.toMap(DetectorAttributeDTO::getKey,
                DetectorAttributeDTO::getValue));
    }

    @Override
    public boolean isServiceDetected() {
        return detected;
    }

    @Override
    public Map<String, String> getServiceAttributes() {
        return getAttributesMap();
    }

    @Override
    public int hashCode() {
        return Objects.hash(detected, error, attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DetectorResponseDTO other = (DetectorResponseDTO) obj;
        return Objects.equals(this.detected, other.detected) &&
                Objects.equals(this.error, other.error) &&
                Objects.equals(this.attributes, other.attributes);
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

}
