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
package org.opennms.core.mate.model;

import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.NoOpEntityScopeProvider;

public class EntityScopeProviderFactoryTest {

    private EntityScopeProviderFactory factory;
    private EntityScopeProviderImpl mockDelegate;

    @Before
    public void setUp() {
        mockDelegate = Mockito.mock(EntityScopeProviderImpl.class);
        factory = new EntityScopeProviderFactory();
        factory.setDelegate(mockDelegate);
    }

    @After
    public void tearDown() {
        System.clearProperty(EntityScopeProviderFactory.ENABLED_PROPERTY);
    }

    @Test
    public void shouldReturnDelegateByDefault() {
        EntityScopeProvider result = factory.getObject();
        assertSame(mockDelegate, result);
    }

    @Test
    public void shouldReturnDelegateWhenEnabled() {
        System.setProperty(EntityScopeProviderFactory.ENABLED_PROPERTY, "true");
        EntityScopeProvider result = factory.getObject();
        assertSame(mockDelegate, result);
    }

    @Test
    public void shouldReturnNoOpWhenDisabled() {
        System.setProperty(EntityScopeProviderFactory.ENABLED_PROPERTY, "false");
        EntityScopeProvider result = factory.getObject();
        assertSame(NoOpEntityScopeProvider.INSTANCE, result);
    }

    @Test
    public void shouldReturnEntityScopeProviderClass() {
        assertSame(EntityScopeProvider.class, factory.getObjectType());
    }

    @Test
    public void shouldBeSingleton() {
        assertSame(true, factory.isSingleton());
    }
}
