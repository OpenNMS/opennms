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
