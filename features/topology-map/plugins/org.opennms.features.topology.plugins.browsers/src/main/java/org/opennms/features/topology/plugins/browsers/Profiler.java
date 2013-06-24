/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.topology.plugins.browsers;

import java.util.HashMap;
import java.util.Map;

public class Profiler {

    public static class Timer {
        long startTime;
        long endTime;
        int count;
        int sum;

        synchronized public void start() {
            if (!isStarted()) startTime = System.currentTimeMillis();
            count++;
        }

        synchronized public void stop() {
            endTime = System.currentTimeMillis();
            sum += (endTime -startTime);
            startTime = 0;
            endTime = 0;
        }

        synchronized public boolean isStarted() {
            return startTime > 0;
        }

        synchronized public long getSum() {
            return sum;
        }

        synchronized public int getCount() {
            return count;
        }

        synchronized public double getAVG() {
            return ((double)getSum()) / ((double)count);    // ms
        }
    }

    protected final Map<String, Timer> timerMap = new HashMap<String, Timer>();

    public void start(final String key) {
        if (timerMap.get(key) == null) timerMap.put(key, new Timer());
        timerMap.get(key).start();
    }

    public void stop(final String key) {
        timerMap.get(key).stop();
    }

    @Override
    public String toString() {
        final String HEADER = "%-60s%10s%20s%20s\n";
        final String ROW = "%-60s%10d%20.2f%20.2f\n";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(HEADER, "key", "count", "avg (ms)", "sum (sec)"));
        for (String eachKey : timerMap.keySet()) {
            sb.append(
                String.format(
                        ROW,
                        eachKey,
                        timerMap.get(eachKey).getCount(),
                        timerMap.get(eachKey).getAVG(),
                        toSeconds(timerMap.get(eachKey).getSum())));
        }
        return sb.toString();
    }

    // sum is ms
    private double toSeconds(double sum) {
        return sum / 1000.0;
    }
}
