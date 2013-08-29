/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        "org.opennms.test.OpenNMSConfigurationExecutionListener",
        "org.opennms.core.test.db.TemporaryDatabaseExecutionListener",
        "org.opennms.core.test.snmp.JUnitSnmpAgentExecutionListener",
        "org.opennms.core.test.http.JUnitHttpServerExecutionListener",
        "org.opennms.core.test.dns.JUnitDNSServerExecutionListener",
        "org.opennms.netmgt.collectd.JUnitCollectorExecutionListener",
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
        List<TestExecutionListener> listeners = new ArrayList<TestExecutionListener>(oldListeners.size());
        for (TestExecutionListener old : oldListeners) {
            listeners.add(old);
        }
        oldListeners.clear();

        // Register the standard set of execution listeners
        for (final String className : STANDARD_LISTENER_CLASS_NAMES) {
            try {
                final TestExecutionListener listener = (TestExecutionListener)Class.forName(className).newInstance();
                getTestContextManager().registerTestExecutionListeners(listener);
            } catch (final Throwable t) {
            	LOG.info("Failed while attempting to load default unit test listener class {}: {}", className, t.getLocalizedMessage());
            }
        }

        // Add any additional listeners that may have been specified manually in the test
        final Comparator<TestExecutionListener> comparator = new ClassNameComparator();
        final TreeSet<TestExecutionListener> standardListeners = new TreeSet<TestExecutionListener>(comparator);
        standardListeners.addAll(getTestContextManager().getTestExecutionListeners());
        for (final TestExecutionListener listener : listeners) {
            if (!standardListeners.contains(listener)) {
                getTestContextManager().registerTestExecutionListeners(listener);
            }
        }
    }
}
