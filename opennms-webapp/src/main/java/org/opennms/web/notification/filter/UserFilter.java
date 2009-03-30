package org.opennms.web.notification.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;



/** Encapsulates all user filtering functionality. */
public class UserFilter extends Object implements Filter {
    public static final String TYPE = "user";

    protected String user;

    public UserFilter(String user) {
        this.user = user;
    }

    public String getSql() {
        return (" notifications.notifyid in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid='" + this.user + "')");
    }
    
    public String getParamSql() {
        return (" notifications.notifyid in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)");
    }
    
    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException {
    	ps.setString(parameterIndex, this.user);
    	return 1;
    }

    public String getDescription() {
        return (TYPE + "=" + this.user);
    }

    public String getTextDescription() {
        return this.getDescription();
    }

    public String toString() {
        return ("<NoticeFactory.UserFilter: " + this.getDescription() + ">");
    }

    public String getUser() {
        return (this.user);
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}