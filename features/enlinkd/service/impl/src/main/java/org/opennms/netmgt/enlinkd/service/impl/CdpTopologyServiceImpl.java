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
import org.opennms.netmgt.enlinkd.service.api.TopologyService;
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
        new UpsertTemplate<>(m_transactionManager,
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
        Map<Integer, CdpElementTopologyEntity> cdpelementmap = new HashMap<>();
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
        Set<Integer> parsed = new HashSet<>();

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
            results.add(TopologyService.of(sourceLink, targetLink));
        }
        return results;
    }

    @Override
    public void deletePersistedData() {
        m_cdpElementDao.deleteAll();
        m_cdpElementDao.flush();

        m_cdpLinkDao.deleteAll();
        m_cdpLinkDao.flush();
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
