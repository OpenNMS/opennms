package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;


public class FilesystemForeignSourceRepository extends AbstractForeignSourceRepository {
    private String m_requisitionPath;
    private String m_foreignSourcePath;
    private final JAXBContext m_jaxbContext;
    private Marshaller m_marshaller;
    private Unmarshaller m_unMarshaller;

    public FilesystemForeignSourceRepository() throws ForeignSourceRepositoryException {
        try {
            m_jaxbContext = JAXBContext.newInstance(ForeignSource.class, Requisition.class);
            m_marshaller = m_jaxbContext.createMarshaller();
            m_marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m_unMarshaller = m_jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to create JAXB context", e);
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to get schema", e);
        }
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

    public void save(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("can't save a null foreign source!");
        }
        File outputFile = getOutputFileForForeignSource(foreignSource);
        FileWriter writer = null;
        try {
            foreignSource.updateDateStamp();
            writer = new FileWriter(outputFile);
            m_marshaller.marshal(foreignSource, writer);
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
                    requisitions.add(getRequisition(file));
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

    public void save(Requisition requisition) throws ForeignSourceRepositoryException {
        if (requisition == null) {
            throw new ForeignSourceRepositoryException("can't save a null requisition!");
        }
        File outputFile = getOutputFileForRequisition(requisition);
        FileWriter writer = null;
        try {
            requisition.updateDateStamp();
            writer = new FileWriter(outputFile);
            m_marshaller.marshal(requisition, writer);
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
    
    private ForeignSource get(File file) throws ForeignSourceRepositoryException {
        try {
            return m_unMarshaller.unmarshal(new StreamSource(file), ForeignSource.class).getValue();
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to unmarshal " + file.getPath(), e);
        }
    }

    private Requisition getRequisition(File inputFile) throws ForeignSourceRepositoryException {
        try {
            return m_unMarshaller.unmarshal(new StreamSource(inputFile), Requisition.class).getValue();
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

}
