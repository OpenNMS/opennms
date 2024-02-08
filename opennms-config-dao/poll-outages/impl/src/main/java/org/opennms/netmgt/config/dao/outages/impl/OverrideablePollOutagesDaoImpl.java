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
package org.opennms.netmgt.config.dao.outages.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.dao.outages.api.OverrideablePollOutagesDao;
import org.opennms.netmgt.config.poller.outages.Outages;

public class OverrideablePollOutagesDaoImpl extends AbstractPollOutagesDao implements OverrideablePollOutagesDao {
    private Outages outagesConfig;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public OverrideablePollOutagesDaoImpl() {
        super();
    }

    @Override
    public synchronized void overrideConfig(InputStream config) {
        Objects.requireNonNull(config);

        try (Reader reader = new InputStreamReader(config)) {
            overrideConfig(JaxbUtils.unmarshal(Outages.class, reader));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void overrideConfig(Outages config) {
        outagesConfig = Objects.requireNonNull(config);
    }

    @Override
    public Lock getReadLock() {
        return readWriteLock.readLock();
    }

    @Override
    public Lock getWriteLock() {
        return readWriteLock.writeLock();
    }

    @Override
    public void withWriteLock(Consumer<Outages> consumerWithLock) {
        getWriteLock().lock();

        try {
            consumerWithLock.accept(getReadOnlyConfig());
        } finally {
            getWriteLock().unlock();
        }
    }

    @Override
    public void saveConfig() {
        // no-op
    }

    @Override
    public void onConfigChanged() {
        // no-op
    }

    @Override
    public synchronized Outages getReadOnlyConfig() {
        return outagesConfig;
    }

    @Override
    public synchronized void reload() {
        if (outagesConfig == null) {
            outagesConfig = new Outages();
        }
    }
}
