package org.opennms.web.event.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventIdFilter implements Filter {
    public static final String TYPE = "eventId";
    
    int m_eventId;
    
    public EventIdFilter(int eventId){
        m_eventId = eventId;
    }
    
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        ps.setInt(parameterIndex, m_eventId);
        return 1;
    }

    public String getDescription() {
        return (TYPE + "=" + m_eventId);
    }

    public String getParamSql() {
        return " EVENTS.EVENTID=?";
    }

    public String getSql() {
        return " EVENTS.EVENTID="+m_eventId;
    }

    public String getTextDescription() {
        return getDescription();
    }

}
