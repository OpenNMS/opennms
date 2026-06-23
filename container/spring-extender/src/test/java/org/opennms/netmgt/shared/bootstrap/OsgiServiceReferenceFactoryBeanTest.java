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
package org.opennms.netmgt.shared.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Unit tests for {@link OsgiServiceReferenceFactoryBean}, focused on the dynamic-proxy rebinding behavior
 * that replaced raw-service caching: an already-injected reference must follow the backing OSGi service when
 * it is unregistered and replaced at runtime, the way Gemini's {@code <osgi:reference>} proxy did.
 */
public class OsgiServiceReferenceFactoryBeanTest {

    /** Simple service interface to back the proxy. */
    public interface Greeter {
        String greet();
    }

    /** Subclass that injects a mock tracker instead of opening one against a live framework. */
    private static final class TestableBean extends OsgiServiceReferenceFactoryBean {
        private final ServiceTracker<Object, Object> mockTracker;
        TestableBean(ServiceTracker<Object, Object> mockTracker) {
            this.mockTracker = mockTracker;
        }
        @Override
        ServiceTracker<Object, Object> createTracker() {
            return mockTracker;
        }
    }

    @SuppressWarnings("unchecked")
    private static ServiceTracker<Object, Object> mockTracker() {
        return mock(ServiceTracker.class);
    }

    private static OsgiServiceReferenceFactoryBean newBean(ServiceTracker<Object, Object> tracker) {
        final OsgiServiceReferenceFactoryBean bean = new TestableBean(tracker);
        bean.setBeanClassLoader(Greeter.class.getClassLoader());
        bean.setInterfaceName(Greeter.class.getName());
        return bean;
    }

    @Test
    public void returnsAProxyOfTheServiceInterface() throws Exception {
        final Greeter a = mock(Greeter.class);
        final ServiceTracker<Object, Object> tracker = mockTracker();
        when(tracker.waitForService(anyLong())).thenReturn(a);
        when(tracker.getService()).thenReturn(a);

        final Object obj = newBean(tracker).getObject();
        assertNotNull(obj);
        // It is a dynamic proxy, not the raw service instance.
        assertNotSame(a, obj);
        assertEquals(true, obj instanceof Greeter);
    }

    @Test
    public void proxyFollowsServiceReplacement() throws Exception {
        final Greeter a = mock(Greeter.class);
        when(a.greet()).thenReturn("A");
        final Greeter b = mock(Greeter.class);
        when(b.greet()).thenReturn("B");

        final ServiceTracker<Object, Object> tracker = mockTracker();
        when(tracker.waitForService(anyLong())).thenReturn(a);
        // The tracker initially holds A, then A is unregistered and replaced by B.
        when(tracker.getService()).thenReturn(a, a, b);

        final Greeter proxy = (Greeter) newBean(tracker).getObject();
        assertEquals("A", proxy.greet());   // dispatched to the original service
        assertEquals("B", proxy.greet());   // transparently follows the replacement
    }

    @Test
    public void proxyThrowsWhenServiceGoesAwayPermanently() throws Exception {
        final Greeter a = mock(Greeter.class);
        final ServiceTracker<Object, Object> tracker = mockTracker();
        when(tracker.waitForService(anyLong())).thenReturn(a, (Object) null);
        // First call resolves A; on the second call the service is gone and never comes back.
        when(tracker.getService()).thenReturn(a, (Object) null);

        final Greeter proxy = (Greeter) newBean(tracker).getObject();
        assertNotNull(proxy);
        assertThrows(IllegalStateException.class, proxy::greet);
    }

    @Test
    public void getObjectTimesOutWhenServiceNeverAppears() throws Exception {
        final ServiceTracker<Object, Object> tracker = mockTracker();
        when(tracker.getService()).thenReturn(null);
        when(tracker.waitForService(anyLong())).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> newBean(tracker).getObject());
    }

    @Test
    public void objectMethodsDoNotTouchTheBackingService() throws Exception {
        final Greeter a = mock(Greeter.class);
        final ServiceTracker<Object, Object> tracker = mockTracker();
        when(tracker.waitForService(anyLong())).thenReturn(a);
        when(tracker.getService()).thenReturn(a);

        final Greeter proxy = (Greeter) newBean(tracker).getObject();
        // hashCode/equals/toString resolve on the proxy itself; equals is identity.
        assertEquals(proxy, proxy);
        assertNotNull(proxy.toString());
        assertEquals(System.identityHashCode(proxy), proxy.hashCode());
    }
}
