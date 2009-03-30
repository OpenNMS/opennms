package org.opennms.web.notification.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;



/** Encapsulates all interface filtering functionality. */
public class InterfaceFilter extends Object implements Filter {
    public static final String TYPE = "interface";

    protected String ipAddress;

    public InterfaceFilter(String ipAddress) {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.ipAddress = ipAddress;
    }

    public String getSql() {
        return (" INTERFACEID='" + this.ipAddress + "'");
    }
    
    public String getParamSql() {
        return (" INTERFACEID=?");
    }
    
    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException {
    	ps.setString(parameterIndex, this.ipAddress);
    	return 1;
    }

    public String getDescription() {
        return (TYPE + "=" + this.ipAddress);
    }

    public String getTextDescription() {
        return this.getDescription();
    }

    public String toString() {
        return ("<NoticeFactory.InterfaceFilter: " + this.getDescription() + ">");
    }

    public String getIpAddress() {
        return (this.ipAddress);
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}