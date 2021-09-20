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

import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnInfoDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRibDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class AsnInfoClient {

    private static final Logger LOG = LoggerFactory.getLogger(AsnInfoClient.class);

    private static final Integer DEFAULT_HOUR_OF_THE_DAY = 2;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("UpdateAsnInfo-%d")
            .build();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
            threadFactory);

    @Autowired
    private BmpAsnInfoDao bmpAsnInfoDao;

    @Autowired
    private BmpGlobalIpRibDao bmpGlobalIpRibDao;

    @Autowired
    private SessionUtils sessionUtils;

    private Integer hourOfTheDay = DEFAULT_HOUR_OF_THE_DAY;

    public void init() {
        Long hourOfTheDayInMinutes = Utils.getHourOfTheDayInMinutes(hourOfTheDay);
        scheduledExecutorService.scheduleAtFixedRate(this::updateAsnInfo, hourOfTheDayInMinutes, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
    }

    public void destroy() {
        scheduledExecutorService.shutdownNow();
    }


    private void updateAsnInfo() {
        LOG.debug("Updating AsnInfo ++");
        List<BigInteger> asnList = bmpGlobalIpRibDao.getAsnsNotExistInAsnInfo();
        LOG.debug("Fetched `{}` asn elements", asnList.size());
        saveOrUpdateInDB(asnList);
        LOG.debug("Updating AsnInfo --");
    }

    private void saveOrUpdateInDB(List<BigInteger> asnList) {
        Set<BigInteger> batchedAsns = new HashSet<>();
        for (int i = 0; i < asnList.size(); i++) {
            batchedAsns.add(asnList.get(i));
            if ((i % 100 == 0 && i != 0) || i == asnList.size() - 1) {
                Set<BmpAsnInfo> bmpAsnInfos = fetchAsnInfoForBatch(batchedAsns);
                saveOrUpdateInSession(bmpAsnInfos);
                batchedAsns = new HashSet<>();
            }
        }

    }

    private BmpAsnInfo fetchAndBuildAsnInfo(BigInteger asn) {
        Optional<AsnInfo> asnInfoOptional = BmpWhoIsClient.getAsnInfo(asn.longValue());
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

    private Set<BmpAsnInfo> fetchAsnInfoForBatch(Set<BigInteger> asnSet) {
        Set<BmpAsnInfo> bmpAsnInfos = new HashSet<>();
        asnSet.forEach(asn -> {
            BmpAsnInfo bmpAsnInfo = fetchAndBuildAsnInfo(asn);
            if (bmpAsnInfo != null) {
                bmpAsnInfos.add(bmpAsnInfo);
            }
        });
        return bmpAsnInfos;
    }

    private void saveOrUpdateInSession(Set<BmpAsnInfo> bmpAsnInfos) {
        sessionUtils.withTransaction(() -> {
            bmpAsnInfos.forEach(this::saveOrUpdateAsnInfo);
        });
    }

    private void saveOrUpdateAsnInfo(BmpAsnInfo bmpAsnInfo) {
        if (bmpAsnInfo != null) {
            try {
                bmpAsnInfoDao.saveOrUpdate(bmpAsnInfo);
            } catch (Exception e) {
                LOG.error("Exception while persisting BMP ASN Info  {}", bmpAsnInfo, e);
            }
        }
    }


    public void setBmpAsnInfoDao(BmpAsnInfoDao bmpAsnInfoDao) {
        this.bmpAsnInfoDao = bmpAsnInfoDao;
    }

    public void setBmpGlobalIpRibDao(BmpGlobalIpRibDao bmpGlobalIpRibDao) {
        this.bmpGlobalIpRibDao = bmpGlobalIpRibDao;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }

    public void setHourOfTheDay(Integer hourOfTheDay) {
        this.hourOfTheDay = hourOfTheDay;
    }
}
