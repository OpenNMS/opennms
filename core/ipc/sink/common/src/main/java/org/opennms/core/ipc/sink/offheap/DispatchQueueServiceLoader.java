/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.offheap;

import java.util.Optional;

import org.opennms.core.ipc.sink.api.DispatchQueueFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public class DispatchQueueServiceLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DispatchQueueServiceLoader.class);
    private static BundleContext context;
    private static volatile DispatchQueueFactory dispatchQueueFactory;

    public BundleContext getBundleContext() {
        return context;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        context = bundleContext;
    }

    public static Optional<DispatchQueueFactory> getDispatchQueueFactory() {
        if (dispatchQueueFactory != null) {
            return Optional.of(dispatchQueueFactory);
        }

        if (context != null) {
            try {
                ServiceReference<DispatchQueueFactory> serviceReference = context.getServiceReference(DispatchQueueFactory.class);
                return serviceReference == null ? Optional.empty() : Optional.of(context.getService(serviceReference));
            } catch (Exception e) {
                LOG.error("Exception while retrieving DispatchQueueFactory Service from registry", e);
                throw e;
            }
        }

        return Optional.empty();
    }

    @VisibleForTesting
    public static void setDispatchQueue(DispatchQueueFactory factory) {
        dispatchQueueFactory = factory;
    }

}
