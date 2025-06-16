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

import java.util.Hashtable;
import java.util.Optional;

import org.apache.felix.cm.PersistenceManager;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<PersistenceManager> registration;

    @Override
    public void start(BundleContext context) {

        // Register CmPersistenceManager
        Hashtable<String, Object> config = new Hashtable<>();
        config.put("name", CmPersistenceManager.class.getName());

        LOG.info("Registering service {}.", CmPersistenceManager.class.getSimpleName());
        final ConfigurationManagerService cm = findService(context, ConfigurationManagerService.class);
        CmPersistenceManager persistenceManager = new CmPersistenceManager(cm);
        registration = context.registerService(PersistenceManager.class, persistenceManager, config);
        final ConfigurationAdmin configurationAdmin = findService(context, ConfigurationAdmin.class);
        new CallbackManager().registerCallbacks(configurationAdmin, cm, persistenceManager);

        LOG.info("{} started.", CmPersistenceManager.class.getSimpleName());
    }

    private <T> T findService(BundleContext context, Class<T> clazz) {
        return Optional.ofNullable(context.getServiceReference(clazz))
                .map(context::getService)
                .orElseThrow(() -> new IllegalStateException("Cannot find " + clazz.getName()));
    }

    @Override
    public void stop(BundleContext context) {
        if (registration != null) {
            registration.unregister();
        }
        LOG.info( "{} stopped.", CmPersistenceManager.class.getSimpleName());
    }
}