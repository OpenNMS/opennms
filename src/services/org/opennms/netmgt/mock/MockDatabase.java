//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.hsqldb.Server;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DbConnectionFactory;
import org.opennms.netmgt.eventd.db.Constants;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.utils.Updater;
import org.opennms.netmgt.xml.event.Event;

/**
 * In memory database comparable to the postgres database that can be used for unit
 * testing.  Can be populated from a MockNetwork
 * @author brozow
 */
public class MockDatabase implements DbConnectionFactory, EventWriter {


    private Server m_server;

    public MockDatabase() {
        try {
            Class.forName("org.hsqldb.jdbcDriver" );
        } catch (Exception e) {
            throw new RuntimeException("Unable to locate hypersonic driver");
        }
        create();
    }
    
    public void create() {
        update("shutdown");
        update("create table node (" +
                   "nodeID integer, " +
                   "dpName      varchar(12)," +
                   "nodeCreateTime  timestamp not null," +
                   "nodeParentID    integer," +
                   "nodeType    char(1)," +
                   "nodeSysOID  varchar(256)," +
                   "nodeSysName varchar(256)," +
                   "nodeSysDescription  varchar(256)," +
                   "nodeSysLocation varchar(256)," +
                   "nodeSysContact  varchar(256)," +
                   "nodeLabel   varchar(256)," +
                   "nodeLabelSource char(1)," +
                   "nodeNetBIOSName varchar(16)," +
                   "nodeDomainName  varchar(16)," +
                   "operatingSystem varchar(64)," +
                   "lastCapsdPoll   timestamp," +
                   //"constraint fk_dpName foreign key (dpName) references distPoller," +
                   "constraint pk_nodeID primary key (nodeID)" +
        ")");
        
        update("create table ipInterface (" +
                   "nodeID integer, " +
                   "ipAddr varchar(16) not null, " +
                   "ifIndex         integer," +
                   "ipHostname      varchar(256)," +
                   "ipStatus        integer," +
                   "ipLastCapsdPoll     timestamp," +
                   "isSnmpPrimary           char(1)," +
                   "isManaged char(1), " +
                   "constraint fk_nodeID1 foreign key (nodeID) references node ON DELETE CASCADE" +
        ");");
        
        update("create table service (" +
                   "serviceID integer, " +
                   "serviceName varchar(32) not null, " +
                   "constraint pk_serviceID primary key (serviceID)" +
        ")");
        
        update("create table ifServices (" +
                   "nodeID          integer, " +
                   "ipAddr          varchar(16) not null," +
                   "ifIndex         integer," +
                   "serviceID       integer," +
                   "lastGood        timestamp," +
                   "lastFail        timestamp," +
                   "qualifier       char(16)," +
                   "status              char(1)," +
                   "source          char(1)," +
                   "notify                  char(1), " +
                   "constraint fk_nodeID3 foreign key (nodeID) references node ON DELETE CASCADE," +
                   "constraint fk_serviceID1 foreign key (serviceID) references service ON DELETE CASCADE" +
        ");");
        
        update("create table events (" +
                   "eventID         integer," +
                   "eventUei        varchar(256) not null," +
                   "nodeID          integer," +
                   "eventTime       timestamp not null," +
                   "eventHost       varchar(256)," +
                   "eventSource     varchar(128) not null," +
                   "ipAddr          varchar(16)," +
                   "eventDpName     varchar(12) not null," +
                   "eventSnmphost       varchar(256)," +
                   "serviceID       integer," +
                   "eventSnmp       varchar(256)," +
                   "eventParms      longvarchar," +
                   "eventCreateTime     timestamp not null," +
                   "eventDescr      varchar(4000)," +
                   "eventLoggroup       varchar(32)," +
                   "eventLogmsg     varchar(256)," +
                   "eventSeverity       integer not null," +
                   "eventPathOutage     varchar(1024)," +
                   "eventCorrelation    varchar(1024)," +
                   "eventSuppressedCount    integer," +
                   "eventOperInstruct   varchar(1024)," +
                   "eventAutoAction     varchar(256)," +
                   "eventOperAction     varchar(256)," +
                   "eventOperActionMenuText varchar(64)," +
                   "eventNotification   varchar(128)," +
                   "eventTticket        varchar(128)," +
                   "eventTticketState   integer," +
                   "eventForward        varchar(256)," +
                   "eventMouseOverText  varchar(64)," +
                   "eventLog        char(1) not null," +
                   "eventDisplay        char(1) not null," +
                   "eventAckUser        varchar(256)," +
                   "eventAckTime        timestamp," +
                   "constraint pk_eventID primary key (eventID)," +
                   "constraint fk_nodeID6 foreign key (nodeID) references node ON DELETE CASCADE" +
        ")");
        
        update("create table outages (" +
                   "outageID        integer," +
                   "svcLostEventID      integer," +
                   "svcRegainedEventID  integer," +
                   "nodeID          integer," +
                   "ipAddr          varchar(16) not null," +
                   "serviceID       integer," +
                   "ifLostService       timestamp not null," +
                   "ifRegainedService   timestamp," +
                   "constraint pk_outageID primary key (outageID)," +
                   "constraint fk_eventID1 foreign key (svcLostEventID) references events (eventID) ON DELETE CASCADE," +
                   "constraint fk_eventID2 foreign key (svcRegainedEventID) references events (eventID) ON DELETE CASCADE," +
                   "constraint fk_nodeID4 foreign key (nodeID) references node (nodeID) ON DELETE CASCADE," +
                   "constraint fk_serviceID2 foreign key (serviceID) references service (serviceID) ON DELETE CASCADE" +
        ")");
        
        update("create table notifications (" + 
                "       textMsg      varchar(4000) not null," + 
                "       numericMsg   varchar(256)," + 
                "       notifyID        integer," + 
                "       pageTime     timestamp," + 
                "       respondTime  timestamp," + 
                "       answeredBy   varchar(256)," + 
                "       nodeID      integer," + 
                "       interfaceID  varchar(16)," + 
                "       serviceID    integer," + 
                "       eventID      integer," + 
                "       eventUEI     varchar(256) not null," + 
                "                   constraint pk_notifyID primary key (notifyID)," + 
                "                   constraint fk_nodeID7 foreign key (nodeID) references node (nodeID) ON DELETE CASCADE," + 
                "                   constraint fk_eventID3 foreign key (eventID) references events (eventID) ON DELETE CASCADE" + 
                "       )");
        
        update("create table usersNotified (\n" + 
                "        userID          varchar(256) not null," + 
                "        notifyID        integer," + 
                "        notifyTime      timestamp," + 
                "        media           varchar(32)," + 
                "        contactinfo     varchar(64)," + 
                "           constraint fk_notifID2 foreign key (notifyID) references notifications (notifyID) ON DELETE CASCADE" + 
                ");");
        
        update("create sequence outageNxtId start with 1");
        update("create sequence eventNxtId start with 1");
        update("create sequence notifNxtId start with 1");
        update("create table seqQueryTable (row integer)");
        update("insert into seqQueryTable (row) values (0)");
        
    }
    
