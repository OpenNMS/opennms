/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.profiler;

import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.opennms.core.logging.Logging;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

@Aspect
public class ProfilerAspect {

    public interface Block<T> {
        T execute() throws Exception;
    }

    @Around("@within(profile) || @annotation(profile)")
    public Object logAroundByMethod(ProceedingJoinPoint joinPoint, Profile profile) throws Throwable {
        Timer timer = new Timer();
        try {
            timer.start();
            return joinPoint.proceed();
        } finally {
            log(joinPoint.getKind(), joinPoint.getSignature().toShortString(), timer.stop());
        }
    }

    // if no AOP is available, whatever the reason or manually profiling is preferred
    public static <T> T wrapProfile(Class clazz, String signature, Block<T> block) {
        return wrapProfile(clazz.getSimpleName() + "." + signature, block);
    }

    private static <T> T wrapProfile(String signature, Block<T> block) {
        Objects.requireNonNull(signature);
        Objects.requireNonNull(block);
        Timer timer = new Timer();
        try {
            timer.start();
            return block.execute();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            log("method-execution", signature, timer.stop());
        }
    }

    public static void log(String kind, String signature, long diff) {
        Logging.withPrefix("profiler", () -> LoggerFactory.getLogger(ProfilerAspect.class).info("{} {} took {}, raw = {}ms", signature, kind, humanReadable(diff), diff));
    }

    // input in ms
    public static String humanReadable(final long milliseconds) {
        int seconds = (int)(milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

        if (hours > 0) {
            long ms = milliseconds - hours * 1000 * 60 * 60 - minutes * 1000 * 60 - seconds * 1000;
            return String.format("%dh %dm %ds %dms", hours, minutes, seconds, ms);
        }
        if (minutes > 0) {
            long ms = milliseconds - minutes * 1000 * 60 - seconds * 1000;
            return String.format("%dm %ds %dms", minutes, seconds, ms);
        }
        if (seconds > 0) {
            long ms = milliseconds - seconds * 1000;
            return String.format("%ds %dms", seconds, ms);
        }
        return String.format("%dms", milliseconds);
    }
}

