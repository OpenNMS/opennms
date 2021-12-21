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

package org.opennms.core.tracing.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import org.opennms.core.xml.XmlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.propagation.TextMap;

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
        throw new UnsupportedOperationException();
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
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(tracingMetadata));
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
