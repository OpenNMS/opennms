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

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.tracing.api.TracerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.propagation.TextMapCodec;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;

/**
 * Provides Jaeger Tracer, Configuration options for Jaeger client are through system properties specified in
 *  https://github.com/jaegertracing/jaeger-client-java/blob/master/jaeger-core/README.md
 *
 */
public class JaegerTracerWrapper implements TracerWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(JaegerTracerWrapper.class);

    private static final String JAEGER_SAMPLER_PARAM_PROPERTY = "JAEGER_SAMPLER_PARAM";
    private static final int JAEGER_SAMPLER_PARAM_VALUE = SystemProperties.getInteger(JAEGER_SAMPLER_PARAM_PROPERTY, 1);
    private static final String JAEGER_SAMPLER_TYPE_PROPERTY = "JAEGER_SAMPLER_TYPE";
    private static final String JAEGER_SAMPLER_TYPE_VALUE = System.getProperty(JAEGER_SAMPLER_TYPE_PROPERTY, ConstSampler.TYPE);
    
    @Override
    public Tracer init(String serviceName) {
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv()
                .withType(JAEGER_SAMPLER_TYPE_VALUE)
                .withParam(JAEGER_SAMPLER_PARAM_VALUE);

        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
                .withLogSpans(true);

        Configuration config = new Configuration(serviceName)
                .withSampler(samplerConfig)
                .withReporter(reporterConfig);
        TextMapCodec textMapCodec = new TextMapCodec.Builder().build();
        JaegerTracer tracer = config.getTracerBuilder()
                .registerInjector(Format.Builtin.TEXT_MAP, textMapCodec)
                .registerExtractor(Format.Builtin.TEXT_MAP, textMapCodec)
                .build();
        GlobalTracer.registerIfAbsent(tracer);
        LOG.info("Jaeger tracer initialized with serviceName = {}", serviceName);
        return tracer;
    }
}
