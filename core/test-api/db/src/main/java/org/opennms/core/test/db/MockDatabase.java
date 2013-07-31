/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.test.db;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.Querier;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockVisitor;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.mock.Outage;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventWriter;
import org.opennms.netmgt.model.events.Parameter;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides additional utility methods on top of the basic {@link TemporaryDatabasePostgreSQL}
 * class. For instance, it can be populated from a {@link MockNetwork}.
 * 
 * @author brozow
 */
public class MockDatabase extends TemporaryDatabasePostgreSQL implements EventWriter {
    private static final Logger LOG = LoggerFactory.getLogger(MockDatabase.class);
	
    public MockDatabase(String dbName) throws Exception {
        this(dbName, true);
    }

    public MockDatabase() throws Exception {
        this(true);
    }
    
    public MockDatabase(String name, boolean createNow) throws Exception {
        super(name);
        setPopulateSchema(true);
        if (createNow) {
            create();
        }
    }
    
    public MockDatabase(boolean createNow) throws Exception {
        super();
        setPopulateSchema(true);
        if (createNow) {
            create();
        }
    }
    
    public void populate(MockNetwork network) {

        MockVisitor dbCreater = new MockVisitorAdapter() {
            @Override
            public void visitNode(MockNode node) {
                writeNode(node);
            }
            
            @Override
            public void visitInterface(MockInterface iface) {
                writeInterface(iface);
            }
            
            @Override
            public void visitService(MockService svc) {
                writeService(svc);
            }
        };
        network.visit(dbCreater);
        
        
        
        getJdbcTemplate().queryForInt("SELECT setval('nodeNxtId', max(nodeid)) FROM node");
        
    }
    
    public void writeNode(MockNode node) {
        Object[] values = { Integer.valueOf(node.getNodeId()), node.getLabel(), new Timestamp(System.currentTimeMillis()), "A" };
        update("insert into node (dpName, nodeID, nodeLabel, nodeCreateTime, nodeType) values ('localhost', ?, ?, ?, ?);", values);
        
    }

    public void writeInterface(MockInterface iface) {
        writeSnmpInterface(iface);
		Object[] values = { Integer.valueOf(iface.getNodeId()), str(iface.getAddress()), iface.getIfIndex(), (iface.getIfIndex() == 1 ? "P" : "N"), "A" };
        update("insert into ipInterface (nodeID, ipAddr, ifIndex, isSnmpPrimary, isManaged) values (?, ?, ?, ?, ?);", values);
    }

    public void writeSnmpInterface(MockInterface iface) {
        Object[] values = { Integer.valueOf(iface.getNodeId()), iface.getIfAlias(), iface.getIfIndex() };
        update("insert into snmpInterface (nodeID, snmpifAlias, snmpIfIndex) values (?, ?, ?);", values);
    }

    public void writeService(MockService svc) {
        String svcName = svc.getSvcName();
        Integer serviceId = getServiceID(svcName);
        if (serviceId == null) {
            svc.setId(getNextServiceId());
            Object[] svcValues = { svc.getId(), svcName };
            update("insert into service (serviceID, serviceName) values (?, ?);", svcValues);
            LOG.info("Inserting service \"{}\" into database with ID {}", svcName, svc.getId());
        } else {
            svc.setId(serviceId);
        }
        String status = svc.getMgmtStatus().toDbString();
        Object[] values = { Integer.valueOf(svc.getNodeId()), str(svc.getAddress()), Integer.valueOf(svc.getId()), status };
        update("insert into ifServices (nodeID, ipAddr, serviceID, status) values (?, ?, ?, ?);", values);
    }

    public String getNextOutageIdStatement() {
        return getNextSequenceValStatement("outageNxtId");
    }
    
    public Integer getNextOutageId() {
        return getNextId(getNextOutageIdStatement());
        
    }
    
    public String getNextEventIdStatement() {
        return getNextSequenceValStatement("eventsNxtId");
    }
    
