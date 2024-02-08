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
