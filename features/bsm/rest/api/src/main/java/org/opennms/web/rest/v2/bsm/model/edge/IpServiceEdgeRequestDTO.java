/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

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