    public Integer getNextEventId() {
        return getNextId(getNextEventIdStatement());
    }
    
    public String getNextServiceIdStatement() {
        return getNextSequenceValStatement("serviceNxtId");

    }
    
    public Integer getNextServiceId() {
        return getNextId(getNextServiceIdStatement());
    }
    
    public Integer getServiceID(String serviceName) {
        if (serviceName == null) return null;
        SingleResultQuerier querier = new SingleResultQuerier(this, "select serviceId from service where serviceName = ?");
        querier.execute(serviceName);
        return (Integer)querier.getResult();
    }
    
    public String getServiceName(int serviceId) {
        SingleResultQuerier querier = new SingleResultQuerier(this, "select serviceName from service where serviceId = ?");
        querier.execute(Integer.valueOf(serviceId));
        return (String)querier.getResult();
    }
    
    public int countOutagesForService(MockService svc) {
        return countOutagesForService(svc, null);
    }
    
    public int countOpenOutagesForService(MockService svc) {
        return countOutagesForService(svc, "ifRegainedService is null");
    }
    
    public int countOutagesForService(MockService svc, String criteria) {
        String critSql = (criteria == null ? "" : " and "+criteria);
        Object[] values = { Integer.valueOf(svc.getNodeId()), svc.getIpAddr(), Integer.valueOf(svc.getId()) };
        return countRows("select * from outages where nodeId = ? and ipAddr = ? and serviceId = ?"+critSql, values);
    }

    public void createOutage(MockService svc, Event svcLostEvent) {
        createOutage(svc, svcLostEvent.getDbid(), convertEventTimeToTimeStamp(svcLostEvent.getTime()));
    }

    public void createOutage(MockService svc, int eventId, Timestamp time) {
        Object[] values = {
                getNextOutageId(), // outageID
                Integer.valueOf(eventId),           // svcLostEventId
                Integer.valueOf(svc.getNodeId()), // nodeId
                str(svc.getAddress()),                // ipAddr
                Integer.valueOf(svc.getId()),       // serviceID
                time, // ifLostService
               };
        
        update("insert into outages (outageId, svcLostEventId, nodeId, ipAddr, serviceId, ifLostService) values (?, ?, ?, ?, ?, ?);", values);
        
    }
    
    public void resolveOutage(MockService svc, Event svcRegainEvent) {
        resolveOutage(svc, svcRegainEvent.getDbid(), convertEventTimeToTimeStamp(svcRegainEvent.getTime()));
    }        

    public void resolveOutage(MockService svc, int eventId, Timestamp timestamp) {
        
        Object[] values = {
                Integer.valueOf(eventId),           // svcLostEventId
                timestamp, // ifLostService
                Integer.valueOf(svc.getNodeId()), // nodeId
                svc.getIpAddr(),                // ipAddr
                Integer.valueOf(svc.getId()),       // serviceID
               };
        
        update("UPDATE outages set svcRegainedEventID=?, ifRegainedService=? where (nodeid = ? AND ipAddr = ? AND serviceID = ? and (ifRegainedService IS NULL));", values);
    }
    

    
    public Timestamp convertEventTimeToTimeStamp(String time) {
        try {
            Date date = EventConstants.parseToDate(time);
            Timestamp eventTime = new Timestamp(date.getTime());
            return eventTime;
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format "+time, e);
        }
    }

    /**
     * @param e
     */
    @Override
    public void writeEvent(Event e) {
        Integer eventId = getNextEventId();
        
        if (e.getCreationTime() == null) 
            e.setCreationTime(e.getTime());
        
        Object[] values = {
                eventId,
                e.getSource(),
                e.getUei(),
                convertEventTimeToTimeStamp(e.getCreationTime()),
                convertEventTimeToTimeStamp(e.getTime()),
                Integer.valueOf(OnmsSeverity.get(e.getSeverity()).getId()),
                (e.hasNodeid() ? new Long(e.getNodeid()) : null),
                e.getInterface(),
                getServiceID(e.getService()),
                "localhost",
                "Y",
                "Y",
                e.getTticket() == null ? "" : e.getTticket().getContent(),
                Integer.valueOf(e.getTticket() == null ? "0" : e.getTticket().getState()),
                Parameter.format(e),
                e.getLogmsg() == null? null : e.getLogmsg().getContent()
        };
        e.setDbid(eventId.intValue());
        update("insert into events (" +
                "eventId, eventSource, eventUei, eventCreateTime, eventTime, eventSeverity, " +
                "nodeId, ipAddr, serviceId, eventDpName, " +
                "eventLog, eventDisplay, eventtticket, eventtticketstate, eventparms, eventlogmsg) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", values);
    }
    
