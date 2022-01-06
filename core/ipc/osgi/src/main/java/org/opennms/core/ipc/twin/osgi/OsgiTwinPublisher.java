/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.twin.osgi;

import java.io.IOException;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;

public class OsgiTwinPublisher implements TwinPublisher {

    private final ServiceLookup<Class<?>, String> blockingServiceLookup;

    public OsgiTwinPublisher() {
        this.blockingServiceLookup = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
                .blocking()
                .build();
        ;
    }

    private TwinPublisher getDelegate() throws IOException {
        final TwinPublisher twinPublisher = this.blockingServiceLookup.lookup(TwinPublisher.class, "(!(strategy=delegate))");
        if (twinPublisher != null) {
            return twinPublisher;
        } else {
            throw new IOException("Only delegate publisher is registered. No real publisher available");
        }
    }

    @Override
    public <T> Session<T> register(String key, Class<T> clazz, String location) throws IOException {
        return this.getDelegate().register(key, clazz, location);
    }

    @Override
    public void close() throws IOException {
        this.getDelegate().close();
    }
}
