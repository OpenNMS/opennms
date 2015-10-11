package org.opennms.netmgt.alarmd.api;

import java.io.Serializable;

public interface Destination extends Serializable{
    public String getName();
    public boolean isFirstOccurrenceOnly();
}
