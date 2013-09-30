package org.opennms.features.topology.api;

public interface AutoRefreshSupport {
    boolean isEnabled();

    void setEnabled(boolean value);

    /**
     * Toggles isEnabled and returns the new value.
     * @return the value you would get from {@link #isEnabled()}.
     */
    boolean toggle();

    /**
     * Sets the interval in seconds you have to wait until the next refresh is invoked.
     * @param secondsToWait the delay until the next refresh is performed (in seconds).
     */
    void setInterval(long secondsToWait);

    long getInterval();

}
