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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ip-service-edge")
public class IpServiceEdgeRequestDTO extends AbstractEdgeRequestDTO {

    private Integer ipServiceId;

    private String friendlyName;

    @XmlElement(name="ip-service-id",required = true)
    public Integer getIpServiceId() {
        return ipServiceId;
    }

    public void setIpServiceId(Integer ipServiceId) {
        this.ipServiceId = ipServiceId;
    }

    @XmlElement(name="friendly-name",required = false)
    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (!(obj instanceof IpServiceEdgeRequestDTO)) { return false; }
        if (!super.equals(obj)) {
            return false;
        }
        // compare subclass fields
        return java.util.Objects.equals(ipServiceId, ((IpServiceEdgeRequestDTO) obj).ipServiceId);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + java.util.Objects.hash(ipServiceId);
    }

    @Override
    public void accept(EdgeRequestDTOVisitor visitor) {
        visitor.visit(this);
    }
}
