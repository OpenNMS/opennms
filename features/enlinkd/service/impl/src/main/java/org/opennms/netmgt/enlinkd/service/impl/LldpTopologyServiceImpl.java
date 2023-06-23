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
import org.opennms.netmgt.enlinkd.service.api.TopologyService;
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
        if (link.getLldpPortIfindex() == null) {
            LOG.debug("store: ifindex is null, {}", link);
            link.setLldpPortIfindex(m_lldpLinkDao.getIfIndex(nodeId, link.getLldpPortId()));
        }
        saveLldpLink(nodeId, link);
        updatesAvailable();
    }

    @Transactional
    protected void saveLldpLink(final int nodeId, final LldpLink saveMe) {
        new UpsertTemplate<>(m_transactionManager,
                                                  m_lldpLinkDao) {

            @Override
            protected LldpLink query() {
                return m_dao.get(nodeId, saveMe.getLldpRemLocalPortNum(), saveMe.getLldpRemIndex());
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

            Map<Integer, LldpElementTopologyEntity> nodelldpelementidMap = getTopologyEntityCache().getLldpElementTopologyEntities().stream()
                    .collect(Collectors.toMap(LldpElementTopologyEntity::getNodeId, lldpelem -> lldpelem));
            List<LldpLinkTopologyEntity> lldpLinks = getTopologyEntityCache().getLldpLinkTopologyEntities();
            // 1.) create mapping
            Map<CompositeKey, LldpLinkTopologyEntity> lldpLinkCompositeKeyMap = new HashMap<>();
            List<LldpLinkTopologyEntity> goodLldpLinks = new ArrayList<>();
            for(LldpLinkTopologyEntity lldpLink : lldpLinks){
                if (lldpLink.getLldpPortIfindex() == -1 ) {
                    CompositeKey elementarKeyA = new CompositeKey(
                            lldpLink.getLldpRemChassisId(),
                            nodelldpelementidMap.get(lldpLink.getNodeId()).getLldpChassisId(),
                            lldpLink.getLldpRemPortId(),
                            lldpLink.getLldpRemPortIdSubType());
                    CompositeKey elementarKeyB = new CompositeKey(
                            lldpLink.getLldpRemChassisId(),
                            nodelldpelementidMap.get(lldpLink.getNodeId()).getLldpChassisId(),
                            lldpLink.getLldpRemPortDescr());
                    lldpLinkCompositeKeyMap.put(elementarKeyA,lldpLink);
                    lldpLinkCompositeKeyMap.put(elementarKeyB,lldpLink);
                    continue;
                }
                goodLldpLinks.add(lldpLink);
                CompositeKey key = new CompositeKey(
                        lldpLink.getLldpRemChassisId(),
                        nodelldpelementidMap.get(lldpLink.getNodeId()).getLldpChassisId(),
                        lldpLink.getLldpPortId(),
                        lldpLink.getLldpPortIdSubType(),
                        lldpLink.getLldpRemPortId(),
                        lldpLink.getLldpRemPortIdSubType());
                lldpLinkCompositeKeyMap.put(key, lldpLink);
                CompositeKey descrkey = new CompositeKey(
                        lldpLink.getLldpRemChassisId(),
                        nodelldpelementidMap.get(lldpLink.getNodeId()).getLldpChassisId(),
                        lldpLink.getLldpPortDescr(),
                        lldpLink.getLldpRemPortDescr());
                lldpLinkCompositeKeyMap.put(descrkey,lldpLink);
                CompositeKey sysnameKey = new CompositeKey(
                    lldpLink.getLldpRemSysname(),
                    nodelldpelementidMap.get(lldpLink.getNodeId()).getLldpSysname(),
                    lldpLink.getLldpPortId(),
                    lldpLink.getLldpPortIdSubType(),
                    lldpLink.getLldpRemPortId(),
                    lldpLink.getLldpRemPortIdSubType()
                );
                lldpLinkCompositeKeyMap.put(sysnameKey,lldpLink);
            }

            // 2.) iterate
        Set<Integer> parsed = new HashSet<>();
        List<TopologyConnection<LldpLinkTopologyEntity, LldpLinkTopologyEntity>> results = new ArrayList<>();

        for (LldpLinkTopologyEntity sourceLink : goodLldpLinks) {
                if (parsed.contains(sourceLink.getId())) {
                    continue;
                }

                if (nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpChassisId().equals(sourceLink.getLldpRemChassisId())
                   || nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpSysname().equals(sourceLink.getLldpRemSysname())) {
                    LOG.info("match: self link, skipping:{}",sourceLink);
                    continue;
                }

                String compositeKeyExplained="composite key: default";
                CompositeKey key = new CompositeKey(
                        nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpChassisId(),
                        sourceLink.getLldpRemChassisId(),
                        sourceLink.getLldpRemPortId(),
                        sourceLink.getLldpRemPortIdSubType(),
                        sourceLink.getLldpPortId(),
                        sourceLink.getLldpPortIdSubType());
                LldpLinkTopologyEntity targetLink = lldpLinkCompositeKeyMap.get(key);
                if (targetLink == null) {
                    compositeKeyExplained="composite key: port description";
                    CompositeKey descrkey = new CompositeKey(
                            nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpChassisId(),
                            sourceLink.getLldpRemChassisId(),
                            sourceLink.getLldpRemPortDescr(),
                            sourceLink.getLldpPortDescr());
                    targetLink = lldpLinkCompositeKeyMap.get(descrkey);
                }
                if (targetLink == null) {
                    compositeKeyExplained="composite key: sysname";
                    CompositeKey sysnamekey = new CompositeKey(
                            nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpSysname(),
                            sourceLink.getLldpRemSysname(),
                            sourceLink.getLldpRemPortId(),
                            sourceLink.getLldpRemPortIdSubType(),
                            sourceLink.getLldpPortId(),
                            sourceLink.getLldpPortIdSubType());
                    targetLink = lldpLinkCompositeKeyMap.get(sysnamekey);
                }
                if (targetLink == null) {
                    compositeKeyExplained="composite key: elementary with port id";
                    CompositeKey elementaryA = new CompositeKey(
                            nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpChassisId(),
                            sourceLink.getLldpRemChassisId(),
                            sourceLink.getLldpPortId(),
                            sourceLink.getLldpPortIdSubType());
                    targetLink = lldpLinkCompositeKeyMap.get(elementaryA);
                }
                if (targetLink == null) {
                    compositeKeyExplained="composite key: elementary with port descr";
                    CompositeKey elementaryB = new CompositeKey(
                            nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpChassisId(),
                            sourceLink.getLldpRemChassisId(),
                            sourceLink.getLldpPortDescr());
                    targetLink = lldpLinkCompositeKeyMap.get(elementaryB);
                }
                if (targetLink == null) {
                    LOG.info("match: cannot found target for lldplink:{}", sourceLink);
                    continue;
                }

                LOG.info("match: {}, source:{}, target:{}", compositeKeyExplained, sourceLink, targetLink);

                parsed.add(sourceLink.getId());
                parsed.add(targetLink.getId());
                results.add(TopologyService.of(sourceLink, targetLink));
            }
            return results;
       // }

    }

    @Override
    public void deletePersistedData() {
        m_lldpElementDao.deleteAll();
        m_lldpElementDao.flush();

        m_lldpLinkDao.deleteAll();
        m_lldpLinkDao.flush();
    }
}
