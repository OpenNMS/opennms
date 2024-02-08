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
package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

/**
 * Interface to forward events.
 * 
 * @author antonio
 *
 */

public interface EventForwarder {
	
	/**
	 * Method to add a policy rule
	 * to match event to be forwarded or dropped
	 * @param filter
	 */
	public void setEventPolicyRule(EventPolicyRule filter);	

	/**
	 * 
	 * Method used to flush Event
	 * 
	 * @param event
	 * 
	 */
	 public void flushEvent(Event event);

	/**
	 * 
	 * Method used to flush Sync Event
	 * 
	 * @param event
	 * 
	 */
	 public void flushSyncEvent(Event event);

	 /**
	  * This method should be invoked before
	  * flushing sync events
	  * The class implementation should
	  * send the "startSync" event
	  * in the preferred format
	  * 
	  */
	 public void sendStartSync();
	 
	 /**
	  * This method should be invoked after
	  * flushing sync events.
	  * The class implementation should 
	  * send the "endSync" event
	  * in the preferred format
	  * 
	  */
	 public void sendEndSync();
}
