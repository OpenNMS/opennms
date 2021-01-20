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
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpIpRibLogDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByAsn;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByAsnDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPeer;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPeerDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByPrefixDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsPeerRib;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsPeerRibDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefixDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsByAsn;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsByPeer;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsByPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsPeerRib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class BmpStatsAggregator {

    private static final Logger LOG = LoggerFactory.getLogger(BmpStatsAggregator.class);

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("updateStats-%d")
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
    private SessionUtils sessionUtils;

    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(this::updatePeerStats, 0, 5, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::updateStatsByAsn, 0, 5, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::updateStatsByPrefix, 0, 5, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::updatePeerRibCountStats, 0, 15, TimeUnit.MINUTES);
    }

    public void destroy() {
        scheduledExecutorService.shutdown();
    }

    private void updatePeerStats() {

        LOG.debug("Updating Stat by Peer ++");
        List<StatsByPeer> statsByPeer = bmpIpRibLogDao.getStatsByPeerForInterval("'5 min'");
        if (statsByPeer.isEmpty()) {
            LOG.debug("Stats : Bmp Peer List is empty");
        } else {
            LOG.debug("Retrieved {} StatsByPeer elements", statsByPeer.size());
        }
        statsByPeer.forEach(stat -> {
            BmpStatsByPeer bmpStatsByPeer = buildBmpStatsByPeer(stat);
            try {
                bmpStatsByPeerDao.saveOrUpdate(bmpStatsByPeer);
            } catch (Exception e) {
                LOG.error("Exception while persisting BMP Stats by Peer {}", stat, e);
            }
        });
        LOG.debug("Updating Stat by Peer --");

    }

    private void updateStatsByAsn() {
        LOG.debug("Updating Stat by Asn ++");
        List<StatsByAsn> statsByAsnList = bmpIpRibLogDao.getStatsByAsnForInterval("'5 min'");
        if(statsByAsnList.isEmpty()) {
            LOG.debug("Stats : Bmp ASN List is empty");
        } else {
            LOG.debug("Retrieved {} StatsByAsn elements", statsByAsnList.size());
        }
        statsByAsnList.forEach(stat -> {
            BmpStatsByAsn bmpStatsByAsn = buildBmpStatsByAsn(stat);
            try {
                bmpStatsByAsnDao.saveOrUpdate(bmpStatsByAsn);
            } catch (Exception e) {
                LOG.error("Exception while persisting BMP Stats by Asn {}", stat, e);
            }
        });LOG.debug("Updating Stat by Asn --");
    }

    private void updateStatsByPrefix() {
        LOG.debug("Updating Stat by Prefix ++");
        List<StatsByPrefix> statsByPrefixList = bmpIpRibLogDao.getStatsByPrefixForInterval("'5 min'");
        if(statsByPrefixList.isEmpty()) {
            LOG.debug("Stats : Bmp Prefix List is empty");
        } else {
            LOG.debug("Retrieved {} StatsByPrefix elements", statsByPrefixList.size());
        }
        statsByPrefixList.forEach(stat -> {
            BmpStatsByPrefix bmpStatsByPrefix = buildBmpStatsByPrefix(stat);
            try {
                bmpStatsByPrefixDao.saveOrUpdate(bmpStatsByPrefix);
            } catch (Exception e) {
                LOG.error("Exception while persisting BMP Stats by Prefix {}", stat, e);
            }
        });
        LOG.debug("Updating Stat by Prefix --");
    }

    private void updatePeerRibCountStats() {
        LOG.debug("Updating Stats Peer Rib ++");
        List<StatsPeerRib> statsPeerRibs = bmpUnicastPrefixDao.getPeerRibCountsByPeer();
        if(statsPeerRibs.isEmpty()) {
            LOG.debug("Stats : Bmp Peer Rib is empty");
        } else {
            LOG.debug("Retrieved {} StatsPeerRib elements", statsPeerRibs.size());
        }
        statsPeerRibs.forEach( statsPeerRib -> {
            BmpStatsPeerRib bmpStatsPeerRib =  buildBmpStatPeerRibCount(statsPeerRib);
            try {
                bmpStatsPeerRibDao.saveOrUpdate(bmpStatsPeerRib);
            } catch (Exception e) {
                LOG.error("Exception while persisting BMP Stats Peer Rib {}", bmpStatsPeerRib, e);
            }
        });
        LOG.debug("Updating Stats Peer Rib --");

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

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }
}
