/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

/**
 * SurveillanceStatus
 *
 * @author brozow
 */
public interface SurveillanceStatus {

    /**
     * The number of nodes that do not have at least one service up
     */
    Integer getDownEntityCount();

    /**
     * The total number of nodes represented by this status
     */
    Integer getTotalEntityCount();

    /**
     * A string presenting the status of the associated set of nodes
     * Possible values are:
     * <ul>
     * <li>'Normal' representing that there are no ouages for active services on the set of associated nodes<li>
     * <li>'Warning' representing exactly one service from set of all active services on the associated nodes has an outage
     * <li>'Critical' representing that more than one service on the from the set of all active services on the assocuate nodes has an outage
     * </ul> 
     */
    String getStatus();

}
