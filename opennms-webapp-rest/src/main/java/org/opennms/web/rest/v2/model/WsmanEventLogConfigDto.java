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
package org.opennms.web.rest.v2.model;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.wsman.eventlog.EventMapping;
import org.opennms.netmgt.config.wsman.eventlog.Log;
import org.opennms.netmgt.config.wsman.eventlog.Package;
import org.opennms.netmgt.config.wsman.eventlog.WsmanEventlogConfiguration;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON view of wsman-eventlog-configuration.xml. The same shape is accepted on PUT,
 * where {@code version} must match the file the page loaded.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsmanEventLogConfigDto {

    public String version;
    public Integer threads;
    public Integer retries;
    public String targetRefreshInterval;
    public List<PackageDto> packages = new ArrayList<>();

    public static class PackageDto {
        public String name;
        public String filter;
        public List<LogDto> logs = new ArrayList<>();
        public List<EventMappingDto> eventMappings = new ArrayList<>();
    }

    public static class LogDto {
        public String name;
        public Boolean enabled;
        public Long interval;
        public Integer maxRecords;
        public String lookback;
        public String levels;
        public String includeEventIds;
        public String excludeEventIds;
        public String mode;
    }

    public static class EventMappingDto {
        public String logfile;
        public String source;
        public Integer eventId;
        public String uei;
        public String severity;
    }

    public static WsmanEventLogConfigDto from(final WsmanEventlogConfiguration config, final String version) {
        final WsmanEventLogConfigDto dto = new WsmanEventLogConfigDto();
        dto.version = version;
        dto.threads = config.getThreads();
        dto.retries = config.getRetries();
        dto.targetRefreshInterval = config.getTargetRefreshInterval();
        for (final Package pkg : config.getPackages()) {
            final PackageDto p = new PackageDto();
            p.name = pkg.getName();
            p.filter = pkg.getFilter();
            for (final Log log : pkg.getLogs()) {
                final LogDto l = new LogDto();
                l.name = log.getName();
                l.enabled = log.isEnabled();
                l.interval = log.getInterval();
                l.maxRecords = log.getMaxRecords();
                l.lookback = log.getLookback();
                l.levels = log.getLevels();
                l.includeEventIds = log.getIncludeEventIds();
                l.excludeEventIds = log.getExcludeEventIds();
                l.mode = log.getMode();
                p.logs.add(l);
            }
            for (final EventMapping mapping : pkg.getEventMappings()) {
                final EventMappingDto m = new EventMappingDto();
                m.logfile = mapping.getLogfile();
                m.source = mapping.getSource();
                m.eventId = mapping.getEventId();
                m.uei = mapping.getUei();
                m.severity = mapping.getSeverity();
                p.eventMappings.add(m);
            }
            dto.packages.add(p);
        }
        return dto;
    }

    /** The JAXB tree for the file; validation happens in the REST service before this runs. */
    public WsmanEventlogConfiguration toConfig() {
        final WsmanEventlogConfiguration config = new WsmanEventlogConfiguration();
        config.setThreads(threads);
        config.setRetries(retries);
        config.setTargetRefreshInterval(targetRefreshInterval);
        for (final PackageDto p : packages) {
            final Package pkg = new Package();
            pkg.setName(p.name);
            pkg.setFilter(p.filter);
            for (final LogDto l : p.logs) {
                final Log log = new Log();
                log.setName(l.name);
                log.setEnabled(l.enabled);
                log.setInterval(l.interval);
                log.setMaxRecords(l.maxRecords);
                log.setLookback(blankToNull(l.lookback));
                log.setLevels(blankToNull(l.levels));
                log.setIncludeEventIds(blankToNull(l.includeEventIds));
                log.setExcludeEventIds(blankToNull(l.excludeEventIds));
                log.setMode(blankToNull(l.mode));
                pkg.getLogs().add(log);
            }
            for (final EventMappingDto m : p.eventMappings) {
                final EventMapping mapping = new EventMapping();
                mapping.setLogfile(blankToNull(m.logfile));
                mapping.setSource(blankToNull(m.source));
                mapping.setEventId(m.eventId == null ? 0 : m.eventId);
                mapping.setUei(m.uei);
                mapping.setSeverity(blankToNull(m.severity));
                pkg.getEventMappings().add(mapping);
            }
            config.getPackages().add(pkg);
        }
        return config;
    }

    private static String blankToNull(final String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
