package org.opennms.web.notification.filter;

import java.sql.SQLException;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

/** Encapsulates all service filtering functionality. */
public class ServiceFilter extends EqualsFilter<Integer> {
    public static final String TYPE = "service";

    public ServiceFilter(int serviceId) {
        super(TYPE, SQLType.INT, "SERVICEID", "serviceType.id", serviceId);
    }
    
    public String getTextDescription() {
        String serviceName = Integer.toString(getServiceId());
        try {
            serviceName = NetworkElementFactory.getServiceNameFromId(getServiceId());
        } catch (SQLException e) {
        }

        return (TYPE + "=" + serviceName);
    }

    public String toString() {
        return ("<NotificationFactory.ServiceFilter: " + this.getDescription() + ">");
    }

    public int getServiceId() {
        return getValue();
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}