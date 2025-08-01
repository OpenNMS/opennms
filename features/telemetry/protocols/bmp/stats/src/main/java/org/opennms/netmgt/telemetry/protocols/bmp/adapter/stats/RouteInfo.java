/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
