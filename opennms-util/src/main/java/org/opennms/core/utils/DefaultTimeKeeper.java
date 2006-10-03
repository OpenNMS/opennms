package org.opennms.core.utils;

import java.util.Date;

public class DefaultTimeKeeper implements TimeKeeper {

    public Date getCurrentDate() {
        return new Date();
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

}
