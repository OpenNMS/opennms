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

import org.opennms.integration.api.v1.config.thresholding.GroupDefinition;
import org.opennms.integration.api.v1.config.thresholding.ThresholdingConfigExtension;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThresholdingDao;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.FilterOperator;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThresholdingConfigExtensionManager extends ConfigExtensionManager<ThresholdingConfigExtension,
        ThresholdingConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingConfigExtensionManager.class);

    private final WriteableThresholdingDao thresholdingDao;
    private final ThresholdingService thresholdingService;

    public ThresholdingConfigExtensionManager(WriteableThresholdingDao thresholdingDao,
                                              ThresholdingService thresholdingService) {
        super(ThresholdingConfig.class, new ThresholdingConfig());
        this.thresholdingDao = Objects.requireNonNull(thresholdingDao);
        this.thresholdingService = Objects.requireNonNull(thresholdingService);
        LOG.debug("ThreshdConfigurationExtensionManager initialized.");
    }

    @Override
    protected ThresholdingConfig getConfigForExtensions(Set<ThresholdingConfigExtension> extensions) {
        ThresholdingConfig mergedConfig = new ThresholdingConfig();

        mergedConfig.setGroups(extensions.stream()
                .flatMap(e -> e.getGroupDefinitions().stream())
                .map(ThresholdingConfigExtensionManager::toGroup)
                .collect(Collectors.toList()));

        return mergedConfig;
    }

    @Override
    protected void triggerReload() {
        LOG.debug("Thresholding configuration changed. Triggering a reload.");
        thresholdingDao.onConfigChanged();
        try  {
            thresholdingService.getThresholdingSetPersister().reinitializeThresholdingSets();
            LOG.debug("Requested a reinitialize of the thresholding set persister");
        } catch (ServiceException e) {
            LOG.debug("Failed to reinitialize the thresholding set persister", e);
        }
    }

    private static Group toGroup(GroupDefinition groupDefinition) {
        Group group = new Group();

        group.setName(groupDefinition.getName());
        group.setRrdRepository(groupDefinition.getRrdRepository());
        group.setExpressions(groupDefinition.getExpressions()
                .stream()
                .map(ThresholdingConfigExtensionManager::toExpression)
                .collect(Collectors.toList()));
        group.setThresholds(groupDefinition.getThresholds()
                .stream()
                .map(ThresholdingConfigExtensionManager::toThreshold)
                .collect(Collectors.toList()));

        return group;
    }

    private static Expression toExpression(org.opennms.integration.api.v1.config.thresholding.Expression expression) {
        Expression newExpression = new Expression();

        newExpression.setExpression(expression.getExpression());
        setBaseThresholddef(newExpression, expression);

        return newExpression;
    }

    private static Threshold toThreshold(org.opennms.integration.api.v1.config.thresholding.Threshold threshold) {
        Threshold newThreshold = new Threshold();

        newThreshold.setDsName(threshold.getDsName());
        setBaseThresholddef(newThreshold, threshold);

        return newThreshold;
    }

    private static void setBaseThresholddef(Basethresholddef to,
                                            org.opennms.integration.api.v1.config.thresholding.Basethresholddef from) {
        from.getDescription().ifPresent(to::setDescription);
        from.getDsLabel().ifPresent(to::setDsLabel);
        to.setDsType(from.getDsType());
        to.setFilterOperator(toFilterOperator(from.getFilterOperator()));
        to.setRearm(from.getRearm());
        from.getRearmedUEI().ifPresent(to::setRearmedUEI);
        to.setRelaxed(from.getRelaxed());
        to.setResourceFilters(from.getResourceFilters()
                .stream()
                .map(ThresholdingConfigExtensionManager::toResourceFilter)
                .collect(Collectors.toList()));
        to.setTrigger(from.getTrigger());
        from.getTriggeredUEI().ifPresent(to::setTriggeredUEI);
        to.setType(toThresholdType(from.getType()));
        to.setValue(from.getValue());
    }

    private static FilterOperator toFilterOperator(org.opennms.integration.api.v1.config.thresholding.FilterOperator filterOperator) {
        return FilterOperator.valueOf(filterOperator.name());
    }

    private static ThresholdType toThresholdType(org.opennms.integration.api.v1.config.thresholding.ThresholdType thresholdType) {
        return ThresholdType.valueOf(thresholdType.name());
    }

    private static ResourceFilter toResourceFilter(org.opennms.integration.api.v1.config.thresholding.ResourceFilter resourceFilter) {
        ResourceFilter newResourceFilter = new ResourceFilter();

        resourceFilter.getContent().ifPresent(newResourceFilter::setContent);
        newResourceFilter.setField(resourceFilter.getField());

        return newResourceFilter;
    }
}
