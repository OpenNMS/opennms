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
package org.opennms.features.config.osgi.del;

import org.apache.felix.cm.NotCachablePersistenceManager;
import org.apache.felix.cm.PersistenceManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Our own implementation of a PersistenceManager.
 * It delegates to ConfigurationManagerService for OpenNMS bundles
 * It delegates to FilePersistenceManager all other bundles
 * It does lazy loading for the delegates, loading is deferred until they are actually needed.
 * Must be activated in custom.properties: felix.cm.pm=org.opennms.config.osgi.del.CmPersistenceManagerDelegator
 */
public class CmPersistenceManagerDelegator implements NotCachablePersistenceManager {
    private final BundleContext context;

    private final PersistenceManagerHolder cmManager = new PersistenceManagerHolder("org.opennms.features.config.osgi.cm.CmPersistenceManager");
    private final PersistenceManagerHolder fileManager = new PersistenceManagerHolder("org.apache.felix.cm.file.FilePersistenceManager");

    public CmPersistenceManagerDelegator(final BundleContext context) {
        this.context = context;
    }

    public boolean exists(final String pid) {
        return getDelegate(pid).exists(pid);
    }


    public Enumeration getDictionaries() throws IOException {
        List<Dictionary<String, String>> dictionaries = new ArrayList<>();
        ensurePersistenceManagerIsAvailable(this.fileManager);
        dictionaries.addAll(Collections.list(this.fileManager.persistenceManager.getDictionaries()));
        if (this.cmManager.persistenceManager != null) {
            dictionaries.addAll(Collections.list(this.cmManager.persistenceManager.getDictionaries()));
        }
        return Collections.enumeration(dictionaries);
    }


    public Dictionary load(String pid) throws IOException {
        return getDelegate(pid).load(pid);
    }


    public void store(String pid, Dictionary props) throws IOException {
        getDelegate(pid).store(pid, props);
    }


    public void delete(final String pid) throws IOException {
        getDelegate(pid).delete(pid);
    }

    /**
     * Returns either the native FilePersistenceManger or CmPersistenceManager depending on the pid.
     */
    private PersistenceManager getDelegate(final String pid) {
        PersistenceManagerHolder pm = MigratedServices.isMigrated(pid) ? this.cmManager : this.fileManager;
        ensurePersistenceManagerIsAvailable(pm);
        return pm.persistenceManager;
    }

    private void ensurePersistenceManagerIsAvailable(final PersistenceManagerHolder pm) {
        if (pm.persistenceManager != null) {
            return; // persistence manager already loaded
        }
        // there is a chance that we have a race condition where findPersistenceManager() is called multiple times.
        // this shouldn't be critical but saves us from using any kind of sync mechanism
        pm.persistenceManager = findPersistenceManager(pm.className);
    }

    private PersistenceManager findPersistenceManager(String className) {
        try {
            return context.getServiceReferences(PersistenceManager.class, null)
                    .stream()
                    .map(context::getService)
                    .filter(s -> className.equals(s.getClass().getName()))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("Cannot find " + className));
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final static class PersistenceManagerHolder {
        PersistenceManager persistenceManager;
        final String className;

        PersistenceManagerHolder(String className) {
            this.className = className;
        }
    }

}