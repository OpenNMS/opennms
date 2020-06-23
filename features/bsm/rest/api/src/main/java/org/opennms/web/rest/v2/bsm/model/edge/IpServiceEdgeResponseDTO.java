/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.bsm.model.edge;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ip-service-edge")
@XmlAccessorType(XmlAccessType.NONE)
public class IpServiceEdgeResponseDTO extends AbstractEdgeResponseDTO {

    @XmlElement(name="ip-service")
    private IpServiceResponseDTO ipService;

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
