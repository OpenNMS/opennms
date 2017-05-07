/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.topology.plugins.topo.asset;

import java.io.IOException;
import java.util.Dictionary;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class MockConfigurationAdmin implements ConfigurationAdmin {
	
	Configuration config= new  MockConfiguration();

	@Override
	public Configuration createFactoryConfiguration(String factoryPid)
			throws IOException {
		return null;
	}

	@Override
	public Configuration createFactoryConfiguration(String factoryPid,
			String location) throws IOException {
		return null;
	}

	@Override
	public Configuration getConfiguration(String pid, String location)
			throws IOException {
		return null;
	}

	@Override
	public Configuration getConfiguration(String pid) throws IOException {
		return config;
	}

	@Override
	public Configuration[] listConfigurations(String filter)
			throws IOException, InvalidSyntaxException {
		return null;
	}
	
	private class MockConfiguration implements Configuration{
		
		Dictionary<String, ?> properties = null;

		@Override
		public String getPid() {
			return "org.opennms.features.topology.plugins.topo.asset";
		}

		@Override
		public Dictionary<String, Object> getProperties() {
			return (Dictionary<String, Object>) properties;
		}

		@Override
		public void update(Dictionary<String, ?> properties) throws IOException {
			this.properties=properties;
		}

		@Override
		public void delete() throws IOException {
		}

		@Override
		public String getFactoryPid() {
			return null;
		}

		@Override
		public void update() throws IOException {
			
		}

		@Override
		public void setBundleLocation(String location) {
			
		}

		@Override
		public String getBundleLocation() {
			return null;
		}

		@Override
		public long getChangeCount() {
			return 0;
		}
		
	}

}
