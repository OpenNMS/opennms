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
package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.spring.FileReloadCallback;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import javax.validation.Validation;
import javax.xml.bind.ValidationException;

/**
 * <p>FasterFilesystemForeignSourceRepository class.</p>
 * <p>The directory watcher should keep a cache of all requisitions on disk.</p>
 * <p>The directory watcher will always return the object from the cache, and the cache should be updated when changes are detected on the directories.</p>
 * 
 * <p>The method AbstractForeignSourceRepository.importResourceRequisition will read the requisition and update the copy on disk. This should trigger the update of the cache.</p>
 */
public class FasterFilesystemForeignSourceRepository extends FilesystemForeignSourceRepository implements InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(FasterFilesystemForeignSourceRepository.class);

    /** The foreign sources watcher. */
    private DirectoryWatcher<ForeignSource> m_foreignSources;

    /** The requisitions watcher. */
    private DirectoryWatcher<Requisition> m_requisitions;

    /**
     * Instantiates a new faster filesystem foreign source repository.
     *
     * @throws ForeignSourceRepositoryException the foreign source repository exception
     */
    public FasterFilesystemForeignSourceRepository() throws ForeignSourceRepositoryException {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.AbstractForeignSourceRepository#importResourceRequisition(org.springframework.core.io.Resource)
     */
    @Override
    public Requisition importResourceRequisition(final Resource resource) throws ForeignSourceRepositoryException {
        Assert.notNull(resource);
        try {
            boolean isLocal = true;
            try {
                resource.getFile();
            } catch (Exception e) {
                isLocal = false;
                LOG.debug("importResourceRequisition: resource {} is not local, ignoring cache", resource);
            }
            // Trust whatever is on the cache if exist for local resources only.
            if (isLocal) {
                LOG.debug("importResourceRequisition: saving cached requisition to disk");
                final Requisition req;
                try {
                    req = getRequisitionsDirectoryWatcher().getContents(resource.getFilename());
                    if (req != null) {
                        req.setResource(resource);
                        save(req);
                        return req;
                    }
                } catch(FileNotFoundException e) {
                    LOG.debug(e.getLocalizedMessage());
                }
            }
        } catch (Exception e) {
            LOG.error("importResourceRequisition: can't save cached requisition associated with {}", resource, e);
        }
        // Use the default implementation if the cache doesn't contain the requisition or the requisition comes from an external source.
        return super.importResourceRequisition(resource);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository#getActiveForeignSourceNames()
     */
    @Override
    public Set<String> getActiveForeignSourceNames() {
        m_readLock.lock();
        try {
            final Set<String> activeForeignSourceNames = new TreeSet<>();
            activeForeignSourceNames.addAll(getForeignSourcesDirectoryWatcher().getBaseNamesWithExtension(".xml"));
            activeForeignSourceNames.addAll(getRequisitionsDirectoryWatcher().getBaseNamesWithExtension(".xml"));
            return activeForeignSourceNames;
        } finally {
            m_readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository#getForeignSourceCount()
     */
    @Override
    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            return getForeignSourcesDirectoryWatcher().getBaseNamesWithExtension(".xml").size();
        } finally {
            m_readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository#getForeignSources()
     */
    @Override
    public Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            final Set<ForeignSource> foreignSources = new TreeSet<>();
            for(String baseName : getForeignSourcesDirectoryWatcher().getBaseNamesWithExtension(".xml")) {
                try {
                    ForeignSource contents = getForeignSourcesDirectoryWatcher().getContents(baseName + ".xml");
                    foreignSources.add(contents);
                } catch (FileNotFoundException e) {
                    LOG.info("Unable to load foreignSource {}: It must have been deleted by another thread", baseName, e);
                }
            }
            return foreignSources;
        } finally {
            m_readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository#getForeignSource(java.lang.String)
     */
    @Override
    public ForeignSource getForeignSource(final String foreignSourceName) throws ForeignSourceRepositoryException {
        if (foreignSourceName == null) {
            throw new ForeignSourceRepositoryException("can't get a foreign source with a null name!");
        }
        m_readLock.lock();
        try {
            return getForeignSourcesDirectoryWatcher().getContents(foreignSourceName + ".xml");
        } catch (FileNotFoundException e) {
            final ForeignSource fs = getDefaultForeignSource();
            fs.setName(foreignSourceName);
            return fs;
        } finally {
            m_readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository#getRequisitions()
     */
    @Override
    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            final Set<Requisition> requisitions = new TreeSet<>();
            for(String baseName : getRequisitionsDirectoryWatcher().getBaseNamesWithExtension(".xml")) {
                try {
                    Requisition contents = getRequisitionsDirectoryWatcher().getContents(baseName + ".xml");
                    contents.validate();
                    requisitions.add(contents);
                } catch (FileNotFoundException e) {
                    LOG.info("Unable to load requisition {}: It must have been deleted by another thread", baseName, e);
                } catch (ValidationException e) {
                    LOG.info("Invalid requisition file {}: cannot parse", baseName, e);
                }

            }
            return requisitions;
        } finally {
            m_readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository#getRequisition(java.lang.String)
     */
    @Override
    public Requisition getRequisition(final String foreignSourceName) throws ForeignSourceRepositoryException {
        if (foreignSourceName == null) {
            throw new ForeignSourceRepositoryException("can't get a requisition with a null foreign source name!");
        }
        m_readLock.lock();
        try {
            Requisition req = getRequisitionsDirectoryWatcher().getContents(foreignSourceName + ".xml");
            req.validate();
            return req;
        } catch (FileNotFoundException e) {
            LOG.info("There is no requisition XML file for {} on {}", foreignSourceName, m_requisitionPath);
            return null;
        } catch (ValidationException e) {
            LOG.info("Invalid requisition XML file for {} on {}", foreignSourceName, m_requisitionPath);
            return null;
        } finally {
            m_readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository#getRequisitionURL(java.lang.String)
     */
    @Override
    public URL getRequisitionURL(final String foreignSource) throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            final Requisition requisition = getRequisition(foreignSource);
            if (requisition == null) {
                return null;
            } else {
                return RequisitionFileUtils.getOutputFileForRequisition(m_requisitionPath, requisition).toURI().toURL();
            }
        } catch (final MalformedURLException e) {
            throw new ForeignSourceRepositoryException("an error occurred getting the requisition URL", e);
        } finally {
            m_readLock.unlock();
        }
    }

    /**
     * Gets the foreign sources directory watcher.
     *
     * @return the foreign sources directory watcher
     * @throws ForeignSourceRepositoryException the foreign source repository exception
     */
    public DirectoryWatcher<ForeignSource> getForeignSourcesDirectoryWatcher() throws ForeignSourceRepositoryException {
        if (m_foreignSources == null) {
            try {
                m_foreignSources = new DirectoryWatcher<ForeignSource>(new File(m_foreignSourcePath), fsLoader());
            } catch (InterruptedException e) {
                throw new ForeignSourceRepositoryException("Can't initialize Foreign Sources Directory Watcher for " + m_foreignSourcePath, e);
            }
        }
        return m_foreignSources;
    }

    /**
     * Gets the requisitions directory watcher.
     *
     * @return the requisitions directory watcher
     * @throws ForeignSourceRepositoryException the foreign source repository exception
     */
    public DirectoryWatcher<Requisition> getRequisitionsDirectoryWatcher() throws ForeignSourceRepositoryException {
        if (m_requisitions == null) {
            try {
                m_requisitions = new DirectoryWatcher<Requisition>(new File(m_requisitionPath), reqLoader());
            } catch (InterruptedException e) {
                throw new ForeignSourceRepositoryException("Can't initialize Requisition Directory Watcher for " + m_requisitionPath, e);
            }
        }
        return m_requisitions;
    }

    /**
     * Foreign Sources loader.
     *
     * @return the file reload callback
     */
    private static FileReloadCallback<ForeignSource> fsLoader() {
        return new FileReloadCallback<ForeignSource>() {
            @Override
            public ForeignSource reload(ForeignSource object, Resource resource) throws IOException {
                if (resource == null || resource.getFile() == null) {
                    return object;
                } else {
                    return RequisitionFileUtils.getForeignSourceFromFile(resource.getFile());
                }
            }
        };
    }

    /**
     * Requisitions loader.
     *
     * @return the file reload callback
     */
    private static FileReloadCallback<Requisition> reqLoader() {
        return new FileReloadCallback<Requisition>() {
            @Override
            public Requisition reload(Requisition object, Resource resource) throws IOException {
                if (resource == null || resource.getFile() == null) {
                    return object;
                } else {
                    return RequisitionFileUtils.getRequisitionFromFile(resource.getFile());
                }
            }
        };
    }

}
