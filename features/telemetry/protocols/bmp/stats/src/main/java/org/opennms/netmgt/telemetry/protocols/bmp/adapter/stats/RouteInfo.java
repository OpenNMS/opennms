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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.opennms.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteInfo {

    private static final Logger LOG = LoggerFactory.getLogger(RouteInfo.class);

    private String prefix;

    private Integer prefixLen;

    private String description;

    private Long originAs;

    private String source;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getPrefixLen() {
        return prefixLen;
    }

    public void setPrefixLen(Integer prefixLen) {
        this.prefixLen = prefixLen;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOriginAs() {
        return originAs;
    }

    public void setOriginAs(Long originAs) {
        this.originAs = originAs;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static RouteInfo parseOneRecord(String rawOutput) {
        RouteInfo routeInfo = new RouteInfo();
        // Split output into lines
        String[] lines = rawOutput.split("\\r?\\n");
        for (String line : lines) {
            if (line.contains("route")) {
                getSubStringAfterColon(line).ifPresent(route -> {
                    if (route.contains("/")) {
                        String[] prefixArray = route.split("/", 2);
                        if(isValidIpAddress(prefixArray[0])) {
                            routeInfo.setPrefix(prefixArray[0]);
                        }
                        Integer prefixLen = StringUtils.parseInt(prefixArray[1], null);
                        if (prefixLen != null) {
                            routeInfo.setPrefixLen(prefixLen);
                        }
                    }
                });
            }
            if (line.contains("descr")) {
                getSubStringAfterColon(line).ifPresent(routeInfo::setDescription);
            }
            if (line.contains("source")) {
                getSubStringAfterColon(line).ifPresent(routeInfo::setSource);
            }
            if (line.contains("origin")) {
                getSubStringAfterColon(line).ifPresent(origin -> {
                    if(origin.length() > 2 ) {
                        String originAsnString = origin.substring(2, origin.length());
                        Long originAsn = StringUtils.parseLong(originAsnString, null);
                        if (originAsn != null) {
                            routeInfo.setOriginAs(originAsn);
                        }
                    }
                });
            }
        }
        return routeInfo;
    }

    public static List<RouteInfo> parseRouteInfo(Stream<String> lines) {
        List<RouteInfo> routeInfoList = new ArrayList<>();
        List<String> record = new ArrayList<>();
        for(String line: (Iterable<String>) lines::iterator) {
            // All the route info related lines consists of :
            if(line.contains(":")) {
                // Add to the record till empty line.
                record.add(line);
            }
            // Empty line is a completed routeInfo Object
            if(line.length() == 0) {
                // Parse the record
               RouteInfo routeInfo = parseOneRecord(record);
               routeInfoList.add(routeInfo);
               // Reset the record again.
               record = new ArrayList<>();
            }
        }
        if(record.size() > 0) {
            // Parse the record
            RouteInfo routeInfo = parseOneRecord(record);
            routeInfoList.add(routeInfo);
        }
        return routeInfoList;
    }

    private static RouteInfo parseOneRecord(List<String> lines) {
        RouteInfo routeInfo = new RouteInfo();
        for (String line : lines) {
            if (line.contains("route") || line.contains("route6")) {
                getSubStringAfterColon(line).ifPresent(route -> {
                    if (route.contains("/")) {
                        String[] prefixArray = route.split("/", 2);
                        if(isValidIpAddress(prefixArray[0])) {
                            routeInfo.setPrefix(prefixArray[0]);
                            Integer prefixLen = StringUtils.parseInt(prefixArray[1], null);
                            if (prefixLen != null) {
                                routeInfo.setPrefixLen(prefixLen);
                            }
                        }
                    }
                });
            }
            if (line.contains("descr")) {
                getSubStringAfterColon(line).ifPresent(routeInfo::setDescription);
            }
            if (line.contains("source")) {
                getSubStringAfterColon(line).ifPresent(routeInfo::setSource);
            }
            if (line.contains("origin")) {
                getSubStringAfterColon(line).ifPresent(origin -> {
                    if(origin.length() > 2 ) {
                        String originAsnString = origin.substring(2, origin.length());
                        Long originAsn = StringUtils.parseLong(originAsnString, null);
                        if (originAsn != null) {
                            routeInfo.setOriginAs(originAsn);
                        }
                    }
                });
            }
        }
        return routeInfo;
    }


    private static Optional<String> getSubStringAfterColon(String segment) {
        int index = segment.indexOf(":");
        if (index > 0) {
            String value = segment.substring(index + 1);
            return Optional.of(value.trim());
        }
        return Optional.empty();
    }

    static boolean isValidIpAddress(String prefix) {

        try {
            InetAddress.getByName(prefix);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteInfo routeInfo = (RouteInfo) o;
        return Objects.equals(prefix, routeInfo.prefix) &&
                Objects.equals(prefixLen, routeInfo.prefixLen) &&
                Objects.equals(originAs, routeInfo.originAs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, prefixLen, originAs);
    }
}
