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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="child-edge")
@XmlAccessorType(XmlAccessType.NONE)
public class ChildEdgeResponseDTO extends AbstractEdgeResponseDTO {

    @XmlElement(name="child-id")
    private Long childId;

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    public Long getChildId() {
        return childId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        if (!super.equals(obj)) {
            return false;
        }
        // compare subclass fields
        return java.util.Objects.equals(childId, ((ChildEdgeResponseDTO) obj).childId);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + java.util.Objects.hash(childId);
    }
}
