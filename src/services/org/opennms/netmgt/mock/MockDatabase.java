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
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * In memory database comparable to the postgres database that can be used for unit
 * testing.  Can be populated from a MockNetwork
 * @author brozow
 */
public class MockDatabase {


    public MockDatabase() {
        try {
            Class.forName("org.hsqldb.jdbcDriver" );
        } catch (Exception e) {
            throw new RuntimeException("Unable to locate hypersonic driver");
        }
        create();
    }
    
    public void create() {
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
        
        update("create table ifServices (nodeID          integer, " +
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
        
    }
    
    public void drop() {
        // order matters here because of referential integrity constraints
        update("drop table outages if exists");
        update("drop table events if exists");
        update("drop table ifServices if exists");
        update("drop table service");
        update("drop table ipInterface if exists");
        update("drop table node if exists");
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
        
        Object[] values = { new Integer(svc.getNodeId()), svc.getIpAddr(), new Integer(svc.getId()) };
        update("insert into ifServices (nodeID, ipAddr, serviceID) values (?, ?, ?)", values);
    }
    
    public int countRows(String sql) {
        Querier querier = new Querier(this, sql);
        querier.execute();
        return querier.getCount();
    }

    private boolean serviceDefined(String svcName) {
        Querier querier = new Querier(this, "select serviceId from service where serviceName = ?");
        querier.execute(svcName);
        return querier.getCount() > 0;
    }

    
}
