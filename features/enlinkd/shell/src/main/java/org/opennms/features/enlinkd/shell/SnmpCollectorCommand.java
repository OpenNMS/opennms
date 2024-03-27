/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import org.opennms.netmgt.enlinkd.snmp.IpNetToMediaTableTracker;
import org.opennms.netmgt.enlinkd.snmp.IsisCircTableTracker;
import org.opennms.netmgt.enlinkd.snmp.IsisISAdjTableTracker;
import org.opennms.netmgt.enlinkd.snmp.IsisSysObjectGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.LldpLocalGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.LldpLocalTableTracker;
import org.opennms.netmgt.enlinkd.snmp.LldpRemTableTracker;
import org.opennms.netmgt.enlinkd.snmp.MtxrLldpRemTableTracker;
import org.opennms.netmgt.enlinkd.snmp.MtxrNeighborTableTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfAreaTableTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfGeneralGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfIfTableTracker;
import org.opennms.netmgt.enlinkd.snmp.OspfNbrTableTracker;
import org.opennms.netmgt.enlinkd.snmp.TimeTetraLldpRemTableTracker;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

/**
 * Show Collected Snmp Data on Node using Enlinkd Snmp Collector Classes.
 * Log into console via: ssh -p 8101 admin@localhost
 * Install: feature:install opennms-enlinkd-shell
 * Usage: type 'opennms:enlinkd-snmp-collect -l {location} CiscoVtp {ipAddress | hostname}' in karaf console
 */
@Command(scope = "opennms", name = "enlinkd-snmp-collect", description = "Collect enlinkd snmp data")
@Service
public class SnmpCollectorCommand implements Action, Completer {

    @Reference
    private LocationAwareSnmpClient m_client;

    @Option(name = "-l", aliases = "--location", description = "Location")
    String location;

    public static CollectionTracker getByClassName(String name) {
        switch (name) {
            case CISCO_VTP:
                return new CiscoVtpTracker();
            case CISCO_VTP_VLAN_TABLE:
                return  new CiscoVtpVlanTableTracker();
            case DOT1D_BASE:
                return  new Dot1dBaseTracker();
            case DOT1D_BASE_PORT_TABLE:
                return  new Dot1dBasePortTableTracker();
            case DOT1D_STP_PORT_TABLE:
                return  new Dot1dStpPortTableTracker();
            case DOT1D_TP_FDB_TABLE:
                return new Dot1dTpFdbTableTracker();
            case DOT1Q_TP_FDB_TABLE:
                return new Dot1qTpFdbTableTracker();
            case CDP_GLOBAL_GROUP:
                return  new CdpGlobalGroupTracker();
            case CDP_CACHE_TABLE:
                return new CdpCacheTableTracker();
            case IP_NET_TO_MEDIA_TABLE:
                return new IpNetToMediaTableTracker();
            case ISIS_SYS_OBJECT_GROUP:
                return new IsisSysObjectGroupTracker();
            case ISIS_CIRC_TABLE:
                return new IsisCircTableTracker();
            case ISIS_IS_ADJ_TABLE:
                return new IsisISAdjTableTracker();
            case LLDP_LOCAL_GROUP:
                return new LldpLocalGroupTracker();
            case LLDP_LOCAL_TABLE:
                return  new LldpLocalTableTracker();
            case LLDP_REM_TABLE:
                return new LldpRemTableTracker();
            case MTXR_LLDP_REM_TABLE:
                return new MtxrLldpRemTableTracker();
           case MTXR_NEIGHBOR_TABLE:
                return new MtxrNeighborTableTracker();
            case TIME_TETRA_LLDP_REM_TABLE:
                return new TimeTetraLldpRemTableTracker();
            case OSPF_GENERAL_GROUP:
                return new OspfGeneralGroupTracker();
            case OSPF_AREA_TABLE:
                return new OspfAreaTableTracker();
            case OSPF_IF_TABLE:
                return new OspfIfTableTracker();
            case OSPF_NBR_TABLE:
                return new OspfNbrTableTracker();
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

    private final static String IP_NET_TO_MEDIA_TABLE = "IpNetToMediaTable";

    private final static String ISIS_SYS_OBJECT_GROUP = "IsisSysObjectGroup";
    private final static String ISIS_CIRC_TABLE = "IsisCircTable";
    private final static String ISIS_IS_ADJ_TABLE = "IsisISAdjTable";

    private final static String OSPF_GENERAL_GROUP = "OspfGeneralGroup";
    private final static String OSPF_AREA_TABLE = "OspfAreaTable";
    private final static String OSPF_IF_TABLE = "OspfIfTable";
    private final static String OSPF_NBR_TABLE = "OspfNbrTable";

    private final static String LLDP_LOCAL_GROUP = "LldpLocalGroup";
    private final static String LLDP_LOCAL_TABLE = "LldpLocalTable";
    private final static String LLDP_REM_TABLE = "LldpRemTable";

    private final static String MTXR_LLDP_REM_TABLE = "MtxrLldpRemTable";
    private final static String MTXR_NEIGHBOR_TABLE = "MtxrNeighborTable";

    private final static String TIME_TETRA_LLDP_REM_TABLE = "TimeTetraLldpRemTable";
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
            IP_NET_TO_MEDIA_TABLE,
            ISIS_SYS_OBJECT_GROUP,
            ISIS_CIRC_TABLE,
            ISIS_IS_ADJ_TABLE,
            LLDP_LOCAL_GROUP,
            LLDP_LOCAL_TABLE,
            LLDP_REM_TABLE,
            MTXR_LLDP_REM_TABLE,
            MTXR_NEIGHBOR_TABLE,
            TIME_TETRA_LLDP_REM_TABLE,
            OSPF_GENERAL_GROUP,
            OSPF_AREA_TABLE,
            OSPF_IF_TABLE,
            OSPF_NBR_TABLE
    };

    @Argument(name = "trackerClass", description = "Tracker Collector class", required = true)
    @Completion(SnmpCollectorCommand.class)
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

        if (tracker instanceof AggregateTracker) {
            ((AggregateTracker)  tracker).printSnmpData();
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
