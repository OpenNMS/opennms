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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.Context;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Message;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.BaseAttribute;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Collector;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.UnicastPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnInfoDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnPathAnalysis;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnPathAnalysisDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpBaseAttribute;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpBaseAttributeDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollector;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpCollectorDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRib;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRibDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpPeer;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpPeerDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfoDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouter;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouterDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefixDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.PrefixByAS;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.State;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class BmpMessagePersister implements BmpPersistenceMessageHandler {

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
    private BmpAsnInfoDao bmpAsnInfoDao;

    @Autowired
    private BmpAsnPathAnalysisDao bmpAsnPathAnalysisDao;

    @Autowired
    private BmpRouteInfoDao bmpRouteInfoDao;

    @Autowired
    private SessionUtils sessionUtils;

    private CollectionAgentFactory collectionAgentFactory;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("updateGlobalRibs-%d")
            .build();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
            threadFactory);


    private Map<String, Long> updatesByPeer = new ConcurrentHashMap<>();
    private Map<String, Long> withdrawsByPeer = new ConcurrentHashMap<>();
    private Map<AsnKey, Long> updatesByAsn = new ConcurrentHashMap<>();
    private Map<AsnKey, Long> withdrawsByAsn = new ConcurrentHashMap<>();
    private Map<PrefixKey, Long> updatesByPrefix = new ConcurrentHashMap<>();
    private Map<PrefixKey, Long> withdrawsByPrefix = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<CollectionSetWithAgent> collectionSetQueue = new ConcurrentLinkedQueue<>();


    @Override
    public void handle(Message message, Context context) {
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
                                bmpRouter.setState(State.DOWN);
                            });
                        }
                        try {
                            bmpCollectorDao.saveOrUpdate(collector);
                        } catch (Exception e) {
                            LOG.error("Exception while persisting BMP collector {}", collector, e);
                        }
                    });
                    break;
                case ROUTER:
                    BmpCollector bmpCollector = bmpCollectorDao.findByCollectorHashId(message.getCollectorHashId());
                    if (bmpCollector != null) {
                        List<BmpRouter> bmpRouters = buildBmpRouters(message, bmpCollector);
                        bmpRouters.forEach(router -> {
                            Integer connections = router.getConnectionCount();
                            // Upon initial router message in INIT/FIRST state,  update all corresponding peer state to down.
                            boolean state = !router.getAction().equals(Router.Action.TERM.value);
                            if (connections == 0 && state) {
                                router.getBmpPeers().forEach(bmpPeer -> {
                                    if (bmpPeer.getTimestamp().getTime() < router.getTimestamp().getTime()) {
                                        bmpPeer.setState(State.DOWN);
                                    }
                                });
                            }
                            Integer count = state ? ++connections : --connections;
                            router.setConnectionCount(count);
                            try {
                                bmpRouterDao.saveOrUpdate(router);
                            } catch (Exception e) {
                                LOG.error("Exception while persisting BMP router {}", router, e);
                            }

                        });
                    }
                    break;
                case PEER:
                    List<BmpPeer> bmpPeers = buildBmpPeers(message);
                    // Only retain unicast prefixes that are updated after current peer UP/down message.
                    bmpPeers.forEach(peer -> {
                        Set<BmpUnicastPrefix> unicastPrefixes = peer.getBmpUnicastPrefixes().stream().filter(bmpUnicastPrefix ->
                                bmpUnicastPrefix.getTimestamp().getTime() > peer.getTimestamp().getTime()
                        ).collect(Collectors.toSet());
                        peer.setBmpUnicastPrefixes(unicastPrefixes);
                        try {
                            bmpPeerDao.saveOrUpdate(peer);
                        } catch (Exception e) {
                            LOG.error("Exception while persisting BMP peer {}", peer, e);
                        }
                    });
                    break;
                case BASE_ATTRIBUTE:
                    List<BmpBaseAttribute> bmpBaseAttributes = buildBmpBaseAttributes(message);
                    bmpBaseAttributes.forEach(bmpBaseAttribute -> {
                        try {
                            bmpBaseAttributeDao.saveOrUpdate(bmpBaseAttribute);
                        } catch (Exception e) {
                            LOG.error("Exception while persisting BMP base attribute {}", bmpBaseAttribute, e);
                        }
                        String asPath = bmpBaseAttribute.getAsPath();
                        List<BmpAsnPathAnalysis> asnPaths = buildBmpAsnPath(asPath);
                        asnPaths.forEach(asnPath -> {
                            try {
                                bmpAsnPathAnalysisDao.saveOrUpdate(asnPath);
                            } catch (Exception e) {
                                LOG.error("Exception while persisting BMP asn path {}", asnPath, e);
                            }
                        });

                    });
                    break;
                case UNICAST_PREFIX:
                    List<BmpUnicastPrefix> bmpUnicastPrefixes = buildBmpUnicastPrefix(message);
                    bmpUnicastPrefixes.forEach(unicastPrefix -> {
                        try {
                            updateStats(unicastPrefix, context.location);
                            bmpUnicastPrefixDao.saveOrUpdate(unicastPrefix);
                        } catch (Exception e) {
                            LOG.error("Exception while persisting BMP unicast prefix {}", unicastPrefix, e);
                        }
                    });
                    break;
            }
        });
    }

    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(this::updateGlobalRibsAndAsnInfo, 0, 5, TimeUnit.MINUTES);
    }

    void updateGlobalRibsAndAsnInfo() {
        List<PrefixByAS> prefixByASList = bmpUnicastPrefixDao.getPrefixesGroupedByAS();
        prefixByASList.forEach(prefixByAS -> {
            BmpGlobalIpRib bmpGlobalIpRib = buildGlobalIpRib(prefixByAS);
            if (bmpGlobalIpRib != null) {
                Long asn = bmpGlobalIpRib.getRecvOriginAs();
                if (asn != null) {
                    BmpAsnInfo bmpAsnInfo = bmpAsnInfoDao.findByAsn(asn);
                    if (bmpAsnInfo == null) {
                        bmpAsnInfo = fetchAndBuildAsnInfo(asn);
                        if (bmpAsnInfo != null) {
                            try {
                                bmpAsnInfoDao.saveOrUpdate(bmpAsnInfo);
                            } catch (Exception e) {
                                LOG.error("Exception while persisting BMP ASN Info  {}", bmpAsnInfo, e);
                            }
                        }
                    }
                }
                String prefix = bmpGlobalIpRib.getPrefix();
                if (!Strings.isNullOrEmpty(prefix)) {
                    BmpRouteInfo bmpRouteInfo = bmpRouteInfoDao.findByPrefix(prefix);
                    if (bmpRouteInfo == null) {
                        bmpRouteInfo = fetchAndBuildRouteInfo(prefix);
                        if (bmpRouteInfo != null) {
                            bmpGlobalIpRib.setIrrOriginAs(bmpRouteInfo.getOriginAs());
                            bmpGlobalIpRib.setIrrSource(bmpRouteInfo.getSource());
                            try {
                                bmpRouteInfoDao.saveOrUpdate(bmpRouteInfo);
                            } catch (Exception e) {
                                LOG.error("Exception while persisting BMP Route Info  {}", bmpRouteInfo, e);
                            }
                        }
                    }
                }
                try {
                    bmpGlobalIpRibDao.saveOrUpdate(bmpGlobalIpRib);
                } catch (Exception e) {
                    LOG.error("Exception while persisting BMP global iprib  {}", bmpGlobalIpRib, e);
                }

            }
        });
    }

    private void updateStats(BmpUnicastPrefix unicastPrefix, String location) {
        // Update counts if this is new prefix update or
        // if previous withdrawn state is different or it's an update with different base attributes
        if (unicastPrefix.getId() == null ||
                (unicastPrefix.isWithDrawn() != unicastPrefix.isPrevWithDrawnState() ||
                        (!unicastPrefix.isWithDrawn() && !unicastPrefix.getBaseAttrHashId().equals(unicastPrefix.getPrevBaseAttrHashId())))) {

            String peerHashId = unicastPrefix.getBmpPeer().getHashId();
            Long originAsn = unicastPrefix.getOriginAs();
            String prefix = unicastPrefix.getPrefix();
            Integer prefixLen = unicastPrefix.getPrefixLen();
            boolean isWithdrawn = unicastPrefix.isWithDrawn();
            if (isWithdrawn) {
                withdrawsByPeer.compute(peerHashId, (hashId, value) -> (value == null) ? 1 : value + 1);
                withdrawsByAsn.compute(new AsnKey(peerHashId, originAsn), (hashId, value) -> (value == null) ? 1 : value + 1);
                withdrawsByPrefix.compute(new PrefixKey(peerHashId, prefix, prefixLen), (hashId, value) -> (value == null) ? 1 : value + 1);
            } else {
                updatesByPeer.compute(peerHashId, (hashId, value) -> (value == null) ? 1 : value + 1);
                updatesByAsn.compute(new AsnKey(peerHashId, originAsn), (hashId, value) -> (value == null) ? 1 : value + 1);
                updatesByPrefix.compute(new PrefixKey(peerHashId, prefix, prefixLen), (hashId, value) -> (value == null) ? 1 : value + 1);
            }


            // Find the node for the router who has exported the stats and build a collection agent for it
            String routerAddr = unicastPrefix.getBmpPeer().getBmpRouter().getIpAddress();
            String peerAddr = unicastPrefix.getBmpPeer().getPeerAddr();
            InetAddress sourceAddr = InetAddressUtils.getInetAddress(routerAddr);
            Optional<Integer> nodeId = this.interfaceToNodeCache.getFirstNodeId(location, sourceAddr);
            if (!nodeId.isPresent()) {
                return;
            }
            final CollectionAgent agent = this.collectionAgentFactory.createCollectionAgent(Integer.toString(nodeId.get()), sourceAddr);
            // Build resource for the peer
            final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
            final DeferredGenericTypeResource peerResource = new DeferredGenericTypeResource(nodeResource, "bmp-stats-peer", peerAddr);

            // Build the collection set for the peer
            final CollectionSetBuilder builder = new CollectionSetBuilder(agent);
            builder.withTimestamp(unicastPrefix.getTimestamp());

            if (updatesByPeer.get(peerHashId) != null) {
                builder.withNumericAttribute(peerResource, "bmp-stats-peer", "updates_by_peer", updatesByPeer.get(peerHashId),
                        AttributeType.COUNTER);
            }
            if (withdrawsByPeer.get(peerHashId) != null) {
                builder.withNumericAttribute(peerResource, "bmp-stats-peer", "withdraws_by_peer", withdrawsByPeer.get(peerHashId),
                        AttributeType.COUNTER);
            }

            final DeferredGenericTypeResource asnResource = new DeferredGenericTypeResource(nodeResource, "bmp-stats-asn", peerAddr);
            if (updatesByAsn.get(new AsnKey(peerHashId, originAsn)) != null) {
                String name = "updates_by_asn" + "_" + originAsn;
                builder.withNumericAttribute(asnResource, "bmp-stats-asn", name, updatesByAsn.get(new AsnKey(peerHashId, originAsn)),
                        AttributeType.COUNTER);
            }
            if (withdrawsByAsn.get(new AsnKey(peerHashId, originAsn)) != null) {
                String name = "withdraws_by_asn" + "_" + originAsn;
                builder.withNumericAttribute(asnResource, "bmp-stats-asn", name, withdrawsByAsn.get(new AsnKey(peerHashId, originAsn)),
                        AttributeType.COUNTER);
            }

            final DeferredGenericTypeResource prefixResource = new DeferredGenericTypeResource(nodeResource, "bmp-stats-prefix", peerAddr);
            if (updatesByPrefix.get(new PrefixKey(peerHashId, prefix, prefixLen)) != null) {
                String name = "updates_by_prefix" + "_" + prefix + "_" + prefixLen;

                builder.withNumericAttribute(prefixResource, "bmp-stats-prefix", name, updatesByPrefix.get(new PrefixKey(peerHashId, prefix, prefixLen)),
                        AttributeType.COUNTER);
            }

            if (withdrawsByPrefix.get(new PrefixKey(peerHashId, prefix, prefixLen)) != null) {
                String name = "withdraws_by_prefix" + "_" + prefix + "_" + prefixLen;
                builder.withNumericAttribute(prefixResource, "bmp-stats-prefix", name, withdrawsByPrefix.get(new PrefixKey(peerHashId, prefix, prefixLen)),
                        AttributeType.COUNTER);
            }

            CollectionSetWithAgent collectionSetWithAgent = new CollectionSetWithAgent(agent, builder.build());
            collectionSetQueue.add(collectionSetWithAgent);
        }

    }


    @Override
    public void close() {
        scheduledExecutorService.shutdown();
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
                    State state = !(collector.action.equals(Collector.Action.STOPPED)) ? State.UP : State.DOWN;
                    collectorEntity.setState(state);
                    collectorEntity.setTimestamp(Date.from(collector.timestamp));
                    bmpCollectors.add(collectorEntity);
                } catch (Exception e) {
                    LOG.error("Exception while mapping collector with admin Id {} to Collector entity", collector.adminId, e);
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
                    State state = !(router.action.equals(Router.Action.TERM)) ? State.UP : State.DOWN;
                    bmpRouterEntity.setAction(router.action.value);
                    bmpRouterEntity.setState(state);
                    bmpRouterEntity.setBmpCollector(bmpCollector);
                    bmpRouters.add(bmpRouterEntity);
                } catch (Exception e) {
                    LOG.error("Exception while mapping Router with IpAddress '{}' to Router entity", InetAddressUtils.str(router.ipAddress), e);
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
                    peerEntity.setPeerAsn(peer.remoteAsn);
                    State state = !peer.action.equals(Peer.Action.DOWN) ? State.UP : State.DOWN;
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
                    peerEntity.setBgpErrSubCode(peer.bgpErrorSubcode);
                    peerEntity.setErrorText(peer.errorText);
                    peerEntity.setLocRib(peer.locRib);
                    peerEntity.setLocRibFiltered(peer.locRibFiltered);
                    peerEntity.setTableName(peer.tableName);
                    bmpPeers.add(peerEntity);
                } catch (Exception e) {
                    LOG.error("Exception while mapping Peer with peer addr '{}' to Peer entity", InetAddressUtils.str(peer.remoteIp), e);
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
                    LOG.error("Exception while mapping base attribute with hashId {} to BaseAttribute entity", baseAttribute.hash, e);
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
                        bmpUnicastPrefix.setPrevBaseAttrHashId(bmpUnicastPrefix.getBaseAttrHashId());
                        bmpUnicastPrefix.setPrevWithDrawnState(bmpUnicastPrefix.isWithDrawn());
                        bmpPeer = bmpUnicastPrefix.getBmpPeer();
                    }
                    if (bmpPeer == null) {
                        LOG.warn("Peer entity with hashId '{}' doesn't exist yet", unicastPrefix.peerHash);
                        return;
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
                    LOG.error("Exception while mapping Unicast prefix with prefix {} to UnicastPrefix entity",
                            InetAddressUtils.str(unicastPrefix.prefix), e);
                }
            }
        });
        return bmpUnicastPrefixes;
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
            LOG.error("Exception while mapping prefix {} to GlobalIpRib entity", prefixByAS.getPrefix(), e);
        }
        return null;

    }

    private BmpAsnInfo fetchAndBuildAsnInfo(Long asn) {
        Optional<AsnInfo> asnInfoOptional = BmpWhoIsClient.getAsnInfo(asn);
        if (asnInfoOptional.isPresent()) {
            BmpAsnInfo bmpAsnInfo = new BmpAsnInfo();
            AsnInfo asnInfo = asnInfoOptional.get();
            bmpAsnInfo.setAsn(asnInfo.getAsn());
            bmpAsnInfo.setOrgId(asnInfo.getOrgId());
            bmpAsnInfo.setAsName(asnInfo.getAsName());
            bmpAsnInfo.setOrgName(asnInfo.getOrgName());
            bmpAsnInfo.setAddress(asnInfo.getAddress());
            bmpAsnInfo.setCity(asnInfo.getCity());
            bmpAsnInfo.setStateProv(asnInfo.getStateProv());
            bmpAsnInfo.setPostalCode(asnInfo.getPostalCode());
            bmpAsnInfo.setCountry(asnInfo.getCountry());
            bmpAsnInfo.setSource(asnInfo.getSource());
            bmpAsnInfo.setRawOutput(asnInfo.getRawOutput());
            bmpAsnInfo.setLastUpdated(Date.from(Instant.now()));
            return bmpAsnInfo;
        }
        return null;
    }

    private BmpRouteInfo fetchAndBuildRouteInfo(String prefix) {
        Optional<RouteInfo> routeInfoOptional = BmpWhoIsClient.getRouteInfo(prefix);
        if(routeInfoOptional.isPresent()) {
            BmpRouteInfo bmpRouteInfo = new BmpRouteInfo();
            RouteInfo routeInfo = routeInfoOptional.get();
            bmpRouteInfo.setPrefix(routeInfo.getPrefix());
            bmpRouteInfo.setPrefixLen(routeInfo.getPrefixLen());
            bmpRouteInfo.setDescr(routeInfo.getDescription());
            bmpRouteInfo.setOriginAs(routeInfo.getOriginAs());
            bmpRouteInfo.setSource(routeInfo.getSource());
            return bmpRouteInfo;
        }
        return null;
    }

    private List<BmpAsnPathAnalysis> buildBmpAsnPath(String asnPath) {

        List<BmpAsnPathAnalysis> bmpAsnPathAnalyses = new ArrayList<>();
        String[] asnArray = asnPath.split(" ");

        Long leftAsn = 0L;
        Long rightAsn = 0L;
        Long asn = 0L;
        for (int i = 0; i < asnArray.length; i++) {
            if (asnArray[i].length() <= 0)
                break;

            try {
                asn = Long.valueOf(asnArray[i]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                break;
            }

            if (asn > 0) {
                if (i + 1 < asnArray.length) {

                    if (asnArray[i + 1].length() <= 0)
                        break;

                    try {
                        rightAsn = Long.valueOf(asnArray[i + 1]);

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        break;
                    }

                    if (rightAsn.equals(asn)) {
                        continue;
                    }

                    Boolean isPeeringAsn = (i == 0 || i == 1) ? TRUE : FALSE;
                    BmpAsnPathAnalysis bmpAsnPathAnalysis = bmpAsnPathAnalysisDao.findByAsnPath(asn, leftAsn, rightAsn, isPeeringAsn);
                    if (bmpAsnPathAnalysis == null) {
                        bmpAsnPathAnalysis = new BmpAsnPathAnalysis();
                        bmpAsnPathAnalysis.setAsn(asn);
                        bmpAsnPathAnalysis.setAsnLeft(leftAsn);
                        bmpAsnPathAnalysis.setAsnRight(rightAsn);
                        bmpAsnPathAnalysis.setAsnLeftPeering(isPeeringAsn);
                    }
                    bmpAsnPathAnalysis.setLastUpdated(Date.from(Instant.now()));
                    bmpAsnPathAnalyses.add(bmpAsnPathAnalysis);

                } else {
                    // No more left in path - Origin ASN
                    BmpAsnPathAnalysis bmpAsnPathAnalysis = bmpAsnPathAnalysisDao.findByAsnPath(asn, leftAsn, 0L, false);
                    if (bmpAsnPathAnalysis == null) {
                        bmpAsnPathAnalysis = new BmpAsnPathAnalysis();
                        bmpAsnPathAnalysis.setAsn(asn);
                        bmpAsnPathAnalysis.setAsnLeft(leftAsn);
                        bmpAsnPathAnalysis.setAsnRight(0L);
                        bmpAsnPathAnalysis.setAsnLeftPeering(false);
                    }
                    bmpAsnPathAnalysis.setLastUpdated(Date.from(Instant.now()));
                    bmpAsnPathAnalyses.add(bmpAsnPathAnalysis);
                    break;
                }

                leftAsn = asn;
            }
        }

        return bmpAsnPathAnalyses;
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

    public BmpGlobalIpRibDao getBmpGlobalIpRibDao() {
        return bmpGlobalIpRibDao;
    }

    public void setBmpGlobalIpRibDao(BmpGlobalIpRibDao bmpGlobalIpRibDao) {
        this.bmpGlobalIpRibDao = bmpGlobalIpRibDao;
    }

    public BmpAsnInfoDao getBmpAsnInfoDao() {
        return bmpAsnInfoDao;
    }

    public void setBmpAsnInfoDao(BmpAsnInfoDao bmpAsnInfoDao) {
        this.bmpAsnInfoDao = bmpAsnInfoDao;
    }

    public BmpAsnPathAnalysisDao getBmpAsnPathAnalysisDao() {
        return bmpAsnPathAnalysisDao;
    }

    public void setBmpAsnPathAnalysisDao(BmpAsnPathAnalysisDao bmpAsnPathAnalysisDao) {
        this.bmpAsnPathAnalysisDao = bmpAsnPathAnalysisDao;
    }

    public SessionUtils getSessionUtils() {
        return sessionUtils;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }

    public CollectionAgentFactory getCollectionAgentFactory() {
        return collectionAgentFactory;
    }

    public void setCollectionAgentFactory(CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public InterfaceToNodeCache getInterfaceToNodeCache() {
        return interfaceToNodeCache;
    }

    public void setInterfaceToNodeCache(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    @Override
    public Stream<CollectionSetWithAgent> getCollectionSet() {
        List<CollectionSetWithAgent> collectionSetWithAgentList = new ArrayList<>();
        while (!collectionSetQueue.isEmpty()) {
            collectionSetWithAgentList.add(collectionSetQueue.poll());
        }
        return collectionSetWithAgentList.stream();
    }


    static class AsnKey {
        private final String peerHashId;
        private final Long peerAsn;

        public AsnKey(String peerHashId, Long peerAsn) {
            this.peerHashId = peerHashId;
            this.peerAsn = peerAsn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AsnKey asnKey = (AsnKey) o;
            return Objects.equals(peerHashId, asnKey.peerHashId) &&
                    Objects.equals(peerAsn, asnKey.peerAsn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(peerHashId, peerAsn);
        }
    }

    static class PrefixKey {
        private final String peerHashId;
        private final String prefix;
        private final Integer prefixLen;

        public PrefixKey(String peerHashId, String prefix, Integer prefixLen) {
            this.peerHashId = peerHashId;
            this.prefix = prefix;
            this.prefixLen = prefixLen;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrefixKey prefixKey = (PrefixKey) o;
            return Objects.equals(peerHashId, prefixKey.peerHashId) &&
                    Objects.equals(prefix, prefixKey.prefix) &&
                    Objects.equals(prefixLen, prefixKey.prefixLen);
        }

        @Override
        public int hashCode() {
            return Objects.hash(peerHashId, prefix, prefixLen);
        }
    }
}
