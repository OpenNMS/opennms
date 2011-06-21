/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.dao;

import static org.opennms.core.utils.LogUtils.debugf;
import static org.opennms.core.utils.LogUtils.infof;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * DefaultUpsertService
 *
 * @author brozow
 */
public class DefaultUpsertService implements UpsertService {
    
    @Autowired
    NodeDao m_nodeDao;

    @Autowired
    SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    PlatformTransactionManager m_transactionManager;

    @Override
    public OnmsSnmpInterface upsert(int nodeId, OnmsSnmpInterface snmpInterface, int sleep) {
        OnmsSnmpInterface dbSnmpIface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, snmpInterface.getIfIndex());
        debugf(this, "nodeId = %d, ifIndex = %d, dbSnmpIface = %s", nodeId, snmpInterface.getIfIndex(), dbSnmpIface);
        if (dbSnmpIface != null) {
            return update(snmpInterface, dbSnmpIface);
        } else {
            try {
                return insert(nodeId, snmpInterface);
            } catch (DataIntegrityViolationException e) {
                dbSnmpIface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, snmpInterface.getIfIndex());
                sleep(sleep);
                if (dbSnmpIface != null) {
                    return update(snmpInterface, dbSnmpIface);
                } else {
                    throw e;
                }
            }
        }
    }

    private OnmsSnmpInterface update(OnmsSnmpInterface snmpInterface, OnmsSnmpInterface dbSnmpIface) {
        // update the interface that was found
        dbSnmpIface.mergeSnmpInterfaceAttributes(snmpInterface);
        infof(this, "Updating SnmpInterface %s", dbSnmpIface);
        m_snmpInterfaceDao.update(dbSnmpIface);
        m_snmpInterfaceDao.flush();
        return dbSnmpIface;
    }

    private OnmsSnmpInterface insert(final int nodeId, final OnmsSnmpInterface snmpInterface) {
        TransactionTemplate template = new TransactionTemplate(m_transactionManager, new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        
        return template.execute(new TransactionCallback<OnmsSnmpInterface>() {

            @Override
            public OnmsSnmpInterface doInTransaction(TransactionStatus status) {
                return doInsert(nodeId, snmpInterface);
            }
        });
    }

    private OnmsSnmpInterface doInsert(int nodeId,
            OnmsSnmpInterface snmpInterface) {
        // add the interface to the node, if it wasn't found
        final OnmsNode dbNode = m_nodeDao.get(nodeId);
        // for performance reasons we don't add the snmp interface to the node so we avoid loading all the interfaces
        // setNode only sets the node in the interface
        snmpInterface.setNode(dbNode);
        infof(this, "Saving SnmpInterface %s", snmpInterface);
        m_snmpInterfaceDao.save(snmpInterface);
        m_snmpInterfaceDao.flush();
        return snmpInterface;
    }
    
    public void sleep(int sleep) {
        try { Thread.sleep(sleep); } catch (InterruptedException e) { /* ignore for the test */}
    }

}
