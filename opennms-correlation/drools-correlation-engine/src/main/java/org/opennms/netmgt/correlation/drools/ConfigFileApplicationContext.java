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
package org.opennms.netmgt.correlation.drools;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

public class ConfigFileApplicationContext extends AbstractXmlApplicationContext {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigFileApplicationContext.class);
    
	private Resource m_resource;
    private String m_configFileLocation;
    
    public ConfigFileApplicationContext(Resource basePath, final String configFileLocation, final ApplicationContext parent) {
        super(parent);
        m_resource = basePath;
        m_configFileLocation = configFileLocation;
        LOG.debug("ConfigFileApplicationContext: initializing using basePath={}, configFileLocation={}", m_resource, m_configFileLocation);
        refresh();
    }
    
    @Override
    protected String[] getConfigLocations() {
        if ( m_configFileLocation == null ) {
            return null;
        }
        return new String[] { m_configFileLocation };
    }

    @Override
    protected Resource getResourceByPath(final String path) {
    	try {
    		return m_resource.createRelative(path);
    	} catch(IOException e) {
		LOG.error("Unable to create resource for path {} relative the directory of {}", path, m_resource, e);
    		throw new IllegalArgumentException("Failed to create relative path for " + path);
    	}
    }
    
}
