/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.internal;

import static org.junit.Assert.assertEquals;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.createAlarmWrapper;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.ReductionKeyHelper;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateChangeHandler;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.test.BsmDatabasePopulator;
import org.opennms.netmgt.bsm.test.BsmTestData;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class DefaultBusinessServiceStateMachineIT {

    @Autowired
    private BusinessServiceManager businessServiceManager;

    @Autowired
    private BsmDatabasePopulator populator;

    @Autowired
    private BusinessServiceDao businessServiceDao;

    private BsmTestData testSpecification;

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        populator.populateDatabase();

        // setup the test data
        testSpecification = new BsmTestData(populator.getDatabasePopulator());
        testSpecification.getServices().forEach( entity -> businessServiceDao.save(entity));
    }

    @After
    public void after() {
        populator.resetDatabase(true);
    }

    @Test
    public void verifyGetOperationalStatusForIpServices() {
        // Determine reduction keys
        final OnmsMonitoredService serviceChild1 = testSpecification.getServiceChild1();
        final OnmsMonitoredService serviceChild2 = testSpecification.getServiceChild2();
        final BusinessServiceEntity root = testSpecification.getRoot();
        final String nodeLostServiceReductionKey = ReductionKeyHelper.getNodeLostServiceReductionKey(serviceChild1);
        final String nodeDownReductionKey = ReductionKeyHelper.getNodeDownReductionKey(serviceChild1);

        // Setup the State Machine
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(
                testSpecification.getServices().stream().map(s -> wrap(s)).collect(Collectors.toList())
        );

        // Verify the initial state
        Assert.assertEquals(null, stateMachine.getOperationalStatus(nodeLostServiceReductionKey));
        Assert.assertEquals(null, stateMachine.getOperationalStatus(nodeDownReductionKey));
        Assert.assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(serviceChild1)));
        Assert.assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(serviceChild2)));
        Assert.assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(root)));
        Assert.assertEquals(Lists.newArrayList(Status.NORMAL), stateMachine.getStatusListForReduceFunction(wrap(testSpecification.getChild1())));
        Assert.assertEquals(Lists.newArrayList(Status.NORMAL, Status.NORMAL), stateMachine.getStatusListForReduceFunction(wrap(testSpecification.getRoot())));

        // node lost service alarm
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, OnmsSeverity.WARNING, nodeLostServiceReductionKey));

        // verify state
        Assert.assertEquals(Status.WARNING, stateMachine.getOperationalStatus(nodeLostServiceReductionKey));
        Assert.assertEquals(null, stateMachine.getOperationalStatus(nodeDownReductionKey));
        Assert.assertEquals(Status.WARNING, stateMachine.getOperationalStatus(wrap(serviceChild1)));
        Assert.assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(serviceChild2)));
        Assert.assertEquals(Lists.newArrayList(Status.WARNING), stateMachine.getStatusListForReduceFunction(wrap(testSpecification.getChild1())));
        Assert.assertEquals(Status.WARNING, stateMachine.getOperationalStatus(wrap(root)));
        Assert.assertEquals(Lists.newArrayList(Status.WARNING, Status.NORMAL), stateMachine.getStatusListForReduceFunction(wrap(testSpecification.getRoot())));

        // node down alarm
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(EventConstants.NODE_DOWN_EVENT_UEI, OnmsSeverity.MINOR, nodeDownReductionKey));

        // verify state
        Assert.assertEquals(Status.WARNING, stateMachine.getOperationalStatus(nodeLostServiceReductionKey));
        Assert.assertEquals(Status.MINOR, stateMachine.getOperationalStatus(nodeDownReductionKey));
        Assert.assertEquals(Status.MINOR, stateMachine.getOperationalStatus(wrap(serviceChild1)));
        Assert.assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(serviceChild2)));
        Assert.assertEquals(Status.MINOR, stateMachine.getOperationalStatus(wrap(root)));
        Assert.assertEquals(Lists.newArrayList(Status.MINOR, Status.NORMAL), stateMachine.getStatusListForReduceFunction(wrap(testSpecification.getRoot())));
    }

    @Test
    public void canMaintainState() {
        BusinessServiceImpl bsChild1 = wrap(testSpecification.getChild1());
        BusinessServiceImpl bsChild2 = wrap(testSpecification.getChild2());
        BusinessServiceImpl bsParent = wrap(testSpecification.getRoot());
        IpServiceImpl svc1 = wrap(testSpecification.getServiceChild1());
        IpServiceImpl svc2 = wrap(testSpecification.getServiceChild2());

        // manually add a reduction key to a business service to verify that this also works
        bsChild1.addReductionKeyEdge("explicitReductionKey", new Identity());

        // Setup the state machine
        List<BusinessService> bss = Lists.newArrayList(bsChild1, bsChild2, bsParent);
        LoggingStateChangeHandler handler = new LoggingStateChangeHandler();
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(bss);
        stateMachine.addHandler(handler, null);

        // Verify the initial state
        assertEquals(0, handler.getStateChanges().size());
        for (BusinessService eachBs : bss) {
            assertEquals(DefaultBusinessServiceStateMachine.DEFAULT_SEVERITY, stateMachine.getOperationalStatus(eachBs));
        }
        assertEquals(0, bsParent.getLevel());
        assertEquals(1, bsChild1.getLevel());
        assertEquals(1, bsChild2.getLevel());

        // Pass alarm to the state machine
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(svc1.getEntity(), OnmsSeverity.MINOR));

        // Verify the updated state
        assertEquals(2, handler.getStateChanges().size());
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(svc1));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(bsChild1));
        assertEquals(DefaultBusinessServiceStateMachine.DEFAULT_SEVERITY, stateMachine.getOperationalStatus(svc2));
        assertEquals(DefaultBusinessServiceStateMachine.DEFAULT_SEVERITY, stateMachine.getOperationalStatus(bsChild2));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(bsParent));

        // Verify that hierarchy works
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(svc2.getEntity(), OnmsSeverity.MAJOR));
        assertEquals(4, handler.getStateChanges().size());
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(svc1));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(bsChild1));
        assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(svc2));
        assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(bsChild2));
        assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(bsParent));

        // Verify that explicit reductionKeys work as well
        AlarmWrapper alarmWrapper = createAlarmWrapper(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, OnmsSeverity.CRITICAL, "explicitReductionKey");
        stateMachine.handleNewOrUpdatedAlarm(alarmWrapper);
        assertEquals(6, handler.getStateChanges().size());
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(svc1));
        assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(svc2));
        assertEquals(Status.CRITICAL, stateMachine.getOperationalStatus(bsChild1));
        assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(bsChild2));
        assertEquals(Status.CRITICAL, stateMachine.getOperationalStatus(bsParent));
    }

    private BusinessServiceImpl wrap(BusinessServiceEntity entity) {
        return new BusinessServiceImpl(businessServiceManager, entity);
    }

    private IpServiceImpl wrap(OnmsMonitoredService entity) {
        return new IpServiceImpl(businessServiceManager, entity);
    }

    public class LoggingStateChangeHandler implements BusinessServiceStateChangeHandler {
        
        public class StateChange {
            private final BusinessService m_businessService;
            private final Status m_newStatus;
            private final Status m_prevStatus;

            public StateChange(BusinessService businessService, Status newStatus, Status prevStatus) {
                m_businessService = businessService;
                m_newStatus = newStatus;
                m_prevStatus = prevStatus;
            }

            public BusinessService getBusinessService() {
                return m_businessService;
            }

            public Status getNewSeverity() {
                return m_newStatus;
            }

            public Status getPrevSeverity() {
                return m_prevStatus;
            }
        }

        private final List<StateChange> m_stateChanges = Lists.newArrayList();

        @Override
        public void handleBusinessServiceStateChanged(BusinessService businessService, Status newStatus, Status prevStatus) {
            m_stateChanges.add(new StateChange(businessService, newStatus, prevStatus));
        }

        public List<StateChange> getStateChanges() {
            return m_stateChanges;
        }
    }
}
