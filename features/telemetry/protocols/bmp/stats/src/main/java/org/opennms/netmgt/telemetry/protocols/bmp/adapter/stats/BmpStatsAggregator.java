/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.stats;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnInfoDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRib;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRibDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpIpRibLogDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfoDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfoDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByAsn;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByAsnDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPeer;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPeerDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPrefixDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsIpOrigins;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsIpOriginsDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsPeerRib;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsPeerRibDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefixDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.PrefixByAS;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsByAsn;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsByPeer;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsByPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsIpOrigins;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsPeerRib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Stats Aggregation happen completely on OpenNMS.
 */
public class BmpStatsAggregator {

    private static final Logger LOG = LoggerFactory.getLogger(BmpStatsAggregator.class);

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("UpdateStats-%d")
            .build();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(20,
            threadFactory);

    @Autowired
    private BmpIpRibLogDao bmpIpRibLogDao;

    @Autowired
    private BmpStatsByPeerDao bmpStatsByPeerDao;

    @Autowired
    private BmpStatsByAsnDao bmpStatsByAsnDao;

    @Autowired
    private BmpStatsByPrefixDao bmpStatsByPrefixDao;

    @Autowired
    private BmpUnicastPrefixDao bmpUnicastPrefixDao;

    @Autowired
    private BmpStatsPeerRibDao bmpStatsPeerRibDao;

    @Autowired
    private BmpGlobalIpRibDao bmpGlobalIpRibDao;

    @Autowired
    private BmpAsnInfoDao bmpAsnInfoDao;

    @Autowired
    private BmpRouteInfoDao bmpRouteInfoDao;

    @Autowired
    private BmpStatsIpOriginsDao bmpStatsIpOriginsDao;

    @Autowired
    private BmpRpkiInfoDao bmpRpkiInfoDao;

