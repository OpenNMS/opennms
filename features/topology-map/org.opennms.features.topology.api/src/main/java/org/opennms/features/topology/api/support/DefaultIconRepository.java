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
package org.opennms.features.topology.api.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import org.opennms.features.topology.api.ConfigurableIconRepository;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class DefaultIconRepository implements ConfigurableIconRepository, ManagedService {

	private final Map<String, String> m_iconMap = Maps.newHashMap();

    @Override
    public boolean contains(String iconKey) {
        // These elements are in the m_iconMap, but should not map to any iconKey, as the mapping does not
        // point to an icon id
        if (Constants.SERVICE_PID.equals(iconKey) || "felix.fileinstall.filename".equals(iconKey)) {
            return false;
        }
        return m_iconMap.containsKey(iconKey);
    }
    
    @Override
    public String getSVGIconId(String iconKey) {
        return m_iconMap.get(iconKey);
    }

    @Override
    public void addIconMapping(String iconKey, String iconId) {
        m_iconMap.put(iconKey, iconId);
    }

    @Override
    public void removeIconMapping(String iconKey) {
        m_iconMap.remove(iconKey);
    }

    @Override
    public void save() {
        final String opennmsHomeStr = System.getProperty("opennms.home", "");
        final String propertiesFilename = m_iconMap.get(Constants.SERVICE_PID) + ".cfg";
        final File configFile = Paths.get(opennmsHomeStr, "etc", propertiesFilename).toFile();
        try {
            Properties properties = new Properties();
            properties.putAll(m_iconMap);
            properties.remove(Constants.SERVICE_PID);
            properties.remove("felix.fileinstall.filename");
            properties.store(new FileOutputStream(configFile), "");
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Could not write config file {}", configFile, e);
        }
    }

    @Override
	public void updated(Dictionary<String,?> properties) throws ConfigurationException {
        if (properties != null && !properties.equals(m_iconMap)) {
            m_iconMap.clear();
            for (String key : Collections.list(properties.keys())) {
                String v = ((String) properties.get(key));
                m_iconMap.put(key, v.trim());
            }
        }
	}
}
