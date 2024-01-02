/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd;

import com.google.common.util.concurrent.Striped;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.Lock;

public class AlarmTransactionExecutor {

    protected static final Integer NUM_STRIPE_LOCKS = SystemProperties.getInteger("org.opennms.alarmd.stripe.locks", Alarmd.THREADS * 4);
    private Striped<Lock> lockStripes = StripedExt.fairLock(NUM_STRIPE_LOCKS);

    @Autowired
    private TransactionOperations transactionOperations;

    private static Collection<String> getLockKeys(Event event) {
        if (event.getAlarmData().getClearKey() == null) {
            return Collections.singletonList(event.getAlarmData().getReductionKey());
        } else {
            return Arrays.asList(event.getAlarmData().getReductionKey(), event.getAlarmData().getClearKey());
        }
    }

    private static Collection<String> getLockKeys(OnmsAlarm alarm) {
        if (alarm.getClearKey() == null) {
            return Collections.singletonList(alarm.getReductionKey());
        } else {
            return Arrays.asList(alarm.getReductionKey(), alarm.getClearKey());
        }
    }

    public OnmsAlarm reduceEvent(Event event, TransactionCallback<OnmsAlarm> action) {
        // Lock both the reduction and clear keys (if set) using a fair striped lock
        // We do this to ensure that clears and triggers are processed in the same order
        // as the calls are made
        final Iterable<Lock> locks = lockStripes.bulkGet(getLockKeys(event));
        return executeWithLock(locks, action);
    }

    public void updateAlarm(OnmsAlarm alarm, TransactionCallback<OnmsAlarm> action) {
        final Iterable<Lock> locks = lockStripes.bulkGet(getLockKeys(alarm));
        executeWithLock(locks, action);
    }

    public <T> T executeWithLock(Iterable<Lock> locks, TransactionCallback<T> action) {
        try {
            locks.forEach(Lock::lock);
            // Process the alarm inside a transaction
            return transactionOperations.execute(action);
        } finally {
            locks.forEach(Lock::unlock);
        }
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }
}
