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

public abstract class AbstractEventPolicyRule implements EventPolicyRule {

    @Override
	public void addForwardRule(EventMatch match) {
		m_filter.add(match);
		m_forwardes.add(Boolean.TRUE);
	}

        @Override
	public void addDropRule(EventMatch match) {
		m_filter.add(match);
		m_forwardes.add(Boolean.FALSE);
	}

        @Override
	public Event filter(Event event) {
		boolean forward = true;
		int count = 0;
		for (EventMatch filter: m_filter) {
			if (filter.match(event)) {
				forward = m_forwardes.get(count).booleanValue();
				break;
			}
			count++;
		}
		if (forward)
			return expand(event);
		return null;
	}

	protected abstract Event expand(Event event);

}
