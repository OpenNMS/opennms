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

import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.base.Preconditions;

public class TestObjectImpl implements TestObject {

    private boolean transactionActive;

    private String someValue;

    public TestObjectImpl(boolean transactionActive) {
        this.transactionActive = transactionActive;
    }

    @Override
    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

    @Override
    public String getSomeValue() {
        return someValue;
    }

    @Override
    public void doSomething() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive() == transactionActive);
    }

    @Override
    public void doSomething2() {
        Preconditions.checkArgument(TransactionSynchronizationManager.isActualTransactionActive() == transactionActive);
    }

    @Override
    public boolean isTransactionActive() {
        return transactionActive;
    }
}
