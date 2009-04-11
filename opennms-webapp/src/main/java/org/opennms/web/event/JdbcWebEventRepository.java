/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.opennms.web.event.EventFactory.AcknowledgeType;
import org.opennms.web.event.EventFactory.SortStyle;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.event.filter.EventIdListFilter;
import org.opennms.web.event.filter.EventCriteria.BaseEventCriteriaVisitor;
import org.opennms.web.event.filter.EventCriteria.EventCriteriaVisitor;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;


public class JdbcWebEventRepository implements WebEventRepository {
    
    @Autowired
    SimpleJdbcTemplate m_simpleJdbcTemplate;
    
    private String getSql(final String selectCause, final EventCriteria criteria) {
        final StringBuilder buf = new StringBuilder(selectCause);
        
        criteria.visit(new EventCriteriaVisitor<RuntimeException>(){
            
            boolean first = true;
            
            public void and(StringBuilder buf){
                if(first){
                    buf.append(" WHERE ");
                    first = false;
                } else {
                    buf.append(" AND ");
                }
            }
            
            public void visitAckType(AcknowledgeType ackType) throws RuntimeException {
                and(buf);
                buf.append(EventFactory.getAcknowledgeTypeClause(ackType));
            }

            public void visitFilter(Filter filter) throws RuntimeException {
                and(buf);
                buf.append(filter.getParamSql());
            }

            public void visitLimit(int limit, int offset) throws RuntimeException {
                buf.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
            }

            public void visitSortStyle(SortStyle sortStyle) throws RuntimeException {
                buf.append(" ");
                buf.append(EventFactory.getOrderByClause(sortStyle));
            }
            
        });
        
        return buf.toString();
    }
    
    private PreparedStatementSetter paramSetter(final EventCriteria criteria, final Object... args) {
        return new PreparedStatementSetter(){
            int paramIndex = 1;
            public void setValues(final PreparedStatement ps) throws SQLException {
                for(Object arg : args) {
                    ps.setObject(paramIndex, arg);
                    paramIndex++;
                }
                criteria.visit(new BaseEventCriteriaVisitor<SQLException>() {
                    @Override
                    public void visitFilter(Filter filter) throws SQLException {
                        paramIndex =+ filter.bindParam(ps, paramIndex);
                    }
                });
            }
            
        };
    }
    
    public static class EventMapper implements ParameterizedRowMapper<Event>{

        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            Event event = new Event();
            event.id = new Integer(rs.getInt("eventID"));
            event.uei = rs.getString("eventUei");
            event.snmp = rs.getString("eventSnmp");
            event.time = new Date(((Timestamp) rs.getTimestamp("eventTime")).getTime());
            event.host = rs.getString("eventHost");
            event.snmphost = rs.getString("eventSnmpHost");
            event.dpName = rs.getString("eventDpName");
            event.parms = rs.getString("eventParms");

            // node id can be null
            Object element = rs.getObject("nodeID");
            if (element == null) {
                event.nodeID = new Integer(0);
            } else {
                event.nodeID = (Integer) element;
            }

            event.ipAddr = rs.getString("ipAddr");
            event.serviceID = (Integer) rs.getObject("serviceID"); 
            event.nodeLabel = rs.getString("nodeLabel");;
            event.serviceName = rs.getString("serviceName"); 
            event.createTime = new Date(((Timestamp) rs.getTimestamp("eventCreateTime")).getTime());
            event.description = rs.getString("eventDescr");
            event.logGroup = rs.getString("eventLoggroup");
            event.logMessage = rs.getString("eventLogmsg");
            event.severity = new Integer(rs.getInt("eventSeverity"));
            event.operatorInstruction = rs.getString("eventOperInstruct");
            event.autoAction = rs.getString("eventAutoAction");
            event.operatorAction = rs.getString("eventOperAction");
            event.operatorActionMenuText = rs.getString("eventOperActionMenuText");
            event.notification = rs.getString("eventNotification");
            event.troubleTicket = rs.getString("eventTticket");
            event.troubleTicketState = (Integer) rs.getObject("eventTticketState");
            event.forward = rs.getString("eventForward");
            event.mouseOverText = rs.getString("eventMouseOverText");
            event.acknowledgeUser = rs.getString("eventAckUser");

            event.acknowledgeTime = getTimestamp("eventAckTime", rs);

            event.alarmId = (Integer) rs.getObject("alarmid");
            
