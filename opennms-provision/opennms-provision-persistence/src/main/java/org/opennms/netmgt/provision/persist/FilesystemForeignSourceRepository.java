package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class FilesystemForeignSourceRepository extends AbstractForeignSourceRepository {
    private String m_requisitionPath;
    private String m_foreignSourcePath;
    private boolean m_updateDateStamps = true;
    
    public FilesystemForeignSourceRepository() throws ForeignSourceRepositoryException {
        super();
    }

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

    public void setUpdateDateStamps(boolean update) {
        m_updateDateStamps = update;
    }
    
    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        return getForeignSources().size();
    }
 
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

    public synchronized void save(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("can't save a null foreign source!");
        }
        File outputFile = getOutputFileForForeignSource(foreignSource);
        FileWriter writer = null;
        try {
            if (m_updateDateStamps) {
                foreignSource.updateDateStamp();
            }
            writer = new FileWriter(outputFile);
            getMarshaller(ForeignSource.class).marshal(foreignSource, writer);
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void delete(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        File deleteFile = getOutputFileForForeignSource(foreignSource);
        if (deleteFile.exists()) {
            if (!deleteFile.delete()) {
                throw new ForeignSourceRepositoryException("unable to delete foreign source file " + deleteFile);
            }
        }
    }
    
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

    public Requisition getRequisition(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("can't get a requisition with a null foreign source name!");
        }
        return getRequisition(foreignSource.getName());
    }

    public synchronized void save(Requisition requisition) throws ForeignSourceRepositoryException {
        if (requisition == null) {
            throw new ForeignSourceRepositoryException("can't save a null requisition!");
        }
        File outputFile = getOutputFileForRequisition(requisition);
        FileWriter writer = null;
        try {
            if (m_updateDateStamps) {
                requisition.updateDateStamp();
            }
            writer = new FileWriter(outputFile);
            getMarshaller(Requisition.class).marshal(requisition, writer);
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void delete(Requisition requisition) throws ForeignSourceRepositoryException {
        File deleteFile = getOutputFileForRequisition(requisition);
        if (deleteFile.exists()) {
            if (!deleteFile.delete()) {
                throw new ForeignSourceRepositoryException("unable to delete requisition file " + deleteFile);
            }
        }
    }
    
    public void setRequisitionPath(String path) {
        m_requisitionPath = path;
    }
    public void setForeignSourcePath(String path) {
        m_foreignSourcePath = path;
    }
    
    private synchronized ForeignSource get(File file) throws ForeignSourceRepositoryException {
        try {
            return getUnmarshaller(ForeignSource.class).unmarshal(new StreamSource(file), ForeignSource.class).getValue();
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to unmarshal " + file.getPath(), e);
        }
    }

    private synchronized Requisition getRequisition(File inputFile) throws ForeignSourceRepositoryException {
        try {
            return getUnmarshaller(Requisition.class).unmarshal(new StreamSource(inputFile), Requisition.class).getValue();
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

    private File encodeFileName(String path, String foreignSourceName) throws ForeignSourceRepositoryException {
//        return new File(path, java.net.URLEncoder.encode(foreignSourceName, "UTF-8") + ".xml");
          return new File(path, foreignSourceName + ".xml");
    }

    private File getOutputFileForForeignSource(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        File fsPath = new File(m_foreignSourcePath);
        createPath(fsPath);
        File outputFile = encodeFileName(m_foreignSourcePath, foreignSource.getName());
        return outputFile;
    }

    private File getOutputFileForRequisition(Requisition requisition) throws ForeignSourceRepositoryException {
        File reqPath = new File(m_requisitionPath);
        createPath(reqPath);
        File outputFile = encodeFileName(m_requisitionPath, requisition.getForeignSource());
        return outputFile;
    }

    public URL getRequisitionURL(String foreignSource) throws ForeignSourceRepositoryException {
        try {
            return getOutputFileForRequisition(getRequisition(foreignSource)).toURL();
        } catch (MalformedURLException e) {
            throw new ForeignSourceRepositoryException("an error occurred getting the requisition URL", e);
        }
    }

    private Unmarshaller getUnmarshaller(Class<?> objectType) throws JAXBException {
        return getJaxbContext(objectType).createUnmarshaller();
    }

}
