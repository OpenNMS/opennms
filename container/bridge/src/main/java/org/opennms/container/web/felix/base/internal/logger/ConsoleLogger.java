/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.opennms.container.web.felix.base.internal.logger;

import org.osgi.framework.ServiceReference;
import java.io.PrintStream;

public final class ConsoleLogger
    extends AbstractLogger
{
    private final PrintStream out;

    public ConsoleLogger()
    {
        this(System.out);
    }

    public ConsoleLogger(PrintStream out)
    {
        this.out = out;
    }

    @Override
    @SuppressWarnings("unchecked") // Because of OSGi API
    public void log(ServiceReference ref, int level, String message, Throwable cause)
    {
        StringBuffer str = new StringBuffer();
        switch (level) {
            case LOG_DEBUG:
                str.append("[DEBUG] ");
                break;
            case LOG_INFO:
                str.append("[INFO] ");
                break;
            case LOG_WARNING:
                str.append("[WARNING] ");
                break;
            case LOG_ERROR:
                str.append("[ERROR] ");
                break;
        }

        if (ref != null) {
            str.append("(").append(ref.toString()).append(") ");
        }

        str.append(message);
        this.out.println(str.toString());
        if (cause != null) {
            cause.printStackTrace(this.out);
        }
    }
}
