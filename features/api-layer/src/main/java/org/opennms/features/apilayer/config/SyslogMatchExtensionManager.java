/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
