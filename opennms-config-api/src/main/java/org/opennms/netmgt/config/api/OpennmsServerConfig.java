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

import java.net.InetAddress;

/**
 * An interface for OpennmsServerConfigManager
 * 
 * @author <a href="ryan@mail1.opennms.com"> Ryan Lambeth </a>
 *
 */
public interface OpennmsServerConfig {

	/**
	 * <p>getServerName</p>
	 * 
	 * @return a String
	 */
	String getServerName();

	/**
	 * <p>getDefaultCriticalPathService</p>
	 * 
	 * @return a String
	 */
	InetAddress getDefaultCriticalPathIp();

	/**
	 * <p>getDefaultCriticalPathService</p>
	 * 
	 * @return a String
	 */
	String getDefaultCriticalPathService();
	
	/**
	 * <p>getDefaultCriticalPathTimeout</p>
	 * 
	 * @return an int
	 */
	int getDefaultCriticalPathTimeout();
	
	/**
	 * <p>getDefaultCriticalPathRetries</p>
	 * 
	 * @return an int
	 */
	int getDefaultCriticalPathRetries();
	
	/**
	 * <p>verifyServer</p>
	 * 
	 * @return a boolean
	 */
	boolean verifyServer();
}
