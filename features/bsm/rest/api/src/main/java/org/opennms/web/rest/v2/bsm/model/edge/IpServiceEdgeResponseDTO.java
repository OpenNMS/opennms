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
package org.opennms.web.rest.v2.bsm.model.edge;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ip-service-edge")
@XmlAccessorType(XmlAccessType.NONE)
@Schema(description = "An edge pointing at a monitored IP service.")
public class IpServiceEdgeResponseDTO extends AbstractEdgeResponseDTO {

    @Schema(description = "The monitored IP service this edge points at.")
    @XmlElement(name="ip-service")
    private IpServiceResponseDTO ipService;

    @Schema(description = "Label shown for this edge in the UI.", example = "web front end")
    @XmlElement(name="friendly-name",required = false)
    private String friendlyName;

    public IpServiceResponseDTO getIpService() {
        return ipService;
    }

    public void setIpService(IpServiceResponseDTO ipService) {
        this.ipService = ipService;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        if (!super.equals(obj)) {
            return false;
        }
        // compare subclass fields
        return Objects.equals(ipService, ((IpServiceEdgeResponseDTO) obj).ipService)
                && Objects.equals(friendlyName, ((IpServiceEdgeResponseDTO) obj).friendlyName);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(ipService, friendlyName);
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