    public void setServiceStatus(MockService svc, char newStatus) {
        Object[] values = { String.valueOf(newStatus), Integer.valueOf(svc.getNodeId()), svc.getIpAddr(), Integer.valueOf(svc.getId()) };
        update("update ifServices set status = ? where nodeId = ? and ipAddr = ? and serviceId = ?", values);
    }

    public char getServiceStatus(MockService svc) {
        SingleResultQuerier querier = new SingleResultQuerier(this, "select status from ifServices where nodeId = ? and ipAddr = ? and serviceID = ?");
        querier.execute(Integer.valueOf(svc.getNodeId()), svc.getIpAddr(), Integer.valueOf(svc.getId()));
        String result = (String)querier.getResult();
        if (result == null || "".equals(result)) {
            return 'X';
        }
        return result.charAt(0);
    }

    public void setInterfaceStatus(MockInterface iface, char newStatus) {
        Object[] values = { String.valueOf(newStatus), Integer.valueOf(iface.getNodeId()), iface.getIpAddr() };
        update("update ipInterface set isManaged = ? where nodeId = ? and ipAddr = ?;", values);
    }
    
    public char getInterfaceStatus(MockInterface iface) {
        SingleResultQuerier querier = new SingleResultQuerier(this, "select isManaged from ipInterface where nodeId = ? and ipAddr = ?");
        querier.execute(Integer.valueOf(iface.getNodeId()), iface.getIpAddr());
        String result = (String)querier.getResult();
        if (result == null || "".equals(result)) {
            return 'X';
        }
        return result.charAt(0);
    }
    
    public int countOutages() {
        return countOutages(null);
    }
    
    public int countOpenOutages() {
        return countOutages("ifRegainedService is null");
    }
    
    public int countOutages(String criteria) {
        String critSql = (criteria == null ? "" : " where "+criteria);
        return countRows("select * from outages"+critSql);
    }
    
    public int countOutagesForInterface(MockInterface iface) {
        return countOutagesForInterface(iface, null);
    }
    
    public int countOpenOutagesForInterface(MockInterface iface) {
        return countOutagesForInterface(iface, "ifRegainedService is null");
    }

    public int countOutagesForInterface(MockInterface iface, String criteria) {
        String critSql = (criteria == null ? "" : " and "+criteria);
        Object[] values = { Integer.valueOf(iface.getNodeId()), iface.getIpAddr() };
        return countRows("select * from outages where nodeId = ? and ipAddr = ? "+critSql, values);
    }
    
    public boolean hasOpenOutage(MockService svc) {
        return countOpenOutagesForService(svc) > 0;
    }
    
    public Collection<Outage> getOutages() {
        return getOutages(null, new Object[0]);
    }
    
    public Collection<Outage> getOutages(String criteria, Object... values) {
        String critSql = (criteria == null ? "" : " where "+criteria);
        final List<Outage> outages = new LinkedList<Outage>();
        Querier loadExisting = new Querier(this, "select * from outages "+critSql) {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Outage outage = new Outage(rs.getInt("nodeId"), rs.getString("ipAddr"), rs.getInt("serviceId"));
                outage.setLostEvent(rs.getInt("svcLostEventID"), rs.getTimestamp("ifLostService"));
                boolean open = (rs.getObject("ifRegainedService") == null);
                if (!open) {
                    outage.setRegainedEvent(rs.getInt("svcRegainedEventID"), rs.getTimestamp("ifRegainedService"));
                }
                outages.add(outage);
            }
        };

