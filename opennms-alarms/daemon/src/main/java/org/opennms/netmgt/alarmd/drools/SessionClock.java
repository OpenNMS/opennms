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

package org.opennms.netmgt.alarmd.drools;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This clock is used to provide the current time as fact within the Drools engine.
 *
 * We use this as opposed to using STREAM mode since STREAM mode does not have
 * deterministic behavior when given out-of-order @events.
 *
 */
public class SessionClock implements org.kie.api.time.SessionPseudoClock {
    /**
     * Used to parse strings like: 30s, 12h, 1d, etc...
     */
    private static final Pattern TIME_PERIOD_PATTERN  = Pattern.compile("\\s*(\\d+)([smhd])\\s*");

    /**
     * Used to cache the conversion of the time period string to milliseconds
     *
     * We're expecting that rules only use a few (<10) different periods, so we can easily
     * cache these instead of having to parse them everytime.
     */
    private LoadingCache<String, Long> timePeriodToMsCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(
                    new CacheLoader<String, Long>() {
                        public Long load(String timePeriod) {
                            return getMsFromTimePeriod(timePeriod);
                        }
                    });

    /**
     * The session to which the clock should be bound
     */
    private final KieSession session;

    /**
     * Current time represented by the clock
     */
    private long now;

    /**
     * Reference to the fact withing the session
     */
    private FactHandle handle;

    public SessionClock(KieSession session, long now) {
        this.session = Objects.requireNonNull(session);
        this.now = now;
        handle = session.insert(this);
    }

    @Override
    public long getCurrentTime() {
        return now;
    }

    @Override
    public long advanceTime(long amount, TimeUnit unit) {
        now += unit.toMillis(amount);
        afterTimeChange();
        return now;
    }

    public void advanceTimeToNow() {
        now = System.currentTimeMillis();
        afterTimeChange();
    }

    public Date now() {
        return new Date(now);
    }

    public Date ago(String timePeriod) {
        try {
            return ago(timePeriodToMsCache.get(timePeriod), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Date ago(long amount, TimeUnit unit) {
        return new Date(now - unit.toMillis(amount));
    }

    private void afterTimeChange() {
        session.update(handle, this);
    }

    public static long getMsFromTimePeriod(String timePeriod) {
        if (timePeriod == null) {
            return 0;
        }
        final Matcher m = TIME_PERIOD_PATTERN.matcher(timePeriod);
        if (!m.matches()) {
            return 0;
        }
        final long amount = Long.parseLong(m.group(1));
        final String unit = m.group(2);
        switch(unit) {
            case "s":
                return TimeUnit.SECONDS.toMillis(amount);
            case "m":
                return TimeUnit.MINUTES.toMillis(amount);
            case "h":
                return TimeUnit.HOURS.toMillis(amount);
            case "d":
                return TimeUnit.DAYS.toMillis(amount);
            default:
                return 0L;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SessionClock.class)
                .add("now", now)
                .toString();
    }
}
