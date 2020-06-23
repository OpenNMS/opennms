/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.netmgt.events.api.EventWriter;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockPathOutage;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockVisitor;
import org.opennms.netmgt.mock.MockVisitorAdapter;
import org.opennms.netmgt.mock.Outage;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
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

    public MockDatabase(boolean createNow) throws Exception {
        this(null, createNow);
    }

    public MockDatabase(String name, boolean createNow) throws Exception {
        super(name);

        setPopulateSchema(true);

        setClassName(MockDatabase.class.getName());
        setMethodName("MockDatabase constructor");
        setTestDetails("I do not know who called me.... which is sad. Will you be my friend?");

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
            
            @Override
            public void visitPathOutage(MockPathOutage out) {
            	writePathOutage(out);
            }
        };
        network.visit(dbCreater);
        
        
        
        getJdbcTemplate().queryForObject("SELECT setval('nodeNxtId', max(nodeid)) FROM node", Integer.class);
        
    }
    
    public void writeNode(MockNode node) {
        LOG.info("Inserting node \"{}\" into database with ID {}", node.getLabel(), node.getNodeId());
        Object[] values = { node.getLocation(), Integer.valueOf(node.getNodeId()), node.getLabel(), new Timestamp(System.currentTimeMillis()), "A" };
        update("insert into node (location, nodeID, nodeLabel, nodeCreateTime, nodeType) values (?, ?, ?, ?, ?);", values);
    }

    public void writeInterface(MockInterface iface) {
        LOG.info("Inserting interface into database with IP address {}", iface.getAddress());
        Integer snmpInterfaceId = writeSnmpInterface(iface);
        Object[] values = { iface.getId(), iface.getNodeId(), str(iface.getAddress()), snmpInterfaceId, (iface.getIfIndex() == 1 ? "P" : "N"), "M" };
        update("insert into ipInterface (id, nodeID, ipAddr, snmpInterfaceId, isSnmpPrimary, isManaged) values (?, ?, ?, ?, ?, ?);", values);
    }

    /**
     * @return The ID of the inserted snmpinterface record
     */
    public Integer writeSnmpInterface(MockInterface iface) {
        Integer nextId = getNextSnmpInterfaceId();
        LOG.info("Inserting into snmpInterface {} {} {} {}", nextId, Integer.valueOf(iface.getNodeId()), iface.getIfAlias(), iface.getIfIndex() );
        Object[] values = { nextId, Integer.valueOf(iface.getNodeId()), iface.getIfAlias(), iface.getIfAlias(), iface.getIfIndex() };
        update("insert into snmpInterface (id, nodeID, snmpifAlias, snmpifDescr, snmpIfIndex) values (?, ?, ?, ?, ?);", values);
        return nextId;
    }

    public void writeService(MockService svc) {
        String svcName = svc.getSvcName();
        Integer serviceId = getServiceID(svcName);
        if (serviceId == null) {
            svc.setSvcId(getNextServiceId());
            Object[] svcValues = { svc.getSvcId(), svcName };
            LOG.info("Inserting service \"{}\" into database with ID {}", svcName, svc.getSvcId());
            update("insert into service (serviceID, serviceName) values (?, ?);", svcValues);
        } else {
            svc.setSvcId(serviceId);
        }
        String status = svc.getMgmtStatus().toDbString();
        Object[] values = { svc.getId(), svc.getInterface().getId(), Integer.valueOf(svc.getSvcId()), status };
        update("insert into ifServices (id, ipInterfaceId, serviceID, status) values (?, ?, ?, ?);", values);
    }

    public void writePathOutage(MockPathOutage out) {
        LOG.info("Inserting into pathoutage {} {} {}" ,out.getNodeId(), InetAddressUtils.str(out.getIpAddress()), out.getServiceName());
        Object[] values = { Integer.valueOf(out.getNodeId()), InetAddressUtils.str(out.getIpAddress()), out.getServiceName() };
        update("insert into pathoutage (nodeid, criticalpathip, criticalpathservicename) values (?, ?, ?);", values);
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
    
    public String getNextSnmpInterfaceIdStatement() {
        return getNextSequenceValStatement("opennmsnxtid");

    }
    
    public Integer getNextSnmpInterfaceId() {
        return getNextId(getNextSnmpInterfaceIdStatement());
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
        Object[] values = { Integer.valueOf(svc.getNodeId()), svc.getIpAddr(), Integer.valueOf(svc.getSvcId()) };
        return countRows("select * from outages, ifservices, ipinterface, node where outages.ifserviceid = ifservices.id and ifservices.ipinterfaceid = ipinterface.id and ipinterface.nodeid = node.nodeid and node.nodeId = ? and ipinterface.ipAddr = ? and ifservices.serviceId = ?"+critSql, values);
    }

    public void createOutage(MockService svc, Event svcLostEvent) {
        createOutage(svc, svcLostEvent.getDbid(), new Timestamp(svcLostEvent.getTime().getTime()));
    }

    public void createOutage(MockService svc, int eventId, Timestamp time) {
        Object[] values = {
                getNextOutageId(), // outageID
                svc.getId(), // service ID
                Integer.valueOf(eventId),           // svcLostEventId
                time, // ifLostService
               };
        
        update("insert into outages (outageId, ifServiceId, svcLostEventId, ifLostService) values (?, ?, ?, ?);", values);
        
    }
    
    public void resolveOutage(MockService svc, Event svcRegainEvent) {
        resolveOutage(svc, svcRegainEvent.getDbid(), new Timestamp(svcRegainEvent.getTime().getTime()));
    }        

    public void resolveOutage(MockService svc, int eventId, Timestamp timestamp) {

        Object[] values = {
                Integer.valueOf(eventId),           // svcLostEventId
                timestamp, // ifLostService
                Integer.valueOf(svc.getId()) // ifServiceId
               };

        // TODO: Alert if more than 1 row is updated, should not be possible with index in place
        update("UPDATE outages set svcRegainedEventID = ?, ifRegainedService = ? WHERE ifServiceId = ? AND ifRegainedService IS NULL", values);
    }

    /**
     * @param e
     */
    @Override
    public void writeEvent(Event e) {
        Integer eventId = getNextEventId();
        
        if (e.getCreationTime() == null) {
            e.setCreationTime(new Date());
        }
        
        Object[] values = {
                eventId,
                e.getSource(),
                e.getUei(),
                new Timestamp(e.getCreationTime().getTime()),
                new Timestamp(e.getTime().getTime()),
                Integer.valueOf(OnmsSeverity.get(e.getSeverity()).getId()),
                (e.hasNodeid() ? new Long(e.getNodeid()) : null),
                e.getInterface(),
                getServiceID(e.getService()),
                e.getDistPoller() == null ? "00000000-0000-0000-0000-000000000000" : e.getDistPoller(),
                "Y",
                "Y",
                e.getTticket() == null ? "" : e.getTticket().getContent(),
                Integer.valueOf(e.getTticket() == null ? "0" : e.getTticket().getState()),
                e.getLogmsg() == null? null : e.getLogmsg().getContent()
        };
        e.setDbid(eventId);
        update("insert into events (" +
                "eventId, eventSource, eventUei, eventCreateTime, eventTime, eventSeverity, " +
                "nodeId, ipAddr, serviceId, systemId, " +
                "eventLog, eventDisplay, eventtticket, eventtticketstate, eventlogmsg) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", values);

        if (e.getParmCollection() != null || e.getParmCollection().size() > 0) {
            for (final Parm parm : e.getParmCollection()) {
                Object[] parmValues = {
                        eventId,
                        parm.getParmName(),
                        parm.getValue().getContent(),
                        parm.getValue().getType()
                };
                update("insert into event_parameters (eventid, name, value, type) values (?, ?, ?, ?)", parmValues);
            }
        }
    }
    
    public void setServiceStatus(MockService svc, char newStatus) {
        Object[] values = { String.valueOf(newStatus), Integer.valueOf(svc.getNodeId()), svc.getIpAddr(), Integer.valueOf(svc.getSvcId()) };
        update("update ifservices set status = ? from ipInterface inner join node on ipInterface.nodeId = node.nodeId where ifServices.ipInterfaceId = ipInterface.id and node.nodeId = ? and ipInterface.ipAddr = ? and ifServices.serviceId = ?", values);
    }

    public char getServiceStatus(MockService svc) {
        SingleResultQuerier querier = new SingleResultQuerier(this, "select ifServices.status as status from ifServices, ipInterface, node where ifServices.ipInterfaceId = ipInterface.id and ipInterface.ipAddr = ? and ipInterface.nodeId = node.nodeId and node.nodeId = ? and serviceID = ?");
        querier.execute(svc.getIpAddr(), Integer.valueOf(svc.getNodeId()), Integer.valueOf(svc.getSvcId()));
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
        return countRows("select * from outages, ifServices, ipInterface, node where outages.ifServiceId = ifServices.id and ifServices.ipInterfaceId = ipInterface.id and ipInterface.nodeId = node.nodeId and node.nodeId = ? and ipInterface.ipAddr = ? "+critSql, values);
    }
    
    public boolean hasOpenOutage(MockService svc) {
        return countOpenOutagesForService(svc) > 0;
    }
    
    public Collection<Outage> getOutages() {
        return getOutages(null, new Object[0]);
    }
    
    public Collection<Outage> getOutages(String criteria, Object... values) {
        String critSql = (criteria == null ? "" : " and "+criteria);
        final List<Outage> outages = new LinkedList<>();
        Querier loadExisting = new Querier(this, "select * from outages, ifServices, ipInterface, node, service where outages.ifServiceId = ifServices.id and ifServices.ipInterfaceId = ipInterface.id and ipInterface.nodeId = node.nodeId and ifServices.serviceId = service.serviceId"+critSql) {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Outage outage = new Outage(rs.getInt("nodeId"), rs.getString("ipAddr"), rs.getInt("serviceId"));
                outage.setServiceName(rs.getString("serviceName"));
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
    
    public Collection<Outage> getOpenOutages(MockService svc) {
        return getOutages("outages.ifServiceId = ? and ifRegainedService is null", svc.getId());
    }
    
    public Collection<Outage> getOutages(MockService svc) {
        return getOutages("outages.ifServiceId = ?", svc.getId());
    }
    
    public Collection<Outage> getClosedOutages(MockService svc) {
        return getOutages("outages.ifServiceId = ? and ifRegainedService is not null", svc.getId());
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
        // Unnecessary now that nodeId field is removed from ifServices table
        // update("update ifServices set nodeId = ? where nodeId = ? and ipAddr = ?", newNode, oldNode, ipAddr);
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
        final List<Integer> notifyIds = new LinkedList<>();
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
