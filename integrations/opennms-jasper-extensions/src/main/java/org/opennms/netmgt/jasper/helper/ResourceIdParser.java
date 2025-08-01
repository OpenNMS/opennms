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
package org.opennms.netmgt.jasper.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceIdParser {
	
	Pattern m_nodePattern;
	Pattern m_resourcePattern;
	
	public ResourceIdParser() {
		m_nodePattern = Pattern.compile("node\\W(\\d.*?)\\W");
		m_resourcePattern = Pattern.compile("responseTime\\W(.*)\\W");
	}
	
	public String getNodeId(String resourceId) {
		return getMatch(m_nodePattern.matcher(resourceId));
	}

	public String getResource(String resourceId) {
		return getMatch(m_resourcePattern.matcher(resourceId));
	}
	
	private String getMatch(Matcher m) {
		m.find();
		return m.group(1);
	}
}
