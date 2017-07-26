/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.minion.heartbeat.consumer;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.minion.heartbeat.common.HeartbeatModule;
import org.opennms.minion.heartbeat.common.MinionIdentityDTO;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.model.requisition.RequisitionInterfaceEntity;
import org.opennms.netmgt.model.requisition.RequisitionMonitoredServiceEntity;
import org.opennms.netmgt.model.requisition.RequisitionNodeEntity;
import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.persist.RequisitionService;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Sets;

public class HeartbeatConsumer implements MessageConsumer<MinionIdentityDTO, MinionIdentityDTO>, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatConsumer.class);

    private static final boolean PROVISIONING = Boolean.valueOf(System.getProperty("opennms.minion.provisioning", "true"));
    private static final String PROVISIONING_FOREIGN_SOURCE_PATTERN = System.getProperty("opennms.minion.provisioning.foreignSourcePattern", "Minions");

    private static final HeartbeatModule heartbeatModule = new HeartbeatModule();

    @Autowired
    private MinionDao minionDao;

    @Autowired
    private MessageConsumerManager messageConsumerManager;

    @Autowired
    private RequisitionService requisitionService;

    @Autowired
    @Qualifier("default")
    private ForeignSourceService foreignSourceService;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy eventProxy;

    @Autowired
    @Qualifier("eventSubscriptionService")
    private EventSubscriptionService eventSubscriptionService;

    @Autowired
    private TransactionOperations transactionOperations;

    @Override
    @Transactional
    public void handleMessage(MinionIdentityDTO minionHandle) {
        LOG.info("Received heartbeat for Minion with id: {} at location: {}",
                 minionHandle.getId(), minionHandle.getLocation());

        OnmsMinion minion = minionDao.findById(minionHandle.getId());
        if (minion == null) {
            minion = new OnmsMinion();
            minion.setId(minionHandle.getId());

            // The real location is filled in below, but we set this to null
            // for now to detect requisition changes
            minion.setLocation(null);
        }

        final String prevLocation = minion.getLocation();
        final String nextLocation = minionHandle.getLocation();

        minion.setLocation(minionHandle.getLocation());

        // Provision the minions node before we alter the location
        this.provision(minion,
                       prevLocation,
                       nextLocation);

        if (minionHandle.getTimestamp() == null) {
            // The heartbeat does not contain a timestamp - use the current time
            minion.setLastUpdated(new Date());
            LOG.info("Received heartbeat without a timestamp: {}", minionHandle);
        } else if (minion.getLastUpdated() == null) {
            // The heartbeat does contain a timestamp, and we don't have
            // one set yet, so use whatever we've been given
            minion.setLastUpdated(minionHandle.getTimestamp());
        } else if (minionHandle.getTimestamp().after(minion.getLastUpdated())) {
            // The timestamp in the heartbeat is more recent than the one we
            // have stored, so update it
            minion.setLastUpdated(minionHandle.getTimestamp());
        } else {
            // The timestamp in the heartbeat is earlier than the
            // timestamp we have stored, so ignore it
            LOG.info("Ignoring stale timestamp from heartbeat: {}", minionHandle);
        }

        minionDao.saveOrUpdate(minion);

        if (prevLocation == null) {
            final EventBuilder eventBuilder = new EventBuilder(EventConstants.MONITORING_SYSTEM_ADDED_UEI,
                    "OpenNMS.Minion.Heartbeat");
            eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION);
            eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_ID, minionHandle.getId());
            eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, nextLocation);
            try {
                eventProxy.send(eventBuilder.getEvent());
            } catch (final EventProxyException e) {
                throw new DataAccessResourceFailureException("Unable to send event", e);
            }
        } else if (!prevLocation.equals(nextLocation)) {

            final EventBuilder eventBuilder = new EventBuilder(EventConstants.MONITORING_SYSTEM_LOCATION_CHANGED_UEI,
                    "OpenNMS.Minion.Heartbeat");
            eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION);
            eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_ID, minionHandle.getId());
            eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_PREV_LOCATION, prevLocation);
            eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, nextLocation);
            try {
                eventProxy.send(eventBuilder.getEvent());
            } catch (final EventProxyException e) {
                throw new DataAccessResourceFailureException("Unable to send event", e);
            }
        }

    }

    private void provision(final OnmsMinion minion,
                           final String prevLocation,
                           final String nextLocation) {
        // Return fast if automatic provisioning is disabled
        if (!PROVISIONING) {
            return;
        }

        // Return fast until the provisioner is running to pick up the events sent below
        if (!this.eventSubscriptionService.hasEventListener(EventConstants.RELOAD_IMPORT_UEI)) {
            return;
        }

        final String prevForeignSource = String.format(PROVISIONING_FOREIGN_SOURCE_PATTERN, prevLocation);
        final String nextForeignSource = String.format(PROVISIONING_FOREIGN_SOURCE_PATTERN, nextLocation);

        final Set<String> alteredForeignSources = Sets.newHashSet();

        transactionOperations.execute(transactionStatus ->  {
            // Remove the node from the previous requisition, if location has changed
            if (!Objects.equals(prevForeignSource, nextForeignSource)) {
                final RequisitionEntity prevRequisition = this.requisitionService.getRequisition(prevForeignSource);
                if (prevRequisition != null && prevRequisition.getNode(minion.getId()) != null) {
                    prevRequisition.removeNode(minion.getId());
                    prevRequisition.updateLastUpdated();

                    requisitionService.saveOrUpdateRequisition(prevRequisition);

                    alteredForeignSources.add(prevForeignSource);
                }
            }

            RequisitionEntity nextRequisition = requisitionService.getRequisition(nextForeignSource);
            if (nextRequisition == null) {
                nextRequisition = new RequisitionEntity(nextForeignSource);
                nextRequisition.updateLastUpdated();

                // We have to save the requisition before we can alter the according foreign source definition
                requisitionService.saveOrUpdateRequisition(nextRequisition);

                // Remove all policies and detectors from the foreign source
                final ForeignSourceEntity foreignSource = foreignSourceService.getForeignSource(nextForeignSource);
                foreignSource.setDetectors(Collections.emptyList());
                foreignSource.setPolicies(Collections.emptyList());
                foreignSourceService.saveForeignSource(foreignSource);

                alteredForeignSources.add(nextForeignSource);
            }

            RequisitionNodeEntity requisitionNode = nextRequisition.getNode(minion.getId());
            if (requisitionNode == null) {
                final RequisitionMonitoredServiceEntity requisitionMonitoredService = new RequisitionMonitoredServiceEntity();
                requisitionMonitoredService.setServiceName("Minion-Heartbeat");

                final RequisitionInterfaceEntity requisitionInterface = new RequisitionInterfaceEntity();
                requisitionInterface.setIpAddress("127.0.0.1");
                requisitionInterface.addMonitoredService(requisitionMonitoredService);

                requisitionNode = new RequisitionNodeEntity();
                requisitionNode.setNodeLabel(minion.getId());
                requisitionNode.setForeignId(minion.getLabel() != null
                        ? minion.getLabel()
                        : minion.getId());
                requisitionNode.setLocation(minion.getLocation());
                requisitionNode.addInterface(requisitionInterface);

                nextRequisition.addNode(requisitionNode);
                nextRequisition.setLastUpdate(new Date());
                requisitionService.saveOrUpdateRequisition(nextRequisition);

                alteredForeignSources.add(nextForeignSource);
            }
            return null;
        });

        for (final String alteredForeignSource : alteredForeignSources) {
            requisitionService.triggerImport(
                    new ImportRequest(this).withForeignSource(alteredForeignSource));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Automatically register the consumer on initialization
        messageConsumerManager.registerConsumer(this);
    }

    @Override
    public SinkModule<MinionIdentityDTO, MinionIdentityDTO> getModule() {
        return heartbeatModule;
    }
}
