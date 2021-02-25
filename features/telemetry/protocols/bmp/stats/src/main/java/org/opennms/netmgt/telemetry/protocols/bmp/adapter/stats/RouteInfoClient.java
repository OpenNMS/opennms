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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class RouteInfoClient {

    private static final Logger LOG = LoggerFactory.getLogger(RouteInfoClient.class);

    private static final Integer DEFAULT_HOUR_OF_THE_DAY = 1;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("UpdateRouteInfo-%d")
            .build();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
            threadFactory);

    private final String routeInfoDbPath;

    private Integer hourOfTheDay = DEFAULT_HOUR_OF_THE_DAY;

    @Autowired
    private BmpRouteInfoDao bmpRouteInfoDao;

    @Autowired
    private SessionUtils sessionUtils;


    public RouteInfoClient(String routeInfoDbPath) {
        this.routeInfoDbPath = routeInfoDbPath;
    }

    public void init() {
        Long midnight = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.MINUTES);
        Long hourOfTheDayInMinutes = midnight + hourOfTheDay * 60;
        scheduledExecutorService.scheduleAtFixedRate(() -> updateRouteInfo(routeInfoDbPath), hourOfTheDayInMinutes, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
    }

    public void destroy() {
        scheduledExecutorService.shutdownNow();
    }


    @VisibleForTesting
    void updateRouteInfo(String folderName) {
        LOG.debug("Update RouteInfo ++");
        if (Strings.isNullOrEmpty(folderName)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(Paths.get(folderName))) {
            paths.filter(Files::isRegularFile).forEach(this::parseAndSaveInDB);
        } catch (IOException e) {
            LOG.error("Exception while walking through files in folder {}", folderName, e);
        }
        LOG.debug("Update RouteInfo --");
    }

    private void parseAndSaveInDB(Path dbPath) {
        try {
            List<RouteInfo> returned = parseEachFile(dbPath);
            LOG.debug("Fetched {} routeinfo elements", returned.size());
            saveOrUpdateInDB(returned);
        } catch (Exception e) {
            LOG.error("Exception while persisting elements from path {} ", dbPath, e);
        }
    }

    @VisibleForTesting
    List<RouteInfo> parseEachFile(Path dbPath) {
        try {
            Stream<String> lines = Files.lines(dbPath);
            return RouteInfo.parseRouteInfo(lines);
        } catch (IOException e) {
            LOG.error("Exception while reading lines from path {} ", dbPath, e);
        }
        return new ArrayList<>();
    }

    private void saveOrUpdateInDB(List<RouteInfo> routeInfos) {

        Set<RouteInfo> batchedRouteInfo = new HashSet<>();
        for (int i = 0; i < routeInfos.size(); i++) {
            batchedRouteInfo.add(routeInfos.get(i));
            if ((i % 100 == 0 && i != 0) || i == routeInfos.size() - 1) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                Set<BmpRouteInfo> bmpRouteInfoList = buildBmpRouteInfoList(batchedRouteInfo);
                saveOrUpdateInSession(bmpRouteInfoList);
                batchedRouteInfo = new HashSet<>();
            }
        }
    }


    private void saveOrUpdateInSession(Set<BmpRouteInfo> bmpRouteInfos) {

        sessionUtils.withTransaction(() -> {
            bmpRouteInfos.forEach(routeInfo -> {
                try {
                    bmpRouteInfoDao.saveOrUpdate(routeInfo);
                } catch (Exception e) {
                    LOG.error("Exception while persisting BMP Route Info {}", routeInfo, e);
                }
            });
        });
    }


    private BmpRouteInfo buildBmpRouteInfo(RouteInfo routeInfo) {
        String prefix = routeInfo.getPrefix();
        Integer prefixLen = routeInfo.getPrefixLen();
        Long originAs = routeInfo.getOriginAs();
        String source = routeInfo.getSource();
        if (prefix != null && originAs != null && prefixLen != null && source != null) {
            BmpRouteInfo bmpRouteInfo = bmpRouteInfoDao.findByPrefixAndOriginAs(prefix, prefixLen, originAs);
            if (bmpRouteInfo == null) {
                bmpRouteInfo = new BmpRouteInfo();
                bmpRouteInfo.setPrefix(prefix);
                bmpRouteInfo.setPrefixLen(prefixLen);
                bmpRouteInfo.setOriginAs(originAs);
                bmpRouteInfo.setDescr(routeInfo.getDescription());
                bmpRouteInfo.setSource(routeInfo.getSource());
            }
            bmpRouteInfo.setLastUpdated(Date.from(Instant.now()));
            return bmpRouteInfo;
        }
        return null;
    }

    private Set<BmpRouteInfo> buildBmpRouteInfoList(Set<RouteInfo> routeInfoList) {
        Set<BmpRouteInfo> bmpRouteInfoSet = new HashSet<>();
        routeInfoList.forEach(routeInfo -> {
            BmpRouteInfo bmpRouteInfo = buildBmpRouteInfo(routeInfo);
            if (bmpRouteInfo != null) {
                bmpRouteInfoSet.add(bmpRouteInfo);
            }
        });
        return bmpRouteInfoSet;
    }


    public void setBmpRouteInfoDao(BmpRouteInfoDao bmpRouteInfoDao) {
        this.bmpRouteInfoDao = bmpRouteInfoDao;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }

    public void setHourOfTheDay(Integer hourOfTheDay) {
        this.hourOfTheDay = hourOfTheDay;
    }
}