    public void startServer() {
        m_server = new Server();
        m_server.setDatabasePath(0, "mem:test");
        synchronized(m_server) {
            m_server.start();
        }
        
    }
    
    public void drop() {
        if (m_server != null)
            m_server.stop();
        update("shutdown");
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
        
    }
    
    public void writeNode(MockNode node) {
        Object[] values = { new Integer(node.getNodeId()), node.getLabel(), new Timestamp(System.currentTimeMillis()) };
        update("insert into node (nodeID, nodeLabel, nodeCreateTime) values (?, ?, ?)", values);
        
    }

    public Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:test", "sa", "");
        return c;
    }
    
     public void update(String sql) {
        update(sql, new Object[0]);
    }
    
    public void update(String stmt, Object[] values) {
        Updater updater = new Updater(this, stmt);
        updater.execute(values);
    }

    public void writeInterface(MockInterface iface) {
        Object[] values = { new Integer(iface.getNodeId()), iface.getIpAddr() };
        update("insert into ipInterface (nodeID, ipAddr) values (?, ?)", values);
    }

    public void writeService(MockService svc) {
        String svcName = svc.getName();
        if (!serviceDefined(svcName)) {
            Object[] svcValues = { new Integer(svc.getId()), svcName };
            update("insert into service (serviceID, serviceName) values (?, ?)", svcValues);
        }
        
        Object[] values = { new Integer(svc.getNodeId()), svc.getIpAddr(), new Integer(svc.getId()), "A" };
        update("insert into ifServices (nodeID, ipAddr, serviceID, status) values (?, ?, ?, ?)", values);
    }
    
    public int countRows(String sql) {
        return countRows(sql, new Object[0]);
    }
    
    public int countRows(String sql, Object[] values) {
        Querier querier = new Querier(this, sql);
        querier.execute(values);
        return querier.getCount();
    }

    private boolean serviceDefined(String svcName) {
        Querier querier = new Querier(this, "select serviceId from service where serviceName = ?");
        querier.execute(svcName);
        return querier.getCount() > 0;
    }
    
    public String getNextOutageIdStatement() {
        return "select next value for outageNxtId from seqQueryTable";
    }
    
    class SingleResultQuerier extends Querier {
        SingleResultQuerier(MockDatabase db, String sql) {
            super(db, sql);
        }
        
        private Object m_result;
        
        public Object getResult() { return m_result; }
        
        public void processRow(ResultSet rs) throws SQLException {
            m_result = rs.getObject(1);
        }
        
    };

    public Integer getNextOutageId() {
        return getNextId(getNextOutageIdStatement());
        
    }
    
    private Integer getNextId(String nxtIdStmt) {
        SingleResultQuerier querier = new SingleResultQuerier(this, nxtIdStmt);
        querier.execute();
        return (Integer)querier.getResult();
    }

    public String getNextEventIdStatement() {
        return "select next value for eventNxtId from seqQueryTable;";
    }
    
    public Integer getNextEventId() {
        return getNextId(getNextEventIdStatement());
    }
    
    public Integer getServiceID(String serviceName) {
        if (serviceName == null) return new Integer(-1);
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
        
        
        Object[] values = {
                getNextOutageId(), // outageID
                new Integer(svcLostEvent.getDbid()),           // svcLostEventId
                new Integer(svc.getNodeId()), // nodeId
                svc.getIpAddr(),                // ipAddr
                new Integer(svc.getId()),       // serviceID
                convertEventTimeToTimeStamp(svcLostEvent.getTime()), // ifLostService
               };
        
        update("insert into outages (outageId, svcLostEventId, nodeId, ipAddr, serviceId, ifLostService) values (?, ?, ?, ?, ?, ?)", values);
        
    }
    
    public void resolveOutage(MockService svc, Event svcRegainEvent) {
        
        
        Object[] values = {
                new Integer(svcRegainEvent.getDbid()),           // svcLostEventId
                convertEventTimeToTimeStamp(svcRegainEvent.getTime()), // ifLostService
                new Integer(svc.getNodeId()), // nodeId
                svc.getIpAddr(),                // ipAddr
                new Integer(svc.getId()),       // serviceID
               };
        
        update("UPDATE outages set svcRegainedEventID=?, ifRegainedService=? where (nodeid = ? AND ipAddr = ? AND serviceID = ? and (ifRegainedService IS NULL))", values);
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
        };
        e.setDbid(eventId.intValue());
        update("insert into events (eventId, eventSource, eventUei, eventCreateTime, eventTime, eventSeverity, nodeId, ipAddr, serviceId, eventDpName, eventLog, eventDisplay) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", values);
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
        update("update ipInterface set isManaged = ? where nodeId = ? and ipAddr = ?", values);
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
    
    public Collection getOutages() {
        return getOutages(null, new Object[0]);
    }
    
    public Collection getOutages(String criteria, Object[] values) {
        String critSql = (criteria == null ? "" : " where "+criteria);
        final List outages = new LinkedList();
        Querier loadExisting = new Querier(this, "select * from outages"+critSql) {
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
        return outages;
    }
    
    public Collection getOpenOutages(MockService svc) {
        Object[] values = { new Integer(svc.getNodeId()), svc.getIpAddr(), new Integer(svc.getId()) };
        return getOutages("nodeId = ? and ipAddr = ? and serviceID = ? and ifRegainedService is null", values);
    }
    
    public Collection getOutages(MockService svc) {
        Object[] values = { new Integer(svc.getNodeId()), svc.getIpAddr(), new Integer(svc.getId()) };
        return getOutages("nodeId = ? and ipAddr = ? and serviceID = ?", values);
    }
    
    public Collection getClosedOutages(MockService svc) {
        Object[] values = { new Integer(svc.getNodeId()), svc.getIpAddr(), new Integer(svc.getId()) };
        return getOutages("nodeId = ? and ipAddr = ? and serviceID = ? and ifRegainedService is not null", values);
    }

    /**
     * @param ipAddr
     * @param nodeId
     * @param nodeId2
     */
    public void reparentInterface(String ipAddr, int oldNode, int newNode) {
        Object[] values = { new Integer(newNode), new Integer(oldNode), ipAddr };
        update("update ipInterface set nodeId = ? where nodeId = ? and ipAddr = ?", values);
        update("update ifServices set nodeId = ? where nodeId = ? and ipAddr = ?", values);
    }

    /**
     * @return
     */
    public String getNextNotifIdSql() {
        return "select next value for notifNxtId from seqQueryTable;";
    }
    

}
