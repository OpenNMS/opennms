package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.core.io.FileSystemResource;


public class FilesystemForeignSourceRepository extends AbstractForeignSourceRepository {
    private String m_requisitionPath;
    private String m_foreignSourcePath;
    private final JAXBContext m_jaxbContext;
    private Marshaller m_marshaller;
    private Unmarshaller m_unMarshaller;

    public FilesystemForeignSourceRepository() throws ForeignSourceRepositoryException {
        try {
            m_jaxbContext = JAXBContext.newInstance(OnmsForeignSource.class, OnmsRequisition.class);
            m_marshaller = m_jaxbContext.createMarshaller();
            m_marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m_unMarshaller = m_jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to create JAXB context", e);
        }
    }

    private OnmsForeignSource get(File file) throws ForeignSourceRepositoryException {
        try {
            return (OnmsForeignSource) m_unMarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to unmarshal " + file.getPath(), e);
        }
    }

    public OnmsForeignSource get(String foreignSourceName) throws ForeignSourceRepositoryException {
        File inputFile = encodeFileName(m_foreignSourcePath, foreignSourceName);
        if (inputFile != null && inputFile.exists()) {
            return get(inputFile);
        } else {
            OnmsForeignSource fs = getDefaultForeignSource();
            fs.setName(foreignSourceName);
            return fs;
        }
    }

    public Set<OnmsForeignSource> getAll() throws ForeignSourceRepositoryException {
        File directory = new File(m_foreignSourcePath);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        };
        TreeSet<OnmsForeignSource> foreignSources = new TreeSet<OnmsForeignSource>();
        for (File file : directory.listFiles(filter)) {
            foreignSources.add(get(file));
        }
        return foreignSources;
    }

    public OnmsRequisition getRequisition(String foreignSourceName) throws ForeignSourceRepositoryException {
        File inputFile = encodeFileName(m_requisitionPath, foreignSourceName);
        try {
            OnmsRequisition req = new OnmsRequisition();
            req.loadResource(new FileSystemResource(inputFile));
            return req;
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to unmarshal " + inputFile.getPath(), e);
        }
    }

    public OnmsRequisition getRequisition(OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        return getRequisition(foreignSource.getName());
    }

    public void save(OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        File outputFile = getOutputFileForForeignSource(foreignSource);
        try {
            m_marshaller.marshal(foreignSource, outputFile);
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        }
    }

    public void save(OnmsRequisition requisition) throws ForeignSourceRepositoryException {
        File outputFile = getOutputFileForRequisition(requisition);
        try {
            requisition.saveResource(new FileSystemResource(outputFile));
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        }
    }

    public void setRequisitionPath(String path) {
        m_requisitionPath = path;
    }
    public void setForeignSourcePath(String path) {
        m_foreignSourcePath = path;
    }
    
    private void createPath(File fsPath) throws ForeignSourceRepositoryException {
        if (!fsPath.exists()) {
            if (!fsPath.mkdirs()) {
                throw new ForeignSourceRepositoryException("unable to create directory " + fsPath.getPath());
            }
        }
    }

    private File encodeFileName(String path, String foreignSourceName) throws ForeignSourceRepositoryException {
//        try {
//            return new File(path, java.net.URLEncoder.encode(foreignSourceName, "UTF-8") + ".xml");
          return new File(path, foreignSourceName + ".xml");
//        } catch (UnsupportedEncodingException e) {
//            throw new ForeignSourceRepositoryException("unable to encode file name for " + foreignSourceName, e);
//        }
    }

    private File getOutputFileForForeignSource(OnmsForeignSource foreignSource) throws ForeignSourceRepositoryException {
        File fsPath = new File(m_foreignSourcePath);
        createPath(fsPath);
        File outputFile = encodeFileName(m_foreignSourcePath, foreignSource.getName());
        return outputFile;
    }

    private File getOutputFileForRequisition(OnmsRequisition requisition) throws ForeignSourceRepositoryException {
        File reqPath = new File(m_requisitionPath);
        createPath(reqPath);
        File outputFile = encodeFileName(m_requisitionPath, requisition.getForeignSource());
        return outputFile;
    }

}
