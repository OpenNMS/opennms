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
	
        /**
         * Number of threads used for consuming/dispatching messages.
         *
         * @return number of threads
         */
        int getNumThreads();
    
        /**
         * Maximum number of messages to keep in memory while waiting to be
         * dispatched.
         *
         * @return queue size
         */
        int getQueueSize();
    
        /**
         * Messages are aggregated in batches before being dispatched. When the
         * batch reaches this size, it will be dispatched.
         *
         * @return batch size
         */
        int getBatchSize();
    
        /**
         * Messages are aggregated in batches before being dispatched. When the
         * batch has been created for longer than this interval it will be
         * dispatched, regardless of the size.
         *
         * @return interval in ms
         */
        int getBatchIntervalMs();
}
