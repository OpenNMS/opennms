package org.opennms.web.notification.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;



/** Encapsulates all interface filtering functionality. */
public class InterfaceFilter extends EqualsFilter<String> {
    public static final String TYPE = "interface";

    public InterfaceFilter(String ipAddress) {
        super(TYPE, SQLType.STRING, "INTERFACEID", "ipAddress", ipAddress);
    }
    
    public String toString() {
        return ("<WebNotificationRepository.InterfaceFilter: " + this.getDescription() + ">");
    }

    public String getIpAddress() {
        return getValue();
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}