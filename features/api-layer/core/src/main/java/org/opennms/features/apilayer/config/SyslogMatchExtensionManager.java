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
package org.opennms.features.apilayer.config;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.config.syslog.SyslogMatchExtension;
import org.opennms.integration.api.v1.config.syslog.ParameterAssignment;
import org.opennms.integration.api.v1.config.syslog.SyslogMatch;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.syslogd.Match;
import org.opennms.netmgt.config.syslogd.SyslogdConfigurationGroup;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogMatchExtensionManager extends ConfigExtensionManager<SyslogMatchExtension, SyslogdConfigurationGroup> {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogMatchExtensionManager.class);

    private final SyslogdConfig syslogdConfig;

    public SyslogMatchExtensionManager(SyslogdConfig syslogdConfig) {
        super(SyslogdConfigurationGroup.class, new SyslogdConfigurationGroup());
        this.syslogdConfig = Objects.requireNonNull(syslogdConfig);
        LOG.debug("SyslogMatchExtensionManager initialized.");
    }

    @Override
    protected SyslogdConfigurationGroup getConfigForExtensions(Set<SyslogMatchExtension> syslogMatchExtensions) {
        final List<UeiMatch> orderedUeiMatches = syslogMatchExtensions.stream()
                .flatMap(ext -> ext.getSyslogMatches().stream())
                .sorted(Comparator.comparing(SyslogMatch::getPriority))
                .map(SyslogMatchExtensionManager::toUeiMatch)
                .collect(Collectors.toList());
        // Re-build the events
        SyslogdConfigurationGroup group = new SyslogdConfigurationGroup();
        group.getUeiMatches().addAll(orderedUeiMatches);
        return group;
    }

    @Override
    protected void triggerReload() {
        LOG.debug("Syslog configuration changed. Triggering a reload.");
        try {
            syslogdConfig.reload();
        } catch (IOException e) {
            LOG.warn("Reloading the syslog configuration failed. New/updated/removed match definitions may not be immediately reflected.", e);
        }
    }

    public static UeiMatch toUeiMatch(SyslogMatch syslogMatch) {
        final UeiMatch ueiMatch = new UeiMatch();
        ueiMatch.setUei(syslogMatch.getUei());

        final Match match = new Match();
        match.setType("regex");
        match.setExpression(syslogMatch.getMatchExpression());
        ueiMatch.setMatch(match);

        final List<org.opennms.netmgt.config.syslogd.ParameterAssignment> parameterAssignments = new LinkedList<>();
        for (ParameterAssignment pa : syslogMatch.getParameterAssignments()) {
            org.opennms.netmgt.config.syslogd.ParameterAssignment mappedPa = new org.opennms.netmgt.config.syslogd.ParameterAssignment();
            mappedPa.setMatchingGroup(pa.getGroupNumber());
            mappedPa.setParameterName(pa.getParameterName());
            parameterAssignments.add(mappedPa);
        }
        ueiMatch.setParameterAssignments(parameterAssignments);

        return ueiMatch;
    }

}
