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
package org.opennms.netmgt.config.tester.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.ConfigFileConstants;

public class ExcludeUeisProperties {
	private String[] m_ueis;

	public String[] getUeis() {
		return m_ueis;
	}

	public void setUeis(String[] ueis) {
		m_ueis = Arrays.copyOf(ueis, ueis.length);
	}

	public ExcludeUeisProperties() throws FileNotFoundException, IOException {
		Properties excludeProperties = new Properties();
		excludeProperties.load( new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.EXCLUDE_UEI_FILE_NAME)));
		m_ueis = BundleLists.parseBundleList(excludeProperties.getProperty("excludes"));
	}
}
