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
package org.opennms.netmgt.correlation;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;

/**
 * <p>CorrelationEngine interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface CorrelationEngine {
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getName();

	/**
	 * <p>getInterestingEvents</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	List<String> getInterestingEvents();

	/**
	 * <p>correlate</p>
	 *
	 * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	void correlate(Event e);

	/**
	 * <p>tearDown</p>
	 */
	void tearDown();

	/**
         * <p>reloadConfig</p>
         */
	void reloadConfig(boolean persistState);
}
