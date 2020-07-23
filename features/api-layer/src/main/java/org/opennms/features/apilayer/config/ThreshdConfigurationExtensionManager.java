/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.config.thresholding.PackageDefinition;
import org.opennms.integration.api.v1.config.thresholding.ThreshdConfigurationExtension;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao;
import org.opennms.netmgt.config.threshd.ExcludeRange;
import org.opennms.netmgt.config.threshd.Filter;
import org.opennms.netmgt.config.threshd.IncludeRange;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Parameter;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ServiceStatus;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreshdConfigurationExtensionManager extends ConfigExtensionManager<ThreshdConfigurationExtension,
        ThreshdConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(ThreshdConfigurationExtensionManager.class);

    private final WriteableThreshdDao threshdDao;
    private final ThresholdingService thresholdingService;

    public ThreshdConfigurationExtensionManager(WriteableThreshdDao threshdDao,
                                                ThresholdingService thresholdingService) {
        super(ThreshdConfiguration.class, new ThreshdConfiguration());
        this.threshdDao = Objects.requireNonNull(threshdDao);
        this.thresholdingService = Objects.requireNonNull(thresholdingService);
        LOG.debug("ThreshdConfigurationExtensionManager initialized.");
    }

    @Override
    protected ThreshdConfiguration getConfigForExtensions(Set<ThreshdConfigurationExtension> extensions) {
        ThreshdConfiguration mergedConfig = new ThreshdConfiguration();

        mergedConfig.setPackages(extensions.stream()
                .flatMap(e -> e.getPackages().stream())
                .map(ThreshdConfigurationExtensionManager::toPackage)
                .collect(Collectors.toList()));

        return mergedConfig;
    }

    @Override
    protected void triggerReload() {
        LOG.debug("Threshd configuration changed. Triggering a reload.");
        threshdDao.onConfigChanged();
        try {
            thresholdingService.getThresholdingSetPersister().reinitializeThresholdingSets();
            LOG.debug("Requested a reinitialize of the thresholding set persister");
        } catch (ServiceException e) {
            LOG.debug("Failed to reinitialize the thresholding set persister", e);
        }
    }

    private static Package toPackage(PackageDefinition packageDefinition) {
        Package newPackage = new Package();

        newPackage.setExcludeRanges(packageDefinition.getExcludeRanges()
                .stream()
                .map(ThreshdConfigurationExtensionManager::toExcludeRange)
                .collect(Collectors.toList()));
        newPackage.setFilter(toFilter(packageDefinition.getFilter()));
        newPackage.setIncludeRanges(packageDefinition.getIncludeRanges()
                .stream()
                .map(ThreshdConfigurationExtensionManager::toIncludeRange)
                .collect(Collectors.toList()));
        newPackage.setIncludeUrls(packageDefinition.getIncludeUrls());
        newPackage.setName(packageDefinition.getName());
        newPackage.setOutageCalendars(packageDefinition.getOutageCalendars());
        newPackage.setServices(packageDefinition.getServices()
                .stream()
                .map(ThreshdConfigurationExtensionManager::toService)
                .collect(Collectors.toList()));
        newPackage.setSpecifics(packageDefinition.getSpecifics());

        return newPackage;
    }

    private static ExcludeRange toExcludeRange(org.opennms.integration.api.v1.config.thresholding.ExcludeRange excludeRange) {
        ExcludeRange newExcludeRange = new ExcludeRange();

        newExcludeRange.setBegin(excludeRange.getBegin());
        newExcludeRange.setEnd(excludeRange.getEnd());

        return newExcludeRange;
    }

    private static Filter toFilter(org.opennms.integration.api.v1.config.thresholding.Filter filter) {
        Filter newFilter = new Filter();

        filter.getContent().ifPresent(newFilter::setContent);

        return newFilter;
    }

    private static IncludeRange toIncludeRange(org.opennms.integration.api.v1.config.thresholding.IncludeRange includeRange) {
        IncludeRange newIncludeRange = new IncludeRange();

        newIncludeRange.setBegin(includeRange.getBegin());
        newIncludeRange.setEnd(includeRange.getEnd());

        return newIncludeRange;
    }

    private static Service toService(org.opennms.integration.api.v1.config.thresholding.Service service) {
        Service newService = new Service();

        newService.setInterval(service.getInterval());
        newService.setName(service.getName());
        newService.setParameters(service.getParameters()
                .stream()
                .map(ThreshdConfigurationExtensionManager::toParameter)
                .collect(Collectors.toList()));
        service.getStatus().ifPresent(s -> newService.setStatus(toServiceStatus(s)));
        newService.setUserDefined(service.getUserDefined());

        return newService;
    }

    private static Parameter toParameter(org.opennms.integration.api.v1.config.thresholding.Parameter parameter) {
        Parameter newParameter = new Parameter();

        newParameter.setKey(parameter.getKey());
        newParameter.setValue(parameter.getValue());

        return newParameter;
    }

    private static ServiceStatus toServiceStatus(org.opennms.integration.api.v1.config.thresholding.ServiceStatus serviceStatus) {
        return ServiceStatus.valueOf(serviceStatus.name());
    }
}
