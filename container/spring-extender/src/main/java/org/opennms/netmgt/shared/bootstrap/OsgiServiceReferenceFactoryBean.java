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
 * thread) until the referenced OSGi service shows up, mirroring Gemini's
 * "waiting for dependencies" behavior, then hands out the service instance.
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
    private Object service;

    @Override
    public synchronized Object getObject() throws Exception {
        if (service != null) {
            return service;
        }
        if (tracker == null) {
            if (filter == null) {
                tracker = new ServiceTracker<>(bundleContext, interfaceName, null);
            } else {
                final String fullFilter = String.format("(&(objectClass=%s)%s)", interfaceName, filter);
                tracker = new ServiceTracker<>(bundleContext, bundleContext.createFilter(fullFilter), null);
            }
            tracker.open();
        }
        if (tracker.getService() == null) {
            LOG.info("Waiting up to {}ms for OSGi service '{}'{}", TIMEOUT_MS, interfaceName,
                    filter == null ? "" : " with filter " + filter);
        }
        service = tracker.waitForService(TIMEOUT_MS);
        if (service == null) {
            throw new IllegalStateException(String.format(
                    "Timed out after %dms waiting for OSGi service '%s'%s", TIMEOUT_MS, interfaceName,
                    filter == null ? "" : " with filter " + filter));
        }
        return service;
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
        service = null;
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
