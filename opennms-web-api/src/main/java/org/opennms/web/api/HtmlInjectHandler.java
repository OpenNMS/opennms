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
package org.opennms.web.api;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows services to inject HTML content at arbitrary URLs.
 *
 * @author jwhite
 */
public class HtmlInjectHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlInjectHandler.class);
    private static AtomicReference<ServiceRegistry> serviceRegistryRef = new AtomicReference<ServiceRegistry>(); 

    public static String inject(final HttpServletRequest request) {
        // Fetch the serviceRegistry bean on the first call and cache it since
        // the reference won't change until the JVM is restarted
        ServiceRegistry serviceRegistry = serviceRegistryRef.updateAndGet(ref -> {
            if (ref == null) {
                return BeanUtils.getBean("soaContext", "serviceRegistry", ServiceRegistry.class);
            } else {
                return ref;
            }
        });

        // Iterate over all the Injector implementations published in the registry
        // and concatenate their results
        final StringBuilder sb = new StringBuilder();
        sb.append("<!-- Begin injected -->");
        for (HtmlInjector injector : serviceRegistry.findProviders(HtmlInjector.class)) {
            try {
                final String content = injector.inject(request);
                if (content != null) {
                    sb.append(content);
                }
            } catch (Throwable t) {
                LOG.warn("Injector {} failed.", injector, t);
            }
        }
        sb.append("<!-- End injected -->\n");
        return sb.toString();
    }
}
