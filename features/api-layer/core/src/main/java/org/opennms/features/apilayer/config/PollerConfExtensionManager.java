/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.config.poller.PollerConfigurationExtension;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.ExcludeRange;
import org.opennms.netmgt.config.poller.IncludeRange;
import org.opennms.netmgt.config.poller.Monitor;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Rrd;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;

public class PollerConfExtensionManager extends ConfigExtensionManager<PollerConfigurationExtension, PollerConfExtensionManager.PollerConfigurationPart> {

    public static class PollerConfigurationPart {
        private List<Package> packages = new ArrayList<>();
        private List<Monitor> monitors = new ArrayList<>();;

        public List<Package> getPackages() {
            return packages;
        }

        public List<Monitor> getMonitors() {
            return monitors;
        }
    }

    private final PollerConfig pollerConfig;

    private final EventForwarder eventForwarder;

    public PollerConfExtensionManager(final PollerConfig pollerConfig,
                                      final EventForwarder eventForwarder) {
        super(PollerConfigurationPart.class, new PollerConfigurationPart());
        this.pollerConfig = Objects.requireNonNull(pollerConfig);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
    }

    @Override
    protected PollerConfigurationPart getConfigForExtensions(Set<PollerConfigurationExtension> extensions) {
        PollerConfigurationPart pollerConfigurationPart = new PollerConfigurationPart();
        pollerConfigurationPart.packages = extensions.stream().flatMap(pce -> pce.getPackages().stream()).map(PollerConfExtensionManager::map).collect(Collectors.toUnmodifiableList());
        pollerConfigurationPart.monitors = extensions.stream().flatMap(pce -> pce.getMonitors().stream()).map(PollerConfExtensionManager::map).collect(Collectors.toUnmodifiableList());
        return pollerConfigurationPart;
    }

    @Override
    protected void triggerReload() {
        final PollerConfigurationPart object = getObject();
        this.pollerConfig.setExternalData(object.packages, object.monitors);

        this.eventForwarder.sendNow(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "oia-plugins")
                                            .addParam(EventConstants.PARM_DAEMON_NAME, "pollerd")
                                            .getEvent());
    }

    public static Monitor map(org.opennms.integration.api.v1.config.poller.Monitor apiMonitor) {
        Monitor result = new Monitor();
        result.setClassName(apiMonitor.getClassName());
        result.setParameters(apiMonitor.getParameters().stream().map(PollerConfExtensionManager::map).collect(Collectors.toList()));
        result.setService(apiMonitor.getService());
        return result;
    }

    public static Parameter map(org.opennms.integration.api.v1.config.poller.Parameter apiParameter) {
        Parameter result = new Parameter();
        result.setKey(apiParameter.getKey());
        result.setValue(apiParameter.getValue());
        return result;
    }

    public static Package map(org.opennms.integration.api.v1.config.poller.Package apiPackage) {
        Package result = new Package();
        result.setDowntimes(apiPackage.getDowntimes().stream().map(PollerConfExtensionManager::map).collect(Collectors.toList()));
        result.setName(apiPackage.getName());
        result.setFilter(apiPackage.getFilter());
        result.setSpecifics(apiPackage.getSpecifics());
        result.setIncludeRanges(apiPackage.getIncludeRanges().stream().map(PollerConfExtensionManager::mapIncludeRanges).collect(Collectors.toList()));
        result.setExcludeRanges(apiPackage.getExcludeRanges().stream().map(PollerConfExtensionManager::mapExcludeRanges).collect(Collectors.toList()));
        result.setRrd(map(apiPackage.getRrd()));
        result.setServices(apiPackage.getServices().stream().map(PollerConfExtensionManager::map).collect(Collectors.toList()));
        result.setOutageCalendars(apiPackage.getOutageCalendars());
        return result;
    }

    private static Service map(org.opennms.integration.api.v1.config.poller.Service apiService) {
        Service result = new Service();
        result.setName(apiService.getName());
        result.setUserDefined(apiService.isUserDefined() ? "true" : "false");
        result.setStatus(apiService.isEnabled() ? "on" : "off");
        result.setPattern(apiService.getPattern().orElse(null));
        result.setParameters(apiService.getParameters().stream().map(PollerConfExtensionManager::map).collect(Collectors.toList()));
        result.setInterval(apiService.getInterval());
        return result;
    }


    private static IncludeRange mapIncludeRanges(org.opennms.integration.api.v1.config.poller.AddressRange addressRange) {
        final IncludeRange result = new IncludeRange();
        result.setBegin(addressRange.getBegin());
        result.setEnd(addressRange.getEnd());
        return result;
    }

    private static ExcludeRange mapExcludeRanges(org.opennms.integration.api.v1.config.poller.AddressRange addressRange) {
        final ExcludeRange result = new ExcludeRange();
        result.setBegin(addressRange.getBegin());
        result.setEnd(addressRange.getEnd());
        return result;
    }

    public static Downtime map(org.opennms.integration.api.v1.config.poller.Downtime apiDowntime) {
        Downtime result = new Downtime();
        result.setBegin(apiDowntime.getBegin().getSeconds());
        result.setInterval(apiDowntime.getInterval().orElse(null));
        result.setEnd(apiDowntime.getEnd().map(Duration::getSeconds).orElse(null));
        if (apiDowntime.getDelete().isPresent()) {
            final org.opennms.integration.api.v1.config.poller.Downtime.DeletingMode deletingMode = apiDowntime.getDelete().get();
            switch (deletingMode) {
                case ALWAYS:
                    result.setDelete(Downtime.DELETE_ALWAYS);
                    break;
                case MANAGED:
                    result.setDelete(Downtime.DELETE_MANAGED);
                    break;
                case NEVER:
                    result.setDelete(Downtime.DELETE_NEVER);
                    break;
            }
        } else {
            result.setDelete(null);
        }

        return result;
    }

    public static Rrd map(org.opennms.integration.api.v1.config.poller.Rrd apiRrd) {
        Rrd result = new Rrd();
        result.setStep(apiRrd.getStep());
        result.setRras(apiRrd.getRras());
        return result;
    }
}
