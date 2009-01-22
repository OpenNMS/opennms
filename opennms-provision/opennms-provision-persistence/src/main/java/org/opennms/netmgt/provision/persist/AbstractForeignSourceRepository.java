package org.opennms.netmgt.provision.persist;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractForeignSourceRepository implements ForeignSourceRepository {

    public OnmsRequisition importRequisition(Resource resource) throws ForeignSourceRepositoryException {
        Assert.notNull(resource);
        OnmsRequisition r = new OnmsRequisition();
        r.loadResource(resource);
        save(r);
        return r;
    }
    
    public OnmsForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException {
        Resource defaultForeignSource = new ClassPathResource("/default-foreign-source.xml");
        if (!defaultForeignSource.exists()) {
            defaultForeignSource = new ClassPathResource("/org/opennms/netmgt/provision/persist/default-foreign-source.xml");
        }
        try {
            InputStream fsStream = defaultForeignSource.getInputStream();
            JAXBContext context = JAXBContext.newInstance(OnmsForeignSource.class);
            Unmarshaller um = context.createUnmarshaller();
            OnmsForeignSource fs = (OnmsForeignSource) um.unmarshal(fsStream);
            fs.setDefault(true);
            return fs;
        } catch (Exception e) {
            throw new ForeignSourceRepositoryException("unable to access default foreign source resource", e);
        }
    }

    public OnmsNodeRequisition getNodeRequisition(String foreignSource, String foreignId) throws ForeignSourceRepositoryException {
        OnmsRequisition req = getRequisition(foreignSource);
        return (req == null ? null : req.getNodeRequistion(foreignId));
    }
}
