/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author roskens
 */
@Distributable(DistributionContext.DAEMON)
public class ActiveMQMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQMonitor.class);

    private static final int DEFAULT_RETRY = 3;
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final String DEFAULT_BROKERURL = "vm://localhost?create=false&broker.persistent=false";

    private static final String PARAMETER_BROKERURL = "broker-url";
    private static final String PARAMETER_USER = "user";
    private static final String PARAMETER_PASSWORD = "password";
    private static final String PARAMETER_USE_NODELABEL = "use-nodelabel";
    private static final String PARAMETER_CLIENTID = "client-id";
    private static final String PARAMETER_CREATE_SESSION = "create-session";

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = PollStatus.unknown("polling never attempted");

        if (parameters == null) {
                throw new NullPointerException("parameter cannot be null");
        }

        String brokerURL = ParameterMap.getKeyedString(parameters, PARAMETER_BROKERURL, DEFAULT_BROKERURL);
        String userName  = ParameterMap.getKeyedString(parameters, PARAMETER_USER, null);
        String password  = ParameterMap.getKeyedString(parameters, PARAMETER_PASSWORD, null);
        Boolean useNodeLabel = ParameterMap.getKeyedBoolean(parameters, PARAMETER_USE_NODELABEL, false);
        Boolean createSession = ParameterMap.getKeyedBoolean(parameters, PARAMETER_CREATE_SESSION, false);
        String clientID = ParameterMap.getKeyedString(parameters, PARAMETER_CLIENTID, null);
        URI uri = null;
        try {
            uri = new URI(brokerURL);
            LOG.debug("using brokerURL: {}", uri);
            if (useNodeLabel) {
                uri = new URI(uri.getScheme(), uri.getUserInfo(), svc.getNodeLabel(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
                LOG.debug("using updated brokerURL: {}", uri);
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("broker URL cannot be parsed");
        }

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(uri);
        if (userName != null && !userName.isEmpty()) {
            connectionFactory.setUserName(userName);
        }
        if (password != null && !password.isEmpty()) {
            connectionFactory.setPassword(password);
        }
        connectionFactory.setClientID(clientID);

        TimeoutTracker timeoutTracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        Connection connection = null;

        for(timeoutTracker.reset(); timeoutTracker.shouldRetry(); timeoutTracker.nextAttempt()) {
            timeoutTracker.startAttempt();
            status = PollStatus.unknown("polling never attempted");
            try {
                connection = connectionFactory.createConnection();
                connection.start();

                if (createSession) {
                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    session.close();
                }

                status = PollStatus.available(timeoutTracker.elapsedTimeInMillis());
                break;
            } catch (JMSException ex) {
                LOG.debug("Received a JMSException while connecting to the remote ActiveMQ Broker", ex);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (JMSException ex) {
                        LOG.debug("Received a JMSException while closing the connection to the remote ActiveMQ Broker", ex);
                }
            }
        }
        return status;
    }

}
