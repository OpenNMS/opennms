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

import org.osgi.service.log.LogService;
import org.osgi.framework.ServiceReference;

public abstract class AbstractLogger
    implements LogService
{
    @Override
    public final void log(int level, String message)
    {
        log(null, level, message, null);
    }

    @Override
    public final void log(int level, String message, Throwable cause)
    {
        log(null, level, message, cause);
    }

    @Override
    @SuppressWarnings("unchecked") // Due to the OSGi API
    public final void log(ServiceReference ref, int level, String message)
    {
        log(ref, level, message, null);
    }
}
