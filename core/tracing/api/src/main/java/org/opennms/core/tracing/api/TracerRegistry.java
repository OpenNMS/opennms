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
package org.opennms.core.tracing.api;

import io.opentracing.Tracer;

/**
 * This registry will fetch the tracer from any @{@link TracerWrapper} that was already registered.
 * If no @{@link TracerWrapper} is registered, it will get @{@link io.opentracing.noop.NoopTracer} from
 * {@link io.opentracing.util.GlobalTracer}
 */
public interface TracerRegistry {

    /**
     * {@link #init(String)} should be performed first otherwise it would return NoopTracer.
     * @return Tracer that was registered by TracerWrapper or by default NoopTracer.
     */
    Tracer getTracer();

    /**
     * Initialize tracer registry with servicename.
     * @param serviceName
     */
    void init(String serviceName);
}
