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
package org.opennms.container.web.bridge.rest;

import java.util.concurrent.Callable;

import org.apache.cxf.Bus;

/**
 * Runs code with the class loader of CXF as the thread context class loader.
 * CXF and the JAX-RS API use the context class loader to find their extensions.
 */
final class CxfClassLoader {

    private CxfClassLoader() {
    }

    static <T> T call(final Callable<T> callable) throws Exception {
        final Thread thread = Thread.currentThread();
        final ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(Bus.class.getClassLoader());
        try {
            return callable.call();
        } finally {
            thread.setContextClassLoader(previous);
        }
    }

    static void run(final Runnable runnable) {
        final Thread thread = Thread.currentThread();
        final ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(Bus.class.getClassLoader());
        try {
            runnable.run();
        } finally {
            thread.setContextClassLoader(previous);
        }
    }
}
