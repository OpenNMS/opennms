/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
