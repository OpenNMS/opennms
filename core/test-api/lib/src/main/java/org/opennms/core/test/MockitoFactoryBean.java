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

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

/**
 * Defines a Mockito mock as a Spring bean.
 *
 * <p>Use this instead of {@code <bean class="org.mockito.Mockito" factory-method="mock">}:
 * since Mockito 4.10 the {@code mock(...)} overloads (reified {@code mock(T...)},
 * {@code mock(String, T...)}) prevent Spring from predicting the factory method's return
 * type, so the bean is treated as {@code Object} and is never an autowire candidate for
 * the mocked interface. A {@link FactoryBean} reports the exact type up front.</p>
 *
 * <pre>
 * &lt;bean id="eventForwarder" class="org.opennms.core.test.MockitoFactoryBean"&gt;
 *     &lt;constructor-arg value="org.opennms.netmgt.events.api.EventForwarder"/&gt;
 * &lt;/bean&gt;
 * </pre>
 */
public class MockitoFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> type;

    public MockitoFactoryBean(final Class<T> type) {
        this.type = type;
    }

    @Override
    public T getObject() {
        return Mockito.mock(type);
    }

    @Override
    public Class<T> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
