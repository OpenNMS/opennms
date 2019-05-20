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

import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.api.TracerWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/**
 * Implements TracerRegistry and provides onBind, OnUnbind for {@link TracerWrapper} to get registered from osgi registry.
 */
public class TracerRegistryImpl implements TracerRegistry {

    @Autowired(required = false)
    private TracerWrapper tracerWrapper;

    private Tracer tracer;

    private String serviceName;

    @Override
    public Tracer getTracer() {
        if (tracerWrapper != null && serviceName != null) {
            if(tracer == null) {
                tracer = tracerWrapper.init(serviceName);
            }
            return tracer;
        }
        // Returns a NoopTracer.
        return GlobalTracer.get();
    }

    public synchronized void onBind(TracerWrapper tracerWrapper, Map properties) {
        this.tracerWrapper = tracerWrapper;
    }

    public synchronized void onUnbind(TracerWrapper tracerWrapper, Map properties) {

    }


    @Override
    public void init(String serviceName) {
        this.serviceName = serviceName;
    }

    public TracerWrapper getTracerWrapper() {
        return tracerWrapper;
    }

    public void setTracerWrapper(TracerWrapper tracerWrapper) {
        this.tracerWrapper = tracerWrapper;
    }

}
