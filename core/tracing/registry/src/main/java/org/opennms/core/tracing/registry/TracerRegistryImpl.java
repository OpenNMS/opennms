/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
