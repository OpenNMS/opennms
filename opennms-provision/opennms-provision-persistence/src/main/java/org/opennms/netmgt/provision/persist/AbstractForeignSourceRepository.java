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
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>Abstract AbstractForeignSourceRepository class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractForeignSourceRepository implements ForeignSourceRepository {
    private final ProvisionPrefixContextResolver m_jaxbContextResolver;

    /**
     * <p>Constructor for AbstractForeignSourceRepository.</p>
     */
    public AbstractForeignSourceRepository() {
        try {
            m_jaxbContextResolver = new ProvisionPrefixContextResolver();
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to get JAXB context resolver", e);
        }
    }

    /** {@inheritDoc} */
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
    
    /**
     * <p>getDefaultForeignSource</p>
     *
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
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

    /** {@inheritDoc} */
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

    /**
     * <p>resetDefaultForeignSource</p>
     *
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public void resetDefaultForeignSource() throws ForeignSourceRepositoryException {
        File deleteFile = new File(ConfigFileConstants.getFilePathString() + "default-foreign-source.xml");
        if (!deleteFile.exists()) {
            return;
        }
        if (!deleteFile.delete()) {
            log().warn("unable to remove " + deleteFile.getPath());
        }
    }

    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(AbstractForeignSourceRepository.class);
    }

    /** {@inheritDoc} */
    public OnmsNodeRequisition getNodeRequisition(String foreignSource, String foreignId) throws ForeignSourceRepositoryException {
        Requisition req = getRequisition(foreignSource);
        return (req == null ? null : req.getNodeRequistion(foreignId));
    }
    
    /**
     * <p>getMarshaller</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a {@link javax.xml.bind.Marshaller} object.
     * @throws javax.xml.bind.JAXBException if any.
     */
    protected synchronized Marshaller getMarshaller(Class<?> clazz) throws JAXBException {
        Marshaller marshaller = m_jaxbContextResolver.getContext(clazz).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }
    
    /**
     * <p>getJaxbContext</p>
     *
     * @param objectType a {@link java.lang.Class} object.
     * @return a {@link javax.xml.bind.JAXBContext} object.
     */
    protected JAXBContext getJaxbContext(Class<?> objectType) {
        return m_jaxbContextResolver.getContext(objectType);
    }
}
