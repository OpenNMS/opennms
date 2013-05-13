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

package org.opennms.web.outage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.OutageIdFilter;
import org.opennms.web.outage.filter.OutageCriteria.BaseOutageCriteriaVisitor;
import org.opennms.web.outage.filter.OutageCriteria.OutageCriteriaVisitor;
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
 * <p>JdbcWebOutageRepository class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class JdbcWebOutageRepository implements WebOutageRepository, InitializingBean {

    @Autowired
    SimpleJdbcTemplate m_simpleJdbcTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** {@inheritDoc} */
    @Override
    public int countCurrentOutages() {
        return getCurrentOutages(0).length;
    }

    /** {@inheritDoc} */
    @Override
    public OutageSummary[] getCurrentOutages(final int rows) {
        return getMatchingOutageSummaries(new OutageCriteria(new Filter[]{}, SortStyle.IFLOSTSERVICE, OutageType.CURRENT, rows, 0));
    }

    /** {@inheritDoc} */
    @Override
    public int countMatchingOutages(OutageCriteria criteria) {
        String sql = getSql("SELECT COUNT(OUTAGEID) as OUTAGECOUNT "
                                + "FROM OUTAGES "
                                + "LEFT OUTER JOIN NODE USING (NODEID) "
                                + "LEFT OUTER JOIN SERVICE USING (SERVICEID) "
                                + "JOIN IPINTERFACE ON OUTAGES.NODEID=IPINTERFACE.NODEID AND OUTAGES.IPADDR=IPINTERFACE.IPADDR "
                                + "JOIN IFSERVICES ON OUTAGES.NODEID=IFSERVICES.NODEID AND OUTAGES.IPADDR=IFSERVICES.IPADDR AND "
                                + "OUTAGES.SERVICEID=IFSERVICES.SERVICEID "
                                , null, criteria);
        // System.err.println("countMatchingOutages() = " + sql);
        return queryForInt(sql, paramSetter(criteria));
    }

    /** {@inheritDoc} */
    @Override
    public Outage[] getMatchingOutages(OutageCriteria criteria) {
        String sql = getSql("SELECT OUTAGES.*, NODE.NODELABEL, IPINTERFACE.IPHOSTNAME, SERVICE.SERVICENAME, "
                            + "NOTIFICATIONS.NOTIFYID, NOTIFICATIONS.ANSWEREDBY FROM OUTAGES "
                            + "JOIN NODE USING(NODEID) "
                            + "JOIN IPINTERFACE ON OUTAGES.NODEID=IPINTERFACE.NODEID AND OUTAGES.IPADDR=IPINTERFACE.IPADDR "
                            + "JOIN IFSERVICES ON OUTAGES.NODEID=IFSERVICES.NODEID AND OUTAGES.IPADDR=IFSERVICES.IPADDR AND "
                            + "OUTAGES.SERVICEID=IFSERVICES.SERVICEID "
                            + "LEFT OUTER JOIN SERVICE ON OUTAGES.SERVICEID=SERVICE.SERVICEID "
                            + "LEFT OUTER JOIN NOTIFICATIONS ON SVCLOSTEVENTID=NOTIFICATIONS.EVENTID "
                            + "LEFT OUTER JOIN ASSETS ON NODE.NODEID=ASSETS.NODEID "
                            , null,criteria);
        // System.err.println("getMatchingOutages() = " + sql);
        return getOutages(sql, paramSetter(criteria));
    }

    /** {@inheritDoc} */
    @Override
    public int countMatchingOutageSummaries(OutageCriteria criteria) {
        String sql = getSql("SELECT COUNT(DISTINCT NODE.NODEID) AS OUTAGECOUNT "
                            + "FROM OUTAGES "
                            + "LEFT OUTER JOIN NODE USING (NODEID) "
                            + "LEFT OUTER JOIN SERVICE USING (SERVICEID) "
                            + "JOIN IPINTERFACE ON OUTAGES.NODEID=IPINTERFACE.NODEID AND OUTAGES.IPADDR=IPINTERFACE.IPADDR "
                            + "JOIN IFSERVICES ON OUTAGES.NODEID=IFSERVICES.NODEID AND OUTAGES.IPADDR=IFSERVICES.IPADDR AND "
                            + "OUTAGES.SERVICEID=IFSERVICES.SERVICEID "
                            , null, criteria);
        // System.err.println("countMatchingOutageSummaries() = " + sql);
        return queryForInt(sql, paramSetter(criteria));
    }

    /** {@inheritDoc} */
    @Override
    public OutageSummary[] getMatchingOutageSummaries(OutageCriteria criteria) {
        String sql = getSql("SELECT DISTINCT "
                            + "NODE.NODEID, NODE.NODELABEL, max(OUTAGES.IFLOSTSERVICE) AS IFLOSTSERVICE, max(OUTAGES.IFREGAINEDSERVICE) AS IFREGAINEDSERVICE, NOW() AS CURRENTTIME "
                            + "FROM OUTAGES "
                            + "LEFT OUTER JOIN NODE USING (NODEID) "
                            + "LEFT OUTER JOIN SERVICE USING (SERVICEID) "
                            + "JOIN IPINTERFACE ON OUTAGES.NODEID=IPINTERFACE.NODEID AND OUTAGES.IPADDR=IPINTERFACE.IPADDR "
                            + "JOIN IFSERVICES ON OUTAGES.NODEID=IFSERVICES.NODEID AND OUTAGES.IPADDR=IFSERVICES.IPADDR AND "
                            + "OUTAGES.SERVICEID=IFSERVICES.SERVICEID "
                            , "NODE.NODEID, NODE.NODELABEL", criteria);
        // System.err.println("getMatchingOutageSummaries() = " + sql);
        return getOutageSummaries(sql, paramSetter(criteria));
    }

    /** {@inheritDoc} */
    @Override
    public Outage getOutage(int outageId) {
        OutageCriteria criteria = new OutageCriteria(new OutageIdFilter(outageId));
        Outage[] outages = getMatchingOutages(criteria);
        if (outages.length == 0) {
            return null;
        } else {
            return outages[0];
        }
    }

    private OutageSummary[] getOutageSummaries(String sql, PreparedStatementSetter setter) {
        List<OutageSummary> summaries = queryForList(sql, setter, new OutageSummaryMapper());
        return summaries.toArray(new OutageSummary[0]);
    }

    private Outage[] getOutages(String sql, PreparedStatementSetter setter) {
        List<Outage> outages = queryForList(sql, setter, new OutageMapper());
        return outages.toArray(new Outage[0]);
    }

    private String getSql(final String selectClause, final String groupByClause, final OutageCriteria criteria) {
        final StringBuilder buf = new StringBuilder(selectClause);

        criteria.visit(new OutageCriteriaVisitor<RuntimeException>() {

            boolean first = true;

            public void and(StringBuilder buf) {
                if (first) {
                    buf.append(" WHERE (NODE.NODETYPE IS NULL OR NODE.NODETYPE != 'D') AND (IPINTERFACE.ISMANAGED IS NULL OR IPINTERFACE.ISMANAGED != 'D') AND (IFSERVICES.STATUS IS NULL OR IFSERVICES.STATUS != 'D') AND ");
//                    buf.append(" WHERE NODE.NODETYPE != 'D' AND ");
//                    buf.append(" WHERE ");
                    first = false;
                } else {
                    buf.append(" AND ");
                }
            }

            @Override
            public void visitOutageType(OutageType outageType) {
                and(buf);
                buf.append(outageType.getClause());
            }


            @Override
            public void visitFilter(Filter filter) {
                and(buf);
                buf.append(filter.getParamSql());
            }

            @Override
            public void visitGroupBy() {
                if (groupByClause != null && groupByClause.trim().length() != 0) {
                    buf.append(" GROUP BY ");
                    buf.append(groupByClause);
                }
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

    private PreparedStatementSetter paramSetter(final OutageCriteria criteria, final Object... args) {
        return new PreparedStatementSetter() {
            int paramIndex = 1;
            @Override
            public void setValues(final PreparedStatement ps) throws SQLException {
                for(Object arg : args) {
                    ps.setObject(paramIndex, arg);
                    paramIndex++;
                }
                criteria.visit(new BaseOutageCriteriaVisitor<SQLException>() {
                    @Override
                    public void visitFilter(Filter filter) throws SQLException {
                        paramIndex += filter.bindParam(ps, paramIndex);
                    }
                });
            }
        };
    }

    private static class OutageSummaryMapper implements ParameterizedRowMapper<OutageSummary> {
        @Override
        public OutageSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new OutageSummary(rs.getInt("nodeID"), rs.getString("nodeLabel"), getTimestamp("ifLostService", rs), getTimestamp("ifRegainedService", rs), getTimestamp("currentTime", rs));
        }
    }

    private static class OutageMapper implements ParameterizedRowMapper<Outage> {
        @Override
        public Outage mapRow(ResultSet rs, int rowNum) throws SQLException {
            Outage outage = new Outage();
            outage.outageId = ((Integer) rs.getObject("outageID"));
            outage.lostServiceEventId = ((Integer) rs.getObject("svcLostEventID"));
            outage.regainedServiceEventId = ((Integer) rs.getObject("svcRegainedEventID"));
            outage.nodeId = ((Integer) rs.getObject("nodeID"));
            outage.ipAddress = ((String) rs.getObject("ipAddr"));
            outage.serviceId = ((Integer) rs.getObject("serviceID"));
            outage.lostServiceTime = getTimestamp("ifLostService", rs);
            outage.regainedServiceTime = getTimestamp("ifRegainedService", rs);
            outage.suppressTime = getTimestamp("suppressTime", rs);
            outage.suppressedBy = ((String) rs.getObject("suppressedBy"));
            
            outage.hostname = ((String) rs.getObject("ipHostname"));
            outage.lostServiceNotificationAcknowledgedBy = ((String) rs.getObject("answeredBy"));
            outage.lostServiceNotificationId = ((Integer) rs.getObject("notifyId"));
            outage.nodeLabel = ((String) rs.getObject("nodeLabel"));
            outage.serviceName = ((String) rs.getObject("serviceName"));

            return outage;
        }
        
    }

    private static Date getTimestamp(String field, ResultSet rs) throws SQLException{
        if(rs.getTimestamp(field) != null){
            return new Date(rs.getTimestamp(field).getTime());
        }else{
            return null;
        }
    }
}
