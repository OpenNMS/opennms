/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.reporting.availability.svclayer;

import java.util.List;

import org.opennms.reporting.datablock.Node;

/**
 * <p>AvailabilityDataService interface.</p>
 */
public interface AvailabilityDataService {
    
    /**
     * <p>getNodes</p>
     *
     * @param category a {@link org.opennms.netmgt.config.categories.Category} object.
     * @param startTime a long.
     * @param endTime a long.
     * @return a {@link java.util.List} object.
     * @throws org.opennms.reporting.availability.svclayer.AvailabilityDataServiceException if any.
     */
    public List<Node> getNodes(org.opennms.netmgt.config.categories.Category category, long startTime, long endTime) throws AvailabilityDataServiceException;

}
