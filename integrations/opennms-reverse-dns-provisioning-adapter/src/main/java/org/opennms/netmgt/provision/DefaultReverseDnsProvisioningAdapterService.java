/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision;

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.sysprops.SystemProperties;
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
        	Integer level = SystemProperties.getInteger(levelString);
        	if (level != null && level.intValue() > 0)
        		m_level=level;
        }
    }
    
    @Override
    public List<ReverseDnsRecord> get(final Integer nodeid) {
        final List<ReverseDnsRecord> records = new ArrayList<>();
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
