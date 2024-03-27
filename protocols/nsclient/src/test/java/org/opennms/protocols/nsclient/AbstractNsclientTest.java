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
package org.opennms.protocols.nsclient;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.server.exchange.RequestHandler;

/**
 * <p>Abstract Class for NSClient Tests.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public abstract class AbstractNsclientTest {

    private SimpleServer m_server = null;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    public SimpleServer getServer() {
        return m_server;
    }

    public void startServer(final String command, final String response) throws Exception {
        m_server  = new SimpleServer() {
            @Override
            public void onInit() {
                addResponseHandler(startsWith(command), new RequestHandler() {
                    @Override
                    public void doRequest(OutputStream out) throws IOException {
                        out.write(response.getBytes());
                    }
                });
            }
        };
        m_server.init();
        m_server.startServer();
        Thread.sleep(100); // make sure the server is really started
    }

    public void stopServer() throws Exception {
        if (m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }

}
