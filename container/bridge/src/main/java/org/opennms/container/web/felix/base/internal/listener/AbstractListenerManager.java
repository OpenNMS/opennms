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

import java.util.ArrayList;
import java.util.Iterator;


import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class AbstractListenerManager<T> extends ServiceTracker<T, T> {

    private ArrayList<T> allContextListeners;

    private final Object lock;

    protected AbstractListenerManager(BundleContext context, Class<T> clazz)
    {
        super(context, clazz, null);
        lock = new Object();
    }

    protected final Iterator<T> getContextListeners()
    {
        ArrayList<T> result = allContextListeners;
        if (result == null)
        {
            synchronized (lock)
            {
                if (allContextListeners == null)
                {
                    @SuppressWarnings("unchecked") // Because of OSGi API
                    T[] services = (T[])getServices();
                    if (services != null && services.length > 0)
                    {
                        result = new ArrayList<T>(services.length);
                        for (T service : services)
                        {
                            result.add(service);
                        }
                    }
                    else
                    {
                        result = new ArrayList<T>(0);
                    }
                    this.allContextListeners = result;
                }
                else
                {
                    result = this.allContextListeners;
                }
            }
        }
        return result.iterator();
    }

    @Override
    public T addingService(ServiceReference<T> reference)
    {
        synchronized (lock)
        {
            allContextListeners = null;
        }

        return super.addingService(reference);
    }

    @Override
    public void modifiedService(ServiceReference<T> reference, T service)
    {
        synchronized (lock)
        {
            allContextListeners = null;
        }

        super.modifiedService(reference, service);
    }

    @Override
    public void removedService(ServiceReference<T> reference, T service)
    {
        synchronized (lock)
        {
            allContextListeners = null;
        }

        super.removedService(reference, service);
    }
}
