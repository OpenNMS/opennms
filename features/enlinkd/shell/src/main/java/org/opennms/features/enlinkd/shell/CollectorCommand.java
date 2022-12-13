/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.enlinkd.shell;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.enlinkd.snmp.CdpCacheTableTracker;
import org.opennms.netmgt.enlinkd.snmp.CdpGlobalGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.CiscoVtpTracker;
import org.opennms.netmgt.enlinkd.snmp.CiscoVtpVlanTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dBasePortTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dBaseTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dStpPortTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dTpFdbTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1qTpFdbTableTracker;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

@Command(scope = "opennms", name = "enlinkd-collect", description = "Collect enlinkd snmp data")
@Service
public class CollectorCommand implements Action, Completer {

    @Reference
    private LocationAwareSnmpClient m_client;


    @Option(name = "-l", aliases = "--location", description = "Location")
    String location;

    public static CollectionTracker getByClassName(String name) {
        switch (name) {
            case CISCO_VTP:
                return new CiscoVtpTracker() {
                    @Override
                    protected void storeResult(SnmpResult res) {
                        super.storeResult(res);
                        printSnmpData();
                     }
                };
            case CISCO_VTP_VLAN_TABLE:
                return  new CiscoVtpVlanTableTracker();
            case DOT1D_BASE:
                return  new Dot1dBaseTracker() {

                    @Override
                    protected void storeResult(SnmpResult res) {
                        super.storeResult(res);
                        printSnmpData();
                    }
                };
            case DOT1D_BASE_PORT_TABLE:
                return  new Dot1dBasePortTableTracker();
            case DOT1D_STP_PORT_TABLE:
                return  new Dot1dStpPortTableTracker();
            case DOT1D_TP_FDB_TABLE:
                return new Dot1dTpFdbTableTracker();
            case DOT1Q_TP_FDB_TABLE:
                return new Dot1qTpFdbTableTracker();
            case CDP_GLOBAL_GROUP:
                return  new CdpGlobalGroupTracker() {
                    @Override
                    protected void storeResult(SnmpResult res) {
                        super.storeResult(res);
                        printSnmpData();
                    }
                };
            case CDP_CACHE_TABLE:
                return new CdpCacheTableTracker();

            default:
                break;

        }
        return null;
    }
    private final static String CISCO_VTP = "CiscoVtp";
    private final static String CISCO_VTP_VLAN_TABLE = "CiscoVtpVlanTable";
    private final static String DOT1D_BASE = "Dot1dBase";
    private final static String DOT1D_BASE_PORT_TABLE = "Dot1dBasePortTable";
    private final static String DOT1D_STP_PORT_TABLE = "Dot1dStpPortTable";
    private final static String DOT1D_TP_FDB_TABLE = "Dot1dTpFdbTable";
    private final static String DOT1Q_TP_FDB_TABLE = "Dot1qTpFdbTable";

    private final static String CDP_GLOBAL_GROUP = "CdpGlobalGroup";
    private final static String CDP_CACHE_TABLE = "CdpCacheTable";
    private static final String[] trackerClassNames = {
            CISCO_VTP,
            CISCO_VTP_VLAN_TABLE,
            DOT1D_BASE,
            DOT1D_BASE_PORT_TABLE,
            DOT1D_STP_PORT_TABLE,
            DOT1D_TP_FDB_TABLE,
            DOT1Q_TP_FDB_TABLE,
            CDP_GLOBAL_GROUP,
            CDP_CACHE_TABLE,
            "IpNetToMediaTable",
            "IsisSysObjectGroup",
            "IsisCircTable",
            "IsisISAdjTable",
            "LldpLocalGroup",
            "LldpRemTable",
            "MtxrLldpLocaTable",
            "MtxrLldpRemTable",
            "MtxrNeighborTable",
            "TimeTetraLldpRemTable",
            "MtxrLldpLocaTable",
            "MtxrLldpRemTable",
            "MtxrNeighborTable",
            "OspfGeneralGroup",
            "OspfAreaTable",
            "OspfIfTable",
            "OspfIpAddrTable",
            "OspfNbrTable"
    };

    @Argument(name = "trackerClass", description = "Tracker Collector class", required = true)
    @Completion(CollectorCommand.class)
    String className;

    @Argument(index = 1, name = "host", description = "Hostname or IP Address of the system to poll", required = true)
    String host;

    @Override
    public Void execute() throws UnknownHostException {
        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(host));
        String trackerName = className+"Tracker";
        CollectionTracker tracker = getByClassName(className);
        if (tracker == null) {
            System.out.printf("className %s, not found a suitable collectionTracker", className);
            return null;
        }
        try {
            m_client.walk(config,tracker)
                    .withDescription(trackerName)
                    .withLocation(location)
                    .execute()
                    .get();
        } catch (final InterruptedException | ExecutionException e) {
            System.out.println("(Empty collection set)");
        }

        return null;
    }

    @Override
    public int complete(Session session, CommandLine commandLine, List<String> candidates) {
        StringsCompleter serviceNames = new StringsCompleter();
        serviceNames.getStrings().addAll(Arrays.asList(trackerClassNames));
        return serviceNames.complete(session, commandLine, candidates);
    }
}
