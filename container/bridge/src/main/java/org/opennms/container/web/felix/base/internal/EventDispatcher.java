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
package org.opennms.container.web.felix.base.internal;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * The <code>EventDispatcher</code> dispatches events sent from the servlet
 * container (embedded Jetty or container in which the framework is running
 * in bridged mode) to any {@link HttpSessionAttributeListener} or
 * {@link HttpSessionListener} services.
 */
public class EventDispatcher implements HttpSessionAttributeListener, HttpSessionListener
{

    private final HttpServiceController controller;

    public EventDispatcher(final HttpServiceController controller)
    {
        this.controller = controller;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se)
    {
        controller.getSessionListener().sessionCreated(se);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se)
    {
        controller.getSessionListener().sessionDestroyed(se);
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent se)
    {
        controller.getSessionAttributeListener().attributeAdded(se);
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent se)
    {
        controller.getSessionAttributeListener().attributeRemoved(se);
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent se)
    {
        controller.getSessionAttributeListener().attributeReplaced(se);
    }
}
