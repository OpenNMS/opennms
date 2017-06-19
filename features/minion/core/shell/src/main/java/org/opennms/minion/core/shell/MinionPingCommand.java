/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/
package org.opennms.minion.core.shell;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.minion.core.api.RestClient;

@Command(scope = "minion", name = "ping", description="Tests connectivity with the controller.")
@Service
public class MinionPingCommand implements Action {

    @Reference
    public ConnectionFactory brokerConnectionFactory;

    @Reference
    public RestClient restClient;

    @Override
    public Object execute() throws Exception {
        System.out.println("Connecting to ReST...");
        restClient.ping();
        System.out.println("OK");

        System.out.println("Connecting to Broker...");
        Connection jmsConnection = null;
        try {
            jmsConnection = brokerConnectionFactory.createConnection();
            // NMS-9445: Attempt to use the connection by creating a session
            // and immediately closing it.
            jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE).close();
        } finally{
            if (jmsConnection != null) {
                try {
                    jmsConnection.close();
                } catch(JMSException ex) {
                    System.out.println("Failed to close JMSConnection: " + ex.getMessage());
                }
            }
        }

        System.out.println("OK");
        return null;
    }
}
