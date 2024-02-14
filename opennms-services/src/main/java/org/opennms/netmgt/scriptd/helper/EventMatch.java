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
 * An EventMatch is an Interface that 
 * is able to specify criteria to match Events
 * An implementation of this interface is a class
 * where you have some criteria to decide if the Event
 * matches or not
 * 
 * @author antonio
 *
 */
public interface EventMatch {

	/**
	 * Method to decide if the Event matches
	 * 
	 * @param event
	 * @return true if the event match
	 */
	public boolean match(Event event);

}
