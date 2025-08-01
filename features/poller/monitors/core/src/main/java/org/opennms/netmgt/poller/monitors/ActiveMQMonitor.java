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
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author roskens
 */
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
