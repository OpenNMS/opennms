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
package org.opennms.netmgt.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * DefaultUpsertService
 *
 * @author brozow
 */
public class DefaultUpsertService implements UpsertService, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultUpsertService.class);
    
    @Autowired
    NodeDao m_nodeDao;

    @Autowired
    SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    PlatformTransactionManager m_transactionManager;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public OnmsSnmpInterface upsert(final int nodeId, final OnmsSnmpInterface snmpInterface, final int sleep) {
        UpsertTemplate<OnmsSnmpInterface, SnmpInterfaceDao> upzerter = new UpsertTemplate<OnmsSnmpInterface, SnmpInterfaceDao>(m_transactionManager, m_snmpInterfaceDao) {
            
            @Override
            public OnmsSnmpInterface query() {
                OnmsSnmpInterface dbSnmpIface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, snmpInterface.getIfIndex());
                sleep(sleep);
                return dbSnmpIface;
            }
            
            @Override
            public OnmsSnmpInterface doUpdate(OnmsSnmpInterface dbSnmpIface) {
                // update the interface that was found
                LOG.debug("nodeId = {}, ifIndex = {}, dbSnmpIface = {}", nodeId, snmpInterface.getIfIndex(), dbSnmpIface);
                dbSnmpIface.mergeSnmpInterfaceAttributes(snmpInterface);
                LOG.info("Updating SnmpInterface {}", dbSnmpIface);
                m_snmpInterfaceDao.update(dbSnmpIface);
                m_snmpInterfaceDao.flush();
                return dbSnmpIface;
            }
            
            @Override
            public OnmsSnmpInterface doInsert() {
                // add the interface to the node, if it wasn't found
                final OnmsNode dbNode = m_nodeDao.get(nodeId);
                // for performance reasons we don't add the snmp interface to the node so we avoid loading all the interfaces
                // setNode only sets the node in the interface
                snmpInterface.setNode(dbNode);
                LOG.info("Saving SnmpInterface {}", snmpInterface);
                m_snmpInterfaceDao.save(snmpInterface);
                m_snmpInterfaceDao.flush();
                return snmpInterface;
            }

        };
        
        return upzerter.execute();
    }

    public void sleep(int sleep) {
        try { Thread.sleep(sleep); } catch (InterruptedException e) { /* ignore for the test */}
    }

}
