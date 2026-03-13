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
package org.opennms.core.daemon.loader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.ServiceDetectorFactory;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local ServiceDetectorRegistry for standalone daemon containers.
 * Discovers ServiceDetectorFactory implementations via ServiceLoader.
 * Used by both Provisiond and Discovery daemon-loaders.
 */
public class LocalServiceDetectorRegistry implements ServiceDetectorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(LocalServiceDetectorRegistry.class);
    private final Map<String, ServiceDetectorFactory<?>> factoryByClassName = new HashMap<>();

    public LocalServiceDetectorRegistry() {
        ServiceLoader<ServiceDetectorFactory> loader = ServiceLoader.load(ServiceDetectorFactory.class);
        for (ServiceDetectorFactory<?> factory : loader) {
            String className = factory.getDetectorClass().getCanonicalName();
            factoryByClassName.put(className, factory);
            LOG.info("Registered detector factory: {} -> {}", className, factory.getClass().getCanonicalName());
        }
        LOG.info("Loaded {} detector factories via ServiceLoader", factoryByClassName.size());
    }

    @Override
    public Map<String, String> getTypes() {
        Map<String, String> types = new HashMap<>();
        for (Map.Entry<String, ServiceDetectorFactory<?>> entry : factoryByClassName.entrySet()) {
            types.put(entry.getKey(), entry.getValue().getDetectorClass().getCanonicalName());
        }
        return types;
    }

    @Override
    public Set<String> getClassNames() {
        return factoryByClassName.keySet();
    }

    @Override
    public ServiceDetector getDetectorByClassName(String className, Map<String, String> properties) {
        ServiceDetectorFactory<?> factory = factoryByClassName.get(className);
        if (factory != null) {
            return factory.createDetector(properties);
        }
        return null;
    }

    @Override
    public ServiceDetectorFactory<?> getDetectorFactoryByClassName(String className) {
        return factoryByClassName.get(className);
    }

    @Override
    public Set<String> getServiceNames() {
        return Collections.emptySet();
    }

    @Override
    public String getDetectorClassNameFromServiceName(String serviceName) {
        return null;
    }

    @Override
    public Class<?> getDetectorClassByServiceName(String serviceName) {
        return null;
    }
}
