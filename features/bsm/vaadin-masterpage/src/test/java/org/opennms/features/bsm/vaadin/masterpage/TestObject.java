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

import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.base.Preconditions;

/**
 * TestObject to verify that the {@link TransactionAwareBeanProxy} works as expected.
 * Please do not change the behaviour here, as it is required to pass the tests in {@link TransactionAwareBeanProxyTest}.
 */
public class TestObject {

    private boolean transactionActive;

    private String someValue;

    public TestObject(boolean transactionActive) {
        this.transactionActive = transactionActive;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

    public String getSomeValue() {
        return someValue;
    }

    public void doSomething() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive() == transactionActive);
    }

    public void doSomething2() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive() == transactionActive);
    }

    public boolean isTransactionActive() {
        return transactionActive;
    }
}
