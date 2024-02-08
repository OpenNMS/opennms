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
