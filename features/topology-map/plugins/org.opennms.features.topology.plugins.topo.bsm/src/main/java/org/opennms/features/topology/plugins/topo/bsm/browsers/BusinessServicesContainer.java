/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.bsm.browsers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.plugins.browsers.OnmsContainerDatasource;
import org.opennms.features.topology.plugins.browsers.OnmsVaadinContainer;
import org.opennms.netmgt.bsm.service.model.BusinessService;

public class BusinessServicesContainer extends OnmsVaadinContainer<BusinessService, Long> {
    private static final long serialVersionUID = 1L;

    public BusinessServicesContainer(BusinessServiceContainerDatasource datasource) {
        super(BusinessService.class, datasource);
    }

    @Override
    protected Long getId(BusinessService bean) {
        return bean == null ? null : bean.getId();
    }

    @Override
    public void verticesUpdated(VerticesUpdateManager.VerticesUpdateEvent event) {

    }

    @Override
    protected List<BusinessService> getItemsForCache(OnmsContainerDatasource<BusinessService, Long> datasource, Page page) {
        // TODO MVR somehow hibernate returns more objects than there are actually. Probably a hashCode(), equals() thing
        // see BSM-104 for more details. We use the following work around to get past that problem for now
        List<BusinessService> itemsForCache = super.getItemsForCache(datasource, page);
        return new ArrayList<>(new HashSet<>(itemsForCache));
    }
}
