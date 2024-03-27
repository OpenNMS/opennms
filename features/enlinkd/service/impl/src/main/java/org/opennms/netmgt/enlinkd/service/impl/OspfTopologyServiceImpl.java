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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.enlinkd.model.OspfArea;
import org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.OspfAreaDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.enlinkd.service.api.CompositeKey;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.TopologyConnection;
import org.opennms.netmgt.enlinkd.service.api.TopologyService;
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
    private OspfAreaDao m_ospfAreaDao;

    public OspfTopologyServiceImpl() {
    }

    @Override
    public void delete(int nodeid) {
        m_ospfElementDao.deleteByNodeId(nodeid);
        m_ospfLinkDao.deleteByNodeId(nodeid);
        m_ospfAreaDao.deleteByNodeId(nodeid);
        m_ospfElementDao.flush();
        m_ospfLinkDao.flush();
        m_ospfAreaDao.flush();
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
        m_ospfAreaDao.deleteByNodeIdOlderThen(nodeId, now);
        m_ospfAreaDao.flush();
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

    @Override
    public void store(int nodeId, OspfArea area) {
        if (area == null)
            return;
        saveOspfArea(nodeId, area);
        updatesAvailable();
    }

    private void saveOspfArea(final int nodeId, final OspfArea area) {
        new UpsertTemplate<>(m_transactionManager,
                m_ospfAreaDao) {

            @Override
            protected OspfArea query() {
                return m_dao.get(nodeId, area.getOspfAreaId());
            }

            @Override
            protected OspfArea doUpdate(OspfArea dbOspfArea) {
                dbOspfArea.merge(area);
                m_dao.update(dbOspfArea);
                m_dao.flush();
                return dbOspfArea;
            }

            @Override
            protected OspfArea doInsert() {
                final OnmsNode node = new OnmsNode();
                node.setId(nodeId);
                area.setNode(node);
                area.setOspfAreaLastPollTime(area.getOspfAreaCreateTime());
                m_dao.saveOrUpdate(area);
                m_dao.flush();
                return area;
            }

        }.execute();

    }

    private void saveOspfLink(final int nodeId, final OspfLink saveMe) {
        new UpsertTemplate<>(m_transactionManager,
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

    public OspfAreaDao getOspfAreaDao() {
        return m_ospfAreaDao;
    }

    public void setOspfAreaDao(OspfAreaDao ospfAreaDao) {
        m_ospfAreaDao = ospfAreaDao;
    }

    @Override
    public List<OspfElement> findAllOspfElements() {
        return m_ospfElementDao.findAll();
    }

    @Override
    public List<OspfAreaTopologyEntity> findAllOspfAreas() {
        return getTopologyEntityCache().getOspfAreaTopologyEntities();
    }

    @Override
    public List<TopologyConnection<OspfLinkTopologyEntity, OspfLinkTopologyEntity>> match() {
        List<OspfLinkTopologyEntity> allLinks = getTopologyEntityCache().getOspfLinkTopologyEntities();
        List<TopologyConnection<OspfLinkTopologyEntity, OspfLinkTopologyEntity>> results = new ArrayList<>();
        Set<Integer> parsed = new HashSet<>();

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
            results.add(TopologyService.of(sourceLink, targetLink));
        }
        return results;

    }

    @Override
    public void deletePersistedData() {
        m_ospfElementDao.deleteAll();
        m_ospfElementDao.flush();

        m_ospfLinkDao.deleteAll();
        m_ospfLinkDao.flush();
    }
}
