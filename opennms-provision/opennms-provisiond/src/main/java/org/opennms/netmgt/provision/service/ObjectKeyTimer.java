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
