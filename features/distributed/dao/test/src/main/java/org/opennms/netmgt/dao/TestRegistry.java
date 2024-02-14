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
