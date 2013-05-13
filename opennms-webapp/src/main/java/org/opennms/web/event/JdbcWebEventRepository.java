/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.event.filter.EventIdListFilter;
import org.opennms.web.event.filter.EventCriteria.BaseEventCriteriaVisitor;
import org.opennms.web.event.filter.EventCriteria.EventCriteriaVisitor;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <p>JdbcWebEventRepository class.</p>
 *
 * @author ranger
 */
public class JdbcWebEventRepository implements WebEventRepository, InitializingBean {
    
    @Autowired
    SimpleJdbcTemplate m_simpleJdbcTemplate;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    private String getSql(final String selectClause, final EventCriteria criteria) {
        final StringBuilder buf = new StringBuilder(selectClause);
        
        criteria.visit(new EventCriteriaVisitor<RuntimeException>(){
            
            boolean first = true;
            
            public void and(StringBuilder buf) {
                if (first) {
                    buf.append(" WHERE ");
                    first = false;
                } else {
                    buf.append(" AND ");
                }
            }
            
            @Override
            public void visitAckType(AcknowledgeType ackType) {
                and(buf);
                buf.append(ackType.getAcknowledgeTypeClause());
            }

            @Override
            public void visitFilter(Filter filter) {
                and(buf);
                buf.append(filter.getParamSql());
            }

            @Override
            public void visitSortStyle(SortStyle sortStyle) {
                buf.append(" ");
                buf.append(sortStyle.getOrderByClause());
            }
            
            @Override
            public void visitLimit(int limit, int offset) {
                buf.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
            }

        });
        
        return buf.toString();
    }
    
    private PreparedStatementSetter paramSetter(final EventCriteria criteria, final Object... args) {
        return new PreparedStatementSetter() {
            int paramIndex = 1;
            @Override
            public void setValues(final PreparedStatement ps) throws SQLException {
                for(Object arg : args) {
                    ps.setObject(paramIndex, arg);
                    paramIndex++;
                }
                criteria.visit(new BaseEventCriteriaVisitor<SQLException>() {
                    @Override
                    public void visitFilter(Filter filter) throws SQLException {
                        paramIndex += filter.bindParam(ps, paramIndex);
                    }
                });
            }
            
        };
    }
    
    public static class EventMapper implements ParameterizedRowMapper<Event>{

        @Override
        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            Event event = new Event();
            event.id = Integer.valueOf(rs.getInt("eventID"));
            event.uei = rs.getString("eventUei");
            event.snmp = rs.getString("eventSnmp");
            event.time = new Date((rs.getTimestamp("eventTime")).getTime());
            event.host = rs.getString("eventHost");
            event.snmphost = rs.getString("eventSnmpHost");
            event.dpName = rs.getString("eventDpName");
            event.parms = rs.getString("eventParms");

            // node id can be null
            Object element = rs.getObject("nodeID");
            if (element == null) {
                event.nodeID = Integer.valueOf(0);
            } else {
                event.nodeID = (Integer) element;
            }

            event.ipAddr = rs.getString("ipAddr");
            event.serviceID = (Integer) rs.getObject("serviceID"); 
            event.nodeLabel = rs.getString("nodeLabel");;
            event.serviceName = rs.getString("serviceName"); 
            event.createTime = new Date((rs.getTimestamp("eventCreateTime")).getTime());
            event.description = rs.getString("eventDescr");
            event.logGroup = rs.getString("eventLoggroup");
            event.logMessage = rs.getString("eventLogmsg");
            event.severity = OnmsSeverity.get(rs.getInt("eventSeverity"));
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
            
            event.eventDisplay = Boolean.valueOf(rs.getString("eventDisplay").equals("Y"));

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

    /** {@inheritDoc} */
    @Override
    public int countMatchingEvents(EventCriteria criteria) {
        String sql = getSql("SELECT COUNT(EVENTID) as EVENTCOUNT FROM EVENTS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) ", criteria);
        return queryForInt(sql, paramSetter(criteria));
    }

    /** {@inheritDoc} */
    @Override
    public int[] countMatchingEventsBySeverity(EventCriteria criteria) {
        String selectClause = "SELECT EVENTSEVERITY, COUNT(*) AS EVENTCOUNT FROM EVENTS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) ";
        String sql = getSql(selectClause, criteria);
        //sql = sql + " AND EVENTDISPLAY='Y'";
        sql = sql + " GROUP BY EVENTSEVERITY";
        
        final int[] alarmCounts = new int[8];
        jdbc().query(sql, paramSetter(criteria), new RowCallbackHandler(){

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int severity = rs.getInt("EVENTSEVERITY");
                int alarmCount = rs.getInt("EVENTCOUNT");
                
                alarmCounts[severity] = alarmCount;
                
            }
            
        });
        return alarmCounts;
    }

    /** {@inheritDoc} */
    @Override
    public Event getEvent(int eventId) {
        Event[] events = getMatchingEvents(new EventCriteria(new EventIdFilter(eventId)));
        if(events.length < 1){
            return null;
        } else {
            return events[0];
        }
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public void acknowledgeAll(String user, Date timestamp) {
        m_simpleJdbcTemplate.update("UPDATE EVENTS SET EVENTACKUSER=?, EVENTACKTIME=? WHERE EVENTACKUSER IS NULL ", user, new Timestamp(timestamp.getTime()));
    }

    /** {@inheritDoc} */
    @Override
    public void acknowledgeMatchingEvents(String user, Date timestamp, EventCriteria criteria) {
        String sql = getSql("UPDATE EVENTS SET EVENTACKUSER=?, EVENTACKTIME=? ", criteria);
        jdbc().update(sql, paramSetter(criteria, user, new Timestamp(timestamp.getTime())));
    }
    
    /**
     * <p>unacknowledgeAll</p>
     */
    @Override
    public void unacknowledgeAll() {
        m_simpleJdbcTemplate.update("UPDATE EVENTS SET EVENTACKUSER=NULL, EVENTACKTIME=NULL WHERE EVENTACKUSER IS NOT NULL ");
    }

    /** {@inheritDoc} */
    @Override
    public void unacknowledgeMatchingEvents(EventCriteria criteria) {
        String sql = getSql("UPDATE EVENTS SET EVENTACKUSER=NULL, EVENTACKTIME=null ", criteria);
        jdbc().update(sql, paramSetter(criteria));
    }
    
    private int queryForInt(String sql, PreparedStatementSetter setter) throws DataAccessException {
        Integer number = queryForObject(sql, setter, new SingleColumnRowMapper<Integer>(Integer.class));
        return (number != null ? number.intValue() : 0);
    }
    
    private <T> T queryForObject(String sql, PreparedStatementSetter setter, RowMapper<T> rowMapper) throws DataAccessException {
        return DataAccessUtils.requiredSingleResult(jdbc().query(sql, setter, new RowMapperResultSetExtractor<T>(rowMapper, 1)));
    }


    private <T> List<T> queryForList(String sql, PreparedStatementSetter setter, ParameterizedRowMapper<T> rm) {
        return jdbc().query(sql, setter, new RowMapperResultSetExtractor<T>(rm));
    }
    
    private JdbcOperations jdbc() {
        return m_simpleJdbcTemplate.getJdbcOperations();
    }

}
