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
package org.opennms.core.test.activemq;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.activemq.broker.BrokerService;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMQBroker extends ExternalResource {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQBroker.class);

    private BrokerService m_broker = new BrokerService();
    private Path m_temporaryDirectory;
    private final String m_brokerURL;

    public ActiveMQBroker() {
        m_brokerURL = null;
    }

    public ActiveMQBroker(final String brokerURL) {
        m_brokerURL = brokerURL;
    }

    @Override
    public void before() throws Exception {
        m_temporaryDirectory = Files.createTempDirectory("activemq-data");
        m_broker.setPersistent(false);
        m_broker.setDataDirectory(m_temporaryDirectory.toString());
        if (m_brokerURL != null) {
            m_broker.addConnector(m_brokerURL);
        }
        m_broker.start();
        if (!m_broker.waitUntilStarted()) {
            throw new Exception("ActiveMQ broker was not started or stopped unexpectedly. Error: " + m_broker.getStartException());
        }
    }

    @Override
    public void after() {
        try {
            m_broker.stop();
            m_broker.waitUntilStopped();
        } catch (Throwable e) {
            LOG.warn("An error occurred while stopping the broker.", e);
        }

        try {
            if (m_temporaryDirectory != null) {
                Files.walkFileTree(m_temporaryDirectory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } 
        } catch (IOException e) {
            LOG.warn("An error occurred while deleting the temporary directory '{}'.", m_temporaryDirectory, e);
        }
    }
}
