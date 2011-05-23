package org.opennms.netmgt.invd.scanners.wmi;

import java.util.Date;

public class WmiAssetState {
    private boolean available = false;
    private Date lastChecked;

    public WmiAssetState(boolean isAvailable) {
        this(isAvailable, new Date());
    }

    public WmiAssetState(boolean isAvailable, Date lastChecked) {
        this.available = isAvailable;
        this.lastChecked = lastChecked;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Date getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(Date lastChecked) {
        this.lastChecked = lastChecked;
    }
}
