/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
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
