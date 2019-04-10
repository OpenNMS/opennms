/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.tracing.registry;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.api.TracerWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/**
 * Implements TracerRegistry and provide onBind, OnUnbind for {@link TracerWrapper} to get registered from osgi registry.
 */
public class TracerRegistryImpl implements TracerRegistry {


    @Autowired(required = false)
    private TracerWrapper tracerWrapper;

    private Tracer tracer;

    private AtomicBoolean registered = new AtomicBoolean(false);

    @Override
    public Tracer getTracer(String serviceName) {
        if (tracerWrapper != null) {
            if(tracer == null) {
                tracer = tracerWrapper.init(serviceName);
                registered.set(true);
            }
            return tracer;
        }
        return GlobalTracer.get();
    }

    public synchronized void onBind(TracerWrapper tracerWrapper, Map properties) {
        this.tracerWrapper = tracerWrapper;
        registered.set(true);
    }

    public synchronized void onUnbind(TracerWrapper tracerWrapper, Map properties) {

    }

    @Override
    public boolean isRegistered() {
        return registered.get();
    }

    public TracerWrapper getTracerWrapper() {
        return tracerWrapper;
    }

    public void setTracerWrapper(TracerWrapper tracerWrapper) {
        this.tracerWrapper = tracerWrapper;
    }

}
