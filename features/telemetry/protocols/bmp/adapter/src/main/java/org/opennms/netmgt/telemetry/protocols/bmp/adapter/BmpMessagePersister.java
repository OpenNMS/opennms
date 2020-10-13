/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.BmpMessageHandler;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Message;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.BaseAttribute;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Collector;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Stat;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.UnicastPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpBaseAttribute;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpBaseAttributeDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollector;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollectorDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRib;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRibDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpPeer;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpPeerDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouter;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouterDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatReports;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefixDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.PrefixByAS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class BmpMessagePersister implements BmpMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BmpMessagePersister.class);

    @Autowired
    private BmpCollectorDao bmpCollectorDao;

    @Autowired
    private BmpRouterDao bmpRouterDao;

    @Autowired
    private BmpPeerDao bmpPeerDao;

    @Autowired
    private BmpBaseAttributeDao bmpBaseAttributeDao;

    @Autowired
    private BmpUnicastPrefixDao bmpUnicastPrefixDao;

    @Autowired
    private BmpGlobalIpRibDao bmpGlobalIpRibDao;

    @Autowired
    private SessionUtils sessionUtils;


    private Map<String, AtomicInteger> routerConnections = new HashMap<>();


    @Override
    public void handle(Message message) {
        sessionUtils.withTransaction(() -> {
            switch (message.getType()) {
                case COLLECTOR:
                    List<BmpCollector> bmpCollectors = buildBmpCollectors(message);
                    // Update routers state to down when collector is just starting or going into stopped state.
                    bmpCollectors.forEach(collector -> {
                        if (collector.getAction().equals(Collector.Action.STARTED.value) ||
                                collector.getAction().equals(Collector.Action.STOPPED.value)) {
                            collector.getBmpRouters().forEach(bmpRouter -> {
                                // Set down state for routers.
                                bmpRouter.setState(false);
                            });
                        }
                        try {
                            bmpCollectorDao.saveOrUpdate(collector);
                        } catch (Exception e) {
                            LOG.error("Exception while persisting BMP collector {}", collector);
                        }
                    });
                    break;
                case ROUTER:
                    BmpCollector bmpCollector = bmpCollectorDao.findByCollectorHashId(message.getCollectorHashId());
                    if (bmpCollector != null) {
                        List<BmpRouter> bmpRouters = buildBmpRouters(message, bmpCollector);
                        bmpRouters.forEach(router -> {
                            // TODO: Understand this usecase better.
                            Integer connections = router.getConnectionCount();
                            // Upon initial router message in INIT/FIRST state,  update all corresponding peer state to down.
                            boolean state = !router.getAction().equals(Router.Action.TERM.value);
                            if (connections == 0 && state) {
                                router.getBmpPeers().forEach(bmpPeer -> {
                                    if (bmpPeer.getTimestamp().getTime() < router.getTimestamp().getTime()) {
                                        bmpPeer.setState(false);
                                    }
                                });
                            }
                            Integer count = state ? ++connections : --connections;
                            router.setConnectionCount(count);
                            try {
                                bmpRouterDao.saveOrUpdate(router);
                            } catch (Exception e) {
                                LOG.error("Exception while persisting BMP router {}", router);
                            }

                        });
                    }
                    break;
                case PEER:
                    List<BmpPeer> bmpPeers = buildBmpPeers(message);
                    // Retain unicast prefixes that are updated after current peer UP/down message.
                    bmpPeers.forEach(peer -> {
                        Set<BmpUnicastPrefix> unicastPrefixes = peer.getBmpUnicastPrefixes().stream().filter(bmpUnicastPrefix ->
                                bmpUnicastPrefix.getTimestamp().getTime() > peer.getTimestamp().getTime()
                        ).collect(Collectors.toSet());
                        peer.setBmpUnicastPrefixes(unicastPrefixes);
                        try {
                            bmpPeerDao.saveOrUpdate(peer);
                        } catch (Exception e) {
                            LOG.error("Exception while persisting BMP peer {}", peer);
                        }
                    });
                    break;
                case BASE_ATTRIBUTE:
                    List<BmpBaseAttribute> bmpBaseAttributes = buildBmpBaseAttributes(message);
                    bmpBaseAttributes.forEach(bmpBaseAttribute -> {
                        try {
                            bmpBaseAttributeDao.saveOrUpdate(bmpBaseAttribute);
                        } catch (Exception e) {
                            LOG.error("Exception while persisting BMP base attribute {}", bmpBaseAttribute);
                        }
                    });
                    break;
                case UNICAST_PREFIX:
                    List<BmpUnicastPrefix> bmpUnicastPrefixes = buildBmpUnicastPrefix(message);
                    bmpUnicastPrefixes.forEach(unicastPrefix -> {
                        try {
                            bmpUnicastPrefixDao.saveOrUpdate(unicastPrefix);
                        } catch (Exception e) {
                            LOG.error("Exception while persisting BMP unicast prefix {}", unicastPrefix);
                        }
                    });
                    List<PrefixByAS> prefixByASList = bmpUnicastPrefixDao.getPrefixesGroupedbyAS();
                    prefixByASList.forEach(prefixByAS -> {
                        BmpGlobalIpRib bmpGlobalIpRib = buildGlobalIpRib(prefixByAS);
                        if (bmpGlobalIpRib != null) {
                            try {
                                bmpGlobalIpRibDao.saveOrUpdate(bmpGlobalIpRib);
                            } catch (Exception e) {
                                LOG.error("Exception while persisting BMP global iprib  {}", bmpGlobalIpRib);
                            }
                        }
                    });
                    break;
            }
        });
    }


    @Override
    public void close() {

    }

    private List<BmpCollector> buildBmpCollectors(Message message) {

        List<BmpCollector> bmpCollectors = new ArrayList<>();
        message.getRecords().forEach(record -> {
            if (record.getType().equals(Type.COLLECTOR)) {
                Collector collector = (Collector) record;
                try {
                    BmpCollector collectorEntity = bmpCollectorDao.findByCollectorHashId(collector.hash);
                    if (collectorEntity == null) {
                        collectorEntity = new BmpCollector();
                    }
                    collectorEntity.setAction(collector.action.value);
                    collectorEntity.setAdminId(collector.adminId);
                    collectorEntity.setHashId(collector.hash);
                    String routers = collector.routers != null ? Joiner.on(',').join(Iterables.transform(collector.routers, InetAddressUtils::str)) : "";
                    collectorEntity.setRouters(routers);
                    int routerCount = collector.routers != null ? collector.routers.size() : 0;
                    collectorEntity.setRoutersCount(routerCount);
                    // Boolean to represent Up/Down, Any state other than stopped is Up
                    boolean state = !(collector.action.equals(Collector.Action.STOPPED));
                    collectorEntity.setState(state);
                    collectorEntity.setTimestamp(Date.from(collector.timestamp));
                    bmpCollectors.add(collectorEntity);
                } catch (Exception e) {
                    LOG.error("Exception while mapping collector with hashId {} to Collector entity", collector.hash);
                }
            }
        });
        return bmpCollectors;
    }

    private List<BmpRouter> buildBmpRouters(Message message, BmpCollector bmpCollector) {
        List<BmpRouter> bmpRouters = new ArrayList<>();
        message.getRecords().forEach(record -> {
            if (record.getType().equals(Type.ROUTER)) {
                Router router = (Router) record;
                try {
                    BmpRouter bmpRouterEntity = bmpRouterDao.findByRouterHashId(router.hash);
                    if (bmpRouterEntity == null) {
                        bmpRouterEntity = new BmpRouter();
                    }
                    bmpRouterEntity.setHashId(router.hash);
                    bmpRouterEntity.setName(router.name);
                    bmpRouterEntity.setIpAddress(InetAddressUtils.str(router.ipAddress));
                    bmpRouterEntity.setTimestamp(Date.from(router.timestamp));
                    bmpRouterEntity.setTermReasonText(router.termReason);
                    bmpRouterEntity.setTermReasonCode(router.termCode);
                    bmpRouterEntity.setTermData(router.termData);
                    bmpRouterEntity.setBgpId(InetAddressUtils.str(router.bgpId));
                    bmpRouterEntity.setDescription(router.description);
                    bmpRouterEntity.setInitData(router.initData);
                    boolean state = !(router.action.equals(Router.Action.TERM));
                    bmpRouterEntity.setAction(router.action.value);
                    bmpRouterEntity.setState(state);
                    bmpRouterEntity.setBmpCollector(bmpCollector);
                    bmpRouters.add(bmpRouterEntity);
                } catch (Exception e) {
                    LOG.error("Exception while mapping collector with hashId {} to Router entity", router.hash);
                }
            }
        });
        return bmpRouters;
    }


    private List<BmpPeer> buildBmpPeers(Message message) {

        List<BmpPeer> bmpPeers = new ArrayList<>();
        message.getRecords().forEach(record -> {
            if (record.getType().equals(Type.PEER)) {
                Peer peer = (Peer) record;
                try {
                    BmpRouter bmpRouter;
                    BmpPeer peerEntity = bmpPeerDao.findByPeerHashId(peer.hash);
                    if (peerEntity == null) {
                        peerEntity = new BmpPeer();
                        bmpRouter = bmpRouterDao.findByRouterHashId(peer.routerHash);
                    } else {
                        bmpRouter = peerEntity.getBmpRouter();
                    }
                    peerEntity.setBmpRouter(bmpRouter);
                    peerEntity.setHashId(peer.hash);
                    peerEntity.setPeerRd(peer.peerRd);
                    peerEntity.setIpv4(peer.ipv4);
                    peerEntity.setPeerAddr(InetAddressUtils.str(peer.remoteIp));
                    peerEntity.setName(peer.name);
                    peerEntity.setPeerBgpId(InetAddressUtils.str(peer.remoteBgpId));
                    boolean state = !peer.action.equals(Peer.Action.DOWN);
                    peerEntity.setState(state);
                    peerEntity.setL3VPNPeer(peer.l3vpn);
                    peerEntity.setTimestamp(Date.from(peer.timestamp));
                    peerEntity.setPrePolicy(peer.prePolicy);
                    peerEntity.setLocalIp(InetAddressUtils.str(peer.localIp));
                    peerEntity.setLocalBgpId(InetAddressUtils.str(peer.localBgpId));
                    peerEntity.setLocalPort(peer.localPort);
                    peerEntity.setLocalHoldTime(peer.advertisedHolddown);
                    peerEntity.setLocalAsn(peer.localAsn);
                    peerEntity.setRemotePort(peer.remotePort);
                    peerEntity.setRemoteHoldTime(peer.remoteHolddown);
                    peerEntity.setSentCapabilities(peer.advertisedCapabilities);
                    peerEntity.setReceivedCapabilities(peer.receivedCapabilities);
                    peerEntity.setBmpReason(peer.bmpReason);
                    peerEntity.setBgpErrCode(peer.bgpErrorCode);
                    peerEntity.setErrorText(peer.errorText);
                    peerEntity.setLocRib(peer.locRib);
                    peerEntity.setLocRibFiltered(peer.locRibFiltered);
                    peerEntity.setTableName(peer.tableName);
                    bmpPeers.add(peerEntity);
                } catch (Exception e) {
                    LOG.error("Exception while mapping peer with hashId {} to Peer entity", peer.hash);
                }
            }
        });
        return bmpPeers;
    }

    private static List<BmpBaseAttribute> buildBmpBaseAttributes(Message message) {

        List<BmpBaseAttribute> bmpBaseAttributes = new ArrayList<>();
        message.getRecords().forEach(record -> {
            if (record.getType().equals(Type.BASE_ATTRIBUTE)) {
                BaseAttribute baseAttribute = (BaseAttribute) record;
                try {
                    BmpBaseAttribute bmpBaseAttribute = new BmpBaseAttribute();
                    bmpBaseAttribute.setHashId(baseAttribute.hash);
                    bmpBaseAttribute.setPeerHashId(baseAttribute.peerHash);
                    bmpBaseAttribute.setOrigin(baseAttribute.origin);
                    bmpBaseAttribute.setAsPath(baseAttribute.asPath);
                    bmpBaseAttribute.setAsPathCount(baseAttribute.asPathCount);
                    bmpBaseAttribute.setOriginAs(baseAttribute.originAs);
                    bmpBaseAttribute.setNextHop(InetAddressUtils.str(baseAttribute.nextHop));
                    bmpBaseAttribute.setMed(baseAttribute.med);
                    bmpBaseAttribute.setLocalPref(baseAttribute.localPref);
                    bmpBaseAttribute.setAggregator(baseAttribute.aggregator);
                    bmpBaseAttribute.setCommunityList(baseAttribute.communityList);
                    bmpBaseAttribute.setExtCommunityList(baseAttribute.extCommunityList);
                    bmpBaseAttribute.setLargeCommunityList(baseAttribute.largeCommunityList);
                    bmpBaseAttribute.setClusterList(baseAttribute.clusterList);
                    bmpBaseAttribute.setAtomicAgg(baseAttribute.atomicAgg);
                    bmpBaseAttribute.setNextHopIpv4(baseAttribute.nextHopIpv4);
                    bmpBaseAttribute.setTimestamp(Date.from(baseAttribute.timestamp));
                    bmpBaseAttribute.setOriginatorId(baseAttribute.originatorId);
                    bmpBaseAttributes.add(bmpBaseAttribute);
                } catch (Exception e) {
                    LOG.error("Exception while mapping base attribute with hashId {} to BaseAttribute entity", baseAttribute.hash);
                }
            }

        });
        return bmpBaseAttributes;
    }

    private List<BmpUnicastPrefix> buildBmpUnicastPrefix(Message message) {

        List<BmpUnicastPrefix> bmpUnicastPrefixes = new ArrayList<>();
        message.getRecords().forEach(record -> {
            if (record.getType().equals(Type.UNICAST_PREFIX)) {
                BmpPeer bmpPeer;
                UnicastPrefix unicastPrefix = (UnicastPrefix) record;
                try {
                    BmpUnicastPrefix bmpUnicastPrefix = bmpUnicastPrefixDao.findByHashId(unicastPrefix.hash);
                    if (bmpUnicastPrefix == null) {
                        bmpUnicastPrefix = new BmpUnicastPrefix();
                        bmpUnicastPrefix.setFirstAddedTimestamp(Date.from(unicastPrefix.timestamp));
                        bmpPeer = bmpPeerDao.findByPeerHashId(unicastPrefix.peerHash);
                    } else {
                        bmpPeer = bmpUnicastPrefix.getBmpPeer();
                    }
                    bmpUnicastPrefix.setBmpPeer(bmpPeer);
                    bmpUnicastPrefix.setHashId(unicastPrefix.hash);
                    bmpUnicastPrefix.setBaseAttrHashId(unicastPrefix.baseAttrHash);
                    bmpUnicastPrefix.setIpv4(unicastPrefix.ipv4);
                    bmpUnicastPrefix.setOriginAs(unicastPrefix.originAs);
                    bmpUnicastPrefix.setPrefix(InetAddressUtils.str(unicastPrefix.prefix));
                    bmpUnicastPrefix.setPrefixLen(unicastPrefix.length);
                    bmpUnicastPrefix.setTimestamp(Date.from(unicastPrefix.timestamp));
                    boolean withDrawn = !unicastPrefix.action.equals(UnicastPrefix.Action.ADD);
                    bmpUnicastPrefix.setWithDrawn(withDrawn);
                    bmpUnicastPrefix.setPathId(unicastPrefix.pathId);
                    bmpUnicastPrefix.setLabels(unicastPrefix.labels);
                    bmpUnicastPrefix.setPrePolicy(unicastPrefix.prePolicy);
                    bmpUnicastPrefix.setAdjRibIn(unicastPrefix.adjIn);
                    bmpUnicastPrefixes.add(bmpUnicastPrefix);
                } catch (Exception e) {
                    LOG.error("Exception while mapping unicast prefix with hashId {} to UnicastPrefix entity", unicastPrefix.hash);
                }
            }
        });
        return bmpUnicastPrefixes;
    }

    private static List<BmpStatReports> buildBmpStatReports(Message message) {
        List<BmpStatReports> bmpStatReportsList = new ArrayList<>();
        message.getRecords().forEach(record -> {
            if (record.getType().equals(Type.BMP_STAT)) {
                Stat statreports = (Stat) record;
                BmpStatReports bmpStatReports = new BmpStatReports();
                bmpStatReports.setPeerHashId(statreports.peerHash);
                bmpStatReports.setPrefixesRejected(statreports.prefixesRejected);
                bmpStatReports.setKnownDupPrefixes(statreports.knownDupPrefixes);
                bmpStatReports.setKnownDupWithdraws(statreports.knownDupWithdraws);
                bmpStatReports.setUpdatesInvalidByClusterList(statreports.invalidClusterList);
                bmpStatReports.setUpdatesInvalidByAsPathLoop(statreports.invalidAsPath);
                bmpStatReports.setUpdatesInvalidByOriginatorId(statreports.invalidOriginatorId);
                bmpStatReports.setUpdatesInvalidByAsConfedLoop(statreports.invalidAsConfed);
                bmpStatReports.setNumRoutesAdjRibIn(statreports.prefixesPrePolicy);
                bmpStatReports.setNumROutesLocalRib(statreports.prefixesPostPolicy);
                bmpStatReports.setTimestamp(Date.from(statreports.timestamp));
                bmpStatReportsList.add(bmpStatReports);
            }
        });
        return bmpStatReportsList;

    }

    private BmpGlobalIpRib buildGlobalIpRib(PrefixByAS prefixByAS) {
        try {
            BmpGlobalIpRib bmpGlobalIpRib = bmpGlobalIpRibDao.findByPrefixAndAS(prefixByAS.getPrefix(), prefixByAS.getOriginAs());
            if (bmpGlobalIpRib == null) {
                bmpGlobalIpRib = new BmpGlobalIpRib();
            }
            bmpGlobalIpRib.setPrefix(prefixByAS.getPrefix());
            bmpGlobalIpRib.setPrefixLen(prefixByAS.getPrefixLen());
            bmpGlobalIpRib.setTimeStamp(prefixByAS.getTimeStamp());
            bmpGlobalIpRib.setRecvOriginAs(prefixByAS.getOriginAs());
            return bmpGlobalIpRib;
        } catch (Exception e) {
            LOG.error("Exception while mapping prefix {} to GlobalIpRib entity", prefixByAS.getPrefix());
        }
        return null;

    }


    public BmpCollectorDao getBmpCollectorDao() {
        return bmpCollectorDao;
    }

    public void setBmpCollectorDao(BmpCollectorDao bmpCollectorDao) {
        this.bmpCollectorDao = bmpCollectorDao;
    }

    public BmpRouterDao getBmpRouterDao() {
        return bmpRouterDao;
    }

    public void setBmpRouterDao(BmpRouterDao bmpRouterDao) {
        this.bmpRouterDao = bmpRouterDao;
    }

    public BmpPeerDao getBmpPeerDao() {
        return bmpPeerDao;
    }

    public void setBmpPeerDao(BmpPeerDao bmpPeerDao) {
        this.bmpPeerDao = bmpPeerDao;
    }

    public BmpBaseAttributeDao getBmpBaseAttributeDao() {
        return bmpBaseAttributeDao;
    }

    public void setBmpBaseAttributeDao(BmpBaseAttributeDao bmpBaseAttributeDao) {
        this.bmpBaseAttributeDao = bmpBaseAttributeDao;
    }

    public BmpUnicastPrefixDao getBmpUnicastPrefixDao() {
        return bmpUnicastPrefixDao;
    }

    public void setBmpUnicastPrefixDao(BmpUnicastPrefixDao bmpUnicastPrefixDao) {
        this.bmpUnicastPrefixDao = bmpUnicastPrefixDao;
    }

    public SessionUtils getSessionUtils() {
        return sessionUtils;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }
}
