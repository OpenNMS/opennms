/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import org.opennms.core.criteria.CriteriaBuilder;
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

    /**
     * Constructor to create the daemon instance with a given configuraton file

     * @param alarmDao              the alarm dao instance to be used
     * @param transactionOperations the transaction template to be used
     * @param ifTttConfigFile       the configuration file to be used
     */
    public IfTttDaemon(final AlarmDao alarmDao, final TransactionOperations transactionOperations, final File ifTttConfigFile) {
        this.alarmDao = alarmDao;
        this.transactionOperations = transactionOperations;
        this.ifTttConfigFile = ifTttConfigFile;
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
    public IfTttDaemon(final AlarmDao alarmDao, final TransactionOperations transactionOperations) {
        this(alarmDao, transactionOperations, Paths.get(System.getProperty("opennms.home", ""), "etc", "ifttt-config.xml").toFile());
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
                if (triggerPackage.getOnlyUnacknowledged()) {
                    return alarms.stream()
                            .filter(alarm -> alarm.getNodeId() != null)
                            .filter(alarm -> !alarm.isAcknowledged())
                            .filter(alarm -> Strings.isNullOrEmpty(triggerPackage.getCategoryFilter()) ||
                                    alarm.getNode().getCategories().stream()
                                            .anyMatch(category -> category.getName().matches(triggerPackage.getCategoryFilter())))
                            .collect(Collectors.toList());
                } else {
                    return alarms.stream()
                            .filter(alarm -> alarm.getNodeId() != null)
                            .filter(alarm -> Strings.isNullOrEmpty(triggerPackage.getCategoryFilter()) ||
                                    alarm.getNode().getCategories().stream()
                                            .anyMatch(category -> category.getName().matches(triggerPackage.getCategoryFilter())))
                            .collect(Collectors.toList());
                }
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
                                    .isNotNull("node")
                                    .gt("severity", OnmsSeverity.NORMAL);

                            final List<OnmsAlarm> alarms = alarmDao.findMatching(criteriaBuilder.toCriteria());

                            for (final TriggerPackage triggerPackage : ifTttConfig.getTriggerPackages()) {

                                if (!oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).containsKey(triggerPackage.getCategoryFilter())) {
                                    oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).put(triggerPackage.getCategoryFilter(), OnmsSeverity.INDETERMINATE);
                                    oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).put(triggerPackage.getCategoryFilter(), 0);
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
                                        oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getCategoryFilter()), newSeverity,
                                        oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getCategoryFilter()), newAlarmCount
                                );

                                // Trigger IFTTT event if necessary.

                                if (!newSeverity.equals(oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getCategoryFilter())) ||
                                        newAlarmCount != oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getCategoryFilter())) {
                                    fireIfTttTriggerSet(ifTttConfig, triggerPackage.getCategoryFilter(), newSeverity, defaultVariableNameExpansion);
                                }

                                LOG.debug("Old severity: {}, new severity: {}, old alarm count: {}, new alarm count: {}",
                                        oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getCategoryFilter()), newSeverity,
                                        oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).get(triggerPackage.getCategoryFilter()), newAlarmCount
                                );

                                oldSeverity.get(triggerPackage.getOnlyUnacknowledged()).put(triggerPackage.getCategoryFilter(), newSeverity);
                                oldAlarmCount.get(triggerPackage.getOnlyUnacknowledged()).put(triggerPackage.getCategoryFilter(), newAlarmCount);
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
            fireIfTttTriggerSet(ifTttConfig, triggerPackage.getCategoryFilter(), name.toUpperCase(), string -> string);
        }
    }

    /**
     * Executes a configured trigger set with a given OnmsSeverity instance.
     *
     * @param ifTttConfig           the IFTTT config instance
     * @param categoryFilter        the category filter
     * @param newSeverity           the new severity
     * @param variableNameExpansion the VariableNameExpansion to be used
     */
    private void fireIfTttTriggerSet(final IfTttConfig ifTttConfig, final String categoryFilter, final OnmsSeverity newSeverity, final VariableNameExpansion variableNameExpansion) {
        fireIfTttTriggerSet(ifTttConfig, categoryFilter, newSeverity.getLabel().toUpperCase(), variableNameExpansion);
    }

    /**
     * Executes a configured trigger set with a given String value.
     *
     * @param ifTttConfig           the IFTTT config instance
     * @param categoryFilter        the category filter
     * @param name                  the event name
     * @param variableNameExpansion the VariableNameExpansion to be used
     */
    protected void fireIfTttTriggerSet(final IfTttConfig ifTttConfig, final String categoryFilter, final String name, final VariableNameExpansion variableNameExpansion) {
        if (ifTttConfig == null) {
            return;
        }

        final TriggerPackage triggerPackage = ifTttConfig.getTriggerPackageForCategoryFilter(categoryFilter);

        if (triggerPackage != null) {

            final TriggerSet triggerSet = triggerPackage.getTriggerSetForName(name);

            if (triggerSet != null) {
                for (final Trigger trigger : triggerSet.getTriggers()) {

                    new IfTttTrigger()
                            .key(ifTttConfig.getKey())
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
            LOG.error("Error retrieving trigger package for category filter{}.", categoryFilter);
        }
    }
}
