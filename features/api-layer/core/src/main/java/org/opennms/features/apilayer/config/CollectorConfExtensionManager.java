/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.config.collector.CollectorConfigurationExtension;
import org.opennms.netmgt.config.api.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.ExcludeRange;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.IncludeRange;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;

public class CollectorConfExtensionManager extends ConfigExtensionManager<CollectorConfigurationExtension, CollectorConfExtensionManager.CollectorConfigurationPart> {

    public static class CollectorConfigurationPart {
        private List<Package> packages = new ArrayList<>();
        private List<Collector> collectors = new ArrayList<>();
    }

    private final CollectdConfigFactory collectdConfig;

    private final EventForwarder eventForwarder;

    public CollectorConfExtensionManager(final CollectdConfigFactory collectdConfig,
                                         final EventForwarder eventForwarder) {
        super(CollectorConfigurationPart.class, new CollectorConfigurationPart());
        this.collectdConfig = Objects.requireNonNull(collectdConfig);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
    }

    @Override
    protected CollectorConfigurationPart getConfigForExtensions(Set<CollectorConfigurationExtension> extensions) {
        CollectorConfigurationPart pollerConfigurationPart = new CollectorConfigurationPart();
        pollerConfigurationPart.packages = extensions.stream().flatMap(pce -> pce.getPackages().stream()).map(CollectorConfExtensionManager::map).collect(Collectors.toUnmodifiableList());
        pollerConfigurationPart.collectors = extensions.stream().flatMap(pce -> pce.getCollectors().stream()).map(CollectorConfExtensionManager::map).collect(Collectors.toUnmodifiableList());
        return pollerConfigurationPart;
    }

    @Override
    protected void triggerReload() {
        final CollectorConfigurationPart object = getObject();
        this.collectdConfig.setExternalData(object.packages, object.collectors);

        this.eventForwarder.sendNow(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "oia-plugins")
                                            .addParam(EventConstants.PARM_DAEMON_NAME, "collectd")
                                            .getEvent());
    }

    public static Collector map(org.opennms.integration.api.v1.config.collector.Collector apiCollector) {
        Collector result = new Collector();
        result.setClassName(apiCollector.getClassName());
        result.setParameters(apiCollector.getParameters().stream().map(CollectorConfExtensionManager::map).collect(Collectors.toList()));
        result.setService(apiCollector.getService());
        return result;
    }

    public static Parameter map(org.opennms.integration.api.v1.config.collector.Parameter apiParameter) {
        Parameter result = new Parameter();
        result.setKey(apiParameter.getKey());
        result.setValue(apiParameter.getValue());
        return result;
    }

    public static Package map(org.opennms.integration.api.v1.config.collector.Package apiPackage) {
        Package result = new Package();
        result.setName(apiPackage.getName());
        result.setFilter(new Filter(apiPackage.getFilter()));
        result.setSpecifics(apiPackage.getSpecifics());
        result.setIncludeRanges(apiPackage.getIncludeRanges().stream().map(CollectorConfExtensionManager::mapIncludeRanges).collect(Collectors.toList()));
        result.setExcludeRanges(apiPackage.getExcludeRanges().stream().map(CollectorConfExtensionManager::mapExcludeRanges).collect(Collectors.toList()));
        result.setServices(apiPackage.getServices().stream().map(CollectorConfExtensionManager::map).collect(Collectors.toList()));
        return result;
    }

    private static Service map(org.opennms.integration.api.v1.config.collector.Service apiService) {
        Service result = new Service();
        result.setName(apiService.getName());
        result.setUserDefined("false");
        result.setStatus("on");
        result.setParameters(apiService.getParameters().stream().map(CollectorConfExtensionManager::map).collect(Collectors.toList()));
        result.setInterval(apiService.getInterval());
        return result;
    }


    private static IncludeRange mapIncludeRanges(org.opennms.integration.api.v1.config.collector.AddressRange addressRange) {
        final IncludeRange result = new IncludeRange();
        result.setBegin(addressRange.getBegin());
        result.setEnd(addressRange.getEnd());
        return result;
    }

    private static ExcludeRange mapExcludeRanges(org.opennms.integration.api.v1.config.collector.AddressRange addressRange) {
        final ExcludeRange result = new ExcludeRange();
        result.setBegin(addressRange.getBegin());
        result.setEnd(addressRange.getEnd());
        return result;
    }

}
