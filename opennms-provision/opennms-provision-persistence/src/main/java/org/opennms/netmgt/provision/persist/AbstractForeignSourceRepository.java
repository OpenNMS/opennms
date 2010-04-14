package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractForeignSourceRepository implements ForeignSourceRepository {
    private final ProvisionPrefixContextResolver m_jaxbContextResolver;

    public AbstractForeignSourceRepository() {
        try {
            m_jaxbContextResolver = new ProvisionPrefixContextResolver();
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to get JAXB context resolver", e);
        }
    }

    public Requisition importResourceRequisition(Resource resource) throws ForeignSourceRepositoryException {
        Assert.notNull(resource);
        try {
            InputStream resourceStream = resource.getInputStream();
            JAXBContext context = JAXBContext.newInstance(Requisition.class);
            Unmarshaller um = context.createUnmarshaller();
            um.setSchema(null);
            Requisition req = (Requisition) um.unmarshal(resourceStream);
            save(req);
            return req;
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to import requisition resource " + resource, e);
        }
    }
    
    public ForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException {
        Resource defaultForeignSource = new ClassPathResource("/default-foreign-source.xml");
        if (!defaultForeignSource.exists()) {
            defaultForeignSource = new ClassPathResource("/org/opennms/netmgt/provision/persist/default-foreign-source.xml");
        }
        try {
            InputStream fsStream = defaultForeignSource.getInputStream();
            JAXBContext context = JAXBContext.newInstance(ForeignSource.class);
            Unmarshaller um = context.createUnmarshaller();
            um.setSchema(null);
            ForeignSource fs = (ForeignSource) um.unmarshal(fsStream);
            fs.setDefault(true);
            return fs;
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to access default foreign source resource", e);
        }
    }

    public void putDefaultForeignSource(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("foreign source was null");
        }
        foreignSource.setName("default");
        foreignSource.updateDateStamp();
        
        File outputFile = new File(ConfigFileConstants.getFilePathString() + "default-foreign-source.xml");
        Writer writer = null;
        try {
            foreignSource.updateDateStamp();
            writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
            getMarshaller(ForeignSource.class).marshal(foreignSource, writer);
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void resetDefaultForeignSource() throws ForeignSourceRepositoryException {
        File deleteFile = new File(ConfigFileConstants.getFilePathString() + "default-foreign-source.xml");
        if (!deleteFile.exists()) {
            return;
        }
        if (!deleteFile.delete()) {
            log().warn("unable to remove " + deleteFile.getPath());
        }
    }

    
    private Category log() {
        return ThreadCategory.getInstance(AbstractForeignSourceRepository.class);
    }

    public OnmsNodeRequisition getNodeRequisition(String foreignSource, String foreignId) throws ForeignSourceRepositoryException {
        Requisition req = getRequisition(foreignSource);
        return (req == null ? null : req.getNodeRequistion(foreignId));
    }
    
    protected synchronized Marshaller getMarshaller(Class<?> clazz) throws JAXBException {
        Marshaller marshaller = m_jaxbContextResolver.getContext(clazz).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }
    
    protected JAXBContext getJaxbContext(Class<?> objectType) {
        return m_jaxbContextResolver.getContext(objectType);
    }
}
