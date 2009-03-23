package org.opennms.web.event.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventIdListFilter implements Filter {
    
    private int[] m_eventIds;
    
    public EventIdListFilter(int[] eventIds){
        m_eventIds = eventIds;
    }
    
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        for(int i = 0; i < m_eventIds.length; i++) {
            ps.setInt(parameterIndex+i, m_eventIds[i]);
        }
        return m_eventIds.length;
    }

    public String getDescription() {
        StringBuilder buf = new StringBuilder("eventId in ");
        appendIdList(buf);
        return buf.toString();
    }

    public String getParamSql() {
        StringBuilder buf = new StringBuilder(m_eventIds.length*3 + 20);
        
        buf.append(" EVENTS.EVENTID IN ");
        
        buf.append('(');
        for(int i = 0; i < m_eventIds.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append('?');
        }
        
        buf.append(')');
        
        return buf.toString();
    }

    public String getSql() {
        StringBuilder buf = new StringBuilder(m_eventIds.length*5 + 20);
        
        buf.append(" EVENTS.EVENTID IN ");
        
        appendIdList(buf);
        
        return buf.toString();
    }

    public String getTextDescription() {
        return getDescription();
    }
    
    private void appendIdList(StringBuilder buf) {
        buf.append("(");
        for(int i = 0; i < m_eventIds.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(m_eventIds[i]);
        }
        
        buf.append(")");
    }

}
