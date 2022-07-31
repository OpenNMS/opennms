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

package org.opennms.core.tracing.jaeger;

import org.opennms.core.tracing.api.TracerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;



/**
 * Provides Jaeger Tracer, Configuration options for Jaeger client are through system properties specified in
 *  https://github.com/jaegertracing/jaeger-client-java/blob/master/jaeger-core/README.md
 *
 */
public class JaegerTracerWrapper implements TracerWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(JaegerTracerWrapper.class);

    // XXX need to convert
    //private static final String JAEGER_SAMPLER_PARAM_PROPERTY = "JAEGER_SAMPLER_PARAM";
    //private static final int JAEGER_SAMPLER_PARAM_VALUE = SystemProperties.getInteger(JAEGER_SAMPLER_PARAM_PROPERTY, 1);
    //private static final String JAEGER_SAMPLER_TYPE_PROPERTY = "JAEGER_SAMPLER_TYPE";
    //private static final String JAEGER_SAMPLER_TYPE_VALUE = System.getProperty(JAEGER_SAMPLER_TYPE_PROPERTY, ConstSampler.TYPE);
    
    @Override
    public Tracer init(String serviceName) {
        if (GlobalTracer.isRegistered()) {
            LOG.info("Returning already existing OpenTracing GlobalTracer");
            return GlobalTracer.get();
        }

// XXX need to convert
//        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv()
//                .withType(JAEGER_SAMPLER_TYPE_VALUE)
//                .withParam(JAEGER_SAMPLER_PARAM_VALUE);
//
//        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
//                .withLogSpans(true);
//
//        Configuration config = new Configuration(serviceName)
//                .withSampler(samplerConfig)
//                .withReporter(reporterConfig);
//        GlobalTracer.get();

// XXX need to convert??? or can we continue to use autoconfiguration?
// https://opentelemetry.io/docs/migration/opentracing/
// https://medium.com/jaegertracing/migrating-from-jaeger-client-to-opentelemetry-sdk-bd337d796759
//
//        ManagedChannel jaegerChannel = ManagedChannelBuilder
//            .forAddress("localhost", 14250)
//            .usePlaintext()
//            .build();
//
//        JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
//            .setChannel(jaegerChannel)
//            .setTimeout(1, TimeUnit.SECONDS)
//            .build();
//
// XXX need to convert??? or can we continue to use autoconfiguration?
//        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
//            //.addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
//            .setResource(Resource.getDefault().merge(serviceNameResource))
//            .build();
//
//        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
//            .setPropagators(ContextPropagators.create(
//                TextMapPropagator.composite(
//                    W3CTraceContextPropagator.getInstance(),
//                    JaegerPropagator.getInstance()
//                )
//            ))
//            .setTracerProvider(tracerProvider)
//            .buildAndRegisterGlobal();
//
//
//        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

        System.setProperty("otel.propagators", "tracecontext,baggage,jaeger");

        // This will do auto-configuration if the Java agent isn't used.
        System.setProperty("otel.java.global-autoconfigure.enabled", "true");
        OpenTelemetry otel = GlobalOpenTelemetry.get();

        if (otel == OpenTelemetry.noop()) {
            LOG.info("OpenTelemetry SDK initialized with no-op implementation");
        } else {
            LOG.info("OpenTelemetry SDK initialized");
        }

        // https://github.com/open-telemetry/opentelemetry-java/blob/main/opentracing-shim/README.md
        GlobalTracer.registerIfAbsent(OpenTracingShim.createTracerShim(GlobalOpenTelemetry.get()));

        LOG.info("OpenTracing->OpenTelmetry shim registered");

        return GlobalTracer.get();
    }
}
