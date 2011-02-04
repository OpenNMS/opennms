package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

/**
 * <p>FilesystemForeignSourceRepository class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class FilesystemForeignSourceRepository extends AbstractForeignSourceRepository {
    private String m_requisitionPath;
    private String m_foreignSourcePath;
    private boolean m_updateDateStamps = true;
    
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    /**
     * <p>Constructor for FilesystemForeignSourceRepository.</p>
     *
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public FilesystemForeignSourceRepository() throws ForeignSourceRepositoryException {
        super();
    }

    /**
     * <p>getActiveForeignSourceNames</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getActiveForeignSourceNames() {
        m_readLock.lock();
        try {
            final Set<String> fsNames = new TreeSet<String>();
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
     * <p>setUpdateDateStamps</p>
     *
     * @param update a boolean.
     */
    public void setUpdateDateStamps(final boolean update) {
        m_writeLock.lock();
        try {
            m_updateDateStamps = update;
        } finally {
            m_writeLock.unlock();
        }
    }
    
    /**
     * <p>getForeignSourceCount</p>
     *
     * @return a int.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
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
    public Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            final File directory = new File(m_foreignSourcePath);
            final TreeSet<ForeignSource> foreignSources = new TreeSet<ForeignSource>();
            if (directory.exists()) {
                for (final File file : directory.listFiles()) {
                    if (file.getName().endsWith(".xml")) {
                        foreignSources.add(get(file));
                    }
                }
            }
            return foreignSources;
        } finally {
            m_readLock.unlock();
        }
    }

    /** {@inheritDoc} */
    public ForeignSource getForeignSource(final String foreignSourceName) throws ForeignSourceRepositoryException {
        if (foreignSourceName == null) {
            throw new ForeignSourceRepositoryException("can't get a foreign source with a null name!");
        }
        m_readLock.lock();
        try {
            final File inputFile = encodeFileName(m_foreignSourcePath, foreignSourceName);
            if (inputFile != null && inputFile.exists()) {
                return get(inputFile);
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
    public void save(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("can't save a null foreign source!");
        }
        m_writeLock.lock();
        try {
            if (foreignSource.getName().equals("default")) {
                putDefaultForeignSource(foreignSource);
                return;
            }
            final File outputFile = getOutputFileForForeignSource(foreignSource);
            Writer writer = null;
            try {
                if (m_updateDateStamps) {
                    foreignSource.updateDateStamp();
                }
                writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
                getMarshaller(ForeignSource.class).marshal(foreignSource, writer);
            } catch (final Exception e) {
                throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
            } finally {
                IOUtils.closeQuietly(writer);
            }
        } finally {
            m_writeLock.unlock();
        }
    }

    /** {@inheritDoc} */
    public void delete(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_writeLock.lock();
        try {
            final File deleteFile = getOutputFileForForeignSource(foreignSource);
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
    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            final File directory = new File(m_requisitionPath);
            final TreeSet<Requisition> requisitions = new TreeSet<Requisition>();
            if (directory.exists()) {
                for (final File file : directory.listFiles()) {
                    if (file.getName().endsWith(".xml")) {
                        try {  
                            requisitions.add(getRequisition(file));
                        } catch (ForeignSourceRepositoryException e) {
                            // race condition, probably got deleted by the importer as part of moving things
                            // need a better way to handle this; move "pending" to the database?
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
    public Requisition getRequisition(final String foreignSourceName) throws ForeignSourceRepositoryException {
        if (foreignSourceName == null) {
            throw new ForeignSourceRepositoryException("can't get a requisition with a null foreign source name!");
        }
        m_readLock.lock();
        try {
            final File inputFile = encodeFileName(m_requisitionPath, foreignSourceName);
            if (inputFile != null && inputFile.exists()) {
                return getRequisition(inputFile);
            }
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
    public void save(final Requisition requisition) throws ForeignSourceRepositoryException {
        if (requisition == null) {
            throw new ForeignSourceRepositoryException("can't save a null requisition!");
        }
        m_writeLock.lock();
        try {
            final File outputFile = getOutputFileForRequisition(requisition);
            Writer writer = null;
            try {
                if (m_updateDateStamps) {
                    requisition.updateDateStamp();
                }
                writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
                getMarshaller(Requisition.class).marshal(requisition, writer);
            } catch (final Exception e) {
                throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
            } finally {
                IOUtils.closeQuietly(writer);
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
    public void delete(final Requisition requisition) throws ForeignSourceRepositoryException {
        if (requisition == null) {
            throw new ForeignSourceRepositoryException("can't delete a null requisition!");
        }
        m_writeLock.lock();
        try {
            final File deleteFile = getOutputFileForRequisition(requisition);
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
    public URL getRequisitionURL(final String foreignSource) throws ForeignSourceRepositoryException {
        m_readLock.lock();
        try {
            return getOutputFileForRequisition(getRequisition(foreignSource)).toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new ForeignSourceRepositoryException("an error occurred getting the requisition URL", e);
        } finally {
            m_readLock.unlock();
        }
    }

    private ForeignSource get(final File inputFile) throws ForeignSourceRepositoryException {
        try {
            final Unmarshaller um = getUnmarshaller(ForeignSource.class);
            final JAXBElement<ForeignSource> fs = um.unmarshal(new StreamSource(inputFile), ForeignSource.class);
            return fs.getValue();
        } catch (final JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to unmarshal " + inputFile.getPath(), e);
        }
    }

    private Requisition getRequisition(final File inputFile) throws ForeignSourceRepositoryException {
        try {
            final Unmarshaller um = getUnmarshaller(Requisition.class);
            final JAXBElement<Requisition> req = um.unmarshal(new StreamSource(inputFile), Requisition.class);
            return req.getValue();
        } catch (final Exception e) {
            throw new ForeignSourceRepositoryException("unable to unmarshal " + inputFile.getPath(), e);
        }
    }

    private void createPath(final File fsPath) throws ForeignSourceRepositoryException {
        if (!fsPath.exists()) {
            if (!fsPath.mkdirs()) {
                throw new ForeignSourceRepositoryException("unable to create directory " + fsPath.getPath());
            }
        }
    }

    private File encodeFileName(final String path, final String foreignSourceName) {
//        return new File(path, java.net.URLEncoder.encode(foreignSourceName, "UTF-8") + ".xml");
          return new File(path, foreignSourceName + ".xml");
    }

    private File getOutputFileForForeignSource(final ForeignSource foreignSource) {
        final File fsPath = new File(m_foreignSourcePath);
        createPath(fsPath);
        return encodeFileName(m_foreignSourcePath, foreignSource.getName());
    }

    private File getOutputFileForRequisition(final Requisition requisition) {
        final File reqPath = new File(m_requisitionPath);
        createPath(reqPath);
        return encodeFileName(m_requisitionPath, requisition.getForeignSource());
    }

    private Unmarshaller getUnmarshaller(final Class<?> objectType) throws JAXBException {
        return getJaxbContext(objectType).createUnmarshaller();
    }

}
