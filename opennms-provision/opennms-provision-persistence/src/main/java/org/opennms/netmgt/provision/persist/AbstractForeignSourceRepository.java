package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractForeignSourceRepository implements ForeignSourceRepository {
    /**
     * <p>Constructor for AbstractForeignSourceRepository.</p>
     */
    public AbstractForeignSourceRepository() {
    	/* using JAXBUtils now, it should resolve properly
        try {
            m_jaxbContextResolver = new ProvisionPrefixContextResolver();
        } catch (JAXBException e) {
            throw new ForeignSourceRepositoryException("unable to get JAXB context resolver", e);
        }
        */
    }

    /** {@inheritDoc} */
    public Requisition importResourceRequisition(Resource resource) throws ForeignSourceRepositoryException {
        Assert.notNull(resource);
 
        final Requisition requisition = JaxbUtils.unmarshal(Requisition.class, resource);
        save(requisition);
        return requisition;
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
        final ForeignSource fs = JaxbUtils.unmarshal(ForeignSource.class, defaultForeignSource);
        fs.setDefault(true);
        return fs;
    }

    /** {@inheritDoc} */
    public void putDefaultForeignSource(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("foreign source was null");
        }
        foreignSource.setName("default");
        foreignSource.updateDateStamp();
 
        final File outputFile = new File(ConfigFileConstants.getFilePathString() + "default-foreign-source.xml");
        Writer writer = null;
        OutputStream outputStream = null;
        try {
            foreignSource.updateDateStamp();
            outputStream = new FileOutputStream(outputFile);
			writer = new OutputStreamWriter(outputStream, "UTF-8");
            JaxbUtils.marshal(foreignSource, writer);
        } catch (final Throwable e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * <p>resetDefaultForeignSource</p>
     *
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    public void resetDefaultForeignSource() throws ForeignSourceRepositoryException {
    	final File deleteFile = new File(ConfigFileConstants.getFilePathString() + "default-foreign-source.xml");
        if (!deleteFile.exists()) {
            return;
        }
        if (!deleteFile.delete()) {
            LogUtils.warnf(this, "unable to remove %s", deleteFile.getPath());
        }
    }

    /** {@inheritDoc} */
    public OnmsNodeRequisition getNodeRequisition(String foreignSource, String foreignId) throws ForeignSourceRepositoryException {
        Requisition req = getRequisition(foreignSource);
        return (req == null ? null : req.getNodeRequistion(foreignId));
    }
}
