//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 10: Make sure we call create() in all of the constructors,
//              update snmpInterface entries in reparentInterface, and
//              use Java 5 varargs. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.mock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.db.TemporaryDatabase;
import org.opennms.netmgt.eventd.db.Constants;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.utils.SingleResultQuerier;
import org.opennms.netmgt.xml.event.Event;

/**
 * In memory database comparable to the postgres database that can be used for unit
 * testing.  Can be populated from a MockNetwork
 * @author brozow
 */
public class MockDatabase extends TemporaryDatabase implements EventWriter {
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
            public void visitNode(MockNode node) {
                writeNode(node);
            }
            
            public void visitInterface(MockInterface iface) {
                writeInterface(iface);
            }
            
            public void visitService(MockService svc) {
                writeService(svc);
            }
        };
        network.visit(dbCreater);
        
        
        
        getJdbcTemplate().queryForInt("SELECT setval('nodeNxtId', max(nodeid)) FROM node");
        
    }
    
    public void writeNode(MockNode node) {
        Object[] values = { new Integer(node.getNodeId()), node.getLabel(), new Timestamp(System.currentTimeMillis()), "A" };
        update("insert into node (dpName, nodeID, nodeLabel, nodeCreateTime, nodeType) values ('localhost', ?, ?, ?, ?);", values);
        
    }

    public void writeInterface(MockInterface iface) {
        writeSnmpInterface(iface);
		Object[] values = { new Integer(iface.getNodeId()), iface.getIpAddr(), iface.getIfIndex(), (iface.getIfIndex() == 1 ? "P" : "N"), "A" };
        update("insert into ipInterface (nodeID, ipAddr, ifIndex, isSnmpPrimary, isManaged) values (?, ?, ?, ?, ?);", values);
    }

    public void writeSnmpInterface(MockInterface iface) {
        Object[] values = { new Integer(iface.getNodeId()), iface.getIpAddr(), iface.getIfAlias(), iface.getIfIndex() };
        update("insert into snmpInterface (nodeID, ipAddr, snmpifAlias, snmpIfIndex) values (?, ?, ?, ?);", values);
    }

    public void writeService(MockService svc) {
        String svcName = svc.getSvcName();
        if (!serviceDefined(svcName)) {
            Object[] svcValues = { new Integer(svc.getId()), svcName };
            //Object[] svcValues = { getNextServiceId(), svcName };
            getNextServiceId();
            update("insert into service (serviceID, serviceName) values (?, ?);", svcValues);
        }
        
        String status = svc.getMgmtStatus().toDbString();
        Object[] values = { new Integer(svc.getNodeId()), svc.getIpAddr(), new Integer(svc.getId()), status };
        update("insert into ifServices (nodeID, ipAddr, serviceID, status) values (?, ?, ?, ?);", values);
    }
    
    private boolean serviceDefined(String svcName) {
        Querier querier = new Querier(this, "select serviceId from service where serviceName = ?");
        querier.execute(svcName);
        return querier.getCount() > 0;
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
        querier.execute(new Integer(serviceId));
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
        Object[] values = { new Integer(svc.getNodeId()), svc.getIpAddr(), new Integer(svc.getId()) };
        return countRows("select * from outages where nodeId = ? and ipAddr = ? and serviceId = ?"+critSql, values);
    }

    public void createOutage(MockService svc, Event svcLostEvent) {
        createOutage(svc, svcLostEvent.getDbid(), convertEventTimeToTimeStamp(svcLostEvent.getTime()));
    }

    public void createOutage(MockService svc, int eventId, Timestamp time) {
        Object[] values = {
                getNextOutageId(), // outageID
                new Integer(eventId),           // svcLostEventId
                new Integer(svc.getNodeId()), // nodeId
                svc.getIpAddr(),                // ipAddr
                new Integer(svc.getId()),       // serviceID
                time, // ifLostService
               };
        
        update("insert into outages (outageId, svcLostEventId, nodeId, ipAddr, serviceId, ifLostService) values (?, ?, ?, ?, ?, ?);", values);
        
    }
    
    public void resolveOutage(MockService svc, Event svcRegainEvent) {
        resolveOutage(svc, svcRegainEvent.getDbid(), convertEventTimeToTimeStamp(svcRegainEvent.getTime()));
    }        

    public void resolveOutage(MockService svc, int eventId, Timestamp timestamp) {
        
        Object[] values = {
                new Integer(eventId),           // svcLostEventId
                timestamp, // ifLostService
                new Integer(svc.getNodeId()), // nodeId
                svc.getIpAddr(),                // ipAddr
                new Integer(svc.getId()),       // serviceID
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
                new Integer(Constants.getSeverity(e.getSeverity())),
                (e.hasNodeid() ? new Long(e.getNodeid()) : null),
                e.getInterface(),
                getServiceID(e.getService()),
                "localhost",
                "Y",
                "Y",
                e.getTticket() == null ? "" : e.getTticket().getContent(),
                Integer.valueOf(e.getTticket() == null ? "0" : e.getTticket().getState()),
        };
        e.setDbid(eventId.intValue());
        update("insert into events (" +
                "eventId, eventSource, eventUei, eventCreateTime, eventTime, eventSeverity, " +
                "nodeId, ipAddr, serviceId, eventDpName, " +
                "eventLog, eventDisplay, eventtticket, eventtticketstate) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", values);
    }
    
    public void setServiceStatus(MockService svc, char newStatus) {
        Object[] values = { String.valueOf(newStatus), new Integer(svc.getNodeId()), svc.getIpAddr(), new Integer(svc.getId()) };
        update("update ifServices set status = ? where nodeId = ? and ipAddr = ? and serviceId = ?", values);
    }

    public char getServiceStatus(MockService svc) {
        SingleResultQuerier querier = new SingleResultQuerier(this, "select status from ifServices where nodeId = ? and ipAddr = ? and serviceID = ?");
        querier.execute(new Integer(svc.getNodeId()), svc.getIpAddr(), new Integer(svc.getId()));
        String result = (String)querier.getResult();
        if (result == null || "".equals(result)) {
            return 'X';
        }
        return result.charAt(0);
    }

    public void setInterfaceStatus(MockInterface iface, char newStatus) {
        Object[] values = { String.valueOf(newStatus), new Integer(iface.getNodeId()), iface.getIpAddr() };
        update("update ipInterface set isManaged = ? where nodeId = ? and ipAddr = ?;", values);
    }
    
    public char getInterfaceStatus(MockInterface iface) {
        SingleResultQuerier querier = new SingleResultQuerier(this, "select isManaged from ipInterface where nodeId = ? and ipAddr = ?");
        querier.execute(new Integer(iface.getNodeId()), iface.getIpAddr());
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
        Object[] values = { new Integer(iface.getNodeId()), iface.getIpAddr() };
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
            public void processRow(ResultSet rs) throws SQLException {
                notifyIds.add(rs.getInt(1));
            }
        };
        loadExisting.execute(new Integer(event.getDbid()));
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