        loadExisting.execute(values);
        
        Querier setServiceNames = new Querier(this, "select * from service") {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int serviceId = rs.getInt("serviceId");
                String serviceName = rs.getString("serviceName");
                for(Outage outage : outages) {
                    if (outage.getServiceId() == serviceId) {
                        outage.setServiceName(serviceName);
                    }
                }
            }
        };
        
        setServiceNames.execute();
        
        return outages;
    }
    
    public Collection<Outage> getOpenOutages(MockService svc) {
        return getOutages("nodeId = ? and ipAddr = ? and serviceID = ? and ifRegainedService is null",
                svc.getNodeId(), svc.getIpAddr(), svc.getId());
    }
    
    public Collection<Outage> getOutages(MockService svc) {
        return getOutages("nodeId = ? and ipAddr = ? and serviceID = ?",
                svc.getNodeId(), svc.getIpAddr(), svc.getId());
    }
    
    public Collection<Outage> getClosedOutages(MockService svc) {
        return getOutages("nodeId = ? and ipAddr = ? and serviceID = ? and ifRegainedService is not null",
                svc.getNodeId(), svc.getIpAddr(), svc.getId());
    }

    /**
     * @param ipAddr
     * @param nodeId
     * @param nodeId2
     */
    public void reparentInterface(String ipAddr, int oldNode, int newNode) {
        update("delete from snmpInterface where id in (" +
                "select oldif.id from snmpinterface as oldIf " +
                "    where exists( " +
                "        select * from snmpinterface as newIf " +
                "        join ipinterface ip " +
                "          on oldif.id = ip.snmpinterfaceid " +
                "        where " +
                "           newIf.nodeId = ? and " +
                "           oldIf.nodeId = ? and " +
                "           ip.ipaddr = ? and " +
                "           oldIf.snmpifindex = newif.snmpifindex " +
                "       )" +
                ")", newNode, oldNode, ipAddr);
        update("update snmpInterface set nodeId = ? where id in (select snmpInterfaceId from ipInterface where nodeId = ? and ipAddr = ?)", newNode, oldNode, ipAddr);
        update("update ipInterface set nodeId = ? where nodeId = ? and ipAddr = ?", newNode, oldNode, ipAddr);
        update("update ifServices set nodeId = ? where nodeId = ? and ipAddr = ?", newNode, oldNode, ipAddr);
    }

    /**
     * @return
     */
    public String getNextNotifIdSql() {
        return getNextSequenceValStatement("notifyNxtId");
    }

    /**
     * @param e
     */
    public void acknowledgeNoticesForEvent(Event e) {
        update("update notifications set respondTime = ? where eventID = ? and respondTime is null",
                new Timestamp(System.currentTimeMillis()), e.getDbid());
    }

    /**
     * @param event
     * @return
     */
    public Collection<Integer> findNoticesForEvent(Event event) {
        final List<Integer> notifyIds = new LinkedList<Integer>();
        Querier loadExisting = new Querier(this, "select notifyId from notifications where eventID = ?") {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                notifyIds.add(rs.getInt(1));
            }
        };
        loadExisting.execute(Integer.valueOf(event.getDbid()));
        return notifyIds;
    }

    public Integer getAlarmCount(String reductionKey) {
        SingleResultQuerier querier = new SingleResultQuerier(this, "select counter from alarms where reductionKey = ?");
        querier.execute(reductionKey);
        return (Integer)querier.getResult();
    }

    public Integer getAlarmId(String reductionKey) {
        SingleResultQuerier querier = new SingleResultQuerier(this, "select alarmid from alarms where reductionKey = ?");
        querier.execute(reductionKey);
        return (Integer)querier.getResult();
    }

    public String getNextUserNotifIdSql() {
        return getNextSequenceValStatement("userNotifNxtId");
    }
    

}
