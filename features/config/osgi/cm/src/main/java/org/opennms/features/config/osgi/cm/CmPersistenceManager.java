/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.config.osgi.cm;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.felix.cm.PersistenceManager;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;

/**
 * Our own implementation of a PersistenceManager (subclass of FilePersistenceManager).
 * Must be activated in custom.properties: felix.cm.pm=org.opennms.config.osgi.CmPersistenceManager
 */
public class CmPersistenceManager implements PersistenceManager {

    private interface OsgiProperties {
        String SERVICE_PID = "service.pid";
    }
    private final static String CONFIG_ID = "default"; // TODO: Patrick deal with services with multiple configurations

    private final ConfigurationManagerService configService;

    public CmPersistenceManager(final ConfigurationManagerService configService) {
        this.configService = configService;
    }

    @Override
    public boolean exists(final String pid) {
        return loadInternal(pid).isPresent();
    }

    @Override
    public Enumeration getDictionaries() {
        List<Dictionary<String, Object>> dictionaries = MigratedServices.PIDS.stream()
                .map(this::loadInternal)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return Collections.enumeration(dictionaries);
    }

    @Override
    public Dictionary load(String pid) {
        return loadInternal(pid)
                .orElse(new Hashtable());
    }

    private Optional<Dictionary<String, Object>> loadInternal(String pid) {
        try {
            return configService.getJSONStrConfiguration(pid, CONFIG_ID)
                    .map(s -> new JsonAsString(s))
                    .map(DictionaryUtil::createFromJson)
                    .map(m -> {
                        if(m.get(OsgiProperties.SERVICE_PID) == null) {
                            m.put(OsgiProperties.SERVICE_PID, pid); // make sure pid is set otherwise we will run into a Nullpointer later
                        }
                        return m;
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void store(String pid, Dictionary props) throws IOException {
        Optional<Dictionary<String, Object>> confFromConfigService = loadInternal(pid);
        if(confFromConfigService.isEmpty() || !equalsWithoutRevision(props, confFromConfigService.get())) {
            configService.updateConfiguration(pid, CONFIG_ID, new JsonAsString(DictionaryUtil.writeToJson(props).toString()));
        }
    }

    @Override
    public void delete(final String pid) throws IOException {
        this.configService.unregisterConfiguration(pid, CONFIG_ID); // TODO: Patrick do we want to allow delete?
    }

    public static boolean equalsWithoutRevision(Dictionary<String, Object> a, Dictionary<String, Object> b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else if (a.size() != b.size()) {
            return false;
        }

        return Collections.list(a.keys())
                .stream()
                .filter(key -> !":org.apache.felix.configadmin.revision:".equals(key))
                .allMatch(key -> a.get(key).equals(b.get(key)));
    }
}