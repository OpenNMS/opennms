package org.opennms.netmgt.provision.persist;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractForeignSourceRepository implements ForeignSourceRepository {

    public Requisition importRequisition(Resource resource) throws ForeignSourceRepositoryException {
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

    public OnmsNodeRequisition getNodeRequisition(String foreignSource, String foreignId) throws ForeignSourceRepositoryException {
        Requisition req = getRequisition(foreignSource);
        return (req == null ? null : req.getNodeRequistion(foreignId));
    }
}
