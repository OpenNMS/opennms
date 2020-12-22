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

import java.util.Optional;

import org.opennms.core.utils.StringUtils;

public class RouteInfo {

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

    public static RouteInfo parseOutput(String rawOutput) {
        RouteInfo routeInfo = new RouteInfo();
        // Split output into lines
        String[] lines = rawOutput.split("\\r?\\n");
        for (String line : lines) {
            if (line.contains("route")) {
                getSubStringAfterColon(line).ifPresent(route -> {
                    if (route.contains("/")) {
                        String[] prefixArray = route.split("/", 2);
                        routeInfo.setPrefix(prefixArray[0]);
                        Integer prefixLen = StringUtils.parseInt(prefixArray[1], null);
                        if (prefixLen != null) {
                            routeInfo.setPrefixLen(Integer.parseInt(prefixArray[1]));
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
                    String originAs = origin.substring(2, origin.length());
                    Long originAsn = StringUtils.parseLong(originAs, null);
                    if (originAsn != null) {
                        routeInfo.setOriginAs(originAsn);
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
}
