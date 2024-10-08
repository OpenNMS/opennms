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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import javax.xml.bind.ValidationException;

/**
 * <p>FilesystemForeignSourceRepository class.</p>
 */
public class FilesystemForeignSourceRepository extends AbstractForeignSourceRepository implements InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemForeignSourceRepository.class);
    protected String m_requisitionPath;
    protected String m_foreignSourcePath;
    
    protected final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    protected final Lock m_readLock = m_globalLock.readLock();
    protected final Lock m_writeLock = m_globalLock.writeLock();

    /**
     * <p>Constructor for FilesystemForeignSourceRepository.</p>
     *
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public FilesystemForeignSourceRepository() throws ForeignSourceRepositoryException {
        super();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_requisitionPath, "Requisition path must not be empty.");
        Assert.notNull(m_foreignSourcePath, "Foreign source path must not be empty.");
    }

    /**
     * <p>getActiveForeignSourceNames</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @Override
    public Set<String> getActiveForeignSourceNames() {
        m_readLock.lock();
        try {
            final Set<String> fsNames = new TreeSet<>();
            File directory = new File(m_foreignSourcePath);
            if (directory.exists()) {
                for (final File file : directory.listFiles()) {
                    if (file.getName().endsWith(".xml")) {
                        fsNames.add(file.getName().replaceAll(".xml$", ""));
                    }
                }
            }
            directory = new File(m_requisitionPath);
            if (directory.exists()) {
                for (final File file : directory.listFiles()) {
                    if (file.getName().endsWith(".xml")) {
                        fsNames.add(file.getName().replaceAll(".xml$", ""));
                    }
                }
            }
            return fsNames;
        } finally {
            m_readLock.unlock();
        }
    }

    /**
     * <p>getForeignSourceCount</p>
     *
     * @return a int.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            return getForeignSources().size();
        } finally {
            m_readLock.unlock();
        }
    }
 
    /**
     * <p>getForeignSources</p>
     *
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            final File directory = new File(m_foreignSourcePath);
            final TreeSet<ForeignSource> foreignSources = new TreeSet<>();
            if (directory.exists()) {
                for (final File file : directory.listFiles()) {
                    if (file.getName().endsWith(".xml")) {
                        foreignSources.add(RequisitionFileUtils.getForeignSourceFromFile(file));
                    }
                }
            }
            return foreignSources;
        } finally {
            m_readLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ForeignSource getForeignSource(final String foreignSourceName) throws ForeignSourceRepositoryException {
        if (foreignSourceName == null) {
            throw new ForeignSourceRepositoryException("can't get a foreign source with a null name!");
        }
        m_readLock.lock();
        try {
            final File inputFile = RequisitionFileUtils.encodeFileName(m_foreignSourcePath, foreignSourceName);
            if (inputFile != null && inputFile.exists()) {
                return RequisitionFileUtils.getForeignSourceFromFile(inputFile);
            } else {
                final ForeignSource fs = getDefaultForeignSource();
                fs.setName(foreignSourceName);
                return fs;
            }
        } finally {
            m_readLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void save(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
    	if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("can't save a null foreign source!");
        }

    	LOG.debug("Writing foreign source {} to {}", foreignSource.getName(), m_foreignSourcePath);
    	validate(foreignSource);

        m_writeLock.lock();
        try {
            if (foreignSource.getName().equals("default")) {
                putDefaultForeignSource(foreignSource);
                return;
            }
            final File outputFile = RequisitionFileUtils.getOutputFileForForeignSource(m_foreignSourcePath, foreignSource);
            OutputStream outputStream = null;
            Writer writer = null;
            try {
                outputStream = new FileOutputStream(outputFile);
                writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                JaxbUtils.marshal(foreignSource, writer);
            } catch (final Throwable e) {
                throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
            } finally {
                IOUtils.closeQuietly(writer);
                IOUtils.closeQuietly(outputStream);
            }
        } finally {
            m_writeLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void delete(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_writeLock.lock();
        try {
            LOG.debug("Deleting foreign source {} from {} (if necessary)", foreignSource.getName(), m_foreignSourcePath);
            final File fileToDelete = RequisitionFileUtils.getOutputFileForForeignSource(m_foreignSourcePath, foreignSource);
            if (fileToDelete.exists()) {
                if (!fileToDelete.delete()) {
                    throw new ForeignSourceRepositoryException("unable to delete foreign source file " + fileToDelete);
                }
            }
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /**
     * <p>getRequisitions</p>
     *
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            final File directory = new File(m_requisitionPath);
            final TreeSet<Requisition> requisitions = new TreeSet<>();
            if (directory.exists()) {
                for (final File file : directory.listFiles()) {
                    if (file.getName().endsWith(".xml")) {
                        try {  
                            Requisition req = RequisitionFileUtils.getRequisitionFromFile(file);
                            req.validate();
                            requisitions.add(req);
                        } catch (ForeignSourceRepositoryException e) {
                            // race condition, probably got deleted by the importer as part of moving things
                            // need a better way to handle this; move "pending" to the database?
                        } catch (ValidationException e) {
                            LOG.warn("Invalid requisition file {}", file.getName(), e);
                        }

                    }
                }
            }
            return requisitions;
        } finally {
            m_readLock.unlock();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Requisition getRequisition(final String foreignSourceName) throws ForeignSourceRepositoryException {
        if (foreignSourceName == null) {
            throw new ForeignSourceRepositoryException("can't get a requisition with a null foreign source name!");
        }
        m_readLock.lock();
        try {
            final File inputFile = RequisitionFileUtils.encodeFileName(m_requisitionPath, foreignSourceName);
            Requisition req = null;
            if (inputFile != null && inputFile.exists()) {
                req = RequisitionFileUtils.getRequisitionFromFile(inputFile);
                req.validate();
            }
            return req;
        } catch (ValidationException e) {
            LOG.warn("Invalid requisition file {}/{}", m_requisitionPath, foreignSourceName, e);
            return null;
        } finally {
            m_readLock.unlock();
        }
    }

    /**
     * <p>getRequisition</p>
     *
     * @param foreignSource a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public final Requisition getRequisition(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("can't get a requisition with a null foreign source name!");
        }
        m_readLock.lock();
        try {
            return getRequisition(foreignSource.getName());
        } finally {
            m_readLock.unlock();
        }
    }

    /**
     * <p>save</p>
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public final void save(final Requisition requisition) throws ForeignSourceRepositoryException {
        if (requisition == null) {
            throw new ForeignSourceRepositoryException("can't save a null requisition!");
        }
        
        LOG.debug("Writing requisition {} to {}", requisition.getForeignSource(), m_requisitionPath);
        validate(requisition);

        m_writeLock.lock();
        try {
            final File outputFile = RequisitionFileUtils.getOutputFileForRequisition(m_requisitionPath, requisition);
            Writer writer = null;
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(outputFile);
                writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                JaxbUtils.marshal(requisition, writer);
            } catch (final Throwable e) {
                throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
            } finally {
                IOUtils.closeQuietly(writer);
                IOUtils.closeQuietly(outputStream);
            }
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
     * <p>delete</p>
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public final void delete(final Requisition requisition) throws ForeignSourceRepositoryException {
        if (requisition == null) {
            throw new ForeignSourceRepositoryException("can't delete a null requisition!");
        }
        m_writeLock.lock();
        try {
            LOG.debug("Deleting requisition {} from {} (if necessary)", requisition.getForeignSource(), m_requisitionPath);
            final File fileToDelete = RequisitionFileUtils.getOutputFileForRequisition(m_requisitionPath, requisition);
            if (fileToDelete.exists()) {
                if (!fileToDelete.delete()) {
                    throw new ForeignSourceRepositoryException("Unable to delete requisition file " + fileToDelete);
                }
            } else {
                LOG.debug("File {} does not exist.", fileToDelete);
            }
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /**
     * <p>setRequisitionPath</p>
     *
     * @param path a {@link java.lang.String} object.
     */
    public final void setRequisitionPath(final String path) {
        m_writeLock.lock();
        try {
            m_requisitionPath = path;
        } finally {
            m_writeLock.unlock();
        }
    }
    /**
     * <p>setForeignSourcePath</p>
     *
     * @param path a {@link java.lang.String} object.
     */
    public final void setForeignSourcePath(final String path) {
        m_writeLock.lock();
        try {
            m_foreignSourcePath = path;
        } finally {
            m_writeLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Date getRequisitionDate(final String foreignSource) throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            final Requisition requisition = getRequisition(foreignSource);
            if (requisition == null) {
                return null;
            }
            return requisition.getDate();
        } finally {
            m_readLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public URL getRequisitionURL(final String foreignSource) throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            return RequisitionFileUtils.getOutputFileForRequisition(m_requisitionPath, foreignSource).toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new ForeignSourceRepositoryException("an error occurred getting the requisition URL", e);
        } finally {
            m_readLock.unlock();
        }
    }

    @Override
    public final void flush() throws ForeignSourceRepositoryException {
        // Unnecessary, there is no caching/delayed writes in FilesystemForeignSourceRepository
        LOG.debug("flush() called");
    }
}
