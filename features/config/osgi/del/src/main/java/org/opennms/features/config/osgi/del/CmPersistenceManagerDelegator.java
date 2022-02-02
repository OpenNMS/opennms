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