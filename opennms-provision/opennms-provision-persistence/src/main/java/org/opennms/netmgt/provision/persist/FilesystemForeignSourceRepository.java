package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

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
        Set<String> fsNames = new TreeSet<String>();
        File directory = new File(m_foreignSourcePath);
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".xml")) {
                    fsNames.add(file.getName().replaceAll(".xml$", ""));
                }
            }
        }
        directory = new File(m_requisitionPath);
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".xml")) {
                    fsNames.add(file.getName().replaceAll(".xml$", ""));
                }
            }
        }
        return fsNames;
    }

    /**
     * <p>setUpdateDateStamps</p>
     *
     * @param update a boolean.
     */
    public void setUpdateDateStamps(boolean update) {
        m_updateDateStamps = update;
    }
    
    /**
     * <p>getForeignSourceCount</p>
     *
     * @return a int.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        return getForeignSources().size();
    }
 
    /**
     * <p>getForeignSources</p>
     *
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        File directory = new File(m_foreignSourcePath);
        TreeSet<ForeignSource> foreignSources = new TreeSet<ForeignSource>();
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".xml")) {
                    foreignSources.add(get(file));
                }
            }
        }
        return foreignSources;
    }

    /** {@inheritDoc} */
    public ForeignSource getForeignSource(String foreignSourceName) throws ForeignSourceRepositoryException {
        if (foreignSourceName == null) {
            throw new ForeignSourceRepositoryException("can't get a foreign source with a null name!");
        }
        File inputFile = encodeFileName(m_foreignSourcePath, foreignSourceName);
        if (inputFile != null && inputFile.exists()) {
            return get(inputFile);
        } else {
            ForeignSource fs = getDefaultForeignSource();
            fs.setName(foreignSourceName);
            return fs;
        }
    }

    /** {@inheritDoc} */
    public synchronized void save(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("can't save a null foreign source!");
        }
        if (foreignSource.getName().equals("default")) {
            putDefaultForeignSource(foreignSource);
            return;
        }
        File outputFile = getOutputFileForForeignSource(foreignSource);
        Writer writer = null;
        try {
            if (m_updateDateStamps) {
                foreignSource.updateDateStamp();
            }
            writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
            getMarshaller(ForeignSource.class).marshal(foreignSource, writer);
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /** {@inheritDoc} */
    public void delete(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        File deleteFile = getOutputFileForForeignSource(foreignSource);
        if (deleteFile.exists()) {
            if (!deleteFile.delete()) {
                throw new ForeignSourceRepositoryException("unable to delete foreign source file " + deleteFile);
            }
        }
    }
    
    /**
     * <p>getRequisitions</p>
     *
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        File directory = new File(m_requisitionPath);
        TreeSet<Requisition> requisitions = new TreeSet<Requisition>();
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
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
    }
    
    /** {@inheritDoc} */
    public Requisition getRequisition(String foreignSourceName) throws ForeignSourceRepositoryException {
        if (foreignSourceName == null) {
            throw new ForeignSourceRepositoryException("can't get a requisition with a null foreign source name!");
        }
        File inputFile = encodeFileName(m_requisitionPath, foreignSourceName);
        if (inputFile != null && inputFile.exists()) {
            return getRequisition(inputFile);
        }
        return null;
    }

    /**
     * <p>getRequisition</p>
     *
     * @param foreignSource a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public Requisition getRequisition(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("can't get a requisition with a null foreign source name!");
        }
        return getRequisition(foreignSource.getName());
    }

    /**
     * <p>save</p>
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public synchronized void save(Requisition requisition) throws ForeignSourceRepositoryException {
        if (requisition == null) {
            throw new ForeignSourceRepositoryException("can't save a null requisition!");
        }
        File outputFile = getOutputFileForRequisition(requisition);
        Writer writer = null;
        try {
            if (m_updateDateStamps) {
                requisition.updateDateStamp();
            }
            writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
            getMarshaller(Requisition.class).marshal(requisition, writer);
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * <p>delete</p>
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public void delete(Requisition requisition) throws ForeignSourceRepositoryException {
        if (requisition == null) {
            throw new ForeignSourceRepositoryException("can't delete a null requisition!");
        }
        File deleteFile = getOutputFileForRequisition(requisition);
        if (deleteFile.exists()) {
            if (!deleteFile.delete()) {
                throw new ForeignSourceRepositoryException("unable to delete requisition file " + deleteFile);
            }
        }
    }
    
    /**
     * <p>setRequisitionPath</p>
     *
     * @param path a {@link java.lang.String} object.
     */
    public void setRequisitionPath(String path) {
        m_requisitionPath = path;
    }
    /**
     * <p>setForeignSourcePath</p>
     *
     * @param path a {@link java.lang.String} object.
     */
    public void setForeignSourcePath(String path) {
        m_foreignSourcePath = path;
    }
    
    private synchronized ForeignSource get(File inputFile) throws ForeignSourceRepositoryException {
        try {
            Unmarshaller um = getUnmarshaller(ForeignSource.class);
            JAXBElement<ForeignSource> fs = um.unmarshal(new StreamSource(inputFile), ForeignSource.class);
            return fs.getValue();
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to unmarshal " + inputFile.getPath(), e);
        }
    }

    private synchronized Requisition getRequisition(File inputFile) throws ForeignSourceRepositoryException {
        try {
            Unmarshaller um = getUnmarshaller(Requisition.class);
            JAXBElement<Requisition> req = um.unmarshal(new StreamSource(inputFile), Requisition.class);
            return req.getValue();
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to unmarshal " + inputFile.getPath(), e);
        }
    }

    private void createPath(File fsPath) throws ForeignSourceRepositoryException {
        if (!fsPath.exists()) {
            if (!fsPath.mkdirs()) {
                throw new ForeignSourceRepositoryException("unable to create directory " + fsPath.getPath());
            }
        }
    }

    private File encodeFileName(String path, String foreignSourceName) {
//        return new File(path, java.net.URLEncoder.encode(foreignSourceName, "UTF-8") + ".xml");
          return new File(path, foreignSourceName + ".xml");
    }

    private File getOutputFileForForeignSource(ForeignSource foreignSource) {
        File fsPath = new File(m_foreignSourcePath);
        createPath(fsPath);
        File outputFile = encodeFileName(m_foreignSourcePath, foreignSource.getName());
        return outputFile;
    }

    private File getOutputFileForRequisition(Requisition requisition) {
        File reqPath = new File(m_requisitionPath);
        createPath(reqPath);
        File outputFile = encodeFileName(m_requisitionPath, requisition.getForeignSource());
        return outputFile;
    }

    /** {@inheritDoc} */
    public URL getRequisitionURL(String foreignSource) throws ForeignSourceRepositoryException {
        try {
            return getOutputFileForRequisition(getRequisition(foreignSource)).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new ForeignSourceRepositoryException("an error occurred getting the requisition URL", e);
        }
    }

    private Unmarshaller getUnmarshaller(Class<?> objectType) throws JAXBException {
        return getJaxbContext(objectType).createUnmarshaller();
    }

}
