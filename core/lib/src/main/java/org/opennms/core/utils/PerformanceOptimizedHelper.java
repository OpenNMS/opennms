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

package org.opennms.core.utils;


/**
 * class can be deleted once we are happy with the performance improvements
 * DO NOT MERGE!
 */
public class PerformanceOptimizedHelper {
    public static boolean isPerformanceOptimized() {
        return Boolean.getBoolean("topologyOptimized");
    }

    public final static class TimeLogger {
        private final long start;
        private String name;

        public TimeLogger() {
            this("");
        }

        public TimeLogger(String name) {
            this.name = name;
            this.start = System.currentTimeMillis();
        }

        public void logTimeStop() {
            System.out.println(getMessage());
        }

        String getMessage() {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            String message = String.format("TimeLogger %s: call to %s took %s ms. Stack: %s::%s::%s",
                    name,
                    stack[3].getMethodName(),
                    System.currentTimeMillis() - start,
                    print(stack[6]),
                    print(stack[5]),
                    print(stack[4]));
            return message;
        }
        private final static String print(StackTraceElement stackTraceElement){
            return stackTraceElement.getClassName()+"."+stackTraceElement.getMethodName();
        }
    }
}
