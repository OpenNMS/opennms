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
package org.opennms.container.web.felix.base.internal.listener;

import java.util.Iterator;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.osgi.framework.BundleContext;

/**
 * The <code>ProxyListener</code> implements the Servlet API 2.4 listener
 * interfaces forwarding any event calls to registered OSGi services
 * implementing the respective Servlet API 2.4 listener interface.
 */
public class ServletRequestListenerManager extends AbstractListenerManager<ServletRequestListener>
{

    public ServletRequestListenerManager(BundleContext context)
    {
        super(context, ServletRequestListener.class);
    }

    public void requestDestroyed(final ServletRequestEvent sre)
    {
        final Iterator<ServletRequestListener> listeners = getContextListeners();
        while (listeners.hasNext())
        {
            listeners.next().requestDestroyed(sre);
        }
    }

    public void requestInitialized(final ServletRequestEvent sre)
    {
        final Iterator<ServletRequestListener> listeners = getContextListeners();
        while (listeners.hasNext())
        {
            listeners.next().requestInitialized(sre);
        }
    }

}
