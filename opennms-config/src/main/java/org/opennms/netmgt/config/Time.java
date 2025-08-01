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

import java.util.Optional;

/* wrapper object to deal with old XML resources */
public class Time {

	private String m_id;
	private String m_day;
	private String m_begins;
	private String m_ends;

	public Time() {
	}
	
	public Time(final String id, final String day, final String begins, final String ends) {
		m_id = id;
		m_day = day;
		m_begins = begins;
		m_ends = ends;
	}

	public Optional<String> getId() {
		return Optional.ofNullable(m_id);
	}
	
	public void setId(final String id) {
		m_id = id;
	}
	
	public Optional<String> getDay() {
		return Optional.ofNullable(m_day);
	}

	public void setDay(final String day) {
		m_day = day;
	}

	public String getBegins() {
		return m_begins;
	}
	
	public void setBegins(final String begins) {
		m_begins = begins;
	}
	
	public String getEnds() {
		return m_ends;
	}
	
	public void setEnds(final String ends) {
		m_ends = ends;
	}
}
