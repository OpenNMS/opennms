/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
