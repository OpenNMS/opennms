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
package org.opennms.web.admin.discovery;

import javax.servlet.http.HttpServletRequest;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GeneralSettingsLoader {

	/** Constant <code>log</code> */
	private static final Logger LOG = LoggerFactory.getLogger(GeneralSettingsLoader.class);
	
	/**
	 * <p>load</p>
	 *
	 * @param request a {@link javax.servlet.http.HttpServletRequest} object.
	 * @param config a {@link org.opennms.netmgt.config.discovery.DiscoveryConfiguration} object.
	 * @return a {@link org.opennms.netmgt.config.discovery.DiscoveryConfiguration} object.
	 */
	public static DiscoveryConfiguration load(HttpServletRequest request, DiscoveryConfiguration config){
		String initSTStr = request.getParameter("initialsleeptime");
		String restartSTStr = request.getParameter("restartsleeptime");
		String foreignSource = request.getParameter("foreignsource");
		String location = request.getParameter("location");
		String retriesStr = request.getParameter("retries");
		String timeoutStr = request.getParameter("timeout");
		String chunksizeStr = request.getParameter("chunksize");
		
		LOG.debug("initialsleeptime: {}", initSTStr);
		LOG.debug("restartsleeptime: {}", restartSTStr);
		LOG.debug("foreignSource: {}", foreignSource);
		LOG.debug("location: {}", location);
		LOG.debug("retries: {}", retriesStr);
		LOG.debug("timeout: {}", timeoutStr);
		LOG.debug("chunksize: {}", chunksizeStr);
		
		
		//set the general settings loaded into current configuration

		try {
			long initSt = WebSecurityUtils.safeParseLong(initSTStr);
			config.setInitialSleepTime(initSt);
		} catch (NumberFormatException e) {
			LOG.debug("Null value in discovery config for initial sleep");
		}
		try {
			long restartSt = WebSecurityUtils.safeParseLong(restartSTStr);
			config.setRestartSleepTime(restartSt);
		} catch (NumberFormatException e) {
			LOG.debug("Null value in discovery config for restart sleep");
		}
		

		// TODO: Validate foreign source value
		if (foreignSource != null && !"".equals(foreignSource.trim())) {
			config.setForeignSource(foreignSource);
		} else {
			config.setForeignSource(null);
		}

		// TODO: Validate location value
		if (location != null && !"".equals(location.trim())) {
			config.setLocation(location);
		} else {
			config.setLocation(null);
		}
		
		if (retriesStr!=null && (!"".equals(retriesStr.trim()) && !String.valueOf(DiscoveryConfigFactory.DEFAULT_RETRIES).equals(retriesStr.trim()))) {
			config.setRetries(WebSecurityUtils.safeParseInt(retriesStr));
		} else {
			config.setRetries(null);
		}
		
		if (timeoutStr!=null && (!"".equals(timeoutStr.trim()) && !String.valueOf(DiscoveryConfigFactory.DEFAULT_TIMEOUT).equals(timeoutStr.trim()))) {
			config.setTimeout(Long.parseLong(timeoutStr));
		} else {
			config.setTimeout(null);
		}
		
		if (chunksizeStr!=null && (!"".equals(chunksizeStr.trim()) && !String.valueOf(DiscoveryConfigFactory.DEFAULT_CHUNK_SIZE).equals(chunksizeStr.trim()))) {
			config.setChunkSize(Integer.parseInt(chunksizeStr));
		} else {
			config.setChunkSize(null);
		}
		
		LOG.debug("General settings uploaded.");
		
		return config;
	}

}
