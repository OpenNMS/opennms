package org.opennms.core.utils;

import java.util.Date;

public interface TimeKeeper {
    
    long getCurrentTime();
    
    Date getCurrentDate();

}
