/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.log4j;

/*
 * Parts Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Plugin(
        name = OpenTelemetryAppender.PLUGIN_NAME,
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public class OpenTelemetryAppender extends AbstractAppender {

    static final String PLUGIN_NAME = "OpenTelemetry";

//    private static final LogEmitterProviderHolder logEmitterProviderHolder =
//            new LogEmitterProviderHolder();

//    private final LogEventMapper<ReadOnlyStringMap> mapper;

    @PluginBuilderFactory
    public static <B extends Builder<B>> B builder() {
        return new Builder<B>().asBuilder();
    }

    static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<OpenTelemetryAppender> {

        @PluginBuilderAttribute private boolean captureExperimentalAttributes;
        @PluginBuilderAttribute private boolean captureMapMessageAttributes;
        @PluginBuilderAttribute private String captureContextDataAttributes;

        /**
         * Sets whether experimental attributes should be set to logs. These attributes may be changed
         * or removed in the future, so only enable this if you know you do not require attributes
         * filled by this instrumentation to be stable across versions.
         */
        public B setCaptureExperimentalAttributes(boolean captureExperimentalAttributes) {
            this.captureExperimentalAttributes = captureExperimentalAttributes;
            return asBuilder();
        }

        /** Sets whether log4j {@link MapMessage} attributes should be copied to logs. */
        public B setCaptureMapMessageAttributes(boolean captureMapMessageAttributes) {
            this.captureMapMessageAttributes = captureMapMessageAttributes;
            return asBuilder();
        }

        /** Configures the {@link ThreadContext} attributes that will be copied to logs. */
        public B setCaptureContextDataAttributes(String captureContextDataAttributes) {
            this.captureContextDataAttributes = captureContextDataAttributes;
            return asBuilder();
        }

        @Override
        public OpenTelemetryAppender build() {
            return new OpenTelemetryAppender(
                    getName(),
                    getLayout(),
                    getFilter(),
                    isIgnoreExceptions(),
                    getPropertyArray(),
                    captureExperimentalAttributes,
                    captureMapMessageAttributes,
                    captureContextDataAttributes);
        }
    }

    private OpenTelemetryAppender(
            String name,
            Layout<? extends Serializable> layout,
            Filter filter,
            boolean ignoreExceptions,
            Property[] properties,
            boolean captureExperimentalAttributes,
            boolean captureMapMessageAttributes,
            String captureContextDataAttributes) {

        super(name, filter, layout, ignoreExceptions, properties);
        /*
        this.mapper =
                new LogEventMapper<>(
                        ContextDataAccessorImpl.INSTANCE,
                        captureExperimentalAttributes,
                        captureMapMessageAttributes,
                        splitAndFilterBlanksAndNulls(captureContextDataAttributes));
         */
    }

    private static List<String> splitAndFilterBlanksAndNulls(String value) {
        if (value == null) {
            return emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public void append(LogEvent event) {
        String instrumentationName = event.getLoggerName();
        if (instrumentationName == null || instrumentationName.isEmpty()) {
            instrumentationName = "ROOT";
        }
        Span span = Span.current();
        if (!span.isRecording()) {
            return;
        }

        AttributesBuilder attributes = Attributes.builder();

        captureMessage(attributes, event.getMessage());

        if (event.getLevel() != null) {
            //builder.setSeverity(levelToSeverity(level));
            attributes.put("severity", event.getLevel().name());
        }

        if (event.getThrown() != null) {
            setThrowable(attributes, event.getThrown());
        }

        //captureContextDataAttributes(attributes, contextData);
        // visible for testing
        ReadOnlyStringMap contextData = event.getContextData();
        for (Map.Entry<String, String> entry : contextData.toMap().entrySet()) {
            String attributeKey;
            if (entry.getKey() == "span_id" ||
                    entry.getKey() == "trace_flags" ||
                    entry.getKey() == "trace_id") {
                continue;
            } else if (entry.getKey() == "prefix" ||
                    entry.getKey() == "nodeId" ||
                    entry.getKey() == "nodeLabel" ||
                    entry.getKey() == "ipAddress" ||
                    entry.getKey() == "service") {
                attributeKey = entry.getKey(); // don't add "log4j.context_data." on the front
            } else {
                attributeKey = "log4j.context_data." + entry.getKey();
            }
            if (entry.getValue() != null) {
                attributes.put(attributeKey, entry.getValue());
            }
        }

        //if (captureExperimentalAttributes) {
        if (true) {
            Thread currentThread = Thread.currentThread();
            attributes.put(SemanticAttributes.THREAD_NAME, currentThread.getName());
            attributes.put(SemanticAttributes.THREAD_ID, currentThread.getId());
        }

        attributes.put("source", event.getLoggerName());
        //attributes.put("logger_fqcn", event.getLoggerFqcn());

        span.addEvent("log", attributes.build(), event.getTimeMillis(), TimeUnit.MILLISECONDS);

        /*
        LogRecordBuilder builder =
                logEmitterProviderHolder.get().logEmitterBuilder(instrumentationName).build().logBuilder();
        ReadOnlyStringMap contextData = event.getContextData();
        mapper.mapLogEvent(
                builder, event.getMessage(), event.getLevel(), event.getThrown(), contextData);

        Instant timestamp = event.getInstant();
        if (timestamp != null) {
            builder.setEpoch(
                    TimeUnit.MILLISECONDS.toNanos(timestamp.getEpochMillisecond())
                            + timestamp.getNanoOfMillisecond(),
                    TimeUnit.NANOSECONDS);
        }
        builder.emit();
         */
    }

    // visible for testing
    void captureMessage(AttributesBuilder attributes, Message message) {
        if (message == null) {
            return;
        }
        if (!(message instanceof MapMessage)) {
            attributes.put("message", message.getFormattedMessage());
            return;
        }

        MapMessage<?, ?> mapMessage = (MapMessage<?, ?>) message;

        String body = mapMessage.getFormat();
        boolean checkSpecialMapMessageAttribute = (body == null || body.isEmpty());
//        if (checkSpecialMapMessageAttribute) {
//            body = mapMessage.get(SPECIAL_MAP_MESSAGE_ATTRIBUTE);
//        }

        if (body != null && !body.isEmpty()) {
            attributes.put("message", body);
        }

//        if (captureMapMessageAttributes) {
//            // TODO (trask) this could be optimized in 2.9 and later by calling MapMessage.forEach()
//            mapMessage
//                    .getData()
//                    .forEach(
//                            (key, value) -> {
//                                if (value != null
//                                        && (!checkSpecialMapMessageAttribute
//                                        || !key.equals(SPECIAL_MAP_MESSAGE_ATTRIBUTE))) {
//                                    attributes.put(
//                                            mapMessageAttributeKeyCache.computeIfAbsent(key, AttributeKey::stringKey),
//                                            value.toString());
//                                }
//                            });
//        }
    }

    private static void setThrowable(AttributesBuilder attributes, Throwable throwable) {
        // TODO (trask) extract method for recording exception into
        // instrumentation-appender-api-internal
        attributes.put(SemanticAttributes.EXCEPTION_TYPE, throwable.getClass().getName());
        attributes.put(SemanticAttributes.EXCEPTION_MESSAGE, throwable.getMessage());
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        attributes.put(SemanticAttributes.EXCEPTION_STACKTRACE, writer.toString());
    }

    /**
     * This should be called once as early as possible in your application initialization logic, often
     * in a {@code static} block in your main class. It should only be called once - an attempt to
     * call it a second time will result in an error. If trying to set the {@link
     * SdkLogEmitterProvider} multiple times in tests, use {@link
     * OpenTelemetryAppender#resetSdkLogEmitterProviderForTest()} between them.
     */
    /*
    public static void setSdkLogEmitterProvider(SdkLogEmitterProvider sdkLogEmitterProvider) {
        logEmitterProviderHolder.set(DelegatingLogEmitterProvider.from(sdkLogEmitterProvider));
    }
*/

    /**
     * Unsets the global {@link LogEmitterProvider}. This is only meant to be used from tests which
     * need to reconfigure {@link LogEmitterProvider}.
     */
    /*
    public static void resetSdkLogEmitterProviderForTest() {
        logEmitterProviderHolder.resetForTest();
    }
     */

        /*
    private enum ContextDataAccessorImpl implements ContextDataAccessor<ReadOnlyStringMap> {
        INSTANCE;

        @Override
        @Nullable
        public Object getValue(ReadOnlyStringMap contextData, String key) {
            return contextData.getValue(key);
        }

        @Override
        public void forEach(ReadOnlyStringMap contextData, BiConsumer<String, Object> action) {
            contextData.forEach(action::accept);
        }
    }
         */
}
