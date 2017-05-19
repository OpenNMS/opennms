/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vaadin.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Tests that the {@TransactionAwareBeanProxy} is creating a proxy of a certain object and enforces the execution of each method within a transactional scope.
 * To ensure the transactional scope is enforced the method {@link TransactionSynchronizationManager#isActualTransactionActive()} is used.
 * This requires to have an actual {@link org.springframework.transaction.PlatformTransactionManager} in place.
 * In order to have one, we setup a real database and pull in existing DAOs.
 * If we were to use a mock somehow the method {@link TransactionSynchronizationManager#isActualTransactionActive()} does not return true.
 * Therefore it is hard to test if the implementation actually works.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath*:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase // we need a real database in order to haave the TransactionOperations available, etc.
public class TransactionAwareBeanProxyFactoryIT {

    public class Dummy {

    }

    @Autowired
    private TransactionOperations transactionOperations;

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        // ensure that we do not by accident run within a transaction
        Assert.assertEquals(false, TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    public void testMethodInterception() {
        // no transaction active, if not created by TransactionAwareBeanProxyFactory
        TestObject testObject = new TestObjectImpl(false);
        testObject.doSomething();
        testObject.doSomething2();

        // transaction active
        TestObject testObject2 = new TestObjectImpl(true);
        TestObject anotherObject = new TransactionAwareBeanProxyFactory(transactionOperations).createProxy(testObject2);
        Assert.assertNotNull(anotherObject);
        Assert.assertEquals(true, anotherObject.isTransactionActive());
        anotherObject.doSomething();
        anotherObject.doSomething2();
    }

    // Verify that the proxy is created correctly
    @Test
    public void testProxyCreation() {
        // no transaction active, if not created by TransactionAwareBeanProxyFactory
        TestObject testObject = new TestObjectImpl(true);
        testObject.setSomeValue("someValue");

        TestObject anotherObject = new TransactionAwareBeanProxyFactory(transactionOperations).createProxy(testObject);
        Assert.assertEquals(testObject.isTransactionActive(), anotherObject.isTransactionActive());
        Assert.assertEquals(testObject.getSomeValue(), anotherObject.getSomeValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProxyCreationFailure() {
        new TransactionAwareBeanProxyFactory(transactionOperations).createProxy(new Dummy());
    }
}
