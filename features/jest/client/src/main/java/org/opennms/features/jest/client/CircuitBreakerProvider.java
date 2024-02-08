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
package org.opennms.features.jest.client;

import java.time.Duration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public class CircuitBreakerProvider {

    private Float failureRateThreshold = null;
    private Long waitDurationInOpenStateInMills = null;
    private Integer ringBufferSizeInHalfOpenState = null;
    private Integer ringBufferSizeInClosedState = null;
    private Class<? extends Throwable>[] recordExceptions = null;
    private Class<? extends Throwable> ignoreExceptions = null;

    public CircuitBreaker getCircuitBreaker(String name) {
        final CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom();
        if (failureRateThreshold != null) {
            builder.failureRateThreshold(failureRateThreshold);
        }
        if (waitDurationInOpenStateInMills != null) {
            builder.waitDurationInOpenState(Duration.ofMillis(waitDurationInOpenStateInMills));
        }
        if (ringBufferSizeInHalfOpenState != null) {
            builder.ringBufferSizeInHalfOpenState(ringBufferSizeInHalfOpenState);
        }
        if (ringBufferSizeInClosedState != null) {
            builder.ringBufferSizeInClosedState(ringBufferSizeInClosedState);
        }
        if (recordExceptions != null) {
            builder.recordExceptions(recordExceptions);
        }
        if (ignoreExceptions != null) {
            builder.ignoreExceptions(ignoreExceptions);
        }
        return CircuitBreakerRegistry.of(builder.build()).circuitBreaker(name);
    }

    public void setFailureRateThreshold(Float failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public void setWaitDurationInOpenStateInMills(Long waitDurationInOpenState) {
        this.waitDurationInOpenStateInMills = waitDurationInOpenState;
    }

    public void setRingBufferSizeInHalfOpenState(Integer ringBufferSizeInHalfOpenState) {
        this.ringBufferSizeInHalfOpenState = ringBufferSizeInHalfOpenState;
    }

    public void setRingBufferSizeInClosedState(Integer ringBufferSizeInClosedState) {
        this.ringBufferSizeInClosedState = ringBufferSizeInClosedState;
    }

    public void setRecordExceptions(Class<? extends Throwable>[] recordExceptions) {
        this.recordExceptions = recordExceptions;
    }

    public void setIgnoreExceptions(Class<? extends Throwable> ignoreExceptions) {
        this.ignoreExceptions = ignoreExceptions;
    }
}
