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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
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
