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
package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/* wrapper object to deal with old non-JAXB resources */
public class BasicSchedule {

	private String m_name;
	private String m_type;
	private List<Time> m_times = new ArrayList<>();

	public String getName() {
		return m_name;
	}
	
	public void setName(final String name) {
		m_name = name;
	}

	public String getType() {
		return m_type;
	}

	public void setType(final String type) {
		m_type = type;
	}

	public void setTimeCollection(final Collection<Time> times) {
		synchronized(m_times) {
			if (m_times == times) return;
			m_times.clear();
			m_times.addAll(times);
		}
	}

	public Collection<Time> getTimeCollection() {
		synchronized(m_times) {
			return m_times;
		}
	}
	
	public Enumeration<Time> enumerateTime() {
		synchronized(m_times) {
			return Collections.enumeration(m_times);
		}
	}

	public int getTimeCount() {
		synchronized(m_times) {
			return m_times.size();
		}
	}

	public Time getTime(final int index) {
		synchronized(m_times) {
			return m_times.get(index);
		}
	}
}
