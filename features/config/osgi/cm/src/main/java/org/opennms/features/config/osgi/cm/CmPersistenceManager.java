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
package org.opennms.features.config.osgi.cm;

import static org.opennms.features.config.osgi.cm.CmIdentifierUtil.pidToCmIdentifier;
import static org.osgi.framework.Constants.SERVICE_PID;
import static org.osgi.service.cm.ConfigurationAdmin.SERVICE_BUNDLELOCATION;
import static org.osgi.service.cm.ConfigurationAdmin.SERVICE_FACTORYPID;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.felix.cm.PersistenceManager;
import org.opennms.features.config.osgi.del.MigratedServices;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Our own implementation of a PersistenceManager, using the CM system instead of files.
 * It is responsible for providing configurations to Osgi.
 */
public class CmPersistenceManager implements PersistenceManager {

    private static final Logger LOG = LoggerFactory.getLogger(CmPersistenceManager.class);
    /** Properties that are relevant for the inner workings of osgi but shouldn't be exposed to cm.
     * They will be filtered our while writing to cm.
     */
    public static final Set<String> OSGI_PROPERTIES = Set.of(
            ":org.apache.felix.configadmin.revision:",
            SERVICE_BUNDLELOCATION,
            SERVICE_FACTORYPID,
            SERVICE_PID
    );

    private final ConfigurationManagerService configService;

    // This is some ugly business here.
    // For factory managed services the org.apache.felix.cm.impl.ConfigurationManager creates its own unique pid suffix:
    // org.apache.felix.cm.impl.ConfigurationManager.createPid(String factoryPid)
    // This doesn't match our config id, therefore we need this mapping.
    // This applies only for instances created during the current runtime, thus it is sufficient to do the mapping in memory.
    // Instances which are created at startup (existed already in the database) have the correct config id assigned.
    private final Map<String, String> osgiPidToCmPid = new ConcurrentHashMap<>();
    private final Map<String, String> cmPidToOsgiPid = new ConcurrentHashMap<>();

    public CmPersistenceManager(final ConfigurationManagerService configService) {
        this.configService = configService;
    }

    @Override
    public boolean exists(final String pid) {
        return MigratedServices.isMigrated(pid)
                && loadInternal(pid).isPresent();
    }

    @Override
    public Enumeration getDictionaries() {

        Set<String> pids = new HashSet<>();
        for(String configName : MigratedServices.PIDS_MULTI_INSTANCE) {
            Set<String> configIds = this.configService.getConfigIds(configName);
            configIds
                    .stream()
                    .map(id -> configName + "-" + id)
                    .forEach(pids::add);
        }
        pids.addAll(MigratedServices.PIDS_SINGLE_INSTANCE);

        List<Dictionary<String, Object>> dictionaries = pids.stream()
                .map(this::loadInternal)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        return Collections.enumeration(dictionaries);
    }

    @Override
    public Dictionary load(String pid) {
        return loadInternal(pid)
                .orElse(new Hashtable());
    }

    private Optional<Dictionary<String, Object>> loadInternal(String osgiPid) {
        Objects.requireNonNull(osgiPid);
        String cmPid = getCmPid(osgiPid);
        ConfigUpdateInfo identifier = pidToCmIdentifier(cmPid);
        return configService.getJSONStrConfiguration(identifier).map(s -> {
            Dictionary d = DictionaryUtil.createFromJson(new JsonAsString(s));
            if (d.get(SERVICE_PID) == null) {
                d.put(SERVICE_PID, osgiPid); // make sure pid is set, otherwise we will run into a Nullpointer later
            }
            // set factoryPid for multi instance services
            if (MigratedServices.isMultiInstanceService(cmPid) && d.get(SERVICE_FACTORYPID) == null) {
                d.put(SERVICE_FACTORYPID, identifier.getConfigName());
            }
            return d;
        });
    }

    @Override
    public void store(String osgiPid, Dictionary props) throws IOException {
        Optional<Dictionary<String, Object>> confFromConfigService = loadInternal(osgiPid);
        if (confFromConfigService.isEmpty() || !equalsWithoutOsgiProperties(props, confFromConfigService.get())) {
            ConfigUpdateInfo identifier = pidToCmIdentifier(getCmPid(osgiPid));
            configService.updateConfiguration(identifier.getConfigName(), identifier.getConfigId(),
                    new JsonAsString(DictionaryUtil.writeToJson(props).toString()), false);
        }
    }

    @Override
    public void delete(final String pid) throws IOException {
        LOG.warn("Deletion of pid={} not supported.", pid);
    }

    void setPidMapping(String osgiPid, String cmPid) {
        this.osgiPidToCmPid.put(osgiPid, cmPid);
        this.cmPidToOsgiPid.put(cmPid, osgiPid);
    }

    String getCmPid(String osgiPid) {
        return this.osgiPidToCmPid.getOrDefault(osgiPid, osgiPid);
    }

    String getOsgiPid(ConfigUpdateInfo key) {
        String cmPid = CmIdentifierUtil.cmIdentifierToPid(key);
        return this.cmPidToOsgiPid.getOrDefault(cmPid, cmPid);
    }


    public static boolean equalsWithoutOsgiProperties(Dictionary<String, Object> a, Dictionary<String, Object> b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }

        return Collections.list(a.keys())
                .stream()
                .filter(key -> !OSGI_PROPERTIES.contains(key)) // remove all osgi specific from comparison since we don't save them anyway
                .allMatch(key -> a.get(key).equals(b.get(key)));
    }
}