    @Autowired
    private SessionUtils sessionUtils;

    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(this::updatePeerStats, 0, 5, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::updateStatsByAsn, 0, 5, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::updateStatsByPrefix, 0, 5, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::updatePeerRibCountStats, 0, 15, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::updateGlobalRibsAndAsnInfo, 0, 60, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::updateStatsIpOrigins, 0, 60, TimeUnit.MINUTES);
    }

    public void destroy() {
        scheduledExecutorService.shutdown();
    }

    private void updateGlobalRibsAndAsnInfo() {
        LOG.debug("Updating GlobalRibs ++");
        setShouldDeleteForExisting();
        List<PrefixByAS> prefixByASList = bmpUnicastPrefixDao.getPrefixesGroupedByAS();
        LOG.debug("Retrieved {} PrefixByAS elements", prefixByASList.size());
        prefixByASList.forEach(prefixByAS -> {
            BmpGlobalIpRib bmpGlobalIpRib = buildGlobalIpRib(prefixByAS);
            if (bmpGlobalIpRib != null) {
                try {
                    bmpGlobalIpRibDao.saveOrUpdate(bmpGlobalIpRib);
                } catch (Exception e) {
                    LOG.error("Exception while persisting BMP global iprib  {}", bmpGlobalIpRib, e);
                }

            }
        });
        deleteExpiredGlobalRibs();
        LOG.debug("Updating GlobalRibs --");
    }

    private void setShouldDeleteForExisting() {

        List<BmpGlobalIpRib> bmpGlobalIpRibs = bmpGlobalIpRibDao.findAll();
        bmpGlobalIpRibs.forEach(bmpGlobalIpRib -> {
            bmpGlobalIpRib.setShouldDelete(true);
            bmpGlobalIpRibDao.update(bmpGlobalIpRib);
        });
    }

    private void deleteExpiredGlobalRibs() {
        // Delete Global ribs before given time in Secs.
        LOG.debug("Deleting expired global ribs ++ ");
        bmpGlobalIpRibDao.deleteGlobalRibsBeforeGivenTime(2*60*60);
        LOG.debug("Deleting expired global ribs -- ");
    }

    private BmpGlobalIpRib buildGlobalIpRib(PrefixByAS prefixByAS) {
        try {
            BmpGlobalIpRib bmpGlobalIpRib = bmpGlobalIpRibDao.findByPrefixAndAS(prefixByAS.getPrefix(), prefixByAS.getOriginAs());
            if (bmpGlobalIpRib == null) {
                bmpGlobalIpRib = new BmpGlobalIpRib();
                bmpGlobalIpRib.setPrefix(prefixByAS.getPrefix());
                bmpGlobalIpRib.setPrefixLen(prefixByAS.getPrefixLen());
                bmpGlobalIpRib.setTimeStamp(prefixByAS.getTimeStamp());
                bmpGlobalIpRib.setRecvOriginAs(prefixByAS.getOriginAs());
                String prefix = bmpGlobalIpRib.getPrefix();
                if (!Strings.isNullOrEmpty(prefix)) {
                    BmpRouteInfo bmpRouteInfo = fetchRouteInfo(prefix);
                    if (bmpRouteInfo != null) {
                        bmpGlobalIpRib.setIrrOriginAs(bmpRouteInfo.getOriginAs());
                        bmpGlobalIpRib.setIrrSource(bmpRouteInfo.getSource());
                    }
                }
                // Fetch Rpki info
                BmpRpkiInfo bmpRpkiInfo = bmpRpkiInfoDao.findMatchingRpkiInfoForGlobalRIb(prefix, bmpGlobalIpRib.getPrefixLen());
                if(bmpRpkiInfo != null) {
                    bmpGlobalIpRib.setRpkiOriginAs(bmpRpkiInfo.getOriginAs());
                }
            }
            bmpGlobalIpRib.setNumPeers(prefixByAS.getCount());
            return bmpGlobalIpRib;
        } catch (Exception e) {
            LOG.error("Exception while mapping prefix {} to GlobalIpRib entity", prefixByAS.getPrefix(), e);
        }
        return null;

    }


    private BmpRouteInfo fetchRouteInfo(String prefix) {
        List<BmpRouteInfo> routeInfoList = bmpRouteInfoDao.findByPrefix(prefix);
        // Any RouteInfo will have same source and originAsn
        if (routeInfoList.size() > 0) {
            return routeInfoList.get(0);
        }
        return null;
    }

    private void updateStatsIpOrigins() {
        LOG.debug("Updating StatsIpOrigins ++");
        List<StatsIpOrigins> statsIpOrigins = bmpGlobalIpRibDao.getStatsIpOrigins();
        LOG.debug("Retrieved {} StatsIpOrigins elements", statsIpOrigins.size());

        statsIpOrigins.forEach(stat -> {
            BmpStatsIpOrigins bmpStatsIpOrigins = buildBmpStatsOrigins(stat);
            try {
                bmpStatsIpOriginsDao.saveOrUpdate(bmpStatsIpOrigins);
            } catch (Exception e) {
                // Possibly unique constraint violation, query and update.
                BmpStatsIpOrigins retrieved = bmpStatsIpOriginsDao.findByAsnAndIntervalTime(bmpStatsIpOrigins.getAsn(), bmpStatsIpOrigins.getTimestamp());
                if (retrieved != null) {
                    retrieved.setV4prefixes(bmpStatsIpOrigins.getV4prefixes());
                    retrieved.setV6prefixes(bmpStatsIpOrigins.getV6prefixes());
                    retrieved.setV4withrpki(bmpStatsIpOrigins.getV4withrpki());
                    retrieved.setV6withrpki(bmpStatsIpOrigins.getV6withrpki());
                    retrieved.setV4withirr(bmpStatsIpOrigins.getV4withirr());
                    retrieved.setV6withirr(bmpStatsIpOrigins.getV6withirr());
                    saveBmpStatsIpOrigin(retrieved);
                }
            }
        });
        LOG.debug("Updating StatsIpOrigins --");
    }

    private void saveBmpStatsIpOrigin(BmpStatsIpOrigins bmpStatsIpOrigins) {
        try {
            bmpStatsIpOriginsDao.saveOrUpdate(bmpStatsIpOrigins);
        } catch (Exception e) {
            LOG.error("Exception while persisting BMP Stats IpOrigin {}", bmpStatsIpOrigins, e);
        }
    }


    private void updatePeerStats() {

        LOG.debug("Updating StatsByPeer ++");
        List<StatsByPeer> statsByPeer = bmpIpRibLogDao.getStatsByPeerForInterval("'5 min'");
        LOG.debug("Retrieved {} StatsByPeer elements", statsByPeer.size());

        statsByPeer.forEach(stat -> {
            BmpStatsByPeer bmpStatsByPeer = buildBmpStatsByPeer(stat);
            try {
                bmpStatsByPeerDao.saveOrUpdate(bmpStatsByPeer);
            } catch (Exception e) {
                // // Possibly unique constraint violation, query and update.
                BmpStatsByPeer retrieved = bmpStatsByPeerDao.findByPeerAndIntervalTime(bmpStatsByPeer.getPeerHashId(), bmpStatsByPeer.getTimestamp());
                if (retrieved != null) {
                    retrieved.setUpdates(bmpStatsByPeer.getUpdates());
                    retrieved.setWithdraws(bmpStatsByPeer.getWithdraws());
                    saveBmpStatsByPeer(retrieved);
                }
            }
        });
        LOG.debug("Updating StatsByPeer --");

    }

    private void saveBmpStatsByPeer(BmpStatsByPeer bmpStatsByPeer) {
        try {
            bmpStatsByPeerDao.saveOrUpdate(bmpStatsByPeer);
        } catch (Exception e) {
            LOG.error("Exception while persisting BMP Stats by Peer {}", bmpStatsByPeer, e);
        }
    }


    private void updateStatsByAsn() {
        LOG.debug("Updating StatsByAsn ++");
        List<StatsByAsn> statsByAsnList = bmpIpRibLogDao.getStatsByAsnForInterval("'5 min'");
        LOG.debug("Retrieved {} StatsByAsn elements", statsByAsnList.size());

        statsByAsnList.forEach(stat -> {
            BmpStatsByAsn bmpStatsByAsn = buildBmpStatsByAsn(stat);
            try {
                bmpStatsByAsnDao.saveOrUpdate(bmpStatsByAsn);
            } catch (Exception e) {
                // Possibly unique constraint violation, query and update.
                BmpStatsByAsn retrieved = bmpStatsByAsnDao.findByAsnAndIntervalTime(bmpStatsByAsn.getPeerHashId(), bmpStatsByAsn.getOriginAsn(), bmpStatsByAsn.getTimestamp());
                if (retrieved != null) {
                    retrieved.setUpdates(bmpStatsByAsn.getUpdates());
                    retrieved.setWithdraws(bmpStatsByAsn.getWithdraws());
                    saveStatsByAsn(retrieved);
                }
            }
        });
        LOG.debug("Updating StatsByAsn --");
    }

    private void saveStatsByAsn(BmpStatsByAsn bmpStatsByAsn) {
        try {
            bmpStatsByAsnDao.saveOrUpdate(bmpStatsByAsn);
        } catch (Exception e) {
            LOG.error("Exception while persisting BMP Stats by Asn {}", bmpStatsByAsn, e);
        }
    }

    private void updateStatsByPrefix() {
        LOG.debug("Updating StatsByPrefix ++");
        List<StatsByPrefix> statsByPrefixList = bmpIpRibLogDao.getStatsByPrefixForInterval("'5 min'");
        LOG.debug("Retrieved {} StatsByPrefix elements", statsByPrefixList.size());

        statsByPrefixList.forEach(stat -> {
            BmpStatsByPrefix bmpStatsByPrefix = buildBmpStatsByPrefix(stat);
            try {
                bmpStatsByPrefixDao.saveOrUpdate(bmpStatsByPrefix);
            } catch (Exception e) {
                // Possibly unique constraint violation, query and update.
                BmpStatsByPrefix retrieved = bmpStatsByPrefixDao.findByPrefixAndIntervalTime(bmpStatsByPrefix.getPeerHashId(), bmpStatsByPrefix.getPrefix(),
                        bmpStatsByPrefix.getTimestamp());
                if (retrieved != null) {
                    retrieved.setUpdates(bmpStatsByPrefix.getUpdates());
                    retrieved.setWithdraws(bmpStatsByPrefix.getWithdraws());
                    saveBmpStatsByPrefix(retrieved);
                }
            }
        });
        LOG.debug("Updating StatsByPrefix --");
    }

    private void saveBmpStatsByPrefix(BmpStatsByPrefix bmpStatsByPrefix) {
        try {
            bmpStatsByPrefixDao.saveOrUpdate(bmpStatsByPrefix);
        } catch (Exception e) {
            LOG.error("Exception while persisting BMP Stats by Prefix {}", bmpStatsByPrefix, e);
        }
    }

    private void updatePeerRibCountStats() {
        LOG.debug("Updating StatsPeerRib ++");
        List<StatsPeerRib> statsPeerRibs = bmpUnicastPrefixDao.getPeerRibCountsByPeer();
        LOG.debug("Retrieved {} StatsPeerRib elements", statsPeerRibs.size());

        statsPeerRibs.forEach(statsPeerRib -> {
            BmpStatsPeerRib bmpStatsPeerRib = buildBmpStatPeerRibCount(statsPeerRib);
            try {
                bmpStatsPeerRibDao.saveOrUpdate(bmpStatsPeerRib);
            } catch (Exception e) {
                // Possibly unique constraint violation, query and update.
                BmpStatsPeerRib retrieved = bmpStatsPeerRibDao.findByPeerAndIntervalTime(bmpStatsPeerRib.getPeerHashId(), bmpStatsPeerRib.getTimestamp());
                if (retrieved != null) {
                    retrieved.setV6prefixes(bmpStatsPeerRib.getV6prefixes());
                    retrieved.setV4prefixes(bmpStatsPeerRib.getV4prefixes());
                    saveStatsPeerRib(retrieved);
                }
            }
        });
        LOG.debug("Updating StatsPeerRib --");

    }

    private void saveStatsPeerRib(BmpStatsPeerRib bmpStatsPeerRib) {
        try {
            bmpStatsPeerRibDao.saveOrUpdate(bmpStatsPeerRib);
        } catch (Exception e) {
            LOG.error("Exception while persisting BMP Stats Peer Rib {}", bmpStatsPeerRib, e);
        }

    }

    private BmpStatsPeerRib buildBmpStatPeerRibCount(StatsPeerRib statsPeerRib) {
        BmpStatsPeerRib bmpStatsPeerRib = new BmpStatsPeerRib();
        bmpStatsPeerRib.setPeerHashId(statsPeerRib.getPeerHashId());
        bmpStatsPeerRib.setTimestamp(statsPeerRib.getIntervalTime());
        bmpStatsPeerRib.setV4prefixes(statsPeerRib.getV4prefixes());
        bmpStatsPeerRib.setV6prefixes(statsPeerRib.getV6prefixes());
        return bmpStatsPeerRib;
    }

    private BmpStatsByPeer buildBmpStatsByPeer(StatsByPeer statsByPeer) {
        BmpStatsByPeer bmpStatsByPeer = new BmpStatsByPeer();
        bmpStatsByPeer.setPeerHashId(statsByPeer.getPeerHashId());
        bmpStatsByPeer.setTimestamp(statsByPeer.getIntervalTime());
        bmpStatsByPeer.setUpdates(statsByPeer.getUpdates());
        bmpStatsByPeer.setWithdraws(statsByPeer.getWithdraws());
        return bmpStatsByPeer;
    }

    private BmpStatsByAsn buildBmpStatsByAsn(StatsByAsn statsByAsn) {
        BmpStatsByAsn bmpStatsByAsn = new BmpStatsByAsn();
        bmpStatsByAsn.setPeerHashId(statsByAsn.getPeerHashId());
        bmpStatsByAsn.setOriginAsn(statsByAsn.getOriginAs());
        bmpStatsByAsn.setTimestamp(statsByAsn.getIntervalTime());
        bmpStatsByAsn.setUpdates(statsByAsn.getUpdates());
        bmpStatsByAsn.setWithdraws(statsByAsn.getWithdraws());
        return bmpStatsByAsn;
    }

    private BmpStatsByPrefix buildBmpStatsByPrefix(StatsByPrefix statsByPrefix) {
        BmpStatsByPrefix bmpStatsByPrefix = new BmpStatsByPrefix();
        bmpStatsByPrefix.setPeerHashId(statsByPrefix.getPeerHashId());
        bmpStatsByPrefix.setPrefix(statsByPrefix.getPrefix());
        bmpStatsByPrefix.setPrefixLen(statsByPrefix.getPrefixLen());
        bmpStatsByPrefix.setTimestamp(statsByPrefix.getIntervalTime());
        bmpStatsByPrefix.setUpdates(statsByPrefix.getUpdates());
        bmpStatsByPrefix.setWithdraws(statsByPrefix.getWithdraws());
        return bmpStatsByPrefix;
    }

    private BmpStatsIpOrigins buildBmpStatsOrigins(StatsIpOrigins statsIpOrigins) {

        BmpStatsIpOrigins bmpStatsIpOrigins = new BmpStatsIpOrigins();
        bmpStatsIpOrigins.setAsn(statsIpOrigins.getRecvOriginAs());
        bmpStatsIpOrigins.setTimestamp(statsIpOrigins.getIntervalTime());
        bmpStatsIpOrigins.setV4prefixes(statsIpOrigins.getV4prefixes());
        bmpStatsIpOrigins.setV6prefixes(statsIpOrigins.getV6prefixes());
        bmpStatsIpOrigins.setV4withrpki(statsIpOrigins.getV4withrpki());
        bmpStatsIpOrigins.setV6withrpki(statsIpOrigins.getV6withrpki());
        bmpStatsIpOrigins.setV4withirr(statsIpOrigins.getV4withirr());
        bmpStatsIpOrigins.setV6withirr(statsIpOrigins.getV6withirr());
        return bmpStatsIpOrigins;

    }

    public void setBmpIpRibLogDao(BmpIpRibLogDao bmpIpRibLogDao) {
        this.bmpIpRibLogDao = bmpIpRibLogDao;
    }

    public void setBmpStatsByPeerDao(BmpStatsByPeerDao bmpStatsByPeerDao) {
        this.bmpStatsByPeerDao = bmpStatsByPeerDao;
    }

    public void setBmpStatsByAsnDao(BmpStatsByAsnDao bmpStatsByAsnDao) {
        this.bmpStatsByAsnDao = bmpStatsByAsnDao;
    }

    public void setBmpStatsByPrefixDao(BmpStatsByPrefixDao bmpStatsByPrefixDao) {
        this.bmpStatsByPrefixDao = bmpStatsByPrefixDao;
    }

    public void setBmpUnicastPrefixDao(BmpUnicastPrefixDao bmpUnicastPrefixDao) {
        this.bmpUnicastPrefixDao = bmpUnicastPrefixDao;
    }

    public void setBmpStatsPeerRibDao(BmpStatsPeerRibDao bmpStatsPeerRibDao) {
        this.bmpStatsPeerRibDao = bmpStatsPeerRibDao;
    }

    public void setBmpGlobalIpRibDao(BmpGlobalIpRibDao bmpGlobalIpRibDao) {
        this.bmpGlobalIpRibDao = bmpGlobalIpRibDao;
    }

    public void setBmpAsnInfoDao(BmpAsnInfoDao bmpAsnInfoDao) {
        this.bmpAsnInfoDao = bmpAsnInfoDao;
    }

    public void setBmpRouteInfoDao(BmpRouteInfoDao bmpRouteInfoDao) {
        this.bmpRouteInfoDao = bmpRouteInfoDao;
    }

    public void setBmpStatsIpOriginsDao(BmpStatsIpOriginsDao bmpStatsIpOriginsDao) {
        this.bmpStatsIpOriginsDao = bmpStatsIpOriginsDao;
    }

    public void setBmpRpkiInfoDao(BmpRpkiInfoDao bmpRpkiInfoDao) {
        this.bmpRpkiInfoDao = bmpRpkiInfoDao;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }
}
