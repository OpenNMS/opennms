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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.support.GenericApplicationContext;

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

    /** Consumer whose destroy method calls through the proxy, as a SessionFactory teardown would. */
    public static class GreeterConsumer implements DisposableBean {
        private final Greeter greeter;
        Throwable destroyFailure;
        GreeterConsumer(Greeter greeter) {
            this.greeter = greeter;
        }
        @Override
        public void destroy() {
            try {
                greeter.greet();
            } catch (Throwable t) {
                destroyFailure = t;
            }
        }
    }

    /**
     * During context close the referenced service is typically already unregistered; a consumer bean's
     * destroy method calling through the proxy must fail fast, NOT park in waitForService(5min) — the
     * extender closes contexts on the OSGi event thread under its lifecycle lock, so that wait would
     * hang container shutdown. ContextClosedEvent fires before bean destruction, which is what arms
     * the fail-fast; if this regresses, the verify below sees a second waitForService call.
     */
    @Test
    public void consumerDestroyMethodFailsFastDuringContextClose() throws Exception {
        final Greeter a = mock(Greeter.class);
        final ServiceTracker<Object, Object> tracker = mockTracker();
        when(tracker.waitForService(anyLong())).thenReturn(a);
        // Present while the context starts, unregistered by the time it shuts down.
        when(tracker.getService()).thenReturn(a, (Object) null);

        final GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("greeterRef", OsgiServiceReferenceFactoryBean.class, () -> newBean(tracker));
        context.registerBean("consumer", GreeterConsumer.class,
                () -> new GreeterConsumer(context.getBean(Greeter.class)));
        context.refresh();
        final GreeterConsumer consumer = context.getBean(GreeterConsumer.class);

        context.close();

        assertNotNull("consumer destroy method must have failed (service is gone)", consumer.destroyFailure);
        assertTrue("expected fail-fast IllegalStateException, got " + consumer.destroyFailure,
                consumer.destroyFailure instanceof IllegalStateException);
        // Exactly one wait: the mandatory-reference wait in getObject(). The shutdown-path call must not wait.
        verify(tracker, times(1)).waitForService(anyLong());
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
