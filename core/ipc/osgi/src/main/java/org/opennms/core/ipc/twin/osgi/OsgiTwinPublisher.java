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
