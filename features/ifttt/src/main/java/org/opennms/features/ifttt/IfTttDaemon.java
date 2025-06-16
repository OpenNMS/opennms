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
package org.opennms.features.ifttt;

import java.io.File;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.spring.FileReloadContainer;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.ifttt.config.IfTttConfig;
import org.opennms.features.ifttt.config.Trigger;
import org.opennms.features.ifttt.config.TriggerPackage;
import org.opennms.features.ifttt.config.TriggerSet;
import org.opennms.features.ifttt.helper.DefaultVariableNameExpansion;
import org.opennms.features.ifttt.helper.IfTttTrigger;
import org.opennms.features.ifttt.helper.VariableNameExpansion;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Strings;

/**
 * This class represents a daemon for polling alarms in order to generate IFTTT events.
 */
public class IfTttDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(IfTttDaemon.class);
    /**
     * the alarm dao instance
     */
    private final AlarmDao alarmDao;
    /**
     * the transaction template
     */
    private final TransactionOperations transactionOperations;
    /**
     * file reload container for the ifttt-config.xml file
     */
    private final FileReloadContainer<IfTttConfig> m_fileReloadContainer;
    /**
     * scheduler for alarm poller
     */
    private ScheduledExecutorService m_alarmPoller;
    /**
     * the config file instance
     */
    private final File ifTttConfigFile;

    private final EntityScopeProvider entityScopeProvider;

    /**
     * Constructor to create the daemon instance with a given configuraton file

     * @param alarmDao              the alarm dao instance to be used
     * @param transactionOperations the transaction template to be used
     * @param ifTttConfigFile       the configuration file to be used
     */
    public IfTttDaemon(final AlarmDao alarmDao, final TransactionOperations transactionOperations, final EntityScopeProvider entityScopeProvider, final File ifTttConfigFile) {
        this.alarmDao = alarmDao;
        this.transactionOperations = transactionOperations;
        this.ifTttConfigFile = ifTttConfigFile;
        this.entityScopeProvider = entityScopeProvider;
        // initialize the FileReloadContainer instance for the configuration file

        this.m_fileReloadContainer = new FileReloadContainer<>(ifTttConfigFile, (object, resource) -> {
            LOG.debug("Reloading configuration file {}.", resource.getFilename());
            return JaxbUtils.unmarshal(IfTttConfig.class, ifTttConfigFile);
        });
    }

    /**
     * Constructor to create the daemon instance.
     *
     * @param alarmDao              the alarm dao instance to be used
     * @param transactionOperations the transaction template to be used
     */
    public IfTttDaemon(final AlarmDao alarmDao, final TransactionOperations transactionOperations, final EntityScopeProvider entityScopeProvider) {
        this(alarmDao, transactionOperations, entityScopeProvider, Paths.get(System.getProperty("opennms.home", ""), "etc", "ifttt-config.xml").toFile());
    }

    /**
     * Starts the daemon
     */
    public void start() {
        LOG.debug("Starting IFTTT daemon.");

        if (m_fileReloadContainer.getObject().getEnabled()) {
            fireIfTttTriggerSet(m_fileReloadContainer.getObject(), "ON");
        }

        startPoller(m_fileReloadContainer.getObject().getPollInterval());
    }

    /**
     * Stops the daemon
     */
    public void stop() {
        LOG.debug("Stopping IFTTT daemon.");

        if (m_fileReloadContainer.getObject().getEnabled()) {
            fireIfTttTriggerSet(m_fileReloadContainer.getObject(), "OFF");
        }

        stopPoller();
    }

    /**
     * Initializes and starts the alarm poller.
     *
     * @param pollInterval the poller interval to be used
     */
    private void startPoller(final long pollInterval) {
        LOG.debug("Starting alarm poller (interval {}s).", pollInterval);

        m_alarmPoller = Executors.newScheduledThreadPool(1);

        m_alarmPoller.scheduleWithFixedDelay(new Runnable() {
            private Map<Boolean, Map<String, Integer>> oldAlarmCount = new HashMap<>();
            private Map<Boolean, Map<String, OnmsSeverity>> oldSeverity = new HashMap<>();

            {
                oldAlarmCount.put(Boolean.TRUE, new HashMap<>());
                oldAlarmCount.put(Boolean.FALSE, new HashMap<>());
                oldSeverity.put(Boolean.TRUE, new HashMap<>());
                oldSeverity.put(Boolean.FALSE, new HashMap<>());
            }

            private List<OnmsAlarm> filterAlarms(List<OnmsAlarm> alarms, TriggerPackage triggerPackage) {

                Stream<OnmsAlarm> stream = alarms.stream();

                if (triggerPackage.getOnlyUnacknowledged()) {
                    stream = stream.filter(alarm -> !alarm.isAcknowledged());
                }

                if (!Strings.isNullOrEmpty(triggerPackage.getCategoryFilter())) {
                    stream = stream
                            .filter(alarm -> alarm.getNodeId() != null)
                            .filter(alarm -> alarm.getNode().getCategories().stream()
                                    .anyMatch(category -> category.getName().matches(triggerPackage.getCategoryFilter())));
                }

                if (!Strings.isNullOrEmpty(triggerPackage.getReductionKeyFilter())) {
                    stream = stream
                            .filter(alarm -> !Strings.isNullOrEmpty(alarm.getReductionKey()))
                            .filter(alarm -> alarm.getReductionKey().matches(triggerPackage.getReductionKeyFilter()));
                }

                return stream.collect(Collectors.toList());
            }

            @Override
            public void run() {
                try {
                    final IfTttConfig ifTttConfig = m_fileReloadContainer.getObject();

                    if (ifTttConfig.getPollInterval() != pollInterval) {
                        restartPoller(ifTttConfig.getPollInterval());
                        return;
                    }

                    if (!ifTttConfig.getEnabled()) {
                        LOG.debug("Disabled - skipping alarm polling.");
                        return;
                    }

                    transactionOperations.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {

                            // Retrieve the alarms with an associated node and filter for matching categories.

                            final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsAlarm.class)
                                    .gt("severity", OnmsSeverity.NORMAL);

                            final List<OnmsAlarm> alarms = alarmDao.findMatching(criteriaBuilder.toCriteria());

                            for (final TriggerPackage triggerPackage : ifTttConfig.getTriggerPackages()) {

                                if (!oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).containsKey(triggerPackage.getFilterKey())) {
                                    oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).put(triggerPackage.getFilterKey(), OnmsSeverity.INDETERMINATE);
                                    oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).put(triggerPackage.getFilterKey(), 0);
                                }

                                final List<OnmsAlarm> filteredAlarms = filterAlarms(alarms, triggerPackage);

                                // Compute the maximum severity.

                                final Optional<OnmsSeverity> maxAlarmsSeverity = filteredAlarms.stream()
                                        .map(OnmsAlarm::getSeverity)
                                        .max(Comparator.naturalOrder());

                                final OnmsSeverity newSeverity = maxAlarmsSeverity.orElse(OnmsSeverity.NORMAL);
                                final int newAlarmCount = filteredAlarms.size();

                                LOG.debug("Received {} filtered, {} new severity", newAlarmCount, newSeverity);

                                final DefaultVariableNameExpansion defaultVariableNameExpansion = new DefaultVariableNameExpansion(
                                        oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getFilterKey()), newSeverity,
                                        oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getFilterKey()), newAlarmCount
                                );

                                // Trigger IFTTT event if necessary.

                                if (!newSeverity.equals(oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getFilterKey())) ||
                                        newAlarmCount != oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getFilterKey())) {
                                    fireIfTttTriggerSet(ifTttConfig, triggerPackage.getFilterKey(), newSeverity, defaultVariableNameExpansion);
                                }

                                LOG.debug("Old severity: {}, new severity: {}, old alarm count: {}, new alarm count: {}",
                                        oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getFilterKey()), newSeverity,
                                        oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getFilterKey()), newAlarmCount
                                );

                                oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).put(triggerPackage.getFilterKey(), newSeverity);
                                oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).put(triggerPackage.getFilterKey(), newAlarmCount);
                            }
                        }
                    });
                } catch (Exception e) {
                    LOG.error("Error while polling alarm table.", e);
                } finally {
                    LOG.debug("Run complete. Next poll in {}s.", pollInterval);
                }
            }
        }, pollInterval, pollInterval, TimeUnit.SECONDS);
    }

    /**
     * Restarts the poller with the given polling interval. This method is triggered by changes of the pollInterval in
     * the configuration file.
     *
     * @param pollInterval the polling interval to be used
     */
    private void restartPoller(final long pollInterval) {
        LOG.debug("Restarting alarm poller (interval {}s).", pollInterval);

        stopPoller();
        startPoller(pollInterval);
    }

    /**
     * Destroys the alarm poller.
     */
    private void stopPoller() {
        LOG.debug("Stopping alarm poller.");
        if (m_alarmPoller != null) {
            m_alarmPoller.shutdown();
            m_alarmPoller = null;
        }
    }

    /**
     * Executes a configured trigger set. This uses a simple VariableNameExpansion expansion that just return the initial
     * String instance intself.
     *
     * @param ifTttConfig the IFTTT config instance
     * @param name        the event name
     */
    private void fireIfTttTriggerSet(final IfTttConfig ifTttConfig, final String name) {
        if (ifTttConfig == null) {
            return;
        }

        for (final TriggerPackage triggerPackage : ifTttConfig.getTriggerPackages()) {
            fireIfTttTriggerSet(ifTttConfig, triggerPackage.getFilterKey(), name.toUpperCase(), string -> string);
        }
    }

    /**
     * Executes a configured trigger set with a given OnmsSeverity instance.
     *
     * @param ifTttConfig           the IFTTT config instance
     * @param filterKey             the filter
     * @param newSeverity           the new severity
     * @param variableNameExpansion the VariableNameExpansion to be used
     */
    private void fireIfTttTriggerSet(final IfTttConfig ifTttConfig, final String filterKey, final OnmsSeverity newSeverity, final VariableNameExpansion variableNameExpansion) {
        fireIfTttTriggerSet(ifTttConfig, filterKey, newSeverity.getLabel().toUpperCase(), variableNameExpansion);
    }

    /**
     * Executes a configured trigger set with a given String value.
     *
     * @param ifTttConfig           the IFTTT config instance
     * @param filterKey             the filter
     * @param name                  the event name
     * @param variableNameExpansion the VariableNameExpansion to be used
     */
    protected void fireIfTttTriggerSet(final IfTttConfig ifTttConfig, final String filterKey, final String name, final VariableNameExpansion variableNameExpansion) {
        if (ifTttConfig == null) {
            return;
        }

        final TriggerPackage triggerPackage = ifTttConfig.getTriggerPackageForFilters(filterKey);

        if (triggerPackage != null) {

            final TriggerSet triggerSet = triggerPackage.getTriggerSetForName(name);

            if (triggerSet != null) {
                for (final Trigger trigger : triggerSet.getTriggers()) {

                    new IfTttTrigger()
                            .key(Interpolator.interpolate(ifTttConfig.getKey(), entityScopeProvider.getScopeForScv()).output)
                            .event(trigger.getEventName())
                            .value1(variableNameExpansion.replace(trigger.getValue1()))
                            .value2(variableNameExpansion.replace(trigger.getValue2()))
                            .value3(variableNameExpansion.replace(trigger.getValue3()))
                            .trigger();

                    try {
                        Thread.sleep(trigger.getDelay());
                    } catch (InterruptedException e) {
                        LOG.error("Error triggering IFTTT event: ", e);
                    }
                }
            } else {
                LOG.debug("No trigger-set with name '{}' defined.", name);
            }
        } else {
            LOG.error("Error retrieving trigger package for filter {}.", filterKey);
        }
    }

    FileReloadContainer<IfTttConfig> getFileReloadContainer() {
        return m_fileReloadContainer;
    }
}
