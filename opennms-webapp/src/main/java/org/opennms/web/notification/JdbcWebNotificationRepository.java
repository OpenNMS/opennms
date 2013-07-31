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

package org.opennms.web.notification;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.opennms.core.utils.BeanUtils;
import org.opennms.web.notification.filter.NotificationCriteria;
import org.opennms.web.notification.filter.NotificationIdFilter;
import org.opennms.web.notification.filter.NotificationCriteria.BaseNotificationCriteriaVisitor;
import org.opennms.web.notification.filter.NotificationCriteria.NotificationCriteriaVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <p>JdbcWebNotificationRepository class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class JdbcWebNotificationRepository implements WebNotificationRepository, InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(JdbcWebNotificationRepository.class);

    
    @Autowired
    SimpleJdbcTemplate m_simpleJdbcTemplate;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    private String getSql(final String selectClause, final NotificationCriteria criteria) {
        final StringBuilder buf = new StringBuilder(selectClause);
        
        criteria.visit(new NotificationCriteriaVisitor<RuntimeException>() {
            
            boolean first = true;
            
            public void and(StringBuilder buf){
                if(first){
                    buf.append(" WHERE ");
                    first = false;
                } else {
                    buf.append(" AND ");
                }
            }

            @Override
            public void visitAckType(AcknowledgeType ackType) throws RuntimeException {
                and(buf);
                buf.append(ackType.getAcknowledgeTypeClause());
            }

            @Override
            public void visitFilter(org.opennms.web.filter.Filter filter) throws RuntimeException {
                and(buf);
                buf.append(filter.getParamSql());
            }

            @Override
             public void visitLimit(int limit, int offset) throws RuntimeException {
                buf.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
                
            }

            @Override
            public void visitSortStyle(SortStyle sortStyle) throws RuntimeException {
                buf.append(" ");
                buf.append(sortStyle.getOrderByClause());
            }

        });
        
        return buf.toString();
    }

    private PreparedStatementSetter paramSetter(final NotificationCriteria criteria, final Object...args){
        return new PreparedStatementSetter(){
            int paramIndex = 1;
            @Override
            public void setValues(final PreparedStatement ps) throws SQLException {
                for(Object arg : args){
                    ps.setObject(paramIndex, arg);
                    paramIndex++;
                }
                criteria.visit(new BaseNotificationCriteriaVisitor<SQLException>(){
                    @Override
                    public void visitFilter(org.opennms.web.filter.Filter filter) throws SQLException{
                        LOG.info("filter sql: {}", filter.getSql());
                        paramIndex += filter.bindParam(ps, paramIndex);
                    }
                });
            }
            
        };
    }
    
    private static class NotificationMapper implements ParameterizedRowMapper<Notification>{

        @Override
        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
            Notification notice = new Notification();
            
            notice.m_notifyID = Integer.valueOf(rs.getInt("notifyid"));
            notice.m_timeSent = getTimestamp("pagetime", rs);
            notice.m_timeReply = getTimestamp("respondtime", rs);
            notice.m_txtMsg = rs.getString("textmsg");
            notice.m_numMsg = rs.getString("numericmsg");
            notice.m_responder = rs.getString("answeredby");
            notice.m_nodeID = Integer.valueOf(rs.getInt("nodeid"));
            notice.m_interfaceID = rs.getString("interfaceid");
            notice.m_eventId = Integer.valueOf(rs.getInt("eventid"));
            
            notice.m_serviceName = rs.getString("servicename");

            return notice;
        }
    
        private long getTimestamp(String field, ResultSet rs) throws SQLException{
            if(rs.getTimestamp(field) != null){
                return rs.getTimestamp(field).getTime();
            }else{
                return 0;
            }
        }
        
    }
    
    /** {@inheritDoc} */
    @Override
    public void acknowledgeMatchingNotification(String user, Date timestamp, NotificationCriteria criteria) {
        String sql = getSql("UPDATE NOTIFICATIONS SET RESPONDTIME=?, ANSWEREDBY=?", criteria);
        jdbc().update(sql, paramSetter(criteria, new Timestamp(timestamp.getTime()), user));
    }

    /** {@inheritDoc} */
    @Override
    public Notification[] getMatchingNotifications(NotificationCriteria criteria) {
        String sql = getSql("SELECT NOTIFICATIONS.*, SERVICE.SERVICENAME FROM NOTIFICATIONS LEFT OUTER JOIN SERVICE USING (SERVICEID)", criteria);
        return getNotifications(sql, paramSetter(criteria));
    }

    private Notification[] getNotifications(String sql, PreparedStatementSetter paramSetter) {
        List<Notification> notifications = queryForList(sql, paramSetter, new NotificationMapper());
        return notifications.toArray(new Notification[0]);
    }

    /** {@inheritDoc} */
    @Override
    public Notification getNotification(int noticeId) {
        Notification[] notifications = getMatchingNotifications(new NotificationCriteria(new NotificationIdFilter(noticeId)));
        if (notifications.length < 1) {
            return null;
        } else {
            return notifications[0];
        }
    }

    /** {@inheritDoc} */
    @Override
    public int countMatchingNotifications(NotificationCriteria criteria) {
        String sql = getSql("SELECT COUNT(*) AS NOTICECOUNT FROM NOTIFICATIONS", criteria);
        return queryForInt(sql, paramSetter(criteria));

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
    
    private JdbcOperations jdbc(){
        return m_simpleJdbcTemplate.getJdbcOperations();
    }
}
