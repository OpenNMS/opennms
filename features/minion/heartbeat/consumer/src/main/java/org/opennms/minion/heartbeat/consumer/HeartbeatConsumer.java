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

import com.google.common.collect.Sets;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.minion.heartbeat.common.HeartbeatModule;
import org.opennms.minion.heartbeat.common.MinionIdentityDTO;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

public class HeartbeatConsumer implements MessageConsumer<MinionIdentityDTO>, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatConsumer.class);

    private static final boolean PROVISIONING = Boolean.valueOf(System.getProperty("opennms.minion.provisioning", "true"));
    private static final String PROVISIONING_FOREIGN_SOURCE_PATTERN = System.getProperty("opennms.minion.provisioning.foreignSourcePattern", "Minions@%s");

    private static final HeartbeatModule heartbeatModule = new HeartbeatModule();

    @Autowired
    private MinionDao minionDao;

    @Autowired
    private MessageConsumerManager messageConsumerManager;

    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository deployedForeignSourceRepository;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Override
    @Transactional
    public void handleMessage(MinionIdentityDTO minionHandle) {
        LOG.info("Received heartbeat for Minion with id: {} at location: {}",
                 minionHandle.getId(), minionHandle.getLocation());

        OnmsMinion minion = minionDao.findById(minionHandle.getId());
        if (minion == null) {
            minion = new OnmsMinion();
            minion.setId(minionHandle.getId());
        }

        String prevLocation = minion.getLocation();
        String nextLocation = minionHandle.getLocation();

        minion.setLocation(minionHandle.getLocation());

        // Provision the minions node before we alter the location
        this.provision(minion,
                       prevLocation,
                       nextLocation);

        minion.setLastUpdated(new Date());
        minionDao.saveOrUpdate(minion);
    }

    private void provision(final OnmsMinion minion,
                           final String prevLocation,
                           final String nextLocation) {
        if (!PROVISIONING) {
            return;
        }

        final String prevForeignSource = String.format(PROVISIONING_FOREIGN_SOURCE_PATTERN, prevLocation);
        final String nextForeignSource = String.format(PROVISIONING_FOREIGN_SOURCE_PATTERN, nextLocation);

        final Set<String> alteredForeignSources = Sets.newHashSet();

        // Remove the node from the previous requisition, if location has changed
        if (!Objects.equals(prevForeignSource, nextForeignSource)) {
            final Requisition prevRequisition = this.deployedForeignSourceRepository.getRequisition(prevForeignSource);
            if (prevRequisition != null && prevRequisition.getNode(minion.getId()) != null) {
                prevRequisition.deleteNode(minion.getId());
                prevRequisition.setDate(new Date());

                deployedForeignSourceRepository.save(prevRequisition);
                deployedForeignSourceRepository.flush();

                alteredForeignSources.add(prevForeignSource);
            }
        }

        Requisition nextRequisition = deployedForeignSourceRepository.getRequisition(nextForeignSource);
        if (nextRequisition == null) {
            nextRequisition = new Requisition(nextForeignSource);
            nextRequisition.setDate(new Date());

            // We have to save the requisition before we can alter the according foreign source definition
            deployedForeignSourceRepository.save(nextRequisition);

            // Remove all policies and detectors from the foreign source
            final ForeignSource foreignSource = deployedForeignSourceRepository.getForeignSource(nextForeignSource);
            foreignSource.setDetectors(Collections.emptyList());
            foreignSource.setPolicies(Collections.emptyList());
            deployedForeignSourceRepository.save(foreignSource);

            alteredForeignSources.add(nextForeignSource);
        }

        RequisitionNode requisitionNode = nextRequisition.getNode(minion.getId());
        if (requisitionNode == null) {
            final RequisitionMonitoredService requisitionMonitoredService = new RequisitionMonitoredService();
            requisitionMonitoredService.setServiceName("Minion-Heartbeat");

            final RequisitionInterface requisitionInterface = new RequisitionInterface();
            requisitionInterface.setIpAddr("127.0.0.1");
            requisitionInterface.putMonitoredService(requisitionMonitoredService);

            requisitionNode = new RequisitionNode();
            requisitionNode.setNodeLabel(minion.getId());
            requisitionNode.setForeignId(minion.getLabel() != null
                                         ? minion.getLabel()
                                         : minion.getId());
            requisitionNode.setLocation(minion.getLocation());
            requisitionNode.putInterface(requisitionInterface);

            nextRequisition.putNode(requisitionNode);
            nextRequisition.setDate(new Date());
            deployedForeignSourceRepository.save(nextRequisition);
            deployedForeignSourceRepository.flush();

            alteredForeignSources.add(nextForeignSource);
        }

        for (final String alteredForeignSource : alteredForeignSources) {
            final EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
            eventBuilder.addParam(EventConstants.PARM_URL, String.valueOf(deployedForeignSourceRepository.getRequisitionURL(alteredForeignSource)));

            try {
                m_eventProxy.send(eventBuilder.getEvent());
            } catch (final EventProxyException e) {
                throw new DataAccessResourceFailureException("Unable to send event to import group " + alteredForeignSource, e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Automatically register the consumer on initialization
        messageConsumerManager.registerConsumer(this);
    }

    @Override
    public SinkModule<MinionIdentityDTO> getModule() {
        return heartbeatModule;
    }

}
