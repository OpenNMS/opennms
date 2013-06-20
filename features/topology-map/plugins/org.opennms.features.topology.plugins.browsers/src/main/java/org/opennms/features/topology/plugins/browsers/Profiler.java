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
