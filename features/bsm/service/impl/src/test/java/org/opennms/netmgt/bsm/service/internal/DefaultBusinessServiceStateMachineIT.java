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
import static org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy.BAMBOO_AGENT_CAROLINA_REDUCTION_KEY;
import static org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy.BAMBOO_AGENT_DUKE_REDUCTION_KEY;
import static org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy.BAMBOO_AGENT_NCSTATE_REDUCTION_KEY;
import static org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy.DISK_USAGE_THRESHOLD_BAMBO_REDUCTION_KEY;
import static org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy.HTTP_8085_BAMBOO_REDUCTION_KEY;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.IPServiceEdgeEntity;
import org.opennms.netmgt.dao.util.ReductionKeyHelper;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.service.AlarmProvider;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.edge.IpServiceEdgeImpl;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.test.BsmDatabasePopulator;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.bsm.test.LoggingStateChangeHandler;
import org.opennms.netmgt.bsm.test.hierarchies.BambooTestHierarchy;
import org.opennms.netmgt.bsm.test.hierarchies.BusinessServicesShareIpServiceHierarchy;
import org.opennms.netmgt.bsm.test.hierarchies.SimpleTestHierarchy;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Tests the state machine against hierarchies stored in
 * the database.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class DefaultBusinessServiceStateMachineIT {

    @Autowired
    private BusinessServiceManager businessServiceManager;

    @Autowired
    @Qualifier("bsmDatabasePopulator")
    private BsmDatabasePopulator populator;

    @Autowired
    private BusinessServiceDao businessServiceDao;

    @Autowired
    private DistPollerDao distPollerDao;

    @Autowired
    private AlarmProvider alarmProvider;

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        populator.populateDatabase();
    }

    @After
    public void after() {
        populator.resetDatabase(true);
    }

    /**
     * Builds 200 business services.
     * Each business Services has one parent and two children.
     * Each children has 12 reduction key edges.
     * In sum there are:
     *  - 600 business services
     *  - 4800 reduction key edges
     *  - 400 child edges
     *  - 5200 edges
     *
     *  See NMS-8978 for more details.
     */
    @Test
    public void ensureReductionKeyLookupIsFastEnough() {
        for (int i = 0; i < 200; i++) {
            BusinessServiceEntity parentEntity = new BusinessServiceEntityBuilder()
                    .name("Parent " + i)
                    .reduceFunction(new HighestSeverityEntity())
                    .toEntity();

            for (int c = 0; c < 2; c++) {
                BusinessServiceEntityBuilder childBuilder = new BusinessServiceEntityBuilder()
                        .name("Child " + i + " " + c)
                        .reduceFunction(new HighestSeverityEntity());
                for (int a=0; a<12; a++) {
                    childBuilder.addReductionKey("custom." + i + "." + c + "." + a, new IdentityEntity());
                }
                BusinessServiceEntity childEntity = childBuilder.toEntity();
                parentEntity.addChildServiceEdge(childEntity, new IdentityEntity());
                businessServiceDao.save(childEntity);
            }
            businessServiceDao.save(parentEntity);
        }

        final Set<String> uniqueReductionKeys = businessServiceDao.findMatching(new CriteriaBuilder(BusinessServiceEntity.class).like("name", "Child%").toCriteria())
                .stream()
                .flatMap(service -> service.getReductionKeyEdges().stream())
                .flatMap(edge -> edge.getReductionKeys().stream())
                .collect(Collectors.toSet());
        for (String eachKey : uniqueReductionKeys) {
            final OnmsAlarm alarm = new OnmsAlarm();
            alarm.setUei("custom");
            alarm.setAlarmType(OnmsAlarm.PROBLEM_TYPE);
            alarm.setDescription("dummy");
            alarm.setLogMsg("dummy");
            alarm.setSeverity(OnmsSeverity.WARNING);
            alarm.setReductionKey(eachKey);
            alarm.setDistPoller(distPollerDao.whoami());
            alarm.setCounter(1);
            populator.getAlarmDao().save(alarm);
        }

        populator.getAlarmDao().flush();
        businessServiceDao.flush();

        // Simulate lookup of reduction keys
        final long start = System.currentTimeMillis();
        final DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setAlarmProvider(alarmProvider);
        stateMachine.setBusinessServices(businessServiceDao.findAll().stream().map(e -> new BusinessServiceImpl(businessServiceManager, e)).collect(Collectors.toList()));
        long diff = System.currentTimeMillis() - start;
        LoggerFactory.getLogger(getClass()).info("Took {} ms to initialize state machine", diff);
        Assert.assertTrue("Reduction Key lookup took much longer than expected. Expected was 1000 ms, but took " + diff + " ms", 1000 >= diff);
    }

    @Test
    public void canGetOperationalStatusForIpServices() {
        // Setup the the test hierarchy
        SimpleTestHierarchy simpleTestHierarchy = new SimpleTestHierarchy(populator);
        simpleTestHierarchy.getServices().forEach(entity -> businessServiceDao.save(entity));
        final BusinessServiceEntity root = simpleTestHierarchy.getRoot();
        final IPServiceEdgeEntity serviceChild1 = simpleTestHierarchy.getServiceChild1();
        final IPServiceEdgeEntity serviceChild2 = simpleTestHierarchy.getServiceChild2();

        // Setup the State Machine
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(
                simpleTestHierarchy.getServices().stream().map(s -> wrap(s)).collect(Collectors.toList())
        );

        // Verify the initial state
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(root)));
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(simpleTestHierarchy.getChild1())));
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(simpleTestHierarchy.getChild2())));
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(serviceChild1)));
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(serviceChild2)));

        // Node lost service alarm
        String nodeLostServiceReductionKey = ReductionKeyHelper.getNodeLostServiceReductionKey(serviceChild1.getIpService());
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, OnmsSeverity.WARNING, nodeLostServiceReductionKey));

        // Verify state
        assertEquals(Status.WARNING, stateMachine.getOperationalStatus(wrap(serviceChild1)));
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(serviceChild2)));

        // Interface down alarm
        String interfaceDownReductionKey = ReductionKeyHelper.getInterfaceDownReductionKey(serviceChild1.getIpService());
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(EventConstants.INTERFACE_DOWN_EVENT_UEI, OnmsSeverity.MINOR, interfaceDownReductionKey));

        // Verify state
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(wrap(serviceChild1)));
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(serviceChild2)));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(wrap(root)));

        // Node down alarm
        String nodeDownReductionKey = ReductionKeyHelper.getNodeDownReductionKey(serviceChild1.getIpService());
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(EventConstants.NODE_DOWN_EVENT_UEI, OnmsSeverity.MAJOR, nodeDownReductionKey));

        // Verify state
        Assert.assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(wrap(serviceChild1)));
        Assert.assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(serviceChild2)));
        Assert.assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(wrap(root)));
    }

    @Test
    public void canMaintainStateForBambooHierarchy() {
        // Setup the the test hierarchy
        BambooTestHierarchy testSpecification = new BambooTestHierarchy();
        testSpecification.getServices().forEach( entity -> businessServiceDao.save(entity));

        // Setup the State Machine
        final DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(
                testSpecification.getServices().stream().map(s -> wrap(s)).collect(Collectors.toList())
        );
        LoggingStateChangeHandler handler = new LoggingStateChangeHandler();
        stateMachine.addHandler(handler, null);

        // Verify the initial state
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(testSpecification.getMasterService())));
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(testSpecification.getAgentsService())));
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(wrap(testSpecification.getBambooService())));

        // Send alarms to the state machine
        // Business Service "Master"
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper("uei.opennms.org/dummy", OnmsSeverity.INDETERMINATE, HTTP_8085_BAMBOO_REDUCTION_KEY));
        assertEquals(0, handler.getStateChanges().size()); // no state change
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper("uei.opennms.org/dummy", OnmsSeverity.WARNING, DISK_USAGE_THRESHOLD_BAMBO_REDUCTION_KEY));
        assertEquals(2, handler.getStateChanges().size()); // "Master" and "Bamboo" changed
        // Business Service "Agents"
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper("uei.opennms.org/dummy", OnmsSeverity.MINOR, BAMBOO_AGENT_DUKE_REDUCTION_KEY));
        assertEquals(2 , handler.getStateChanges().size()); // no state change (threshold not met)
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper("uei.opennms.org/dummy", OnmsSeverity.NORMAL, BAMBOO_AGENT_NCSTATE_REDUCTION_KEY));
        assertEquals(2 , handler.getStateChanges().size()); // no state change (threshold not met)
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper("uei.opennms.org/dummy", OnmsSeverity.MAJOR, BAMBOO_AGENT_CAROLINA_REDUCTION_KEY));
        assertEquals(4 , handler.getStateChanges().size()); // state change (threshold met) for "Agents" and "Bamboo"

        // Verify the updated state
        assertEquals(Status.WARNING, stateMachine.getOperationalStatus(wrap(testSpecification.getMasterService()))); // Business Service "Master"
        assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(wrap(testSpecification.getAgentsService()))); // Business Service "Agents"
        assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(wrap(testSpecification.getBambooService()))); // Business Service "Bamboo" (root)
    }

    /**
     * Verifies that two Business Services can share the same Ip Service and both Business Services will change their
     * status when the IP Service changes its status.
     */
    @Test
    public void canShareIpService() {
        BusinessServicesShareIpServiceHierarchy testHierarchy = new BusinessServicesShareIpServiceHierarchy(populator);
        testHierarchy.getServices().forEach(entity -> businessServiceDao.save(entity));
        businessServiceDao.flush();

        BusinessServiceImpl bsChild1 = wrap(testHierarchy.getChild1());
        BusinessServiceImpl bsChild2 = wrap(testHierarchy.getChild2());
        BusinessServiceImpl bsParent = wrap(testHierarchy.getRoot());
        IpServiceEdge svc1 = wrap(testHierarchy.getServiceChild1());
        IpServiceEdge svc2 = wrap(testHierarchy.getServiceChild2());

        // Setup the state machine
        List<BusinessService> bss = Lists.newArrayList(bsChild1, bsChild2, bsParent);
        LoggingStateChangeHandler handler = new LoggingStateChangeHandler();
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(bss);
        stateMachine.addHandler(handler, null);

        // Verify the initial state
        assertEquals(0, handler.getStateChanges().size());
        for (BusinessService eachBs : bss) {
            assertEquals(DefaultBusinessServiceStateMachine.MIN_SEVERITY, stateMachine.getOperationalStatus(eachBs));
        }

        // Pass alarm to the state machine
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(testHierarchy.getServiceChild1().getIpService(), OnmsSeverity.MINOR));

        // Verify the updated state
        assertEquals(3, handler.getStateChanges().size());
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(svc1));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(bsChild1));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(svc2));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(bsChild2));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(bsParent));
    }

    @Test
    public void canMaintainStateForSimpleHierarchy() {
        // Setup the the test hierarchy
        SimpleTestHierarchy testHierarchy = new SimpleTestHierarchy(populator);
        testHierarchy.getServices().forEach(entity -> businessServiceDao.save(entity));

        BusinessServiceImpl bsChild1 = wrap(testHierarchy.getChild1());
        BusinessServiceImpl bsChild2 = wrap(testHierarchy.getChild2());
        BusinessServiceImpl bsParent = wrap(testHierarchy.getRoot());
        IpServiceEdge svc1 = wrap(testHierarchy.getServiceChild1());
        IpServiceEdge svc2 = wrap(testHierarchy.getServiceChild2());

        // Manually add a reduction key to a business service to verify that this also works
        bsChild1.addReductionKeyEdge("explicitReductionKey", new Identity(), Edge.DEFAULT_WEIGHT, null);
        businessServiceDao.flush();

        // Setup the state machine
        List<BusinessService> bss = Lists.newArrayList(bsChild1, bsChild2, bsParent);
        LoggingStateChangeHandler handler = new LoggingStateChangeHandler();
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(bss);
        stateMachine.addHandler(handler, null);

        // Verify the initial state
        assertEquals(0, handler.getStateChanges().size());
        for (BusinessService eachBs : bss) {
            assertEquals(DefaultBusinessServiceStateMachine.MIN_SEVERITY, stateMachine.getOperationalStatus(eachBs));
        }

        // Pass alarm to the state machine
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(testHierarchy.getServiceChild1().getIpService(), OnmsSeverity.MINOR));

        // Verify the updated state
        assertEquals(2, handler.getStateChanges().size());
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(svc1));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(bsChild1));
        assertEquals(DefaultBusinessServiceStateMachine.MIN_SEVERITY, stateMachine.getOperationalStatus(svc2));
        assertEquals(DefaultBusinessServiceStateMachine.MIN_SEVERITY, stateMachine.getOperationalStatus(bsChild2));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(bsParent));

        // Verify that hierarchy works
        stateMachine.handleNewOrUpdatedAlarm(createAlarmWrapper(testHierarchy.getServiceChild2().getIpService(), OnmsSeverity.MAJOR));
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

    private IpServiceEdge wrap(IPServiceEdgeEntity entity) {
        return new IpServiceEdgeImpl(businessServiceManager, entity);
    }
}
