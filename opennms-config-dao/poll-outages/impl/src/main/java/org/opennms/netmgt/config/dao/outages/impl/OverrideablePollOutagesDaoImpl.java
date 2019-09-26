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
