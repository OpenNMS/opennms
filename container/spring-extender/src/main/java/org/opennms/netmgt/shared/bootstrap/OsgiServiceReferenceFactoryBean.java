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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import org.opennms.core.sysprops.SystemProperties;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ClassUtils;

/**
 * Backs {@code <osgi:reference/>}: blocks (on the extender's context-creation
 * thread) until the referenced OSGi service first shows up, mirroring Gemini's
 * "waiting for dependencies" behavior for a mandatory reference.
 *
 * <p>Like Gemini &mdash; and unlike a plain {@link ServiceTracker#getService()} cache &mdash;
 * it hands out a <em>dynamic proxy</em> rather than the raw service instance. Each method
 * invocation is dispatched to whatever service the tracker currently holds, so if the backing
 * service is unregistered and replaced at runtime (e.g. the {@code DataSource} when its bundle
 * is reconfigured or reinstalled), already-injected consumer beans transparently follow the new
 * service instead of being pinned to the stopped one until the whole consumer context restarts.</p>
 */
public class OsgiServiceReferenceFactoryBean implements FactoryBean<Object>, BeanClassLoaderAware, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiServiceReferenceFactoryBean.class);

    private static final long TIMEOUT_MS = SystemProperties.getLong(
            "org.opennms.spring.extender.serviceWaitMillis", TimeUnit.MINUTES.toMillis(5));

    private BundleContext bundleContext;
    private String interfaceName;
    private String filter;
    private ClassLoader beanClassLoader;

    private ServiceTracker<Object, Object> tracker;
    private Object proxy;

    @Override
    public synchronized Object getObject() throws Exception {
        if (proxy != null) {
            return proxy;
        }
        if (tracker == null) {
            tracker = createTracker();
            tracker.open();
        }
        // Mandatory-reference semantics: block until the service is available at least once, so the
        // consuming context does not start without its dependency present (as Gemini's 1..1 reference did).
        if (tracker.getService() == null) {
            LOG.info("Waiting up to {}ms for OSGi service '{}'{}", TIMEOUT_MS, interfaceName,
                    filter == null ? "" : " with filter " + filter);
        }
        if (tracker.waitForService(TIMEOUT_MS) == null) {
            throw new IllegalStateException(String.format(
                    "Timed out after %dms waiting for OSGi service '%s'%s", TIMEOUT_MS, interfaceName,
                    filter == null ? "" : " with filter " + filter));
        }

        final Class<?> type = getObjectType();
        if (type != null && type.isInterface()) {
            proxy = Proxy.newProxyInstance(proxyClassLoader(type), new Class<?>[] { type }, new TrackingInvocationHandler());
        } else {
            // Not an interface (or unresolvable): we cannot build a dynamic proxy, so fall back to the raw
            // service. This loses runtime rebinding, so warn — all current OpenNMS references are interfaces.
            LOG.warn("OSGi reference '{}' is not an interface; injecting the raw service without dynamic rebinding", interfaceName);
            proxy = tracker.getService();
        }
        return proxy;
    }

    /** Creates the service tracker. Package-private seam so tests can substitute a tracker without a live framework. */
    ServiceTracker<Object, Object> createTracker() throws Exception {
        if (filter == null) {
            return new ServiceTracker<>(bundleContext, interfaceName, null);
        }
        final String fullFilter = String.format("(&(objectClass=%s)%s)", interfaceName, filter);
        return new ServiceTracker<>(bundleContext, bundleContext.createFilter(fullFilter), null);
    }

    private ClassLoader proxyClassLoader(final Class<?> type) {
        // The proxy must be defined by a loader that can see the service interface.
        return type.getClassLoader() != null ? type.getClassLoader() : beanClassLoader;
    }

    /** Resolves the current backing service on every call, blocking briefly if it is momentarily absent. */
    private final class TrackingInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(final Object p, final Method method, final Object[] args) throws Throwable {
            // java.lang.Object methods operate on the proxy's identity, not the backing service.
            if (method.getDeclaringClass() == Object.class) {
                switch (method.getName()) {
                    case "hashCode":
                        return System.identityHashCode(p);
                    case "equals":
                        return p == (args == null ? null : args[0]);
                    case "toString":
                        return "OsgiServiceReferenceProxy[" + interfaceName + (filter == null ? "" : filter) + "]";
                    default:
                        break;
                }
            }
            Object service = tracker.getService();
            if (service == null) {
                // The backing service is transiently gone (mid-restart); wait for the replacement.
                service = tracker.waitForService(TIMEOUT_MS);
            }
            if (service == null) {
                throw new IllegalStateException(String.format(
                        "OSGi service '%s'%s is unavailable after waiting %dms", interfaceName,
                        filter == null ? "" : " with filter " + filter, TIMEOUT_MS));
            }
            try {
                return method.invoke(service, args);
            } catch (final InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    @Override
    public Class<?> getObjectType() {
        try {
            return ClassUtils.forName(interfaceName, beanClassLoader);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public synchronized void destroy() {
        if (tracker != null) {
            tracker.close();
            tracker = null;
        }
        proxy = null;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
