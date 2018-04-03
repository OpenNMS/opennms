/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * Abstract DAO used to merge the contents of .xml files stored in a particular directory.
 *
 * @author jwhite
 * @param <K> JAXB class
 * @param <V> Configuration object that is stored in memory (might be the same
 *            as the JAXB class or could be a different class)
 */
public abstract class AbstractMergingJaxbConfigDao<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMergingJaxbConfigDao.class);

    private static final long DEFAULT_RELOAD_CHECK_INTERVAL = 5000;

    // Configuration
    private final Class<K> m_entityClass;
    private final String m_description;
    private Path m_opennmsHome;
    private Path m_rootFile;
    private Path m_includeFolder;
    private Long m_reloadCheckInterval = DEFAULT_RELOAD_CHECK_INTERVAL;

    // State
    private long m_lastUpdate = 0;
    private long m_lastReloadCheck = 0;
    private List<File> m_xmlFiles = null;
    private final Map<File, JaxbConfigDao> m_configDaosByPath = new HashMap<>();
    private V m_object = null;

    public AbstractMergingJaxbConfigDao(final Class<K> entityClass, final String description,
            final Path includeFolder) {
        this(entityClass, description, null, includeFolder);
    }

    public AbstractMergingJaxbConfigDao(final Class<K> entityClass, final String description,
            final Path rootFile, final Path includeFolder) {
        m_entityClass = Objects.requireNonNull(entityClass, "entityClass argument");
        m_description = Objects.requireNonNull(description, "description argument");
        m_rootFile = rootFile;
        m_includeFolder = Objects.requireNonNull(includeFolder, "includeFolder argument");
        m_opennmsHome = Paths.get(ConfigFileConstants.getHome());
    }

    public void setOpennmsHome(Path opennmsHome) {
        m_opennmsHome = opennmsHome;
    }

    public Path getOpennmsHome() {
        return m_opennmsHome;
    }

    public abstract V translateConfig(K config);

    public abstract V mergeConfigs(V source, V target);

    private synchronized void checkForUpdates() {
        if (m_reloadCheckInterval < 0 || System.currentTimeMillis() < (m_lastReloadCheck + m_reloadCheckInterval)) {
            return;
        }
        m_lastReloadCheck = System.currentTimeMillis();

        // Check if any files have been added or removed
        final List<File> updatedListOfXmlFiles = getXmlFiles();
        if (m_xmlFiles == null || !m_xmlFiles.equals(updatedListOfXmlFiles)) {
            m_xmlFiles = updatedListOfXmlFiles;
            // The set of files we need to track changed, reconfigure the DAOs
            reconfigureDaos();
        }

        // Determine the most recent time at which one of the configuration files was updated
        long mostRecentUpdate = 1;
        final List<V> objects = new ArrayList<>();
        for (JaxbConfigDao dao : m_configDaosByPath.values()) {
            // Grab the object, triggering an updated if necessary
            objects.add(dao.getContainer().getObject());
            // Keep track of the most recent update
            mostRecentUpdate = Math.max(mostRecentUpdate, dao.getContainer().getLastUpdate()); 
        }

        // Rebuild the merged configuration if required
        if (mostRecentUpdate > m_lastUpdate) {
            V mergedObject = null;
            for (V object : objects) {
                mergedObject = mergeConfigs(object, mergedObject);
            }
            m_object = mergedObject;
            m_lastUpdate = System.currentTimeMillis();
            onConfigUpdated(m_object);
        }
    }

    /**
     * Called when the configuration updated was updated.
     */
    public void onConfigUpdated(V object) {
        // Do nothing by default, allows subclasses to hook in.
    }

    private void reconfigureDaos() {
        final Set<File> xmlFilesWithUnusedDaos = new HashSet<>();
        xmlFilesWithUnusedDaos.addAll(m_configDaosByPath.keySet());
        for (File xmlFile : m_xmlFiles) {
            // Try to fetch an existing DAO
            JaxbConfigDao dao = m_configDaosByPath.get(xmlFile);
            if (dao == null) {
                // We need to create one
                FileSystemResource fs = new FileSystemResource(xmlFile);
                dao = new JaxbConfigDao();
                dao.setConfigResource(fs);
                dao.afterPropertiesSet();
                m_configDaosByPath.put(xmlFile, dao);
            } else {
                xmlFilesWithUnusedDaos.remove(xmlFile);
            }
        }

        // Remove any DAOs we don't need anymore
        for (File fileWithUnusedDao : xmlFilesWithUnusedDaos) {
            m_configDaosByPath.remove(fileWithUnusedDao);
        }
    }

    public V getObject() {
        checkForUpdates();
        return m_object;
    }

    private List<File> getXmlFiles() {
        final List<File> xmlFiles = new LinkedList<>();
        try (Stream<Path> stream = Files.walk(m_opennmsHome.resolve(m_includeFolder), 1)) {
            stream.map(Path::toFile)
                .filter(File::isFile)
                .filter(File::canRead)
                .filter(f -> f.getName().endsWith(".xml"))
                .sorted()
                .forEach(xmlFiles::add);
        } catch (IOException e) {
            LOG.error("Failed to walk {} for {} ({})", m_includeFolder, m_entityClass, m_description);
        }
        LOG.debug("Found {} files in {}: {}", xmlFiles.size(), m_includeFolder, xmlFiles);

        if (m_rootFile != null) {
            // Prepend the root file, ensure that it is always first in the list
            xmlFiles.add(0, m_opennmsHome.resolve(m_rootFile).toFile()); 
        }
        return xmlFiles;
    }

    public Date getLastUpdate() {
        return new Date(m_lastUpdate);
    }

    public Long getReloadCheckInterval() {
        return m_reloadCheckInterval;
    }

    public void setReloadCheckInterval(final Long reloadCheckInterval) {
        m_reloadCheckInterval = reloadCheckInterval;
        synchronized (m_configDaosByPath) {
            if (m_reloadCheckInterval != null && m_configDaosByPath != null) {
                m_configDaosByPath.values().stream().forEach(c -> c.setReloadCheckInterval(m_reloadCheckInterval));
            }
        }
    }

    private class JaxbConfigDao extends AbstractJaxbConfigDao<K, V> {
        public JaxbConfigDao() {
            super(m_entityClass, m_description);
        }

        @Override
        protected V translateConfig(K config) {
            return AbstractMergingJaxbConfigDao.this.translateConfig(config);
        }
    }
}
