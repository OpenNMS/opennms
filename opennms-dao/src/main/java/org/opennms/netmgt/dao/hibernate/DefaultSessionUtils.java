/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.util.function.Supplier;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionOperations;

public class DefaultSessionUtils implements SessionUtils {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TransactionOperations transactionOperations;

    @Override
    public <V> V withTransaction(Supplier<V> supplier) {
        return transactionOperations.execute(status -> supplier.get());
    }

    @Override
    public <V> V withManualFlush(Supplier<V> supplier) {
        final FlushMode flushMode = sessionFactory.getCurrentSession().getFlushMode();
        try {
            sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
            return supplier.get();
        } finally {
            sessionFactory.getCurrentSession().setFlushMode(flushMode);
        }
    }

}
