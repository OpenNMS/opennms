/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config.api;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.discovery.Detector;
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
	 * @param address @{@link InetAddress}
     * @param location a String
     * @return a boolean
	 */
	boolean isExcluded(final InetAddress address, String location);
	
	/**
	 * <p>getForeignSource</p>
	 * 
	 * @param address InetAddress
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
	Iterator<IPPollAddress> getExcludingIterator(final Iterator<IPPollAddress> it);
	
	/**
	 * <p>getConfiguredAddresses</p>
	 *  
	 * @return an Iterable<IPPollAddress>
	 */
	Iterable<IPPollAddress> getConfiguredAddresses();


	List<Detector> getListOfDetectors(InetAddress inetAddress, String location);
	
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
