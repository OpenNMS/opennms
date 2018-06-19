/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.api.BridgeElementDao;
import org.opennms.netmgt.dao.api.BridgeStpLinkDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.dao.api.CdpElementDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.dao.api.IsIsElementDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OspfElementDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

public class EnhancedLinkdServiceImpl implements EnhancedLinkdService {

    private final static Logger LOG = LoggerFactory.getLogger(EnhancedLinkdServiceImpl.class);

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    private NodeDao m_nodeDao;

    private CdpLinkDao m_cdpLinkDao;

    private CdpElementDao m_cdpElementDao;

    private LldpLinkDao m_lldpLinkDao;

    private LldpElementDao m_lldpElementDao;

    private OspfLinkDao m_ospfLinkDao;

    private OspfElementDao m_ospfElementDao;

    private IsIsLinkDao m_isisLinkDao;

    private IsIsElementDao m_isisElementDao;

    private IpNetToMediaDao m_ipNetToMediaDao;

    private BridgeElementDao m_bridgeElementDao;

    private BridgeStpLinkDao m_bridgeStpLinkDao;

    private BridgeTopologyDao m_bridgeTopologyDao;

    volatile Map<Integer, Set<BridgeForwardingTableEntry>> m_nodetoBroadcastDomainMap= new HashMap<Integer, Set<BridgeForwardingTableEntry>>();
    volatile Set<BroadcastDomain> m_domains;
    
    @Override
    public List<Node> getSnmpNodeList() {
        final List<Node> nodes = new ArrayList<Node>();

        final Criteria criteria = new Criteria(OnmsNode.class);
        criteria.setAliases(Arrays.asList(new Alias[] { new Alias(
                                                                  "ipInterfaces",
                                                                  "iface",
                                                                  JoinType.LEFT_JOIN) }));
        criteria.addRestriction(new EqRestriction("type", NodeType.ACTIVE));
        criteria.addRestriction(new EqRestriction("iface.isSnmpPrimary",
                                                  PrimaryType.PRIMARY));
        for (final OnmsNode node : m_nodeDao.findMatching(criteria)) {
            nodes.add(new Node(node.getId(),
                               node.getPrimaryInterface().getIpAddress(),
                               node.getSysObjectId(), node.getSysName(),node.getLocation() == null ? null : node.getLocation().getLocationName()));
        }
        return nodes;
    }

    @Override
    public Node getSnmpNode(final int nodeid) {
        final Criteria criteria = new Criteria(OnmsNode.class);
        criteria.setAliases(Arrays.asList(new Alias[] { new Alias(
                                                                  "ipInterfaces",
                                                                  "iface",
                                                                  JoinType.LEFT_JOIN) }));
        criteria.addRestriction(new EqRestriction("type", NodeType.ACTIVE));
        criteria.addRestriction(new EqRestriction("iface.isSnmpPrimary",
                                                  PrimaryType.PRIMARY));
        criteria.addRestriction(new EqRestriction("id", nodeid));
        final List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);

