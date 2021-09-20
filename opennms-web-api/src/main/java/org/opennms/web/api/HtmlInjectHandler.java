/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
