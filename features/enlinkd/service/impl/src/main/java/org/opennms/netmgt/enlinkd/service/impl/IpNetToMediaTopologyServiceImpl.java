/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.impl;

import java.util.Date;

import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.service.api.IpNetToMediaTopologyService;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class IpNetToMediaTopologyServiceImpl implements
        IpNetToMediaTopologyService {

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private IpNetToMediaDao m_ipNetToMediaDao;

    public IpNetToMediaTopologyServiceImpl() {
    }

    @Override
    public void delete(int nodeid) {
        m_ipNetToMediaDao.deleteBySourceNodeId(nodeid);
        m_ipNetToMediaDao.flush();
    }

    @Override
    public void reconcile(int nodeId, Date now) {
        m_ipNetToMediaDao.deleteBySourceNodeIdOlderThen(nodeId, now);
        m_ipNetToMediaDao.flush();
    }

    @Override
    public void store(int nodeId, IpNetToMedia ipnettomedia) {
        if (ipnettomedia == null)
            return;
        saveIpNetToMedia(nodeId, ipnettomedia);
    }

    @Transactional
    protected void saveIpNetToMedia(final int nodeId,
            final IpNetToMedia saveMe) {
        new UpsertTemplate<IpNetToMedia, IpNetToMediaDao>(
                                                          m_transactionManager,
                                                          m_ipNetToMediaDao) {

            @Override
            protected IpNetToMedia query() {
                return m_dao.getByNetAndPhysAddress(saveMe.getNetAddress(),
                                                    saveMe.getPhysAddress());
            }

            @Override
            protected IpNetToMedia doUpdate(IpNetToMedia dbIpNetToMedia) {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setSourceNode(node);
                dbIpNetToMedia.merge(saveMe);
                m_dao.update(dbIpNetToMedia);
                m_dao.flush();
                return dbIpNetToMedia;
            }

            @Override
            protected IpNetToMedia doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setSourceNode(node);
                saveMe.setLastPollTime(saveMe.getCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }
    
    public IpNetToMediaDao getIpNetToMediaDao() {
        return m_ipNetToMediaDao;
    }

    public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
        m_ipNetToMediaDao = ipNetToMediaDao;
    }



}
