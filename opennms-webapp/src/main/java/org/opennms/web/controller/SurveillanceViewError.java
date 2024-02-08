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
package org.opennms.web.controller;

/**
 * <p>SurveillanceViewError class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SurveillanceViewError {
	
	private String m_shortDescr;
	private String m_longDescr;

	/**
	 * <p>getShortDescr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getShortDescr() {
		return m_shortDescr;
	}

	/**
	 * <p>setShortDescr</p>
	 *
	 * @param shortDescr a {@link java.lang.String} object.
	 */
	public void setShortDescr(String shortDescr) {
		m_shortDescr = shortDescr;
	}
	
	/**
	 * <p>getLongDescr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLongDescr() {
		return m_longDescr;
	}

	/**
	 * <p>setLongDescr</p>
	 *
	 * @param longDescr a {@link java.lang.String} object.
	 */
	public void setLongDescr(String longDescr) {
		m_longDescr = longDescr;
	}
	
}
