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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.enlinkd.service.api.CompositeKey;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.TopologyConnection;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class LldpTopologyServiceImpl extends TopologyServiceImpl implements LldpTopologyService {

    private static final Logger LOG = LoggerFactory.getLogger(LldpTopologyServiceImpl.class);

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private LldpLinkDao m_lldpLinkDao;
    private LldpElementDao m_lldpElementDao;

    public LldpTopologyServiceImpl() {
    }

    @Override
    public void delete(int nodeid) {
        m_lldpElementDao.deleteByNodeId(nodeid);
        m_lldpLinkDao.deleteByNodeId(nodeid);
        m_lldpElementDao.flush();
        m_lldpLinkDao.flush();
    }

    @Override
    public void reconcile(int nodeId, Date now) {
        LldpElement element = m_lldpElementDao.findByNodeId(nodeId);
        if (element != null
                && element.getLldpNodeLastPollTime().getTime() < now.getTime()) {
            m_lldpElementDao.delete(element);
            m_lldpElementDao.flush();
        }
        m_lldpLinkDao.deleteByNodeIdOlderThen(nodeId, now);
        m_lldpLinkDao.flush();
    }

    @Override
    public void store(int nodeId, LldpLink link) {
        if (link == null)
            return;
        saveLldpLink(nodeId, link);
       updatesAvailable();
    }

    @Transactional
    protected void saveLldpLink(final int nodeId, final LldpLink saveMe) {
        new UpsertTemplate<LldpLink, LldpLinkDao>(m_transactionManager,
                                                  m_lldpLinkDao) {

            @Override
            protected LldpLink query() {
                return m_dao.get(nodeId, saveMe.getLldpLocalPortNum());
            }

            @Override
            protected LldpLink doUpdate(LldpLink dbLldpLink) {
                dbLldpLink.merge(saveMe);
                m_dao.update(dbLldpLink);
                m_dao.flush();
                return dbLldpLink;
            }

            @Override
            protected LldpLink doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setNode(node);
                saveMe.setLldpLinkLastPollTime(saveMe.getLldpLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    @Transactional
    public void store(int nodeId, LldpElement element) {
        if (element == null)
            return;

        LldpElement dbelement = m_lldpElementDao.findByNodeId(nodeId);
        if (dbelement != null) {
            dbelement.merge(element);
            m_lldpElementDao.saveOrUpdate(dbelement);
            m_lldpElementDao.flush();
            return;
        }

        OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        element.setNode(node);
        element.setLldpNodeLastPollTime(element.getLldpNodeCreateTime());
        m_lldpElementDao.saveOrUpdate(element);
        m_lldpElementDao.flush();
        updatesAvailable();

    }

    public LldpLinkDao getLldpLinkDao() {
        return m_lldpLinkDao;
    }

    public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
        m_lldpLinkDao = lldpLinkDao;
    }

    public LldpElementDao getLldpElementDao() {
        return m_lldpElementDao;
    }

    public void setLldpElementDao(LldpElementDao lldpElementDao) {
        m_lldpElementDao = lldpElementDao;
    }

    @Override
    public List<LldpElementTopologyEntity> findAllLldpElements() {
        return getTopologyEntityCache().getLldpElementTopologyEntities();
    }

    @Override
    public List<TopologyConnection<LldpLinkTopologyEntity, LldpLinkTopologyEntity>> match() {
        
            List<TopologyConnection<LldpLinkTopologyEntity, LldpLinkTopologyEntity>> results = new ArrayList<>();

            Map<Integer, LldpElementTopologyEntity> nodelldpelementidMap = getTopologyEntityCache().getLldpElementTopologyEntities().stream()
                    .collect(Collectors.toMap(lldpelem -> lldpelem.getNodeId(), lldpelem -> lldpelem));
            
            List<LldpLinkTopologyEntity> allLinks = getTopologyEntityCache().getLldpLinkTopologyEntities();
            // 1.) create mapping
            Map<CompositeKey, LldpLinkTopologyEntity> targetLinkMap = new HashMap<>();
            for(LldpLinkTopologyEntity targetLink : allLinks){

                CompositeKey key = new CompositeKey(
                        targetLink.getLldpRemChassisId(),
                        nodelldpelementidMap.get(targetLink.getNodeId()).getLldpChassisId(),
                        targetLink.getLldpPortId(),
                        targetLink.getLldpPortIdSubType(),
                        targetLink.getLldpRemPortId(),
                        targetLink.getLldpRemPortIdSubType());
                targetLinkMap.put(key, targetLink);
            }

            // 2.) iterate
            Set<Integer> parsed = new HashSet<Integer>();
            for (LldpLinkTopologyEntity sourceLink : allLinks) {
                if (parsed.contains(sourceLink.getId())) {
                    continue;
                }
                String sourceLldpChassisId = nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpChassisId();
                if (sourceLldpChassisId.equals(sourceLink.getLldpRemChassisId())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getLldpLinks: self link not adding source: {}",sourceLink);
                    }
                    parsed.add(sourceLink.getId());
                    continue;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getLldpLinks: source: {}",sourceLink);
                }

                CompositeKey key = new CompositeKey(
                        nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpChassisId(),
                        sourceLink.getLldpRemChassisId(),
                        sourceLink.getLldpRemPortId(),
                        sourceLink.getLldpRemPortIdSubType(),
                        sourceLink.getLldpPortId(),
                        sourceLink.getLldpPortIdSubType());
                LldpLinkTopologyEntity targetLink = targetLinkMap.get(key);

                if (targetLink == null) {
                    LOG.debug("getLldpLinks: cannot found target for source: '{}'", sourceLink.getId());
                    continue;
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("getLldpLinks: lldp: {} target: {}", sourceLink.getLldpRemChassisId(), targetLink);
                }

                parsed.add(sourceLink.getId());
                parsed.add(targetLink.getId());
                results.add(TopologyConnection.of(sourceLink, targetLink));
            }
            return results;
       // }

    }
}
