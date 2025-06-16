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
package org.opennms.bootstrap;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BootstrapUtils {

    static String getPid() {
        final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        try {
            final Method getPid = runtime.getClass().getMethod("getPid");
            if (getPid != null) {
                final long pid = (long) getPid.invoke(runtime);
                return String.valueOf(pid);
            }
        } catch (final NoSuchMethodException e) {
            // we're on Java 8, fall back to using `RuntimeMXBean#getName`
        } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException|SecurityException e) {
            System.err.println("Unable to determine PID: " + e.getLocalizedMessage());
        }

        final int at = runtime.getName().indexOf("@");
        if (at > 0) {
            return runtime.getName().substring(0, at);
        } else {
            System.err.println("WARNING: unable to determine PID from runtime: " + runtime.getName());
        }

        return "";
    }

}
