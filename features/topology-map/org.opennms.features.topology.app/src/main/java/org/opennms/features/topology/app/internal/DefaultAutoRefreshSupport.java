package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.AutoRefreshSupport;

public class DefaultAutoRefreshSupport implements AutoRefreshSupport {

    private boolean enabled = false;
    private long interval = 60;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        return isEnabled();
    }

    @Override
    public void setInterval(long secondsToWait) {
        // We do not allow to set < 5 Seconds
        if (secondsToWait < 5) {
            secondsToWait = 5;
        }
        interval = secondsToWait;
    }

    @Override
    public long getInterval() {
        return interval;
    }
}
