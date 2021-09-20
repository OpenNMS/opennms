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
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.service.api.IpNetToMediaTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class IpNetToMediaTopologyServiceImpl implements
        IpNetToMediaTopologyService {

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private final static Logger LOG = LoggerFactory.getLogger(IpNetToMediaTopologyServiceImpl.class);

    private IpNetToMediaDao m_ipNetToMediaDao;
    private IpInterfaceDao m_ipInterfaceDao;    

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
                final OnmsNode sourceNode = new OnmsNode();
                sourceNode.setId(nodeId);
                saveMe.setSourceNode(sourceNode);
                putOnmsPropertyForIpNetToMedia(saveMe);
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
                putOnmsPropertyForIpNetToMedia(saveMe);
                saveMe.setLastPollTime(saveMe.getCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }
    
    
    private void putOnmsPropertyForIpNetToMedia(final IpNetToMedia ipnetToMedia) {

        List<OnmsIpInterface> onmsiplist = m_ipInterfaceDao.findByIpAddress(InetAddressUtils.str(ipnetToMedia.getNetAddress()));
        if (onmsiplist.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No OnmsIpInterface found for {}", ipnetToMedia);
            }
            return;
        }
    
        if (onmsiplist.size() > 1) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found {} OnmsIpInterface for {}", onmsiplist.size(),ipnetToMedia);
            }
            List<Integer> nodeids =onmsiplist.stream().map(onmsip -> onmsip.getNodeId()).collect(Collectors.toList());
            ipnetToMedia.setPort("Multiple Nodes: " + nodeids);
            return;
        }

        OnmsIpInterface onmsip = onmsiplist.iterator().next();
        ipnetToMedia.setNode(onmsip.getNode());
        if (onmsip.getSnmpInterface() == null) {
            ipnetToMedia.setIfIndex(-1);
            return;
        }
        ipnetToMedia.setIfIndex(onmsip.getIfIndex());
        ipnetToMedia.setPort(Topology.getPortTextString(SnmpInterfaceTopologyEntity.create(onmsip.getSnmpInterface())));
    }
    
    public IpNetToMediaDao getIpNetToMediaDao() {
        return m_ipNetToMediaDao;
    }

    public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
        m_ipNetToMediaDao = ipNetToMediaDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

}
