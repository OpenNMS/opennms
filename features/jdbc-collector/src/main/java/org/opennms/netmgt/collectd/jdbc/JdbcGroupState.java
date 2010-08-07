package org.opennms.netmgt.collectd.jdbc;

import java.util.Date;

public class JdbcGroupState {
    private boolean available = false;
    private Date lastChecked;
    
    public JdbcGroupState(boolean isAvailable) {
        this(isAvailable, new Date());
    }
    
    public JdbcGroupState(boolean isAvailable, Date lastChecked) {
        this.available = isAvailable;
        this.lastChecked = lastChecked;
    }
    
    public boolean isAvailable() {
        return available;
    }

    public Date getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(Date lastChecked) {
        this.lastChecked = lastChecked;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    
}
