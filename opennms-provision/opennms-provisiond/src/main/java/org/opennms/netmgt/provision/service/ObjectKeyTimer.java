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

package org.opennms.netmgt.provision.service;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * It is a timer class which will keep tracking controlled by key of begin & finish.
 */
public class ObjectKeyTimer {
    private final Timer timer;

    // by the nature of provisiond, thread will be shared between scanExecutor & importExecutor which make ThreadLocal not working
    private final Map<Object, Context> contextMap = new HashMap<>();

    /**
     * <p>Constructor for ThreadTimer.</p>
     *
     * @param timer a {@link Timer} object.
     */
    public ObjectKeyTimer(Timer timer) {
        this.timer = Objects.requireNonNull(timer);
    }

    /**
     * <p>begin</p>
     */
    public void begin(Object key) {
        Objects.requireNonNull(key);
        synchronized (contextMap) {
            contextMap.put(key, timer.time());
        }
    }

    /**
     * <p>end</p>
     */
    public void end(Object key) {
        Objects.requireNonNull(key);
        synchronized (contextMap) {
            Context c = contextMap.remove(key);
            if (c != null) {
                long tmp = c.stop();
            }
        }
    }

    public Timer getTimer() {
        return timer;
    }
}
