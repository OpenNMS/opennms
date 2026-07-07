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
package org.opennms.core.test.junit5;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.BootstrapContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;

/**
 * Registers the standard OpenNMS {@link TestExecutionListener}s the same way
 * {@code org.opennms.core.test.OpenNMSJUnit4ClassRunner} does for JUnit 4: by class name,
 * silently skipping listeners whose test-api module is not on the classpath, and in a fixed
 * registration order that puts the OpenNMS listeners before Spring's
 * {@code DependencyInjectionTestExecutionListener} (the temporary database must exist before
 * the application context is created and injected).
 *
 * <p>Implemented by delegation rather than by extending
 * {@code AbstractTestContextBootstrapper}, because its {@code getTestExecutionListeners()}
 * is final and re-sorts default listeners by {@code @Order} semantics — which would move the
 * OpenNMS listeners (all default order, lowest precedence) after dependency injection.
 * The order of the list below is the contract.</p>
 *
 * <p>Any additional listeners Spring would have registered (declared via
 * {@code @TestExecutionListeners} on the test class, or Spring's remaining defaults) are
 * appended after the standard set, mirroring the JUnit 4 runner's behavior.</p>
 */
public class OpenNMSTestContextBootstrapper implements TestContextBootstrapper {

    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSTestContextBootstrapper.class);

    /** Keep in sync with {@code OpenNMSJUnit4ClassRunner.STANDARD_LISTENER_CLASS_NAMES}. */
    private static final String[] STANDARD_LISTENER_CLASS_NAMES = new String[] {
        "org.opennms.core.test.TestContextAwareExecutionListener",
        "org.opennms.core.test.BeanUtilsTestContextInjectionExecutionListener",
        "org.opennms.test.OpenNMSConfigurationExecutionListener",
        "org.opennms.core.test.db.TemporaryDatabaseExecutionListener",
        "org.opennms.core.test.dns.JUnitDNSServerExecutionListener",
        "org.opennms.core.test.http.JUnitHttpServerExecutionListener",
        "org.opennms.core.test.snmp.JUnitSnmpAgentExecutionListener",
        "org.opennms.core.test.ssh.JUnitSshServerExecutionListener",
        "org.opennms.core.collection.test.JUnitCollectorExecutionListener",
        "org.springframework.test.context.support.DependencyInjectionTestExecutionListener",
        "org.springframework.test.context.support.DirtiesContextTestExecutionListener",
        "org.springframework.test.context.transaction.TransactionalTestExecutionListener"
    };

    private final DefaultTestContextBootstrapper delegate = new DefaultTestContextBootstrapper();

    @Override
    public void setBootstrapContext(final BootstrapContext bootstrapContext) {
        delegate.setBootstrapContext(bootstrapContext);
    }

    @Override
    public BootstrapContext getBootstrapContext() {
        return delegate.getBootstrapContext();
    }

    @Override
    public TestContext buildTestContext() {
        return delegate.buildTestContext();
    }

    @Override
    public MergedContextConfiguration buildMergedContextConfiguration() {
        return delegate.buildMergedContextConfiguration();
    }

    @Override
    public List<TestExecutionListener> getTestExecutionListeners() {
        final List<TestExecutionListener> springListeners = delegate.getTestExecutionListeners();

        final List<TestExecutionListener> listeners = new ArrayList<>();
        final Set<String> registered = new HashSet<>();

        for (final String className : STANDARD_LISTENER_CLASS_NAMES) {
            try {
                final Class<?> listenerClass = Class.forName(className);
                listeners.add((TestExecutionListener) listenerClass.getDeclaredConstructor().newInstance());
                registered.add(className);
            } catch (final Exception | NoClassDefFoundError e) {
                LOG.info("Failed while attempting to load default unit test listener class {}: {}", className, e.getLocalizedMessage());
            }
        }

        // Append whatever Spring computed (class-declared or remaining default listeners)
        // as long as it is not already part of the standard set
        for (final TestExecutionListener listener : springListeners) {
            if (!registered.contains(listener.getClass().getName())) {
                listeners.add(listener);
            }
        }

        return listeners;
    }
}
