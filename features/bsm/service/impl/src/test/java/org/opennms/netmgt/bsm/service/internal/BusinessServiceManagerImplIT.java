/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.internal;

import static org.opennms.netmgt.bsm.test.BsmTestUtils.createAlarm;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.createDummyBusinessService;

import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSet;
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
public class BusinessServiceManagerImplIT {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private BusinessServiceManager businessServiceManager;

    @Autowired
    private BusinessServiceStateMachine businessServiceStateMachine;

    @Autowired
    private BusinessServiceDao businessServiceDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private DatabasePopulator populator;

    @Autowired
    private ReductionFunctionDao reductionFunctionDao;

    MostCritical mostCritical;

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        populator.populateDatabase();
        
        mostCritical = new MostCritical();
        reductionFunctionDao.save(mostCritical);
        reductionFunctionDao.flush();
    }

    @After
    public void after() {
        populator.resetDatabase();
        for (BusinessServiceEntity eachService : businessServiceDao.findAll()) {
            businessServiceDao.delete(eachService);
        }
    }

    @Test
    public void testGetOperationalStatusForBusinessService() {
        final BusinessServiceEntity bsServiceEntity = createDummyBusinessService("Dummy Business Service");
        final BusinessServiceEntity bsServiceEntity2 = createDummyBusinessService("Another Dummy Business Service");
        Long serviceId1 = businessServiceDao.save(bsServiceEntity);
        Long serviceId2 = businessServiceDao.save(bsServiceEntity2);
        businessServiceStateMachine.setBusinessServices(Lists.newArrayList(bsServiceEntity, bsServiceEntity2));

        final BusinessService bsService1 = getBusinessService(serviceId1);
        final BusinessService bsService2 = getBusinessService(serviceId2);
        final IpService ipServiceWithId5 = getIpService(5);
        final IpService ipServiceWithId6 = getIpService(6);

        // no ip services attached
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService1));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService2));

        // ip services attached
        businessServiceManager.assignIpService(getBusinessService(serviceId1), ipServiceWithId5);
        businessServiceManager.assignIpService(getBusinessService(serviceId2), ipServiceWithId6);
        bsService1.save();
        bsService2.save();
        Assert.assertFalse("Services are equal but should not", Objects.equals(bsService1, bsService2));
        businessServiceStateMachine.setBusinessServices(Lists.newArrayList(bsServiceEntity, bsServiceEntity2));

        // should not have any effect
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService1));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService2));

        // attach NORMAL alarm to service 1
        businessServiceStateMachine.handleNewOrUpdatedAlarm(createAlarm(monitoredServiceDao.get(5), OnmsSeverity.NORMAL));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForIPService(ipServiceWithId5));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService1));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService2));

        // attach INDETERMINATE alarm to service 1
        businessServiceStateMachine.handleNewOrUpdatedAlarm(createAlarm(monitoredServiceDao.get(5), OnmsSeverity.INDETERMINATE));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForIPService(ipServiceWithId5));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService1));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService2));

        // attach WARNING alarm to service 1
        businessServiceStateMachine.handleNewOrUpdatedAlarm(createAlarm(monitoredServiceDao.get(5), OnmsSeverity.WARNING));
        Assert.assertEquals(OnmsSeverity.WARNING, businessServiceManager.getOperationalStatusForIPService(ipServiceWithId5));
        Assert.assertEquals(OnmsSeverity.WARNING, businessServiceManager.getOperationalStatusForBusinessService(bsService1));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService2));

        // attach CRITICAL alarm to service 1
        businessServiceStateMachine.handleNewOrUpdatedAlarm(createAlarm(monitoredServiceDao.get(5), OnmsSeverity.CRITICAL));
        Assert.assertEquals(OnmsSeverity.CRITICAL, businessServiceManager.getOperationalStatusForIPService(ipServiceWithId5));
        Assert.assertEquals(OnmsSeverity.CRITICAL, businessServiceManager.getOperationalStatusForBusinessService(bsService1));
        Assert.assertEquals(OnmsSeverity.NORMAL, businessServiceManager.getOperationalStatusForBusinessService(bsService2));
    }

    @Test
    public void testChildMapping() {
        BusinessServiceEntity service_p_1 = createDummyBusinessService("Business Service #p1");
        BusinessServiceEntity service_p_2 = createDummyBusinessService("Business Service #p2");
        BusinessServiceEntity service_c_1 = createDummyBusinessService("Business Service #c1");
        BusinessServiceEntity service_c_2 = createDummyBusinessService("Business Service #c2");

        businessServiceDao.save(service_p_1);
        businessServiceDao.save(service_p_2);
        businessServiceDao.save(service_c_1);
        businessServiceDao.save(service_c_2);

        businessServiceManager.assignChildService(getBusinessService(service_p_1.getId()), getBusinessService(service_c_1.getId()));
        businessServiceManager.assignChildService(getBusinessService(service_p_1.getId()), getBusinessService(service_c_2.getId()));

        businessServiceManager.assignChildService(getBusinessService(service_p_2.getId()), getBusinessService(service_c_1.getId()));
        businessServiceManager.assignChildService(getBusinessService(service_p_2.getId()), getBusinessService(service_c_2.getId()));

        Assert.assertEquals(ImmutableSet.of(service_p_1, service_p_2),
                            service_c_1.getParentServices());

        Assert.assertEquals(ImmutableSet.of(service_p_1, service_p_2),
                            service_c_2.getParentServices());
    }

    @Test
    public void testChildDeletion() {
        BusinessServiceEntity service_p = createDummyBusinessService("Business Service #p");
        BusinessServiceEntity service_c_1 = createDummyBusinessService("Business Service #c1");
        BusinessServiceEntity service_c_2 = createDummyBusinessService("Business Service #c2");

        businessServiceDao.save(service_p);
        businessServiceDao.save(service_c_1);
        businessServiceDao.save(service_c_2);

        BusinessService parentBs = getBusinessService(service_p.getId());
        BusinessService child1Bs = getBusinessService(service_c_1.getId());
        BusinessService child2Bs = getBusinessService(service_c_2.getId());

        businessServiceManager.assignChildService(parentBs, child1Bs);
        businessServiceManager.assignChildService(parentBs, child2Bs);
        parentBs.save();
        child1Bs.save();
        child2Bs.save();

        child1Bs.delete();

        Assert.assertEquals(ImmutableSet.of(service_c_2),
                            service_p.getChildServices());
    }

    @Test
    public void testLoopDetection() {
        BusinessServiceEntity service1 = createDummyBusinessService("Business Service #1");
        BusinessServiceEntity service2 = createDummyBusinessService("Business Service #2");
        BusinessServiceEntity service3 = createDummyBusinessService("Business Service #3");

        Long serviceId1 = businessServiceDao.save(service1);
        Long serviceId2 = businessServiceDao.save(service2);
        Long serviceId3 = businessServiceDao.save(service3);

        BusinessService bs1 = getBusinessService(serviceId1);
        BusinessService bs2 = getBusinessService(serviceId2);
        BusinessService bs3 = getBusinessService(serviceId3);

        Assert.assertEquals(ImmutableSet.of(businessServiceManager.getBusinessServiceById(serviceId2),
                                            businessServiceManager.getBusinessServiceById(serviceId3)),
                            businessServiceManager.getFeasibleChildServices(businessServiceManager.getBusinessServiceById(serviceId1)));
        Assert.assertEquals(ImmutableSet.of(businessServiceManager.getBusinessServiceById(serviceId1),
                                            businessServiceManager.getBusinessServiceById(serviceId3)),
                            businessServiceManager.getFeasibleChildServices(businessServiceManager.getBusinessServiceById(serviceId2)));
        Assert.assertEquals(ImmutableSet.of(businessServiceManager.getBusinessServiceById(serviceId1),
                                            businessServiceManager.getBusinessServiceById(serviceId2)),
                            businessServiceManager.getFeasibleChildServices(businessServiceManager.getBusinessServiceById(serviceId3)));

        businessServiceManager.assignChildService(bs1, bs2);
        bs1.save();
        bs2.save();

        Assert.assertEquals(ImmutableSet.of(businessServiceManager.getBusinessServiceById(serviceId2),
                                            businessServiceManager.getBusinessServiceById(serviceId3)),
                            businessServiceManager.getFeasibleChildServices(businessServiceManager.getBusinessServiceById(serviceId1)));
        Assert.assertEquals(ImmutableSet.of(businessServiceManager.getBusinessServiceById(serviceId3)),
                            businessServiceManager.getFeasibleChildServices(businessServiceManager.getBusinessServiceById(serviceId2)));
        Assert.assertEquals(ImmutableSet.of(businessServiceManager.getBusinessServiceById(serviceId1),
                                            businessServiceManager.getBusinessServiceById(serviceId2)),
                            businessServiceManager.getFeasibleChildServices(businessServiceManager.getBusinessServiceById(serviceId3)));

        businessServiceManager.assignChildService(bs2, bs3);
        bs2.save();
        bs3.save();

        Assert.assertEquals(ImmutableSet.of(businessServiceManager.getBusinessServiceById(serviceId2),
                                            businessServiceManager.getBusinessServiceById(serviceId3)),
                            businessServiceManager.getFeasibleChildServices(businessServiceManager.getBusinessServiceById(serviceId1)));
        Assert.assertEquals(ImmutableSet.of(businessServiceManager.getBusinessServiceById(serviceId3)),
                            businessServiceManager.getFeasibleChildServices(businessServiceManager.getBusinessServiceById(serviceId2)));
        Assert.assertEquals(ImmutableSet.of(),
                            businessServiceManager.getFeasibleChildServices(businessServiceManager.getBusinessServiceById(serviceId3)));
    }

    @Test
    public void testLoopCreation() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Service will form a loop");

        BusinessServiceEntity service1 = createDummyBusinessService("Business Service #1");
        BusinessServiceEntity service2 = createDummyBusinessService("Business Service #2");
        BusinessServiceEntity service3 = createDummyBusinessService("Business Service #3");

        Long serviceId1 = businessServiceDao.save(service1);
        Long serviceId2 = businessServiceDao.save(service2);
        Long serviceId3 = businessServiceDao.save(service3);

        businessServiceManager.assignChildService(getBusinessService(serviceId1), getBusinessService(serviceId2));
        businessServiceManager.assignChildService(getBusinessService(serviceId2), getBusinessService(serviceId3));
        businessServiceManager.assignChildService(getBusinessService(serviceId3), getBusinessService(serviceId1));
    }

    private BusinessService getBusinessService(long serviceId) {
        return new BusinessServiceImpl((BusinessServiceManagerImpl) businessServiceManager, businessServiceDao.get(serviceId));
    }

    private IpService getIpService(int ipServiceId) {
        return new IpServiceImpl((BusinessServiceManagerImpl) businessServiceManager, monitoredServiceDao.get(ipServiceId));
    }
}
