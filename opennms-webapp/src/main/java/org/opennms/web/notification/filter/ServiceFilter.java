package org.opennms.web.notification.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.notification.NoticeFactory;

/** Encapsulates all service filtering functionality. */
public class ServiceFilter extends Object implements Filter {
    public static final String TYPE = "service";

    protected int serviceId;

    public ServiceFilter(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getSql() {
        return (" SERVICEID=" + this.serviceId);
    }
    
    public String getParamSql() {
        return (" SERVICEID=?");
    }
    
    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException {
    	ps.setInt(parameterIndex, this.serviceId);
    	return 1;
    }

    public String getDescription() {
        return (TYPE + "=" + this.serviceId);
    }

    public String getTextDescription() {
        String serviceName = Integer.toString(this.serviceId);
        try {
            serviceName = NetworkElementFactory.getServiceNameFromId(this.serviceId);
        } catch (SQLException e) {
        }

        return (TYPE + "=" + serviceName);
    }

    public String toString() {
        return ("<NoticeFactory.ServiceFilter: " + this.getDescription() + ">");
    }

    public int getServiceId() {
        return (this.serviceId);
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}