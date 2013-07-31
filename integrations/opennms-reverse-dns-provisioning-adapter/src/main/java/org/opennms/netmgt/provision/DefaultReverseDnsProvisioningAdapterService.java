package org.opennms.netmgt.provision;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

public class DefaultReverseDnsProvisioningAdapterService implements
        ReverseDnsProvisioningAdapterService {

    private NodeDao m_nodeDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private TransactionTemplate m_template;
    
    /**
     * <p>setTemplate</p>
     *
     * @param template a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTemplate(TransactionTemplate template) {
        m_template = template;
    }

    /**
     * <p>getTemplate</p>
     *
     * @return a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public TransactionTemplate getTemplate() {
        return m_template;
    }


    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }
    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }
    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
    /**
     * <p>setNodeDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao dao) {
        m_nodeDao = dao;
    }
    
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_nodeDao, "ReverseDnsProvisioner requires a NodeDao which is not null.");
        Assert.notNull(m_ipInterfaceDao, "ReverseDnsProvisioner requires an IpInterfaceDao which is not null.");
    }
    
    @Override
    public List<ReverseDnsRecord> get(final Integer nodeid) {
        final List<ReverseDnsRecord> records = new ArrayList<ReverseDnsRecord>();
        m_template.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus arg0) {
                for (OnmsIpInterface ipInterface : m_nodeDao.get(nodeid).getIpInterfaces()) {
                    records.add(new ReverseDnsRecord(ipInterface));
                }
            }
        });
        return records;
    }

    @Override
    public void update(Integer nodeid, ReverseDnsRecord rdr) {
        OnmsIpInterface ipInterface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeid, rdr.getIp().getHostAddress());
        if (ipInterface != null) {
            ipInterface.setIpHostName(rdr.getHostname());
            m_ipInterfaceDao.update(ipInterface);
        }
    }

}
