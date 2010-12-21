/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
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
