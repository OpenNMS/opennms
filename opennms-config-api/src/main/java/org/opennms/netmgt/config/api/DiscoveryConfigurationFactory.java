/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;

/**
 * An interface for DiscoveryConfigFactory
 * 
 * @author <a href="ryan@mail1.opennms.com"> Ryan Lambeth </a>
 */
public interface DiscoveryConfigurationFactory {

	DiscoveryConfiguration getConfiguration();

	/**
	 * <p>getURLSpecifics</p>
	 * 
	 * @return a List<IPPollAddress>
	 */
	List<IPPollAddress> getURLSpecifics();
	
	/**
	 * <p>getRange</p>
	 * 
	 * @return a List<IPPollRange>
	 */
	List<IPPollRange> getRanges();
	
	/**
	 * <p>getSpecifics</p>
	 * 
	 * @return a List<IPPollAddress>
	 */
	List<IPPollAddress> getSpecifics();
	
	/**
	 * <p>isExcluded</p>
	 * 
	 * @param an InetAddress
	 * @return a boolean
	 */
	boolean isExcluded(final InetAddress address);
	
	/**
	 * <p>getForeignSource</p>
	 * 
	 * @param an InetAddress
	 * @return a String
	 */
	String getForeignSource(InetAddress address);
	
	/**
	 * <p>getIntraPacketDelay</p>
	 * 
	 * @return a long
	 */
	long getIntraPacketDelay();
	
	/**
	 * <p>getPacketsPerSecond</p>
	 * 
	 * @return a double
	 */
	double getPacketsPerSecond();
	
	/**
	 * <p>getExcludingInterator</p>
	 * 
	 * @param an Iterator<IPPollAddress>
	 * @return an Iterator<IPPollAddress>
	 */
	Iterator<IPPollAddress> getExcludingInterator(final Iterator<IPPollAddress> it);
	
	/**
	 * <p>getConfiguredAddresses</p>
	 *  
	 * @return an Iterable<IPPollAddress>
	 */
	Iterable<IPPollAddress> getConfiguredAddresses();
	
	/**
	 * <p>getRestartSleepTime</p>
	 * 
	 * @return a long
	 */
	long getRestartSleepTime();
	
	/**
	 * <p>getInitialSleepTime</p>
	 * 
	 * @return a long
	 */
	long getInitialSleepTime();
}
