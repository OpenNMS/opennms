package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.config.dao.DefaultLinkAdapterConfigurationDao;
import org.opennms.netmgt.provision.config.linkadapter.LinkPattern;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultLinkMatchResolverImpl implements LinkMatchResolver {
    @Autowired
    private DefaultLinkAdapterConfigurationDao m_configDao;
    
    public String getAssociatedEndPoint(String endPoint) {
        if (m_configDao != null) {
            for (LinkPattern p : m_configDao.getPatterns()) {
                String endPointResolvedTemplate = p.resolveTemplate(endPoint);
                if (endPointResolvedTemplate != null) {
                    return endPointResolvedTemplate;
                }
            }
        }

        return null;
    }
}
