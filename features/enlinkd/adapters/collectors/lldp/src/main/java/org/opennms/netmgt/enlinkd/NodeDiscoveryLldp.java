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
package org.opennms.netmgt.enlinkd;

import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.snmp.LldpLocPortGetter;
import org.opennms.netmgt.enlinkd.snmp.LldpLocalGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.LldpRemTableTracker;
import org.opennms.netmgt.enlinkd.snmp.LldpSnmpUtils;
import org.opennms.netmgt.enlinkd.snmp.MtxrLldpRemTableTracker;
import org.opennms.netmgt.enlinkd.snmp.LldpLocalTableTracker;
import org.opennms.netmgt.enlinkd.snmp.MtxrNeighborTableTracker;
import org.opennms.netmgt.enlinkd.snmp.TimeTetraLldpLocPortGetter;
import org.opennms.netmgt.enlinkd.snmp.TimeTetraLldpRemTableTracker;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public final class NodeDiscoveryLldp extends NodeCollector {
    private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryLldp.class);

    private final static String DW_SYSOID=".1.3.6.1.4.1.7262.2.4";
    private static final String DW_NULL_CHASSIS_ID="cf";
    private static final String DW_NULL_SYSOID_ID="NuDesign";

    private static final String TIMETETRA_SYSOID=".1.3.6.1.4.1.6527";

    private static final String MTXR_SYSOID=".1.3.6.1.4.1.14988";

    private final LldpTopologyService m_lldpTopologyService;
    /**
     * Constructs a new SNMP collector for Lldp Node Discovery. 
     * The collection does not occur until the
     * <code>collect</code> method is invoked.
     * 
     *
     */
    public NodeDiscoveryLldp(
            final NodeCollectionGroupLldp group,
            final Node node,
            final int priority) {
        super(group.getLocationAwareSnmpClient(), node, priority);
    	m_lldpTopologyService = group.getLldpTopologyService();
    }

    public void collect() {

    	final Date now = new Date(); 

        final LldpLocalGroupTracker lldpLocalGroup = new LldpLocalGroupTracker();
        SnmpAgentConfig peer = getSnmpAgentConfig();

        try {
            getLocationAwareSnmpClient().walk(peer,
                          lldpLocalGroup)
                          .withDescription("lldpLocalGroup")
                          .withLocation(getLocation())
                          .execute()
                          .get();
        } catch (ExecutionException e) {
            LOG.info("run: node [{}]: ExecutionException: LLDP_MIB not supported {}", 
                     getNodeId(), e.getMessage());
                return;
        } catch (final InterruptedException e) {
            LOG.info("run: node [{}]: InterruptedException: LLDP_MIB not supported {}", 
                     getNodeId(), e.getMessage());
                return;
        }
        
        if (lldpLocalGroup.getLldpLocChassisid() == null ) {
            if (walkMtrx(peer,lldpLocalGroup.getLldpLocSysname())) {
                m_lldpTopologyService.reconcile(getNodeId(), now);
                return;
            }
            LOG.info("run: node[{}]: LLDP_MIB not supported",
                    getNodeId());
            return;
        }
        LOG.debug( "run: node[{}]: lldp identifier : {}",
    				getNodeId(),
    				lldpLocalGroup.getLldpElement().getLldpChassisId());

        m_lldpTopologyService.store(getNodeId(),
                lldpLocalGroup.getLldpElement());

        if (isInactiveDragonWave(lldpLocalGroup)) {
            m_lldpTopologyService.reconcile(getNodeId(),now);
            return;
        }

        if (!walkLldpRemTable(peer)) {
            if (getSysoid() != null && getSysoid().startsWith(TIMETETRA_SYSOID)) {
                walkTimeTetra(peer);
            }
        }
        m_lldpTopologyService.reconcile(getNodeId(), now);
    }

    private boolean isInactiveDragonWave(LldpLocalGroupTracker lldpLocalGroup) {
        if (getSysoid() == null || getSysoid().equals(DW_SYSOID) ) {
            if (lldpLocalGroup.getLldpLocChassisid().toHexString().equals(DW_NULL_CHASSIS_ID) &&
                    lldpLocalGroup.getLldpLocChassisidSubType().intValue() == LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT.getValue()) {
                LOG.info( "run: node[{}]: address {}. lldp identifier : {}. lldp not active for Dragon Wave Device.",
                        getNodeId(),
                        getPrimaryIpAddressString(),
                        lldpLocalGroup.getLldpElement());
                return true;
            }

            if (lldpLocalGroup.getLldpLocSysname().equals(DW_NULL_SYSOID_ID) ) {
                LOG.info( "run: node[{}]: lldp identifier : {}. lldp not active for Dragon Wave Device.",
                        getNodeId(),
                        lldpLocalGroup.getLldpElement());
                return true;
            }
        }

        return false;
    }

    private boolean walkLldpRemTable(SnmpAgentConfig peer) {
        List<LldpRemTableTracker.LldpRemRow> links = new ArrayList<>();
        LldpRemTableTracker lldpRemTable = new LldpRemTableTracker() {

            public void processLldpRemRow(final LldpRemRow row) {
                links.add(row);
            }
        };
        try {
            getLocationAwareSnmpClient().walk(peer,
                            lldpRemTable)
                    .withDescription("lldpRemTable")
                    .withLocation(getLocation())
                    .execute()
                    .get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: {}",
                    getNodeId(), e.getMessage());
            return false;
        } catch (final InterruptedException e) {
            LOG.debug("run: node [{}]: InterruptedException: {}",
                    getNodeId(), e.getMessage());
            return false;
        }
        if (links.isEmpty()) {
            LOG.info("run: no remote table entry found walking LLDP-MIB");
            return false;
        }
        LOG.info("run: {} remote table entry found walking LLDP-MIB", links.size());
        storeLldpLinks(links,
                new LldpLocPortGetter(peer,
                        getLocationAwareSnmpClient(),
                        getLocation()));
        return true;
    }

    private boolean walkMtrx(SnmpAgentConfig peer, String sysname) {
        if (getSysoid() == null || !getSysoid().startsWith(MTXR_SYSOID)) {
            return false;
        }

        final Map<Integer, LldpLocalTableTracker.LldpLocalPortRow> mtxrLldpLocalPortMap = new HashMap<>();
        LldpLocalTableTracker mtxrLldpLocalTable = new LldpLocalTableTracker() {
            @Override
            public void processLldpLocPortRow(final LldpLocalPortRow row) {
                LOG.debug("processLldpLocPortRow: mtxrIndex {} -> {} {} {}", row.getMtxrIndex(), row.getLldpLocalPortIdSubtype(), row.getLldpLocPortId(), row.getLldpLocPortDesc());
                mtxrLldpLocalPortMap.put(row.getMtxrIndex(), row);
            }
        };

        final Map<Integer, Integer> mtxrNeighborMap = new HashMap<>();
        MtxrNeighborTableTracker mtxrNeighborTable = new MtxrNeighborTableTracker() {
            @Override
            public void processMtxrIndexPortRow(final MtxrNeighborRow row) {
                mtxrNeighborMap.put(row.getMtxrNeighborIndex(), row.getMtxrNeighborInterfaceId());
            }
        };

        final List<MtxrLldpRemTableTracker.MtxrLldpRemRow> mtxrlldprowss = new ArrayList<>();
        MtxrLldpRemTableTracker mtxrLldpRemTable = new MtxrLldpRemTableTracker() {

            public void processMtxrLldpRemRow(final MtxrLldpRemRow row) {
                mtxrlldprowss.add(row);
            }
        };

        try {
            getLocationAwareSnmpClient().walk(peer,
                            mtxrLldpRemTable)
                    .withDescription("mtxrLldpRemTable")
                    .withLocation(getLocation())
                    .execute()
                    .get();
            getLocationAwareSnmpClient().walk(peer,
                            mtxrLldpLocalTable)
                    .withDescription("mtxrLldpLocalTable")
                    .withLocation(getLocation())
                    .execute()
                    .get();
            getLocationAwareSnmpClient().walk(peer,
                            mtxrNeighborTable)
                    .withDescription("mtxrNeighborTable")
                    .withLocation(getLocation())
                    .execute()
                    .get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: {}",
                    getNodeId(), e.getMessage());
            return false;
        } catch (final InterruptedException e) {
            LOG.debug("run: node [{}]: InterruptedException: {}",
                    getNodeId(), e.getMessage());
            return false;
        }

        m_lldpTopologyService.store(getNodeId(), LldpLocalTableTracker.getLldpElement(sysname, mtxrLldpLocalPortMap.values()));

        for (MtxrLldpRemTableTracker.MtxrLldpRemRow mtxrLldpRemRow : mtxrlldprowss) {
            m_lldpTopologyService.store(getNodeId(),
                    LldpSnmpUtils.getLldpLink(
                            mtxrLldpRemRow,
                            mtxrNeighborMap.get(mtxrLldpRemRow.getMtxrNeighborIndex()),
                            mtxrLldpLocalPortMap
                    )
            );
        }
        return true;
    }

    private void walkTimeTetra(SnmpAgentConfig peer) {
        LOG.info("run: no remote table entry found. Try to walk TimeTetra-LLDP-MIB");
        List<TimeTetraLldpRemTableTracker.TimeTetraLldpRemRow> ttlinks = new ArrayList<>();
        TimeTetraLldpRemTableTracker timeTetraLldpRemTableTracker = new TimeTetraLldpRemTableTracker() {
            @Override
            public void processLldpRemRow(TimeTetraLldpRemRow row) {
                ttlinks.add(row);
            }
        };

        try {
            getLocationAwareSnmpClient().walk(peer,
                            timeTetraLldpRemTableTracker)
                    .withDescription("timeTetraLldpRemTable")
                    .withLocation(getLocation())
                    .execute()
                    .get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: {}",
                    getNodeId(), e.getMessage());
            return;
        } catch (final InterruptedException e) {
            LOG.debug("run: node [{}]: InterruptedException: {}",
                    getNodeId(), e.getMessage());
            return;
        }
        LOG.info("run: {} remote table entry found walking TIMETETRA-LLDP-MIB", ttlinks.size());
        storeTimeTetraLldpLinks(ttlinks, new TimeTetraLldpLocPortGetter(peer,
                getLocationAwareSnmpClient(),
                getLocation()));
    }

    private void storeTimeTetraLldpLinks(List<TimeTetraLldpRemTableTracker.TimeTetraLldpRemRow> rows, final TimeTetraLldpLocPortGetter timeTetraLldpLocPortGetter) {
        for (TimeTetraLldpRemTableTracker.TimeTetraLldpRemRow row : rows) {
            m_lldpTopologyService.store(getNodeId(), timeTetraLldpLocPortGetter.getLldpLink(row));
        }
    }

    private void storeLldpLinks(List<LldpRemTableTracker.LldpRemRow> links, final LldpLocPortGetter lldpLocPortGetter) {
        for (LldpRemTableTracker.LldpRemRow row : links) {
            m_lldpTopologyService.store(getNodeId(), lldpLocPortGetter.getLldpLink(row));
        }
    }
	@Override
	public String getName() {
		return "NodeDiscoveryLldp";
	}

}