            return event;
        }
        
        private Date getTimestamp(String field, ResultSet rs) throws SQLException{
            if(rs.getTimestamp(field) != null){
                return new Date(rs.getTimestamp(field).getTime());
            }else{
                return null;
            }
        }
        
    }

    public int countMatchingEvents(EventCriteria criteria) {
        String sql = getSql("SELECT COUNT(EVENTID) as EVENTCOUNT FROM EVENTS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) ", criteria);
        return queryForInt(sql, paramSetter(criteria));
    }

    public int[] countMatchingEventsBySeverity(EventCriteria criteria) {
        String selectClause = "SELECT EVENTSEVERITY, COUNT(*) AS EVENTCOUNT FROM EVENTS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) ";
        String sql = getSql(selectClause, criteria);
        //sql = sql + " AND EVENTDISPLAY='Y'";
        sql = sql + " GROUP BY EVENTSEVERITY";
        
        final int[] alarmCounts = new int[8];
        jdbc().query(sql, paramSetter(criteria), new RowCallbackHandler(){

            public void processRow(ResultSet rs) throws SQLException {
                int severity = rs.getInt("EVENTSEVERITY");
                int alarmCount = rs.getInt("EVENTCOUNT");
                
                alarmCounts[severity] = alarmCount;
                
            }
            
        });
        return alarmCounts;
    }

    public Event getEvent(int eventId) {
        Event[] events = getMatchingEvents(new EventCriteria(new EventIdFilter(eventId)));
        if(events.length < 1){
            return null;
        } else {
            return events[0];
        }
    }

    public Event[] getMatchingEvents(EventCriteria criteria) {
        String sql = getSql("SELECT EVENTS.*, NODE.NODELABEL, SERVICE.SERVICENAME FROM EVENTS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) ", criteria);
        return getEvents(sql, paramSetter(criteria));
    }
    
    private Event[] getEvents(String sql, PreparedStatementSetter setter){
        List<Event> events = queryForList(sql, setter, new EventMapper());
        return events.toArray(new Event[0]);
    }
    
    void acknowledgeEvents(String user, Date timestamp, int[] eventIds){
        acknowledgeMatchingEvents(user, timestamp, new EventCriteria(new EventIdListFilter(eventIds)));
    }

    public void acknowledgeAll(String user, Date timestamp) {
        m_simpleJdbcTemplate.update("UPDATE EVENTS SET EVENTACKUSER=?, EVENTACKTIME=? WHERE EVENTACKUSER IS NULL ", user, new Timestamp(timestamp.getTime()));
    }

    public void acknowledgeMatchingEvents(String user, Date timestamp, EventCriteria criteria) {
        String sql = getSql("UPDATE EVENTS SET EVENTACKUSER=?, EVENTACKTIME=? ", criteria);
        jdbc().update(sql, paramSetter(criteria, user, new Timestamp(timestamp.getTime())));
    }
    
    public void unacknowledgeAll() {
        m_simpleJdbcTemplate.update("UPDATE EVENTS SET EVENTACKUSER=NULL, EVENTACKTIME=NULL WHERE EVENTACKUSER IS NOT NULL ");
    }

    public void unacknowledgeMatchingEvents(EventCriteria criteria) {
        String sql = getSql("UPDATE EVENTS SET EVENTACKUSER=NULL, EVENTACKTIME=null ", criteria);
        jdbc().update(sql, paramSetter(criteria));
    }
    
    private int queryForInt(String sql, PreparedStatementSetter setter) {
        Number number = (Number) queryForObject(sql, setter, new SingleColumnRowMapper(Integer.class));
        return (number != null ? number.intValue() : 0);
    }
    
    @SuppressWarnings("unchecked")
    private Object queryForObject(String sql, PreparedStatementSetter setter, RowMapper rowMapper) {
        return DataAccessUtils.requiredSingleResult((List) jdbc().query(sql, setter, new RowMapperResultSetExtractor(rowMapper, 1)));
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> queryForList(String sql, PreparedStatementSetter setter, ParameterizedRowMapper<T> rm) {
        System.err.println("SQL IS "+sql);
         return (List<T>) jdbc().query(sql, setter, new RowMapperResultSetExtractor(rm));
     }

    private JdbcOperations jdbc() {
        return m_simpleJdbcTemplate.getJdbcOperations();
    }

}
