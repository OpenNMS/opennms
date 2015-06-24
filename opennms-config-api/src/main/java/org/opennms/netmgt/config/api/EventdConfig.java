/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.api;

/**
 * An interface for EventdConfigManager
 * 
 * @author <a href="ryan@mail1.opennms.com"> Ryan Lambeth </a>
 */
public interface EventdConfig {
	
	/**
	 * <p>getTCPIpAddress</p>
	 * 
	 * @return a String
	 */
	String getTCPIpAddress();
	
	/**
	 * <p>getTCPPort</p>
	 * 
	 * @return an int
	 */
	int getTCPPort();
	
	/**
	 * <p>getUDPIpAddress</p>
	 * 
	 * @return a String
	 */
	String getUDPIpAddress();
	
	/**
	 * <p>getUDPPort</p>
	 * 
	 * @return an int
	 */
	int getUDPPort();
	
	/**
	 * <p>getReceivers</p>
	 * 
	 * @return an int
	 */
	int getReceivers();
	
	/**
	 * <p>getQueueLength</p>
	 * 
	 * @return an int
	 */
	int getQueueLength();
	
	/**
	 * <p>getSocketsSoTimeoutRequired</p>
	 * 
	 * @return a String
	 */
	String getSocketSoTimeoutRequired();
	
	/**
	 * <p>getSocketSoTimeoutPeriod</p>
	 * 
	 * @return an int
	 */
	int getSocketSoTimeoutPeriod();
	
	/**
	 * <p>hasSocketSoTimeoutPeriod</p>
	 * 
	 * @return a boolean
	 */
	boolean hasSocketSoTimeoutPeriod();
	
	/**
	 * <p>getGetNextEventID</p>
	 * 
	 * @return a String
	 */
	String getGetNextEventID();
}
