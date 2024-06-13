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
package org.opennms.core.tracing.jaeger;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.opennms.core.tracing.util.TracingInfoCarrier;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

public class JaegerTracerWrapperTest {
    @Test
    public void testInit() {
        JaegerTracerWrapper wrapper = new JaegerTracerWrapper();
        wrapper.init("testing");
    }

    @Test
    public void testTracingInfoCarrier() {
        JaegerTracerWrapper wrapper = new JaegerTracerWrapper();
        Tracer tracer = wrapper.init("testing");
        Span span = tracer.buildSpan("inject test").start();

        TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();

        tracer.inject(span.context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
        SpanContext newContext = tracer.extract(Format.Builtin.TEXT_MAP, tracingInfoCarrier);

        if (!span.context().toTraceId().equals(newContext.toTraceId())) {
            System.err.println("Initial trace ID: " + span.context().toTraceId());
            System.err.println("TracingInfoCarrier map after injection:");
            tracingInfoCarrier.getTracingInfoMap().forEach((key, value) -> System.err.println(key + " -> " + value));
            System.err.println("\nExtracted trace ID: " + newContext.toTraceId());

            fail("Before and after span contexts don't match. See above.");
        }
    }
}
