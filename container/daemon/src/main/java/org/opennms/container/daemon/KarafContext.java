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
package org.opennms.container.daemon;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.BundleContext;

/**
 * Makes the embedded Karaf framework context available to other components in
 * the OpenNMS JVM without transferring lifecycle ownership to those components.
 */
public final class KarafContext {
    private static final AtomicReference<BundleContext> CONTEXT = new AtomicReference<>();

    private KarafContext() {
    }

    public static BundleContext getBundleContext() {
        final BundleContext context = CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("The OpenNMS Karaf service is not running");
        }
        return context;
    }

    public static Optional<BundleContext> getBundleContextIfAvailable() {
        return Optional.ofNullable(CONTEXT.get());
    }

    static void publish(final BundleContext context) {
        if (!CONTEXT.compareAndSet(null, Objects.requireNonNull(context))) {
            throw new IllegalStateException("An OpenNMS Karaf context is already published");
        }
    }

    static void clear(final BundleContext context) {
        CONTEXT.compareAndSet(context, null);
    }
}
