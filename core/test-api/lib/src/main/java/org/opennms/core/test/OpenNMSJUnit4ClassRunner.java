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
package org.opennms.core.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This runner will automagically register all of the boilerplate 
 * TestExecutionListener instances that the OpenNMS code craves.
 *
 * @author seth
 */
public class OpenNMSJUnit4ClassRunner extends SpringJUnit4ClassRunner {
	
	private static final Logger LOG = LoggerFactory.getLogger(OpenNMSJUnit4ClassRunner.class);
	
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

    private static class ClassNameComparator implements Comparator<TestExecutionListener> {

        @Override
        public int compare(TestExecutionListener o1, TestExecutionListener o2) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
        
    }

    public OpenNMSJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);

        // Make a deep copy of the existing listeners
        List<TestExecutionListener> oldListeners = getTestContextManager().getTestExecutionListeners();
        List<TestExecutionListener> listeners = new ArrayList<>(oldListeners.size());
        for (TestExecutionListener old : oldListeners) {
            listeners.add(old);
        }
        oldListeners.clear();

        // Register the standard set of execution listeners
        for (final String className : STANDARD_LISTENER_CLASS_NAMES) {
            try {
                final var standardListenerClazz = Class.forName(className);
				final TestExecutionListener listener = (TestExecutionListener)standardListenerClazz.getDeclaredConstructor().newInstance();
                getTestContextManager().registerTestExecutionListeners(listener);
            } catch (final Exception e) {
            	LOG.info("Failed while attempting to load default unit test listener class {}: {}", className, e.getLocalizedMessage());
            }
        }

        // Add any additional listeners that may have been specified manually in the test
        final Comparator<TestExecutionListener> comparator = new ClassNameComparator();
        final TreeSet<TestExecutionListener> standardListeners = new TreeSet<>(comparator);
        standardListeners.addAll(getTestContextManager().getTestExecutionListeners());
        for (final TestExecutionListener listener : listeners) {
            if (!standardListeners.contains(listener)) {
                getTestContextManager().registerTestExecutionListeners(listener);
            }
        }
    }
}
