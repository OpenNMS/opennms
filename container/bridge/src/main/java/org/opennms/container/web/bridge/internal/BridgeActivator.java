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
package org.opennms.container.web.bridge.internal;

import java.util.EventListener;
import java.util.Hashtable;

import javax.servlet.Filter;

import org.opennms.container.web.felix.base.internal.AbstractActivator;
import org.opennms.container.web.felix.base.internal.EventDispatcher;
import org.opennms.container.web.felix.base.internal.HttpServiceController;
import org.opennms.container.web.felix.base.internal.logger.SystemLogger;
import org.osgi.framework.Constants;

public final class BridgeActivator extends AbstractActivator
{
    private DispatcherFilter dispatcher;
    private EventDispatcher eventDispatcher;
    private HttpServiceController controller;

    protected DispatcherFilter getDispatcherFilter() {
        return this.dispatcher;
    }

    protected EventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    protected HttpServiceController getHttpServiceController() {
        return this.controller;
    }

    @Override
    protected void doStart() throws Exception {
        this.controller = new HttpServiceController(getBundleContext());
        this.dispatcher = new DispatcherFilter(this.controller);
        this.eventDispatcher = new EventDispatcher(this.controller);

        // dispatcher servlet
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put("http.felix.dispatcher", getDispatcherFilter().getClass().getName());
        props.put(Constants.SERVICE_DESCRIPTION, "Dispatcher for bridged request handling");
        props.put(Constants.SERVICE_VENDOR, "The OpenNMS Group, Inc.");
        getBundleContext().registerService(Filter.class, getDispatcherFilter(), props);

        // Http Session event dispatcher
        props = new Hashtable<String, Object>();
        props.put("http.felix.dispatcher", getEventDispatcher().getClass().getName());
        props.put(Constants.SERVICE_DESCRIPTION, "Dispatcher for bridged HttpSession events");
        props.put(Constants.SERVICE_VENDOR, "The OpenNMS Group, Inc.");
        getBundleContext().registerService(EventListener.class, getEventDispatcher(), props);

        SystemLogger.info("Started bridged http service");
    }

    @Override
    protected void doStop() throws Exception {
        this.controller.unregister();
        this.dispatcher.destroy();
    }
}
