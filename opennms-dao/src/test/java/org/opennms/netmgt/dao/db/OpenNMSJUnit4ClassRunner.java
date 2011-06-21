/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.dao.db;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.junit.runners.model.InitializationError;
import org.opennms.core.test.JUnitHttpServerExecutionListener;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * This runner will automagically register all of the boilerplate 
 * TestExecutionListener instances that the OpenNMS code craves.
 *
 * @author seth
 */
public class OpenNMSJUnit4ClassRunner extends SpringJUnit4ClassRunner {
    private static final TestExecutionListener[] STANDARD_LISTENERS = new TestExecutionListener[] { 
        new OpenNMSConfigurationExecutionListener(),
        new TemporaryDatabaseExecutionListener(),
        new JUnitSnmpAgentExecutionListener(),
        new JUnitHttpServerExecutionListener(),
        new DependencyInjectionTestExecutionListener(),
        new DirtiesContextTestExecutionListener(),
        new TransactionalTestExecutionListener()
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
        getTestContextManager().registerTestExecutionListeners(STANDARD_LISTENERS);

        // Add any additional listeners that may have been specified manually in the test
        Comparator<TestExecutionListener> comparator = new ClassNameComparator();
        TreeSet<TestExecutionListener> standardListeners = new TreeSet<TestExecutionListener>(comparator);
        for (TestExecutionListener listener : listeners) {
            if (!standardListeners.contains(listener)) {
                getTestContextManager().registerTestExecutionListeners(listener);
            }
        }
    }
}
