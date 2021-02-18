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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class RouteInfoClient {

    private static final Logger LOG = LoggerFactory.getLogger(RouteInfoClient.class);

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("UpdateRouteInfo-%d")
            .build();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
            threadFactory);

    private final String routeInfoDbPath;

    private BmpRouteInfoDao bmpRouteInfoDao;


    public RouteInfoClient(String routeInfoDbPath) {
        this.routeInfoDbPath = routeInfoDbPath;
    }

    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(() -> updateRouteInfo(routeInfoDbPath), 1, 24, TimeUnit.HOURS);
    }

    public void destroy() {
        scheduledExecutorService.shutdown();
    }


    private void updateRouteInfo(String folderName) {

        if (Strings.isNullOrEmpty(folderName)) {
            return;
        }
        List<RouteInfo> routeInfoList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(folderName))) {

            paths.filter(Files::isRegularFile).forEach(path -> {
                 List<RouteInfo> returned = parseEachFile(path);
                routeInfoList.addAll(returned);
            });
        } catch (IOException e) {
            LOG.error("Exception while walking through files in folder {}", folderName, e);
        }
        saveAndUpdateInDB(routeInfoList);
    }

    @VisibleForTesting
    List<RouteInfo> parseEachFile(Path dbPath) {
        try {
            Stream<String> lines = Files.lines(dbPath);
            return RouteInfo.parseRouteInfo(lines);
        } catch (IOException e) {
            LOG.error("Exception while reading lines from path {} ", dbPath.toString());
        }
        return new ArrayList<>();
    }

    private void saveAndUpdateInDB(List<RouteInfo> routeInfos) {

        routeInfos.forEach(routeInfo -> {
            BmpRouteInfo bmpRouteInfo = buildBmpRouteInfo(routeInfo);
            if (bmpRouteInfo != null && bmpRouteInfoDao != null) {
                try {
                    bmpRouteInfoDao.saveOrUpdate(bmpRouteInfo);
                } catch (Exception e) {
                    LOG.error("Exception while persisting BMP Route Info {}", bmpRouteInfo, e);
                }
            }
        });
    }


    private BmpRouteInfo buildBmpRouteInfo(RouteInfo routeInfo) {
        String prefix = routeInfo.getPrefix();
        Integer prefixLen = routeInfo.getPrefixLen();
        Long originAs = routeInfo.getOriginAs();
        if (prefix != null && originAs != null && prefixLen != null) {
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

    public void setBmpRouteInfoDao(BmpRouteInfoDao bmpRouteInfoDao) {
        this.bmpRouteInfoDao = bmpRouteInfoDao;
    }
}
