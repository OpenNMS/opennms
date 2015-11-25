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

package org.opennms.features.bsm.vaadin.masterpage;

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

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath*:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-test.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase // we need a real database in order to haave the TransactionOperations available, etc.
public class TransactionAwareBeanProxyTest {

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
        // no transaction active, if not created by TransactionAwareBeanProxy
        TestObject testObject = new TestObject(false);
        testObject.doSomething();
        testObject.doSomething2();

        // transaction active
        TestObject testObject2 = new TestObject(true);
        TestObject anotherObject = new TransactionAwareBeanProxy(transactionOperations).getProxy(testObject2);
        Assert.assertNotNull(anotherObject);
        Assert.assertEquals(true, anotherObject.isTransactionActive());
        anotherObject.doSomething();
        anotherObject.doSomething2();
    }

    // Verify that the proxy is created correctly
    @Test
    public void testProxyCreation() {
        // no transaction active, if not created by TransactionAwareBeanProxy
        TestObject testObject = new TestObject(true);
        testObject.setSomeValue("someValue");

        TestObject anotherObject = new TransactionAwareBeanProxy(transactionOperations).getProxy(testObject);
        Assert.assertEquals(testObject.isTransactionActive(), anotherObject.isTransactionActive());
        Assert.assertEquals(testObject.getSomeValue(), anotherObject.getSomeValue());
    }
}
