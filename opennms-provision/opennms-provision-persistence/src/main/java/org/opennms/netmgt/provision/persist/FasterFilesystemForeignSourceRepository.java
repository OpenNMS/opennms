/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.FileReloadCallback;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>FilesystemForeignSourceRepository class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class FasterFilesystemForeignSourceRepository extends AbstractForeignSourceRepository implements InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(FasterFilesystemForeignSourceRepository.class);
    private String m_requisitionPath;
    private String m_foreignSourcePath;
    
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
    private DirectoryWatcher<ForeignSource> m_foreignSources;
    private DirectoryWatcher<Requisition> m_requisitions;
    
    /**
     * <p>Constructor for FilesystemForeignSourceRepository.</p>
     *
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public FasterFilesystemForeignSourceRepository() throws ForeignSourceRepositoryException {
        super();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_requisitionPath, "Requisition path must not be empty.");
        Assert.notNull(m_foreignSourcePath, "Foreign source path must not be empty.");
        
        m_foreignSources = new DirectoryWatcher<ForeignSource>(new File(m_foreignSourcePath), fsLoader());
        m_requisitions = new DirectoryWatcher<Requisition>(new File(m_requisitionPath), reqLoader());
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
        	Set<String> activeForeignSourceNames = new LinkedHashSet<String>();
        	activeForeignSourceNames.addAll(m_foreignSources.getBaseNamesWithExtension(".xml"));
        	activeForeignSourceNames.addAll(m_requisitions.getBaseNamesWithExtension(".xml"));
        	return activeForeignSourceNames;
        	
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
        	return m_foreignSources.getBaseNamesWithExtension(".xml").size();
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
        	Set<ForeignSource> foreignSources = new LinkedHashSet<ForeignSource>();
        	for(String baseName : m_foreignSources.getBaseNamesWithExtension(".xml")) {
				try {
	        		ForeignSource contents = m_foreignSources.getContents(baseName+".xml");
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

    /** {@inheritDoc} */
    @Override
    public ForeignSource getForeignSource(final String foreignSourceName) throws ForeignSourceRepositoryException {
        if (foreignSourceName == null) {
            throw new ForeignSourceRepositoryException("can't get a foreign source with a null name!");
        }
        m_readLock.lock();
        try {
        	return m_foreignSources.getContents(foreignSourceName+".xml");
        } catch (FileNotFoundException e) {
            final ForeignSource fs = getDefaultForeignSource();
            fs.setName(foreignSourceName);
            return fs;
		} finally {
            m_readLock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void save(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
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
                writer = new OutputStreamWriter(outputStream, "UTF-8");
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
    public void delete(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_writeLock.lock();
        try {
            LOG.debug("Deleting foreign source {} from {} (if necessary)", foreignSource.getName(), m_foreignSourcePath);
            final File deleteFile = RequisitionFileUtils.getOutputFileForForeignSource(m_foreignSourcePath, foreignSource);
            if (deleteFile.exists()) {
                if (!deleteFile.delete()) {
                    throw new ForeignSourceRepositoryException("unable to delete foreign source file " + deleteFile);
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
        	Set<Requisition> requisitions = new LinkedHashSet<Requisition>();
        	for(String baseName : m_requisitions.getBaseNamesWithExtension(".xml")) {
				try {
	        		Requisition contents = m_requisitions.getContents(baseName+".xml");
					requisitions.add(contents);
				} catch (FileNotFoundException e) {
					LOG.info("Unable to load requisition {}: It must have been deleted by another thread", baseName, e);
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
        	return m_requisitions.getContents(foreignSourceName+".xml");
        } catch (FileNotFoundException e) {
        	throw new ForeignSourceRepositoryException("Requisition: " + foreignSourceName + " does not exist.", e);
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
    public Requisition getRequisition(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
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
    public void save(final Requisition requisition) throws ForeignSourceRepositoryException {
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
                writer = new OutputStreamWriter(outputStream, "UTF-8");
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
    public void delete(final Requisition requisition) throws ForeignSourceRepositoryException {
        if (requisition == null) {
            throw new ForeignSourceRepositoryException("can't delete a null requisition!");
        }
        m_writeLock.lock();
        try {
            LOG.debug("Deleting requisition {} from {} (if necessary)", requisition.getForeignSource(), m_requisitionPath);
            final File deleteFile = RequisitionFileUtils.getOutputFileForRequisition(m_requisitionPath, requisition);
            if (deleteFile.exists()) {
                if (!deleteFile.delete()) {
                    throw new ForeignSourceRepositoryException("unable to delete requisition file " + deleteFile);
                }
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
    public void setRequisitionPath(final String path) {
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
    public void setForeignSourcePath(final String path) {
        m_writeLock.lock();
        try {
            m_foreignSourcePath = path;
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Date getRequisitionDate(final String foreignSource) throws ForeignSourceRepositoryException {
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
            final Requisition requisition = getRequisition(foreignSource);
            if (requisition == null) {
                return null;
            }
            return RequisitionFileUtils.getOutputFileForRequisition(m_requisitionPath, requisition).toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new ForeignSourceRepositoryException("an error occurred getting the requisition URL", e);
        } finally {
            m_readLock.unlock();
        }
    }

    @Override
    public void flush() throws ForeignSourceRepositoryException {
        // Unnecessary, there is no caching/delayed writes in FilesystemForeignSourceRepository
        LOG.debug("flush() called");
    }
    
    private FileReloadCallback<ForeignSource> fsLoader() {
    	return new FileReloadCallback<ForeignSource>() {

			@Override
			public ForeignSource reload(ForeignSource object, Resource resource) throws IOException {
				return RequisitionFileUtils.getForeignSourceFromFile(resource.getFile());
			}
		};
    };
    
	private FileReloadCallback<Requisition> reqLoader() {
		return new FileReloadCallback<Requisition>() {

			@Override
			public Requisition reload(Requisition object, Resource resource) throws IOException {
				return RequisitionFileUtils.getRequisitionFromFile(resource.getFile());
			}
		};
	}
    
}