        if (nodes.size() > 0) {
            final OnmsNode node = nodes.get(0);
            return new Node(node.getId(),
                            node.getPrimaryInterface().getIpAddress(),
                            node.getSysObjectId(), node.getSysName(),node.getLocation() == null ? null : node.getLocation().getLocationName());
        } else {
            return null;
        }
    }

    @Override
    public void delete(int nodeId) throws BridgeTopologyException {

        m_lldpElementDao.deleteByNodeId(nodeId);
        m_lldpLinkDao.deleteByNodeId(nodeId);
        m_lldpElementDao.flush();
        m_lldpLinkDao.flush();

        m_cdpElementDao.deleteByNodeId(nodeId);
        m_cdpLinkDao.deleteByNodeId(nodeId);
        m_cdpElementDao.flush();
        m_cdpLinkDao.flush();

        m_ospfElementDao.deleteByNodeId(nodeId);
        m_ospfLinkDao.deleteByNodeId(nodeId);
        m_ospfElementDao.flush();
        m_ospfLinkDao.flush();

        m_isisElementDao.deleteByNodeId(nodeId);
        m_isisLinkDao.deleteByNodeId(nodeId);
        m_isisElementDao.flush();
        m_isisLinkDao.flush();

        m_ipNetToMediaDao.deleteBySourceNodeId(nodeId);
        m_ipNetToMediaDao.flush();

        m_bridgeElementDao.deleteByNodeId(nodeId);
        m_bridgeElementDao.flush();

        m_bridgeStpLinkDao.deleteByNodeId(nodeId);
        m_bridgeStpLinkDao.flush();
        
        reconcileTopologyForDeleteNode(getBroadcastDomain(nodeId), nodeId);
    
    }
    
    @Override
    public BroadcastDomain reconcileTopologyForDeleteNode(BroadcastDomain domain,int nodeId) throws BridgeTopologyException {
        
        Date now = new Date();
        if (domain == null || domain.isEmpty()) {
            LOG.warn("reconcileTopologyForDeleteNode: node: {}, start: null domain or empty",nodeId);
            return domain;
        }
        if (domain.getBridge(nodeId) == null) {
            LOG.info("reconcileTopologyForDeleteNode: node: {}, not on domain",nodeId);
            return domain;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("reconcileTopologyForDeleteNode: node:[{}], domain:\n{}", nodeId, domain.printTopology());
        }
        
        LOG.info("reconcileTopologyForDeleteNode: node:[{}], start: save topology for domain",nodeId);
        BroadcastDomain.removeBridge(domain,nodeId);
        store(domain,now);
        LOG.info("reconcileTopologyForDeleteNode: node:[{}], end: save topology for domain",nodeId);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("reconcileTopologyForDeleteNode: node:[{}], resulting domain: {}", nodeId, domain.printTopology());
        }
        
        if (domain.isEmpty()) {
            cleanBroadcastDomains();
            m_bridgeTopologyDao.delete(nodeId);
            LOG.info("reconcileTopologyForDeleteNode: node:[{}], empty domain",nodeId);
            return domain;
        }
        if (domain.getRootBridge() == null) {
            throw new BridgeTopologyException("reconcileTopologyForDeleteNode: Domain without root", domain);
        }
        return domain;
    }

    @Override
    public void reconcileLldp(int nodeId, Date now) {
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
    public void reconcileOspf(int nodeId, Date now) {
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
    public void reconcileIsis(int nodeId, Date now) {
        IsIsElement element = m_isisElementDao.findByNodeId(nodeId);
        if (element != null
                && element.getIsisNodeLastPollTime().getTime() < now.getTime()) {
            m_isisElementDao.delete(element);
            m_isisElementDao.flush();
        }
        m_isisLinkDao.deleteByNodeIdOlderThen(nodeId, now);
        m_isisLinkDao.flush();
    }

    @Override
    public void reconcileCdp(int nodeId, Date now) {
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
    public void reconcileIpNetToMedia(int nodeId, Date now) {
        m_ipNetToMediaDao.deleteBySourceNodeIdOlderThen(nodeId, now);
        m_ipNetToMediaDao.flush();
    }

    @Override
    public void store(int nodeId, CdpLink link) {
        if (link == null)
            return;
        saveCdpLink(nodeId, link);
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
                final OnmsNode node = m_nodeDao.get(nodeId);
                if (node == null)
                    return null;
                saveMe.setNode(node);
                saveMe.setCdpLinkLastPollTime(saveMe.getCdpLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    public void store(int nodeId, LldpLink link) {
        if (link == null)
            return;
        saveLldpLink(nodeId, link);
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
                final OnmsNode node = m_nodeDao.get(nodeId);
                if (node == null)
                    return null;
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
    public void store(int nodeId, CdpElement element) {
        if (element == null)
            return;
        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null)
            return;

        CdpElement dbelement = node.getCdpElement();
        if (dbelement != null) {
            dbelement.merge(element);
            node.setCdpElement(dbelement);
        } else {
            element.setNode(node);
            element.setCdpNodeLastPollTime(element.getCdpNodeCreateTime());
            node.setCdpElement(element);
        }

        m_nodeDao.saveOrUpdate(node);
        m_nodeDao.flush();
    }

    @Override
    @Transactional
    public void store(int nodeId, LldpElement element) {
        if (element == null)
            return;
        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null)
            return;

        LldpElement dbelement = node.getLldpElement();
        if (dbelement != null) {
            dbelement.merge(element);
            node.setLldpElement(dbelement);
        } else {
            element.setNode(node);
            element.setLldpNodeLastPollTime(element.getLldpNodeCreateTime());
            node.setLldpElement(element);
        }

        m_nodeDao.saveOrUpdate(node);
        m_nodeDao.flush();

    }

    @Override
    public void store(int nodeId, OspfLink link) {
        if (link == null)
            return;
        saveOspfLink(nodeId, link);
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
                final OnmsNode node = m_nodeDao.get(nodeId);
                if (node == null)
                    return null;
                saveMe.setNode(node);
                saveMe.setOspfLinkLastPollTime(saveMe.getOspfLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();

    }

    @Override
    public void store(int nodeId, IsIsLink link) {
        if (link == null)
            return;
        saveIsisLink(nodeId, link);
    }

    @Transactional
    protected void saveIsisLink(final int nodeId, final IsIsLink saveMe) {
        new UpsertTemplate<IsIsLink, IsIsLinkDao>(m_transactionManager,
                                                  m_isisLinkDao) {

            @Override
            protected IsIsLink query() {
                return m_dao.get(nodeId, saveMe.getIsisCircIndex(),
                                 saveMe.getIsisISAdjIndex());
            }

            @Override
            protected IsIsLink doUpdate(IsIsLink dbIsIsLink) {
                dbIsIsLink.merge(saveMe);
                m_dao.update(dbIsIsLink);
                m_dao.flush();
                return dbIsIsLink;
            }

            @Override
            protected IsIsLink doInsert() {
                final OnmsNode node = m_nodeDao.get(nodeId);
                if (node == null)
                    return null;
                saveMe.setNode(node);
                saveMe.setIsisLinkLastPollTime(saveMe.getIsisLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    @Transactional
    public void store(int nodeId, OspfElement element) {
        if (element == null)
            return;
        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null)
            return;

        OspfElement dbelement = node.getOspfElement();
        if (dbelement != null) {
            dbelement.merge(element);
            node.setOspfElement(dbelement);
        } else {
            element.setNode(node);
            element.setOspfNodeLastPollTime(element.getOspfNodeCreateTime());
            node.setOspfElement(element);
        }

        m_nodeDao.saveOrUpdate(node);
        m_nodeDao.flush();

    }

    @Override
    @Transactional
    public void store(int nodeId, IsIsElement element) {
        if (element == null)
            return;
        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null)
            return;

        IsIsElement dbelement = node.getIsisElement();
        if (dbelement != null) {
            dbelement.merge(element);
            node.setIsisElement(dbelement);
        } else {
            element.setNode(node);
            element.setIsisNodeLastPollTime(element.getIsisNodeCreateTime());
            node.setIsisElement(element);
        }

        m_nodeDao.saveOrUpdate(node);
        m_nodeDao.flush();

    }

    @Override
    public void store(int nodeId, BridgeElement bridge) {
        if (bridge == null)
            return;
        saveBridgeElement(nodeId, bridge);
    }

    @Transactional
    protected void saveBridgeElement(final int nodeId,
            final BridgeElement saveMe) {
        new UpsertTemplate<BridgeElement, BridgeElementDao>(
                                                            m_transactionManager,
                                                            m_bridgeElementDao) {

            @Override
            protected BridgeElement query() {
                return m_dao.getByNodeIdVlan(nodeId, saveMe.getVlan());
            }

            @Override
            protected BridgeElement doUpdate(BridgeElement bridge) {
                bridge.merge(saveMe);
                m_dao.update(bridge);
                m_dao.flush();
                return bridge;
            }

            @Override
            protected BridgeElement doInsert() {
                final OnmsNode node = m_nodeDao.get(nodeId);
                if (node == null)
                    return null;
                saveMe.setNode(node);
                saveMe.setBridgeNodeLastPollTime(saveMe.getBridgeNodeCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    public void store(int nodeId, BridgeStpLink link) {
        if (link == null)
            return;
        saveBridgeStpLink(nodeId, link);
    }

    @Transactional
    protected void saveBridgeStpLink(final int nodeId,
            final BridgeStpLink saveMe) {
        new UpsertTemplate<BridgeStpLink, BridgeStpLinkDao>(
                                                            m_transactionManager,
                                                            m_bridgeStpLinkDao) {

            @Override
            protected BridgeStpLink query() {
                return m_dao.getByNodeIdBridgePort(nodeId,
                                                   saveMe.getStpPort());
            }

            @Override
            protected BridgeStpLink doUpdate(BridgeStpLink link) {
                link.merge(saveMe);
                m_dao.update(link);
                m_dao.flush();
                return link;
            }

            @Override
            protected BridgeStpLink doInsert() {
                final OnmsNode node = m_nodeDao.get(nodeId);
                if (node == null)
                    return null;
                saveMe.setNode(node);
                saveMe.setBridgeStpLinkLastPollTime(saveMe.getBridgeStpLinkCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    public void store(int nodeId, List<BridgeForwardingTableEntry> bft) {
        Set<BridgeForwardingTableEntry> effectiveBFT=new HashSet<BridgeForwardingTableEntry>(); 
        for (BridgeForwardingTableEntry link : bft) {
            link.setNodeId(nodeId);
            effectiveBFT.add(link);
        }
        synchronized (m_nodetoBroadcastDomainMap) {
            m_nodetoBroadcastDomainMap.put(nodeId, effectiveBFT);
        }
    }

    public synchronized Map<Integer,Set<BridgeForwardingTableEntry>> getUpdateBftMap() {
        return m_nodetoBroadcastDomainMap;
    }
    
    @Override
    public void reconcileBridge(int nodeId, Date now) {
        m_bridgeElementDao.deleteByNodeIdOlderThen(nodeId, now);
        m_bridgeElementDao.flush();

        m_bridgeStpLinkDao.deleteByNodeIdOlderThen(nodeId, now);
        m_bridgeStpLinkDao.flush();
    }

    @Override
    public synchronized Set<BroadcastDomain> getAllBroadcastDomains() {
        return m_domains;
    }
    
    @Override
    public void save(BroadcastDomain domain) {
        synchronized (m_domains) {
            m_domains.add(domain);
        }
    }

    public void cleanBroadcastDomains() {
        synchronized (m_domains) {
            m_domains.removeIf(BroadcastDomain::isEmpty);
        }
    }

    @Override
    public BroadcastDomain getBroadcastDomain(int nodeId) {
        synchronized (m_domains) {
            for (BroadcastDomain domain : m_domains) {
                synchronized (domain) {
                    Bridge bridge = domain.getBridge(nodeId);
                    if (bridge != null) {
                        return domain;
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public Set<BridgeForwardingTableEntry> useBridgeTopologyUpdateBFT(int nodeid) {
        synchronized (m_nodetoBroadcastDomainMap) {
            return m_nodetoBroadcastDomainMap.remove(nodeid);
        }
    }


    @Override
    public void store(BroadcastDomain domain, Date now) throws BridgeTopologyException {
        m_bridgeTopologyDao.save(domain, now);
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
                final OnmsNode node = m_nodeDao.get(nodeId);
                if (node == null)
                    return null;
                saveMe.setSourceNode(node);
                dbIpNetToMedia.merge(saveMe);
                m_dao.update(dbIpNetToMedia);
                m_dao.flush();
                return dbIpNetToMedia;
            }

            @Override
            protected IpNetToMedia doInsert() {
                final OnmsNode node = m_nodeDao.get(nodeId);
                if (node == null)
                    return null;
                saveMe.setSourceNode(node);
                saveMe.setLastPollTime(saveMe.getCreateTime());
                m_dao.saveOrUpdate(saveMe);
                m_dao.flush();
                return saveMe;
            }

        }.execute();
    }

    @Override
    public void loadBridgeTopology() {
        m_domains= m_bridgeTopologyDao.load();
    }
    
    public CdpLinkDao getCdpLinkDao() {
        return m_cdpLinkDao;
    }

    public void setCdpLinkDao(CdpLinkDao cdpLinkDao) {
        m_cdpLinkDao = cdpLinkDao;
    }

    public LldpLinkDao getLldpLinkDao() {
        return m_lldpLinkDao;
    }

    public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
        m_lldpLinkDao = lldpLinkDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public OspfLinkDao getOspfLinkDao() {
        return m_ospfLinkDao;
    }

    public void setOspfLinkDao(OspfLinkDao ospfLinkDao) {
        m_ospfLinkDao = ospfLinkDao;
    }

    public IsIsLinkDao getIsisLinkDao() {
        return m_isisLinkDao;
    }

    public void setIsisLinkDao(IsIsLinkDao isisLinkDao) {
        m_isisLinkDao = isisLinkDao;
    }

    public CdpElementDao getCdpElementDao() {
        return m_cdpElementDao;
    }

    public void setCdpElementDao(CdpElementDao cdpElementDao) {
        m_cdpElementDao = cdpElementDao;
    }

    public LldpElementDao getLldpElementDao() {
        return m_lldpElementDao;
    }

    public void setLldpElementDao(LldpElementDao lldpElementDao) {
        m_lldpElementDao = lldpElementDao;
    }

    public OspfElementDao getOspfElementDao() {
        return m_ospfElementDao;
    }

    public void setOspfElementDao(OspfElementDao ospfElementDao) {
        m_ospfElementDao = ospfElementDao;
    }

    public IsIsElementDao getIsisElementDao() {
        return m_isisElementDao;
    }

    public void setIsisElementDao(IsIsElementDao isisElementDao) {
        m_isisElementDao = isisElementDao;
    }

    public BridgeElementDao getBridgeElementDao() {
        return m_bridgeElementDao;
    }

    public void setBridgeElementDao(BridgeElementDao bridgeElementDao) {
        m_bridgeElementDao = bridgeElementDao;
    }

    public BridgeStpLinkDao getBridgeStpLinkDao() {
        return m_bridgeStpLinkDao;
    }

    public void setBridgeStpLinkDao(BridgeStpLinkDao bridgeStpLinkDao) {
        m_bridgeStpLinkDao = bridgeStpLinkDao;
    }

    public IpNetToMediaDao getIpNetToMediaDao() {
        return m_ipNetToMediaDao;
    }

    public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
        m_ipNetToMediaDao = ipNetToMediaDao;
    }

    public BridgeTopologyDao getBridgeTopologyDao() {
        return m_bridgeTopologyDao;
    }

    public void setBridgeTopologyDao(BridgeTopologyDao bridgeTopologyDao) {
        m_bridgeTopologyDao = bridgeTopologyDao;
    }

    @Override
    public void updateBridgeOnDomain(BroadcastDomain domain, Integer nodeId) {
        if (domain == null) {
            return;
        }
        synchronized (domain) {
            for (Bridge bridge: domain.getBridges()) {
                if (bridge.getNodeId().intValue() == nodeId.intValue()) {
                    bridge.clear();
                    List<BridgeElement> elems = m_bridgeElementDao.findByNodeId(nodeId);
                    bridge.getIdentifiers().addAll(Bridge.getIdentifier(elems));
                    bridge.setDesignated(Bridge.getDesignated(elems));
                    break;
                }
            }
        }
    }

    public List<BridgeElement> getBridgeElements(Set<Integer> nodes) {
        List<BridgeElement> elems = new ArrayList<BridgeElement>();
        for (Integer nodeid: nodes)
            elems.addAll(m_bridgeElementDao.findByNodeId(nodeid));
        return elems;
    }

}
