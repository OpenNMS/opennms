/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    private Integer m_level = 3;

    
    /**
     * <p>setTemplate</p>
     *
     * @param template a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTemplate(TransactionTemplate template) {
        m_template = template;
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
        String levelString = System.getProperty("importer.adapter.dns.reverse.level");
        if (levelString != null) {
        	Integer level = Integer.getInteger(levelString);
        	if (level != null && level.intValue() > 0)
        		m_level=level;
        }
    }
    
    @Override
    public List<ReverseDnsRecord> get(final Integer nodeid) {
        final List<ReverseDnsRecord> records = new ArrayList<ReverseDnsRecord>();
        m_template.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus arg0) {
                for (OnmsIpInterface ipInterface : m_nodeDao.get(nodeid).getIpInterfaces()) {
                    records.add(new ReverseDnsRecord(ipInterface,m_level));
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

	public Integer getLevel() {
		return m_level;
	}

	public void setLevel(Integer level) {
		m_level = level;
	}

}
