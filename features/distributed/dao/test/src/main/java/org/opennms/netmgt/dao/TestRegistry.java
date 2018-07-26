/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class TestRegistry {

    private List<Class> ignoredClasses = new ArrayList<>();
    private Map<Class, Consumer> tests = new HashMap<>();
    private List<Class> testsRun = new ArrayList<>();

    public TestRegistry withIgnoredClass(Class<?> ... classesToIgnore) {
        if (classesToIgnore != null) {
            for (Class c : classesToIgnore) {
                ignoredClasses.add(c);
            }
        }
        return this;
    }

    public <T> TestRegistry withTest(Class<T> clazz, Consumer<T> consumer) {
        tests.put(clazz, consumer);
        return this;
    }

    public boolean isTested(Class clazz) {
        return testsRun.contains(clazz);
    }

    public boolean isIgnored(Class clazz) {
        return ignoredClasses.contains(clazz);
    }

    public <T> Consumer<T> getTest(Class<T> type) {
        return findConsumer(type);
    }

    public void markAsRun(Class type) {
        testsRun.add(type);
    }

    private <T> Consumer<T> findConsumer(Class type) {
        final Consumer<T> consumer = tests.get(type);
        if (consumer != null) {
            return consumer;
        }
        for (Class c : type.getInterfaces()) {
            return findConsumer(c);
        }
        return null;
    }
}
