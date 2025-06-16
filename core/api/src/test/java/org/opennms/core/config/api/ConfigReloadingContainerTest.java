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
package org.opennms.core.config.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;

import com.google.common.collect.Maps;

public class ConfigReloadingContainerTest {

    private ServiceRegistry registry = DefaultServiceRegistry.INSTANCE;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        registry.unregisterAll(ConfigurationProvider.class);
    }

    @Test
    public void builderUsesSaneDefaults() {
        ConfigReloadContainer<SomeConfig> container = new ConfigReloadContainer.Builder<>(SomeConfig.class)
                .build();
        assertThat(container.getObject(), is(nullValue()));
        assertThat(container.getLastUpdate(), is(equalTo(-1L)));
    }

    @Test
    public void canUseAnInitialObject() {
        Date before = new Date();
        SomeConfig config = new SomeConfig();
        ConfigReloadContainer<SomeConfig> container = new ConfigReloadContainer.Builder<>(SomeConfig.class)
                .withInitialConfig(config)
                .build();
        assertThat(container.getObject(), is(equalTo(config)));
        assertThat(container.getLastUpdate(), is(greaterThanOrEqualTo(before.getTime())));
    }

    @Test
    public void canReloadOnGetWhenNoObjectIsGiven() {
        final SomeConfig config = new SomeConfig();
        final AtomicInteger loadCount = new AtomicInteger();
        final BeanConfigurationProvider<SomeConfig> provider = new BeanConfigurationProvider<SomeConfig>(config) {
            @Override
            public SomeConfig getObject() {
                loadCount.incrementAndGet();
                return config;
            }
        };
        ConfigReloadContainer<SomeConfig> container = new ConfigReloadContainer.Builder<>(SomeConfig.class)
                .withProvider(provider)
                .build();
        assertThat(container.getObject(), is(equalTo(config)));

        // Access the object another few times for good measure
        container.getObject();
        container.getObject();
        // Reload should only have been called once
        assertThat(loadCount.get(), is(equalTo(1)));
    }

    @Test
    public void canExtend() {
        ConfigReloadContainer<SomeConfig> container = new ConfigReloadContainer.Builder<>(SomeConfig.class)
                .withProvider(new BeanConfigurationProvider<>(new SomeConfig(1)))
                .withFolder((accumulator, next) -> accumulator.add(next.getSum()))
                .build();

        // Verify the original object
        assertThat(container.getObject().getSum(), is(equalTo(1)));

        // Extend it
        BeanConfigurationProvider<SomeConfig> ext = new BeanConfigurationProvider<>(new SomeConfig(2));
        Map<String, String> props = Maps.newHashMap();
        registry.register(ext, props, ConfigurationProvider.class);

        // Verify the extended object
        assertThat(container.getObject().getSum(), is(equalTo(3)));
    }

    private static class SomeConfig {
        int sum;

        public SomeConfig() {

        }

        public SomeConfig(int sum) {
            this.sum = sum;
        }

        public int getSum() {
            return sum;
        }

        public void add(int x) {
            sum += x;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SomeConfig that = (SomeConfig) o;
            return sum == that.sum;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sum);
        }
    }
}
