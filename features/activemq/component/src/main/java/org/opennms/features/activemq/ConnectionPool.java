/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.activemq;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This connection pool backports fixes for AMQ-5534 and AMQ-6290 to ActiveMQ 5.10.
 * This should no longer be necessary after upgrading to ActiveMQ 5.14+.
 *
 * See NMS-8714 for details.
 *
 * @author jwhite
 */
public class ConnectionPool extends org.apache.activemq.jms.pool.ConnectionPool implements ExceptionListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionPool.class);

    public ConnectionPool(Connection connection) {
        super(connection);
        try {
            getConnection().setExceptionListener(this);
        } catch (JMSException ex) {
            LOG.warn("Could not set exception listener on create of ConnectionPool");
        }
    }

    @Override
    public void start() throws JMSException {
        try {
            super.start();
        } catch (JMSException e) {
            close();
        }
    }

    @Override
    public void onException(JMSException exception) {
        close();
    }

}
