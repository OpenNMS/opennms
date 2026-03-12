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
package org.opennms.core.daemon.loader;

import org.opennms.core.tracing.api.TracerRegistry;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/**
 * A no-op implementation of {@link TracerRegistry} that returns the
 * {@link io.opentracing.noop.NoopTracer} from {@link GlobalTracer}.
 * Used in standalone daemon containers where distributed tracing is not configured.
 */
public class NoOpTracerRegistry implements TracerRegistry {

    @Override
    public Tracer getTracer() {
        return GlobalTracer.get();
    }

    @Override
    public void init(String serviceName) {
        // No-op: no tracer to initialize
    }
}
