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

package org.opennms.netmgt.bsm.service.internal;

import java.util.Collection;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.BusinessServiceHierarchy;

/**
 * Helper object to wrap any number of Business Service objects.
 * In this case the hierarchy level is set and the root elements can be determined.
 * It is kind of a "Business Service Graph" object.
 */
class BusinessServiceHierarchyImpl implements BusinessServiceHierarchy {

    // the Root Business Services
    private final Set<BusinessService> roots;

    BusinessServiceHierarchyImpl(Collection<BusinessService> allBusinessServices) {
        roots = BusinessServiceHierarchyUtils.getRoots(allBusinessServices);
        BusinessServiceHierarchyUtils.updateHierarchyLevel(roots);
    }

    @Override
    public Set<BusinessService> getRoots() {
        return roots;
    }

}
