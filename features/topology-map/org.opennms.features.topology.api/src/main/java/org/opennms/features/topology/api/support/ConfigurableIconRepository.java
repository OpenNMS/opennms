/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.support;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.features.topology.api.IconRepository;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class ConfigurableIconRepository implements IconRepository, ManagedService {

	private final AtomicReference<Map<String, String>> m_iconMap;

	public ConfigurableIconRepository() {
		this(Collections.<String, String>emptyMap());
	}

	public ConfigurableIconRepository(Map<String, String> iconMap) {
		 m_iconMap = new AtomicReference<Map<String,String>>(iconMap);
	}
	
	private Map<String, String> iconMap() {
		return m_iconMap.get();
	}
    @Override
    public boolean contains(String type) {
        return iconMap().containsKey(type);
    }
    
    @Override
    public String getIconUrl(String type) {
        return iconMap().get(type);
    }
    
	@Override
	public void updated(Dictionary<String,?> properties) throws ConfigurationException {
		
		while(true) {
			Map<String, String> oldMap = m_iconMap.get();
			
			// create the new map using the old as defaults
			Map<String, String> newMap = new HashMap<String, String>(oldMap);
			for(String key : Collections.list(properties.keys())) {
				String v = ((String)properties.get(key));
				if (v == null || v.trim().isEmpty()) {
					newMap.remove(key);
				} else {
					newMap.put(key, v.trim());
				}
			}

			// only update the newMap if the oldMap is the same one we started with
			// if not then try again with whichever map is there now
			if (m_iconMap.compareAndSet(oldMap, newMap)) {
				return;
			}
		}

	}

}
