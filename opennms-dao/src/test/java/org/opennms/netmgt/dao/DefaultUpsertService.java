/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.BeanUtils;
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
        BeanUtils.assertAutowiring(this);
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
