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
package org.opennms.core.tracing.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.XmlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;

/**
 * Distribute tracing needs span contexts to be transmitted between processes.
 * This bean is a DTO that implements {@link TextMap}
 */
@XmlRootElement(name = "tracing-info")
@XmlAccessorType(XmlAccessType.FIELD)
public class TracingInfoCarrier implements TextMap {

    private static final Logger LOG = LoggerFactory.getLogger(TracingInfoCarrier.class);

    private Map<String, String> tracingInfoMap = new HashMap<>();

    public TracingInfoCarrier(Map<String, String> tracingInfo) {
        this.tracingInfoMap = tracingInfo;
    }

    public TracingInfoCarrier() {
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return tracingInfoMap.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        tracingInfoMap.put(key, value);
    }

    public Map<String, String> getTracingInfoMap() {
        return tracingInfoMap;
    }

    public void setTracingInfoMap(Map<String, String> tracingInfoMap) {
        this.tracingInfoMap = tracingInfoMap;
    }

    // Util method to marshal tracingInfo map to a String.
    public static String marshalTracingInfo(Map<String, String> tracingInfo) {
        try {
            TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier(tracingInfo);
            return TracingInfoMarshaller.marshalRequest(tracingInfoCarrier);
        } catch (Exception e) {
            // Tracing is not core functionality, just log exception.
            LOG.warn("Exception while marshalling tracing info {}", tracingInfo, e);
            return null;
        }
    }

    // Util method to unmarshal tracingInfo into a Map of strings.
    public static Map<String, String> unmarshalTracinginfo(String tracingInfo) {
        TracingInfoCarrier tracingInfoCarrier;
        try {
            tracingInfoCarrier = TracingInfoMarshaller.unmarshalRequest(tracingInfo);
            return tracingInfoCarrier.getTracingInfoMap();
        } catch (Exception e) {
            // Tracing is not core functionality, just log exception.
            LOG.warn("Exception while unmarshalling tracing info {}", tracingInfo, e);
            return new HashMap<>();
        }

    }

    public static void updateTracingMetadata(Tracer tracer, Span span, BiConsumer<String, String> tracingInfoConsumer) {
        TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();
        tracer.inject(span.context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
        tracingInfoCarrier.getTracingInfoMap().forEach(tracingInfoConsumer);
    }

    public static Tracer.SpanBuilder buildSpanFromTracingMetadata(Tracer tracer,
                                                                  String tracingOperationKey,
                                                                  Map<String, String> tracingMetadata,
                                                                  String reference) {
        // Extract base tracer context from TracingMetadata
        Tracer.SpanBuilder spanBuilder;
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(tracingMetadata));
        if (context != null &&
                (References.CHILD_OF.equals(reference) || References.FOLLOWS_FROM.equals(reference))) {
            spanBuilder = tracer.buildSpan(tracingOperationKey).addReference(reference, context);
        } else {
            spanBuilder = tracer.buildSpan(tracingOperationKey);
        }
        return spanBuilder;
    }

    /**
     * Util Class to marshal TracingInfo into xml to be transmitted over the wire.
     */
    private static class TracingInfoMarshaller {

        private static final ThreadLocal<XmlHandler<TracingInfoCarrier>> xmlHandlerThreadLocal = new ThreadLocal<>();

        public static String marshalRequest(TracingInfoCarrier tracingInfo) {
            return createXmlHandler().marshal(tracingInfo);
        }

        public static TracingInfoCarrier unmarshalRequest(String tracingInfo) {
            return createXmlHandler().unmarshal(tracingInfo);
        }

        private static XmlHandler<TracingInfoCarrier> createXmlHandler() {
            XmlHandler<TracingInfoCarrier> xmlHandler = xmlHandlerThreadLocal.get();
            if (xmlHandler == null) {
                xmlHandler = new XmlHandler<>(TracingInfoCarrier.class);
                xmlHandlerThreadLocal.set(xmlHandler);
            }
            return xmlHandler;
        }
    }
}
