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

import org.osgi.util.tracker.ServiceTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public final class LogServiceLogger
    extends AbstractLogger
{
    private final ConsoleLogger consoleLogger;
    private final ServiceTracker<?,?> tracker;

    public LogServiceLogger(BundleContext context)
    {
        this.consoleLogger = new ConsoleLogger();
        this.tracker = new ServiceTracker<ServiceReference<LogService>,LogService>(context, LogService.class.getName(), null);
        this.tracker.open();
    }

    public void close()
    {
        this.tracker.close();
    }

    @Override
    @SuppressWarnings("unchecked") // Because of OSGi API
    public void log(ServiceReference ref, int level, String message, Throwable cause)
    {
        LogService log = (LogService)this.tracker.getService();
        if (log != null) {
            log.log(ref, level, message, cause);
        } else {
            this.consoleLogger.log(ref, level, message, cause);
        }
    }
}
