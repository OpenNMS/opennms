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

import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.enlinkd.service.api.CompositeKey;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.TopologyConnection;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class OspfTopologyServiceImpl extends TopologyServiceImpl implements OspfTopologyService {

    private static final Logger LOG = LoggerFactory.getLogger(OspfTopologyServiceImpl.class);

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private OspfLinkDao m_ospfLinkDao;
    private OspfElementDao m_ospfElementDao;

    public OspfTopologyServiceImpl() {
    }

    @Override
    public void delete(int nodeid) {
        m_ospfElementDao.deleteByNodeId(nodeid);
        m_ospfLinkDao.deleteByNodeId(nodeid);
        m_ospfElementDao.flush();
        m_ospfLinkDao.flush();
    }

    @Override
    public void reconcile(int nodeId, Date now) {
        OspfElement element = m_ospfElementDao.findByNodeId(nodeId);
        if (element != null
                && element.getOspfNodeLastPollTime().getTime() < now.getTime()) {
            m_ospfElementDao.delete(element);
            m_ospfElementDao.flush();
        }
        m_ospfLinkDao.deleteByNodeIdOlderThen(nodeId, now);
        m_ospfLinkDao.flush();
    }

    @Override
    @Transactional
    public void store(int nodeId, OspfElement element) {
        if (element == null)
            return;

        OspfElement dbelement = m_ospfElementDao.findByNodeId(nodeId);
        if (dbelement != null) {
            dbelement.merge(element);
            m_ospfElementDao.saveOrUpdate(dbelement);
            m_ospfElementDao.flush();
            return;
        }

        OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        element.setNode(node);
        element.setOspfNodeLastPollTime(element.getOspfNodeCreateTime());
        m_ospfElementDao.saveOrUpdate(element);
        m_ospfElementDao.flush();
        updatesAvailable();

    }

    @Override
    public void store(int nodeId, OspfLink link) {
        if (link == null)
            return;
        saveOspfLink(nodeId, link);
        updatesAvailable();
    }
    
    private void saveOspfLink(final int nodeId, final OspfLink saveMe) {
        new UpsertTemplate<OspfLink, OspfLinkDao>(m_transactionManager,
                                                  m_ospfLinkDao) {

            @Override
            protected OspfLink query() {
                return m_dao.get(nodeId, saveMe.getOspfRemRouterId(),
                                 saveMe.getOspfRemIpAddr(),
                                 saveMe.getOspfRemAddressLessIndex());
            }

            @Override
            protected OspfLink doUpdate(OspfLink dbOspfLink) {
                dbOspfLink.merge(saveMe);
                m_dao.update(dbOspfLink);
                m_dao.flush();
                return dbOspfLink;
            }

            @Override
            protected OspfLink doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                saveMe.setNode(node);
                saveMe.setOspfLinkLastPollTime(saveMe.getOspfLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();

    }


    public OspfLinkDao getOspfLinkDao() {
        return m_ospfLinkDao;
    }

    public void setOspfLinkDao(OspfLinkDao ospfLinkDao) {
        m_ospfLinkDao = ospfLinkDao;
    }

    public OspfElementDao getOspfElementDao() {
        return m_ospfElementDao;
    }

    public void setOspfElementDao(OspfElementDao ospfElementDao) {
        m_ospfElementDao = ospfElementDao;
    }

    @Override
    public List<OspfElement> findAllOspfElements() {
        return m_ospfElementDao.findAll();
    }

    @Override
    public List<TopologyConnection<OspfLinkTopologyEntity, OspfLinkTopologyEntity>> match() {
        List<OspfLinkTopologyEntity> allLinks = getTopologyEntityCache().getOspfLinkTopologyEntities();
        List<TopologyConnection<OspfLinkTopologyEntity, OspfLinkTopologyEntity>> results = new ArrayList<>();
        Set<Integer> parsed = new HashSet<Integer>();

        // build mapping:
        Map<CompositeKey, OspfLinkTopologyEntity> targetLinks = new HashMap<>();
        for(OspfLinkTopologyEntity targetLink : allLinks){
            targetLinks.put(new CompositeKey(targetLink.getOspfIpAddr(), targetLink.getOspfRemIpAddr()) , targetLink);
        }

        for(OspfLinkTopologyEntity sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            parsed.add(sourceLink.getId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("getOspfLinks: source: {}", sourceLink);
            }
            OspfLinkTopologyEntity targetLink = targetLinks.get(new CompositeKey(sourceLink.getOspfRemIpAddr() , sourceLink.getOspfIpAddr()));
            if(targetLink == null) {
                LOG.debug("getOspfLinks: cannot find target for source: '{}'", sourceLink.getId());
                continue;
            }

            if (sourceLink.getId().equals(targetLink.getId()) || parsed.contains(targetLink.getId())) {
                    continue;
            }

            LOG.debug("getOspfLinks: target: {}", targetLink);
            parsed.add(targetLink.getId());
            results.add(TopologyConnection.of(sourceLink, targetLink));
        }
        return results;

    }
}
