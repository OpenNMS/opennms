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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

    @Override
    public void before() throws Exception {
        m_temporaryDirectory = Files.createTempDirectory("activemq-data");
        m_broker.setPersistent(false);
        m_broker.setDataDirectory(m_temporaryDirectory.toString());
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
