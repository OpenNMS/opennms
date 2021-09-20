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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service;

import java.util.List;

import org.opennms.netmgt.bsm.service.model.BusinessService;

/**
 * Criteria for searching for business services
 *
 * @author Christian Pape <christian@opennms.org>
 */
public interface BusinessServiceSearchCriteria {
    /**
     * This will apply the criteria represented by an instance of this interface to a list of business services and will
     * return a subset of these business services.
     * @param businessServiceManager the business service manager (required to gather the operational status)
     * @param businessServices the list of business services
     * @return a subset of business services
     */
    List<BusinessService> apply(BusinessServiceManager businessServiceManager, List<BusinessService> businessServices);
}
