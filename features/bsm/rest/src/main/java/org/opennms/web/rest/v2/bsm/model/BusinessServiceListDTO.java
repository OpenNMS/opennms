/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2.bsm.model;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;
import org.opennms.web.rest.api.JAXBResourceLocationAdapter;
import org.opennms.web.rest.api.ResourceLocation;

@XmlRootElement(name = "business-services")
@JsonRootName("business-services")
public class BusinessServiceListDTO extends JaxbListWrapper<BusinessServiceDTO> {

    private ResourceLocation location;

    private static final long serialVersionUID = 1L;

    public BusinessServiceListDTO() {

    }

    public BusinessServiceListDTO(final Collection<? extends BusinessServiceDTO> services, ResourceLocation location) {
        super(services);
        this.location = location;
    }

    @XmlElement(name = "business-service")
    @JsonProperty("business-service")
    public List<BusinessServiceDTO> getObjects() {
        return super.getObjects();
    }

    @XmlElement(name = "location")
    @XmlJavaTypeAdapter(JAXBResourceLocationAdapter.class)
    public ResourceLocation getLocation() {
        return location;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }
}
