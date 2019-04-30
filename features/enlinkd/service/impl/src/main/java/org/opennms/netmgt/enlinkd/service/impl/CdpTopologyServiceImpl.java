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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.CdpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpLinkDao;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.CompositeKey;
import org.opennms.netmgt.enlinkd.service.api.TopologyConnection;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class CdpTopologyServiceImpl extends TopologyServiceImpl implements CdpTopologyService {

    private static final Logger LOG = LoggerFactory.getLogger(CdpTopologyServiceImpl.class);

    @Autowired
    private PlatformTransactionManager m_transactionManager;
    
    private CdpLinkDao m_cdpLinkDao;
    private CdpElementDao m_cdpElementDao;
    
    public CdpTopologyServiceImpl() {
    }

    @Override
    public void delete(int nodeid) {
        m_cdpElementDao.deleteByNodeId(nodeid);
        m_cdpLinkDao.deleteByNodeId(nodeid);
        m_cdpElementDao.flush();
        m_cdpLinkDao.flush();        
    }

    @Override
    public void reconcile(int nodeId, Date now) {
        CdpElement element = m_cdpElementDao.findByNodeId(nodeId);
        if (element != null
                && element.getCdpNodeLastPollTime().getTime() < now.getTime()) {
            m_cdpElementDao.delete(element);
            m_cdpElementDao.flush();
        }
        m_cdpLinkDao.deleteByNodeIdOlderThen(nodeId, now);
        m_cdpLinkDao.flush();
    }

    @Override
    @Transactional
    public void store(int nodeId, CdpElement element) {
        if (element == null)
            return;

        CdpElement dbelement = m_cdpElementDao.findByNodeId(nodeId);
        
        if (dbelement != null) {
            dbelement.merge(element);
            m_cdpElementDao.saveOrUpdate(dbelement);
            m_cdpElementDao.flush();
            return;
        } 
        
        OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        element.setNode(node);
        element.setCdpNodeLastPollTime(element.getCdpNodeCreateTime());
        m_cdpElementDao.saveOrUpdate(element);
        m_cdpElementDao.flush();
        updatesAvailable();

    }

    @Override
    public void store(int nodeId, CdpLink link) {
        if (link == null)
            return;
        saveCdpLink(nodeId, link);
        updatesAvailable();
    }
    
    @Transactional
    protected void saveCdpLink(final int nodeId, final CdpLink saveMe) {
        new UpsertTemplate<CdpLink, CdpLinkDao>(m_transactionManager,
                                                m_cdpLinkDao) {

            @Override
            protected CdpLink query() {
                return m_dao.get(nodeId, saveMe.getCdpCacheIfIndex(),
                                 saveMe.getCdpCacheDeviceIndex());
            }

            @Override
            protected CdpLink doUpdate(CdpLink dbCdpLink) {
                dbCdpLink.merge(saveMe);
                m_dao.update(dbCdpLink);
                m_dao.flush();
                return dbCdpLink;
            }

            @Override
            protected CdpLink doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setNode(node);
                saveMe.setCdpLinkLastPollTime(saveMe.getCdpLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    public List<TopologyConnection<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> match() {

        final Collection<CdpElementTopologyEntity> cdpElements = getTopologyEntityCache().getCdpElementTopologyEntities();
        final List<CdpLinkTopologyEntity> allLinks = getTopologyEntityCache().getCdpLinkTopologyEntities();
        // 1. create lookup maps:
        Map<Integer, CdpElementTopologyEntity> cdpelementmap = new HashMap<Integer, CdpElementTopologyEntity>();
        for (CdpElementTopologyEntity cdpelement: cdpElements) {
            cdpelementmap.put(cdpelement.getNodeId(), cdpelement);
        }
        Map<CompositeKey, CdpLinkTopologyEntity> targetLinkMap = new HashMap<>();
        for (CdpLinkTopologyEntity targetLink : allLinks) {
            CompositeKey key = new CompositeKey(targetLink.getCdpCacheDevicePort(),
                    targetLink.getCdpInterfaceName(),
                    cdpelementmap.get(targetLink.getNodeId()).getCdpGlobalDeviceId(),
                    targetLink.getCdpCacheDeviceId());
            targetLinkMap.put(key, targetLink);
        }
        Set<Integer> parsed = new HashSet<Integer>();

        // 2. iterate
        List<TopologyConnection<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> results = new ArrayList<>();
        for (CdpLinkTopologyEntity sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getCdpLinks: source: {} ", sourceLink);
            }
            CdpElementTopologyEntity sourceCdpElement = cdpelementmap.get(sourceLink.getNodeId());

            CdpLinkTopologyEntity targetLink = targetLinkMap.get(new CompositeKey(sourceLink.getCdpInterfaceName(),
                    sourceLink.getCdpCacheDevicePort(),
                    sourceLink.getCdpCacheDeviceId(),
                    sourceCdpElement.getCdpGlobalDeviceId()));

            if (targetLink == null) {
                LOG.debug("getCdpLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }

            if (sourceLink.getId().equals(targetLink.getId()) || parsed.contains(targetLink.getId())) {
                continue;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("getCdpLinks: cdp: {}, target: {} ", sourceLink.getCdpCacheDevicePort(), targetLink);
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            results.add(TopologyConnection.of(sourceLink, targetLink));
        }
        return results;
    }


    @Override
    public List<CdpElementTopologyEntity> findAllCdpElements() {
        return getTopologyEntityCache().getCdpElementTopologyEntities();
    }

    public CdpLinkDao getCdpLinkDao() {
        return m_cdpLinkDao;
    }

    public void setCdpLinkDao(CdpLinkDao cdpLinkDao) {
        m_cdpLinkDao = cdpLinkDao;
    }

    public CdpElementDao getCdpElementDao() {
        return m_cdpElementDao;
    }

    public void setCdpElementDao(CdpElementDao cdpElementDao) {
        m_cdpElementDao = cdpElementDao;
    }

}
